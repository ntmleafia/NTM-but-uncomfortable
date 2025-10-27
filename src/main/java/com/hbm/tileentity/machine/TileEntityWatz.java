package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.ModBlocks.WatzNew;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.inventory.HeatRecipes;
import com.hbm.inventory.container.ContainerWatz;
import com.hbm.inventory.gui.GUIWatz;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemWatzPellet;
import com.hbm.items.machine.ItemWatzPellet.EnumWatzType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundEvents;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import com.hbm.saveddata.RadiationSavedData;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Compat;
import com.hbm.util.EnumUtil;
import com.hbm.util.Function;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileEntityWatz extends TileEntityMachineBase implements  ITickable, IFluidHandler, ITankPacketAcceptor, IControlReceiver, IGUIProvider {
	
	public FluidTank[] tanks;
	public int heat;
	public double fluxLastBase;		//flux created by the previous passive emission, only used for display
	public double fluxLastReaction;	//flux created by the previous reaction, used for the next reaction
	public double fluxDisplay;
	public boolean isOn;
	
	/* lock types for item IO */
	public boolean isLocked = false;
	public ItemStack[] locks;
	
	public TileEntityWatz() {
		super(24, 1);
		this.locks = new ItemStack[inventory.getSlots()];
		this.tanks = new FluidTank[3];
		this.tanks[0] = new FluidTank(64_000);
		this.tanks[1] = new FluidTank(64_000);
		this.tanks[2] = new FluidTank(64_000);
	}

	@Override
	public String getName() {
		return "container.watz";
	}


	@Override
	public void update() {
		
		if(!world.isRemote && !updateLock()) {
			
			boolean turnedOn = world.getBlockState(pos.add(0, 3, 0)).getBlock() == WatzNew.watz_pump && world.getRedstonePower(pos.add(0, 5, 0), EnumFacing.DOWN) > 0;
			List<TileEntityWatz> segments = new ArrayList<>();
			segments.add(this);
			
			/* accumulate all segments */
			for(int y = pos.getY() - 3; y >= 0; y -= 3) {
				TileEntity tile = Compat.getTileStandard(world, pos.getX(), y, pos.getZ());
				if(tile instanceof TileEntityWatz w) {
					segments.add(w);
				} else {
					break;
				}
			}
			
			/* set up shared tanks */
			int size = segments.size();
			FluidTank[] sharedTanks = new FluidTank[] {new FluidTank(64000 * size),  new FluidTank(64000 * size), new FluidTank(64000 * size)};

			for(TileEntityWatz segment : segments) { //merge the lower tanks into this one
				segment.setupCoolant(ModForgeFluids.COOLANT);
				for(int i = 0; i < 3; i++) {
					sharedTanks[i].fill(segment.tanks[i].drain(64000, true), true);
				}
			}


			//update coolant, bottom to top
			for(int i = size - 1; i >= 0; i--) {
				TileEntityWatz segment = segments.get(i);
				segment.updateCoolant(sharedTanks);
			}

			/* update reaction, top to bottom */
			this.updateReaction(null, sharedTanks, turnedOn);
			for(int i = 1; i < size; i++) {
				TileEntityWatz segment = segments.get(i);
				TileEntityWatz above = segments.get(i - 1);
				segment.updateReaction(above, sharedTanks, turnedOn);
			}

			boolean isTooFull = sharedTanks[2].getFluidAmount() == sharedTanks[2].getCapacity();

			/* send sync packets (order doesn't matter) */
			for(TileEntityWatz segment : segments) {
				segment.isOn = turnedOn;
				segment.sendPacket(sharedTanks);
				segment.heat *= 0.99; //cool 1% per tick
			}

			/* re-distribute fluid from shared tanks back into actual tanks, bottom to top */
			for(int i = size - 1; i >= 0; i--) {
				TileEntityWatz segment = segments.get(i);
				for(int j = 0; j < 3; j++) {
					if(sharedTanks[j].getFluidAmount() > 0){
						segment.tanks[j].fill(sharedTanks[j].drain(segment.tanks[j].getCapacity()-segment.tanks[j].getFluidAmount(), true), true);
					}
				}
			}

			segments.get(size - 1).sendOutBottom();

			/* explode on mud overflow */
			if(isTooFull) {
				for(int x = -3; x <= 3; x++) {
					for(int y = 3; y < 6; y++) {
						for(int z = -3; z <= 3; z++) {
							world.setBlockToAir(pos.add(x, y, z));
						}
					}
				}
				for(TileEntityWatz segment : segments) {
					segment.disassemble();
				}
				RadiationSavedData.incrementRad(world, pos.add(0, 1, 0), 1_000F, Integer.MAX_VALUE);
				
				world.playSound(null,pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, HBMSoundEvents.rbmk_explosion, SoundCategory.BLOCKS, 50.0F, 1.0F);
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "rbmkmush");
				data.setFloat("scale", 5);
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 250));
				MainRegistry.proxy.effectNT(data);

                List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                        new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).grow(50, 10, 50));

                for(EntityPlayer player : players) {
                    AdvancementManager.grantAchievement(player, AdvancementManager.progress_watz_boom);
                }
			}
		}
	}
	
	/** basic sanity checking, usually wouldn't do anything except when NBT loading borks */
	public void setupCoolant(Fluid coolant) {
		if(tanks[0].getFluid() == null || tanks[0].getFluid().getFluid() != coolant)
			tanks[0].setFluid(new FluidStack(coolant, 0));
		if(tanks[1].getFluid() == null || tanks[1].getFluid().getFluid() != HeatRecipes.getBoilFluid(coolant))
			tanks[1].setFluid(new FluidStack(HeatRecipes.getBoilFluid(coolant), 0));
	}
	
	public void updateCoolant(FluidTank[] tanks) {
		double coolingFactor = 0.2D; //20% per tick
		double heatToUse = this.heat * coolingFactor;

		int heatReq = HeatRecipes.getRequiredHeat(ModForgeFluids.COOLANT);
		int amountReq = HeatRecipes.getInputAmountHot(ModForgeFluids.COOLANT);
		int amountProduced = HeatRecipes.getOutputAmountHot(ModForgeFluids.COOLANT);

		int heatCycles = (int) (heatToUse / heatReq);
		int coolCycles = tanks[0].getFluidAmount() / amountReq;
		int hotCycles = (tanks[1].getCapacity() - tanks[1].getFluidAmount()) / amountProduced;
		
		int cycles = Math.min(heatCycles, Math.min(hotCycles, coolCycles));

		if(cycles == 0) return;
		this.heat -= cycles * heatReq;
		tanks[0].drain(cycles * amountReq, true);
		tanks[1].fill(new FluidStack(HeatRecipes.getBoilFluid(ModForgeFluids.COOLANT), cycles * amountProduced), true);
	}

	/** enforces strict top to bottom update order (instead of semi-random based on placement) */
	public void updateReaction(TileEntityWatz above, FluidTank[] tanks, boolean turnedOn) {

		if(turnedOn) {
			List<ItemStack> pellets = new ArrayList<>();
			
			for(int i = 0; i < 24; i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if(!stack.isEmpty() && stack.getItem() == ModItems.watz_pellet) {
					pellets.add(stack);
				}
			}
			
			double baseFlux = 0D;
			
			/* init base flux */
			for(ItemStack stack : pellets) {
				EnumWatzType type = EnumUtil.grabEnumSafely(EnumWatzType.class, stack.getItemDamage());
				baseFlux += type.passive;
			}
			
			double inputFlux = baseFlux + fluxLastReaction;
			double addedFlux = 0D;
			double addedHeat = 0D;
			
			for(ItemStack stack : pellets) {
				EnumWatzType type = EnumUtil.grabEnumSafely(EnumWatzType.class, stack.getItemDamage());
				Function burnFunc = type.burnFunc;
				Function heatDiv = type.heatDiv;
				Function absorbFunc = type.absorbFunc;

				if(burnFunc != null) {
					double div = heatDiv != null ? heatDiv.effonix(heat) : 1D;
					double burn = burnFunc.effonix(inputFlux) / div;
					addedFlux += burn;
					addedHeat += type.heatEmission * burn;
					ItemWatzPellet.setYield(stack, ItemWatzPellet.getYield(stack) - burn);
					tanks[2].fill(new FluidStack(ModForgeFluids.MUD_FLUID, (int) Math.round(type.mudContent * burn)), true);
				}

				if(absorbFunc != null) {
					double absorb = absorbFunc.effonix(inputFlux);
					addedHeat += absorb;
					ItemWatzPellet.setYield(stack, ItemWatzPellet.getYield(stack) - absorb);
					tanks[2].fill(new FluidStack(ModForgeFluids.MUD_FLUID, (int) Math.round(type.mudContent * absorb)), true);
				}
			}
			
			this.heat += (int) addedHeat;
			this.fluxLastBase = baseFlux;
			this.fluxLastReaction = addedFlux;
			
		} else {
			this.fluxLastBase = 0;
			this.fluxLastReaction = 0;
			
		}
		
		for(int i = 0; i < 24; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			
			/* deplete */
			if(!stack.isEmpty() && stack.getItem() == ModItems.watz_pellet && ItemWatzPellet.getEnrichment(stack) <= 0) {
				inventory.setStackInSlot(i, new ItemStack(ModItems.watz_pellet_depleted, 1, stack.getItemDamage()));
				// depleted pellets may persist for one tick
			}
		}
		
		if(above != null) {
			for(int i = 0; i < 24; i++) {
				ItemStack stackBottom = inventory.getStackInSlot(i);
				ItemStack stackTop = above.inventory.getStackInSlot(i);
				
				/* items fall down if the bottom slot is empty */
				if(stackBottom.isEmpty() && !stackTop.isEmpty()) {
					inventory.setStackInSlot(i, stackTop.copy());
					above.inventory.getStackInSlot(i).shrink(stackTop.getCount());
				}
				
				/* items switch places if the top slot is depleted */
				if(!stackBottom.isEmpty() && stackBottom.getItem() == ModItems.watz_pellet && !stackTop.isEmpty() && stackTop.getItem() == ModItems.watz_pellet_depleted) {
					ItemStack buf = stackTop.copy();
					above.inventory.setStackInSlot(i, stackBottom.copy());
					inventory.setStackInSlot(i, buf);
				}
			}
		}
	}
	
	public void sendPacket(FluidTank[] sharedTanks) {
		
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("heat", this.heat);
		data.setBoolean("isOn", isOn);
		data.setBoolean("lock", isLocked);
		data.setDouble("flux", this.fluxLastReaction + this.fluxLastBase);
		data.setTag("tanks", FFUtils.serializeTankArray(sharedTanks));
		this.networkPack(data, 25);
	}

	@Override
	public void networkUnpack(NBTTagCompound nbt) {
		
		this.heat = nbt.getInteger("heat");
		this.isOn = nbt.getBoolean("isOn");
		this.isLocked = nbt.getBoolean("lock");
		this.fluxDisplay = nbt.getDouble("flux");
		if(nbt.hasKey("tanks")){
			FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
		}
	}
	
	/** Prevent manual updates when another segment is above this one */
	public boolean updateLock() {
		return Compat.getTileStandard(world, pos.getX(), pos.getY() + 3, pos.getZ()) instanceof TileEntityWatz;
	}
	
	protected void sendOutBottom() {
		
		for(DirPos pos : getSendingPos()) {
			if(tanks[1].getFluidAmount() > 0) FFUtils.fillFluid(this, tanks[1], world, pos.getPos(), tanks[1].getCapacity()>>1);
			if(tanks[2].getFluidAmount() > 0) FFUtils.fillFluid(this, tanks[2], world, pos.getPos(), tanks[2].getCapacity()>>1);
		}
	}
	
	protected DirPos[] getSendingPos() {
		return new DirPos[] {
				new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), ForgeDirection.DOWN),
				new DirPos(pos.getX() + 2, pos.getY() - 1, pos.getZ(), ForgeDirection.DOWN),
				new DirPos(pos.getX() - 2, pos.getY() - 1, pos.getZ(), ForgeDirection.DOWN),
				new DirPos(pos.getX(), pos.getY() - 1, pos.getZ() + 2, ForgeDirection.DOWN),
				new DirPos(pos.getX(), pos.getY() - 1, pos.getZ() - 2, ForgeDirection.DOWN)
		};
	}


	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		NBTTagList list = nbt.getTagList("locks", 10);
		
		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < inventory.getSlots()) {
				locks[b0] = new ItemStack(nbt1);
			}
		}

		if(nbt.hasKey("tanks")){
			FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
		}
		this.heat = nbt.getInteger("heat");
		this.fluxLastBase = nbt.getDouble("lastFluxB");
		this.fluxLastReaction = nbt.getDouble("lastFluxR");
		
		this.isLocked = nbt.getBoolean("isLocked");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		
		NBTTagList list = new NBTTagList();
		
		for(int i = 0; i < locks.length; i++) {
			if(locks[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				locks[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("locks", list);

		nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));
		nbt.setInteger("heat", this.heat);
		nbt.setDouble("lastFluxB", fluxLastBase);
		nbt.setDouble("lastFluxR", fluxLastReaction);
		
		nbt.setBoolean("isLocked", isLocked);
		return super.writeToNBT(nbt);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		
		if(data.hasKey("lock")) {
			
			if(this.isLocked) {
				this.locks = new ItemStack[inventory.getSlots()];
			} else {
				for(int i = 0; i < inventory.getSlots(); i++) {
					this.locks[i] = inventory.getStackInSlot(i);
				}
			}
			
			this.isLocked = !this.isLocked;
			this.markDirty();
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(stack.getItem() != ModItems.watz_pellet) return false;
		if(!this.isLocked) return true;
		return this.locks[i] != null && this.locks[i].getItem() == stack.getItem() && locks[i].getItemDamage() == stack.getItemDamage();
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing side) {
		return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, int j) {
		return stack.getItem() != ModItems.watz_pellet;
	}

	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 3,
					pos.getY(),
					pos.getZ() - 3,
					pos.getX() + 4,
					pos.getY() + 3,
					pos.getZ() + 4
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
	
	private void disassemble() {

		int count = 20;
		Random rand = world.rand;
		for(int i = 0; i < count * 5; i++) {
			EntityShrapnel shrapnel = new EntityShrapnel(world);
			shrapnel.posX = pos.getX() + 0.5;
			shrapnel.posY = pos.getY() + 3;
			shrapnel.posZ = pos.getZ() + 0.5;
			shrapnel.motionY = ((rand.nextFloat() * 0.5) + 0.5) * (1 + (count / (15.0F + rand.nextInt(21)))) + (rand.nextFloat() / 50 * count);
			shrapnel.motionX = rand.nextGaussian() * 1	* (1 + (count / 100.0F));
			shrapnel.motionZ = rand.nextGaussian() * 1	* (1 + (count / 100.0F));
			shrapnel.setWatz(true);
			world.spawnEntity(shrapnel);
		}

		world.setBlockState(pos, ModBlocks.mud_block.getDefaultState());
		world.setBlockState(pos.up(), ModBlocks.mud_block.getDefaultState());
		world.setBlockState(pos.up(2), ModBlocks.mud_block.getDefaultState());
		
		setBrokenColumn(0, WatzNew.watz_element, 0, 1, 0);
		setBrokenColumn(0, WatzNew.watz_element, 0, 2, 0);
		setBrokenColumn(0, WatzNew.watz_element, 0, 0, 1);
		setBrokenColumn(0, WatzNew.watz_element, 0, 0, 2);
		setBrokenColumn(0, WatzNew.watz_element, 0, -1, 0);
		setBrokenColumn(0, WatzNew.watz_element, 0, -2, 0);
		setBrokenColumn(0, WatzNew.watz_element, 0, 0, -1);
		setBrokenColumn(0, WatzNew.watz_element, 0, 0, -2);
		setBrokenColumn(0, WatzNew.watz_element, 0, 1, 1);
		setBrokenColumn(0, WatzNew.watz_element, 0, 1, -1);
		setBrokenColumn(0, WatzNew.watz_element, 0, -1, 1);
		setBrokenColumn(0, WatzNew.watz_element, 0, -1, -1);
		setBrokenColumn(0, WatzNew.watz_cooler, 0, 2, 1);
		setBrokenColumn(0, WatzNew.watz_cooler, 0, 2, -1);
		setBrokenColumn(0, WatzNew.watz_cooler, 0, 1, 2);
		setBrokenColumn(0, WatzNew.watz_cooler, 0, -1, 2);
		setBrokenColumn(0, WatzNew.watz_cooler, 0, -2, 1);
		setBrokenColumn(0, WatzNew.watz_cooler, 0, -2, -1);
		setBrokenColumn(0, WatzNew.watz_cooler, 0, 1, -2);
		setBrokenColumn(0, WatzNew.watz_cooler, 0, -1, -2);
		
		for(int j = -1; j < 2; j++) {
			setBrokenColumn(1, WatzNew.watz_casing, 1, 3, j);
			setBrokenColumn(1, WatzNew.watz_casing, 1, j, 3);
			setBrokenColumn(1, WatzNew.watz_casing, 1, -3, j);
			setBrokenColumn(1, WatzNew.watz_casing, 1, j, -3);
		}
		setBrokenColumn(1, WatzNew.watz_casing, 1, 2, 2);
		setBrokenColumn(1, WatzNew.watz_casing, 1, 2, -2);
		setBrokenColumn(1, WatzNew.watz_casing, 1, -2, 2);
		setBrokenColumn(1, WatzNew.watz_casing, 1, -2, -2);
		
		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).expand(50, 50, 50));
		
//		for(EntityPlayer player : players) {
//			player.triggerAchievement(MainRegistry.achWatzBoom);
//		}
	}
	
	private void setBrokenColumn(int minHeight, Block b, int meta, int x, int z) {
		
		int height = minHeight + world.rand.nextInt(3 - minHeight);
		
		for(int i = 0; i < 3; i++) {
			
			if(i <= height) {
				world.setBlockState(pos.add(x, i, z), b.getDefaultState(), 3);
			} else {
				world.setBlockState(pos.add(x, i, z), ModBlocks.mud_block.getDefaultState());
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerWatz(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIWatz(player.inventory, this);
	}

	@Override
	public void recievePacket(NBTTagCompound[] tags) {
		if(tags.length != 3) {
			return;
		} else {
			tanks[0].readFromNBT(tags[0]);
			tanks[1].readFromNBT(tags[1]);
			tanks[2].readFromNBT(tags[2]);
		}
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] { tanks[0].getTankProperties()[0], tanks[1].getTankProperties()[0], tanks[2].getTankProperties()[0] };
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(resource == null) return 0;

		if(tanks[0].getFluid() == null || resource.isFluidEqual(tanks[0].getFluid())) {
			return tanks[0].fill(resource, doFill);
		}
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if(resource == null) {
			return null;
		}
		if(resource.getFluid() == ModForgeFluids.HOTCOOLANT)
			return tanks[1].drain(resource.amount, doDrain);
		if(resource.getFluid() == ModForgeFluids.MUD_FLUID)
			return tanks[2].drain(resource.amount, doDrain);
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if(tanks[1].getFluidAmount() > 0){
			return tanks[1].drain(maxDrain, doDrain);
		} else if(tanks[2].getFluidAmount() > 0){
			return tanks[2].drain(maxDrain, doDrain);
		} else {
			return null;
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		} else {
			return super.getCapability(capability, facing);
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		} else {
			return super.hasCapability(capability, facing);
		}
	}
}
