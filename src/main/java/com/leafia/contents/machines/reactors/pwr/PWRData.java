package com.leafia.contents.machines.reactors.pwr;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.fluid.CoriumBlock;
import com.leafia.contents.machines.reactors.pwr.blocks.components.element.MachinePWRElement;
import com.leafia.contents.machines.reactors.pwr.blocks.components.PWRComponentBlock;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.inventory.HeatRecipes;
import com.leafia.contents.machines.reactors.pwr.blocks.components.element.TileEntityPWRElement;
import com.leafia.dev.container_utility.LeafiaPacket;
import com.leafia.dev.container_utility.LeafiaPacketReceiver;
import com.leafia.contents.control.fuel.nuclearfuel.ItemLeafiaRod;
import com.llib.exceptions.LeafiaDevFlaw;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import com.hbm.saveddata.RadiationSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PWRData implements ITickable, IFluidHandler, ITankPacketAcceptor, LeafiaPacketReceiver {
	public BlockPos corePos;
	public FluidTank[] tanks;
	public Fluid[] tankTypes;
	public String coolantName = ModForgeFluids.coolant.getName();
	public int compression = 0;
	public double heat = 20;

	public Set<BlockPos> members = new HashSet<>();

	public TileEntity companion;

	public PWRData(TileEntity entity) {
		tanks = new FluidTank[] {
				new FluidTank(128_000),
				new FluidTank(128_000),
				new FluidTank(16_000),

				new FluidTank(512_000),
				new FluidTank(256_000)
		};
		tankTypes = new Fluid[] {
				ModForgeFluids.coolant,
				ModForgeFluids.hotcoolant,
				ModForgeFluids.malcoolant,

				FluidRegistry.WATER,
				ModForgeFluids.steam
		};
		this.companion = entity;
		valid = !entity.isInvalid();
	}
	public World getWorld() {
		return this.companion.getWorld();
	}
	public PWRData readFromNBT(NBTTagCompound nbt) {
		nbt = nbt.getCompoundTag("data");
		if(nbt.hasKey("compression"))
			compression = nbt.getInteger("compression");
		if(compression == 0){
			if (tankTypes[4] != ModForgeFluids.steam)
				tanks[4].drain(tanks[4].getCapacity(),true);
			tankTypes[4] = ModForgeFluids.steam;
		} else if(compression == 1){
			if (tankTypes[4] != ModForgeFluids.hotsteam)
				tanks[4].drain(tanks[4].getCapacity(),true);
			tankTypes[4] = ModForgeFluids.hotsteam;
		} else if(compression == 2){
			if (tankTypes[4] != ModForgeFluids.superhotsteam)
				tanks[4].drain(tanks[4].getCapacity(),true);
			tankTypes[4] = ModForgeFluids.superhotsteam;
		}
		tankTypes[0] = ModForgeFluids.coolant;
		tankTypes[1] = ModForgeFluids.hotcoolant;
		tankTypes[2] = ModForgeFluids.malcoolant;
		if (nbt.hasKey("coolantName")) {
			Fluid coolant = FluidRegistry.getFluid(coolantName);
			if (coolant != null) {
				Fluid hot = HeatRecipes.getBoilFluid(coolant);
				if (hot != null) {
					if (tankTypes[0] != coolant)
						tanks[0].drain(tanks[0].getCapacity(),true);
					if (tankTypes[1] != hot)
						tanks[1].drain(tanks[1].getCapacity(),true);
					tankTypes[0] = coolant;
					tankTypes[1] = hot;
					Fluid hotter = hot;
					while (true) {
						Fluid hottest = HeatRecipes.getBoilFluid(hotter);
						if (hottest == null) break;
						else {
							hotter = hottest;
							if (hottest.isGaseous()) {
								if (tankTypes[2] != hottest)
									tanks[2].drain(tanks[2].getCapacity(),true);
								tankTypes[2] = hottest;
							}
						}
					}
				}
			}
		}
		if (nbt.hasKey("heat"))
			heat = nbt.getDouble("heat");
		if(nbt.hasKey("tanks"))
			FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
		return this;
	}
	public NBTTagCompound writeToNBT(NBTTagCompound mainCompound) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setDouble("heat", heat);
		nbt.setInteger("compression", compression);
		nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));

		mainCompound.setTag("data",nbt);
		return mainCompound;
	}

	@Override
	public void recievePacket(NBTTagCompound[] tags) {
		if(tags.length != 5){
			return;
		} else {
			for (int i = 0; i < 5; i++) {
				tanks[i].readFromNBT(tags[i]);
			}
		}
	}
	boolean valid = false;
	int timeToDrainMalcoolant = 10;
	@Override
	public void update() {
		if (!valid) {
			if (!companion.isInvalid()) {
				valid = true;
				Block block = companion.getBlockType();
				if (block instanceof PWRComponentBlock) {
					((PWRComponentBlock)block).beginDiagnosis(getWorld(),companion.getPos());
				} else
					companion.invalidate(); // you're coming with me
			}
			return;
		}
		if (getWorld().isRemote) {
			//Minecraft.getMinecraft().player.sendMessage(new TextComponentString("hello... here at "+companion.getPos()+".. ,,uwu,,"));
			// The debug code above is a serious sign of mental illness.
		} else {
			//for (EntityPlayer player : getWorld().playerEntities) {
			//    player.sendMessage(new TextComponentString("" + getWorld().isRemote + "! Im at " + companion.getPos()));
			//}
			if (tanks[2].getFluidAmount() > 0) {
				int decr = tanks[2].getCapacity()/timeToDrainMalcoolant;
				if (tanks[1].getCapacity()-tanks[1].getFluidAmount() >= decr) {
					tanks[1].fill(new FluidStack(tankTypes[1],decr),true);
					tanks[2].drain(decr,true);
				}
			}
		}
	}

	boolean exploded = false;
	World explodeWorld = null;
	Set<BlockPos> growMembers(Set<BlockPos> set) { // have this highly laggy retarded Solution
		Set<BlockPos> offsets = new HashSet<>();
		for (BlockPos member : set) {
			for (EnumFacing facing : EnumFacing.values()) {
				BlockPos offset = member.add(facing.getFrontOffsetX(),facing.getFrontOffsetY(),facing.getFrontOffsetZ());
				//if (!members.contains(offset)) {
					//if (explodeWorld.getBlockState(offset).getBlock().getExplosionResistance(null) >= 50)
						offsets.add(offset);
				//}
			}
		}
		for (BlockPos offset : offsets) {
			members.add(offset); // fuck you ConcurrentModificationException
		}
		return offsets;
	}
	public void explode(World world,@Nullable ItemStack prevStack) {
		if (exploded) return;
		exploded = true;
		explodeWorld = world;
		Vec3d centerPoint = new Vec3d(0,0,0);
		for (BlockPos member : members) {
			centerPoint = centerPoint.add(new Vec3d(member.getX(),member.getY(),member.getZ()).scale(1d/members.size()));
			Block block = world.getBlockState(member).getBlock();
			if (block instanceof PWRComponentBlock) {
				if (block instanceof MachinePWRElement) {
					if (((MachinePWRElement) block).tileEntityShouldCreate(world,member)) {
						TileEntity entity = world.getTileEntity(member);
						if (entity != null) {
							if (entity instanceof TileEntityPWRElement) {
								TileEntityPWRElement element = (TileEntityPWRElement)entity;
								if (element.inventory != null) {
									prevStack = ItemLeafiaRod.comparePriority(element.inventory.getStackInSlot(0),prevStack);
								}
							}
						}
					}
				}
				//world.setBlockToAir(member);
			}
		}
		// mmm this is gonna be crispy computer
		growMembers(growMembers(growMembers(growMembers(members))));

		// HashSet is one giant dick. It randomly doesn't detect its OWN element by contains(). You just wasted my precious time a LOT
		// Don't believe me? Try replacing all "motherfucker" occurrences to original "members" (which is a Set) and boom IT BREAKS SOMEHOW
		List<BlockPos> motherfucker = new ArrayList<>();
		motherfucker.addAll(members);
		world.createExplosion(null,centerPoint.x+0.5, centerPoint.y+0.5, centerPoint.z+0.5, 24.0F, true);
		for (BlockPos member : motherfucker) {
			if (world.isAirBlock(member)) continue;
			Block block = world.getBlockState(member).getBlock();
			if (block instanceof BlockFire) continue;
			if (block instanceof CoriumBlock) continue;
			if (block instanceof MachinePWRElement) {
				//world.newExplosion(null,member.getX()+0.5,member.getY()+0.5,member.getZ()+0.5,11,true,true);
				//world.setBlockState(member,ModBlocks.corium_block.getDefaultState());
				world.setBlockToAir(member);
				continue;
			}
			//Block fuckyou = Blocks.BLACK_GLAZED_TERRACOTTA;

			boolean destroyed = false;
			Vec3d ray = new Vec3d(member).addVector(0.5,0.5,0.5).subtract(centerPoint);
			int counter = 0;
			int threshold = 7+world.rand.nextInt(3);
			for (double s = 0; s <= 2; s+=0.2) {
				BlockPos rayHit = new BlockPos(centerPoint.add(ray.scale(Math.pow(s,1.5)+1))/*.add(ray.normalize().scale(1.732/1.5))*/);
						//member;//new BlockPos(centerPoint.add(ray.scale(s) ));
				if (!world.isValid(rayHit) || world.isAirBlock(rayHit) || motherfucker.contains(rayHit)) {
					/*
					switch (counter+2) {
						case 0: fuckyou = Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA; break;
						case 1: fuckyou = Blocks.BLUE_GLAZED_TERRACOTTA; break;
						case 2: fuckyou = Blocks.GREEN_GLAZED_TERRACOTTA; break;
						case 3: fuckyou = Blocks.LIME_GLAZED_TERRACOTTA; break;
						case 4: fuckyou = Blocks.YELLOW_GLAZED_TERRACOTTA; break;
						case 5: fuckyou = Blocks.ORANGE_GLAZED_TERRACOTTA; break;
						case 6: fuckyou = Blocks.RED_GLAZED_TERRACOTTA; break;
					}*/
					if (++counter >= threshold) {
						world.setBlockToAir(member);
						destroyed = true;
						break;
					}
				}
			}
			//if (motherfucker.contains(member)) { The line that proves the fact HashSet sometimes doesn't detect its own elements
			// This looks like always "true" if branch, except it is not for HashSet. Bruh.
			//	fuckyou = Blocks.LIME_GLAZED_TERRACOTTA;
			//}
			if (!destroyed) {
				// myaaaaa
				if (block instanceof PWRComponentBlock) {
					world.setBlockState(member,((world.rand.nextInt(3) == 0) ? ModBlocks.pribris_burning : ModBlocks.pribris).getDefaultState());
				} else {
					// almost broken
					if (counter >= threshold-2) {
						world.setBlockState(member,ModBlocks.pribris_radiating.getDefaultState()); // Test
					}
				}
				//world.setBlockState(member,fuckyou.getDefaultState());
			}
		}
		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "rbmkmush");
		data.setFloat("scale", 4);
		PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data,centerPoint.x+0.5,centerPoint.y+0.5,centerPoint.z+0.5), new NetworkRegistry.TargetPoint(world.provider.getDimension(), centerPoint.x+0.5,centerPoint.y+0.5,centerPoint.z+0.5, 250));
		world.playSound(null,centerPoint.x+0.5,centerPoint.y+0.5,centerPoint.z+0.5,HBMSoundHandler.rbmk_explosion,SoundCategory.BLOCKS,50.0F,1.0F);

		boolean nope = true;
		if (prevStack != null) {
			if (prevStack.getItem() instanceof ItemLeafiaRod) {
				nope = false;
				ItemLeafiaRod rod = (ItemLeafiaRod)(prevStack.getItem());
				rod.resetDetonate();
				rod.detonateRadius = 18;
				rod.detonateVisualsOnly = true;
				rod.detonate(world,new BlockPos(centerPoint));
			}
		}
		if (nope) {
			ExplosionNukeGeneric.waste(world, (int)centerPoint.x, (int)centerPoint.y, (int)centerPoint.z, 35);
			RadiationSavedData.incrementRad(world, new BlockPos(centerPoint), 3000F, 4000F);
		}
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[]{tanks[0].getTankProperties()[0], tanks[1].getTankProperties()[0], tanks[2].getTankProperties()[0]};
	}
	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(resource == null){
			return 0;
		} else if(resource.getFluid() == tankTypes[0]){
			return tanks[0].fill(resource, doFill);
		} else if(resource.getFluid() == tankTypes[1]){
			return tanks[1].fill(resource, doFill);
		} else {
			return 0;
		}
	}
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if(resource != null && resource.getFluid() == tankTypes[1]) {
			return tanks[1].drain(resource.amount,doDrain);
		} else if(resource != null && resource.getFluid() == tankTypes[4]){
			return tanks[4].drain(resource.amount, doDrain);
		} else {
			return null;
		}
	}
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return tanks[1].drain(maxDrain, doDrain);
	}
	public static LeafiaPacket addDataToPacket(LeafiaPacket packet,@Nullable PWRData self) {
		return packet.__write(31,(self != null) ? self.writeToNBT(new NBTTagCompound()) : false);
	}
	@Nullable
	public static PWRData tryLoadFromPacket(TileEntity entity,Object value) {
		if (value.equals(false)) return null;
		else if (value instanceof NBTTagCompound)
			return new PWRData(entity).readFromNBT((NBTTagCompound)value);
		else
			return null;
	}

	@Override
	public String getPacketIdentifier() { throw new LeafiaDevFlaw("Method PWRData.getPacketIdentifier() is not supposed to be used! Spaghetti coding moment."); }
	@SideOnly(Side.CLIENT)
	@Override
	public void onReceivePacketLocal(byte key,Object value) {

	}
	@Override
	public void onReceivePacketServer(byte key,Object value,EntityPlayer plr) {

	}
	@Override
	public void onPlayerValidate(EntityPlayer plr) {
		addDataToPacket(LeafiaPacket._start(companion),this).__sendToClient(plr);
	}
}
