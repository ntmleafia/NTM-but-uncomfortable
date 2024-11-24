package com.leafia.contents.machines.reactors.pwr;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.fluid.BlockLiquidCorium;
import com.hbm.items.ModItems;
import com.hbm.util.Tuple.Pair;
import com.leafia.contents.machines.reactors.pwr.blocks.components.PWRComponentEntity;
import com.leafia.contents.machines.reactors.pwr.blocks.components.control.MachinePWRControl;
import com.leafia.contents.machines.reactors.pwr.blocks.components.control.TileEntityPWRControl;
import com.leafia.contents.machines.reactors.pwr.blocks.components.element.MachinePWRElement;
import com.leafia.contents.machines.reactors.pwr.blocks.components.PWRComponentBlock;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.inventory.HeatRecipes;
import com.leafia.contents.machines.reactors.pwr.blocks.components.element.TileEntityPWRElement;
import com.leafia.contents.machines.reactors.pwr.blocks.wreckage.PWRMeshedWreck;
import com.leafia.contents.machines.reactors.pwr.blocks.wreckage.PWRMeshedWreck.Erosion;
import com.leafia.contents.machines.reactors.pwr.debris.EntityPWRDebris;
import com.leafia.dev.container_utility.LeafiaPacket;
import com.leafia.dev.container_utility.LeafiaPacketReceiver;
import com.leafia.contents.control.fuel.nuclearfuel.ItemLeafiaRod;
import com.llib.exceptions.LeafiaDevFlaw;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import com.hbm.saveddata.RadiationSavedData;
import com.llib.exceptions.messages.TextWarningLeafia;
import com.llib.group.LeafiaSet;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

public class PWRData implements ITickable, IFluidHandler, ITankPacketAcceptor, LeafiaPacketReceiver {
	public BlockPos corePos;
	public FluidTank[] tanks;
	public Fluid[] tankTypes;
	public String coolantName = ModForgeFluids.coolant.getName();
	public int compression = 0;
	//public double heat = 20;
	public int coriums = 0;
	public double masterControl = 1;
	public final Map<String,Double> controlDemand = new HashMap<>();

	PWRComponentBlock getPWRBlock(World world,BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		if (block instanceof PWRComponentBlock)
			return (PWRComponentBlock)block;
		return null;
	}
	PWRComponentBlock getPWRComponent(World world,BlockPos pos) {
		PWRComponentBlock block = getPWRBlock(world,pos);
		if (block == null) return null;
		return block.tileEntityShouldCreate(world,pos) ? block : null;
	}
	PWRComponentEntity getPWREntity(World world,BlockPos pos) {
		PWRComponentBlock pwr = getPWRBlock(world,pos);
		if (pwr != null)
			return pwr.getPWR(world,pos);
		return null;
	}

	public Set<BlockPos> members = new HashSet<>();
	public Set<BlockPos> controls = new HashSet<>();
	public Set<BlockPos> fuels = new HashSet<>();
	public LeafiaSet<BlockPos> projection = new LeafiaSet<>();
	public void onDiagnosis(World world) {
		if (world.isRemote) {

		} else {
			int slots = 0;
			for (BlockPos pos : projection) {
				PWRComponentBlock block = getPWRComponent(world,pos);
				if (block != null) {
					if (block instanceof MachinePWRElement) {
						slots++;
					}
				}
			}
			if (remoteSize != slots) {
				remoteContainer = new ItemStackHandler(slots);
				remoteSize = slots;
			}
			for (FluidTank tank : tanks) {
				if (tank.getFluidAmount() > tank.getCapacity())// whoops
					tank.drain(tank.getFluidAmount()-tank.getCapacity(),true);
			}
			addDataToPacket(LeafiaPacket._start(companion),this).__sendToAffectedClients();
		}
	}
	public Pair<LeafiaSet<BlockPos>,LeafiaSet<BlockPos>> getProjectionFuelAndControlPositions() {
		Pair<LeafiaSet<BlockPos>,LeafiaSet<BlockPos>> output = new Pair<>(new LeafiaSet<>(),new LeafiaSet<>());
		for (BlockPos pos : projection) {
			Block block = getWorld().getBlockState(pos).getBlock();
			if (block instanceof MachinePWRElement)
				output.getA().add(pos);
			else if (block instanceof MachinePWRControl)
				output.getB().add(pos);
		}
		return output;
	}
	public BlockPos terminal_toGlobal(IBlockState terminal,BlockPos terminalPos,BlockPos pos) {
		EnumFacing face = terminal.getValue(BlockHorizontal.FACING).getOpposite();
		Vec3d lookVector = new Vec3d(face.getDirectionVec()); // amazingly, Vec3i does not has .scale() method LMAO
		Vec3d rightVector = lookVector.crossProduct(new Vec3d(0,1,0));
		return new BlockPos(new Vec3d(terminalPos).addVector(0.5,0.5+pos.getY(),0.5).add(lookVector.scale(-pos.getZ())).add(rightVector.scale(pos.getX())));
	}
	public BlockPos terminal_toLocal(IBlockState terminal,BlockPos terminalPos,BlockPos pos) {
		EnumFacing face = terminal.getValue(BlockHorizontal.FACING).getOpposite();
		BlockPos relative = pos.subtract(terminalPos);
		switch(face) { // IM STUPID OK??
			case NORTH: return relative;
			case SOUTH: return new BlockPos(-relative.getX(),relative.getY(),-relative.getZ());
			case WEST: return new BlockPos(-relative.getZ(),relative.getY(),relative.getX());
			case EAST: return new BlockPos(relative.getZ(),relative.getY(),-relative.getX());
			default: throw new LeafiaDevFlaw("PWR Terminals should only face sideways, got "+face.getName()+" how the fuck does this even happen");
		}
	}

	public TileEntity companion;

	public ItemStackHandler resourceContainer = new ItemStackHandler(3) {
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			companion.markDirty();
		}
	};
	public ItemStackHandler remoteContainer = new ItemStackHandler(0);
	int remoteSize = 0;

	public void invalidate(World world) {
		if (!(world instanceof WorldServer)) return;
		BlockPos checkPos = companion.getPos();
		((PWRComponentEntity)companion).assignCore(null);
		world.getMinecraftServer().addScheduledTask(()->{
			if (world.getBlockState(checkPos).getBlock() instanceof BlockLiquidCorium)
				this.explode(world,null);
		});
	}

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
		this.corePos = companion.getPos();
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
		//if (nbt.hasKey("heat"))
		//	heat = nbt.getDouble("heat");
		if (nbt.hasKey("tanks"))
			FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
		if (nbt.hasKey("remoteContainerSize"))
			remoteSize = nbt.getInteger("remoteContainerSize");
		if (nbt.hasKey("resourceContainer"))
			resourceContainer.deserializeNBT(nbt.getCompoundTag("resourceContainer"));
		if (nbt.hasKey("projectionMap")) {
			projection.clear();
			NBTTagList nbtjection = nbt.getTagList("projectionMap",11/*INT[], refer to NBTBase*/);
			for (NBTBase item : nbtjection) {
				NBTTagIntArray array = (NBTTagIntArray)item;
				int[] coords = array.getIntArray();
				projection.add(new BlockPos(coords[0],coords[1],coords[2]));
			}
		}
		if (nbt.hasKey("controlMaster"))
			masterControl = nbt.getDouble("controlMaster");
		if (nbt.hasKey("controlDemand"))
			readControlPositions(nbt.getCompoundTag("controlDemand"));
		return this;
	}
	public NBTTagCompound writeToNBT(NBTTagCompound mainCompound) {
		NBTTagCompound nbt = new NBTTagCompound();
		//nbt.setDouble("heat", heat);
		nbt.setInteger("compression", compression);
		nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));
		nbt.setInteger("remoteContainerSize",remoteSize);
		nbt.setTag("resourceContainer",resourceContainer.serializeNBT());
		NBTTagList nbtjection = new NBTTagList();
		for (BlockPos pos : projection) {
			nbtjection.appendTag(new NBTTagIntArray(new int[]{pos.getX(),pos.getY(),pos.getZ()}));
		}
		nbt.setTag("projectionMap",nbtjection);

		nbt.setDouble("controlMaster",masterControl);
		nbt.setTag("controlDemand",writeControlPositions());

		mainCompound.setTag("data",nbt);
		return mainCompound;
	}
	public void readControlPositions(NBTTagCompound nbt) {
		controlDemand.clear();
		for (String key : nbt.getKeySet())
			controlDemand.put(key,nbt.getDouble(key));
	}
	public NBTTagCompound writeControlPositions() {
		Set<String> existingNames = new LeafiaSet<>();
		for (BlockPos pos : controls) {
			TileEntity entity = getWorld().getTileEntity(pos);
			if (entity instanceof TileEntityPWRControl) {
				TileEntityPWRControl control = (TileEntityPWRControl)entity;
				existingNames.add(control.name);
			}
		}
		NBTTagCompound nbt = new NBTTagCompound();
		for (Entry<String,Double> entry : controlDemand.entrySet()) {
			if (existingNames.contains(entry.getKey()))
				nbt.setDouble(entry.getKey(),entry.getValue());
		}
		return nbt;
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
					this.corePos = companion.getPos();
					((PWRComponentBlock)block).beginDiagnosis(getWorld(),companion.getPos(),companion.getPos());
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
			if (tanks[3].getCapacity() > 0) {
				int consumption = (int)Math.round(Math.pow(tanks[1].getFluidAmount()/(double)Math.max(tanks[3].getCapacity(),1),0.4)/20);
				FluidStack stack = tanks[1].drain(consumption,false);
				FluidStack stack2 = tanks[3].drain(consumption,false);
				if (stack != null && stack2 != null) {
					int boilAmt = Math.min(stack.amount,stack2.amount);
					int division = (int)Math.pow(10,compression);
					int filled = tanks[4].fill(new FluidStack(tankTypes[4],boilAmt/division),true);
					tanks[1].drain(Math.min(boilAmt,filled*division),true);
					tanks[3].drain(Math.min(boilAmt,filled*division),true);
				}
			}
			LeafiaPacket._start(companion).__write(30,new int[]{
					compression,
					tanks[0].getCapacity(),
					tanks[1].getCapacity(),
					tanks[2].getCapacity(),
					tanks[3].getCapacity(),
					tanks[4].getCapacity(),
					tanks[0].getFluidAmount(),
					tanks[1].getFluidAmount(),
					tanks[2].getFluidAmount(),
					tanks[3].getFluidAmount(),
					tanks[4].getFluidAmount()
			}).__sendToAffectedClients();
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
	double signedPow(double x,double y) {
		return Math.pow(Math.abs(x),y)*Math.signum(x);
	}
	public void explode(World world,@Nullable ItemStack prevStack) {
		if (exploded) return;
		exploded = true;
		explodeWorld = world;
		if (members.size() <= 0) return;
		Vec3d centerPoint = new Vec3d(0,0,0);
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for (BlockPos member : members) {
			minX = Math.min(minX,member.getX());
			minY = Math.min(minY,member.getY());
			minZ = Math.min(minZ,member.getZ());
			maxX = Math.max(maxX,member.getX());
			maxY = Math.max(maxY,member.getY());
			maxZ = Math.max(maxZ,member.getZ());
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
		double reactorSize = (maxX-minX+1+2+4+maxY-minY+1+2+4+maxZ-minZ+1+2+4)/3d;
		for (EntityPlayer plr : world.playerEntities) {
			if (plr.getHeldItem(EnumHand.OFF_HAND).getItem() == ModItems.wand_d) {
				plr.sendMessage(new TextComponentString("PWR Exploded"));
				plr.sendMessage(new TextComponentString("  Size: " + reactorSize));
				plr.sendMessage(new TextComponentString("  x: " + minX + " : " + maxX));
				plr.sendMessage(new TextComponentString("  y: " + minY + " : " + maxY));
				plr.sendMessage(new TextComponentString("  z: " + minZ + " : " + maxZ));
			}
		}
		// mmm this is gonna be crispy computer
		growMembers(growMembers(growMembers(growMembers(members))));

		// HashSet is one giant dick. It randomly doesn't detect its OWN element by contains(). You just wasted my precious time a LOT
		// Don't believe me? Try replacing all "motherfucker" occurrences to original "members" (which is a Set) and boom IT BREAKS SOMEHOW
		List<BlockPos> motherfucker = new ArrayList<>();
		motherfucker.addAll(members);
		world.newExplosion(null,centerPoint.x+0.5,centerPoint.y+0.5,centerPoint.z+0.5,24.0F,true,true);
		List<BlockPos> placeWrecks = new ArrayList<>();
		List<BlockPos> vaporized = new ArrayList<>();
		List<BlockPos> remains = new ArrayList<>();
		List<BlockPos> allDebris = new ArrayList<>();
		Vec3d pressure = new Vec3d(0,0,0);
		for (BlockPos member : motherfucker) {
			Vec3d ray = new Vec3d(member).addVector(0.5,0.5,0.5).subtract(centerPoint);
			if (world.getBlockState(member).getBlock().isPassable(world,member)) {
				pressure = pressure.add(ray.scale(2d/motherfucker.size()));
				continue;
			}
			IBlockState state = world.getBlockState(member);
			Block block = state.getBlock();
			if (block instanceof BlockFire) continue;
			if (block instanceof BlockLiquidCorium) continue;
			if (block instanceof MachinePWRElement) {
				//world.newExplosion(null,member.getX()+0.5,member.getY()+0.5,member.getZ()+0.5,11,true,true);
				//world.setBlockState(member,ModBlocks.corium_block.getDefaultState());
				world.setBlockToAir(member);
				continue;
			}
			//Block fuckyou = Blocks.BLACK_GLAZED_TERRACOTTA;

			boolean destroyed = false;
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
						//world.setBlockToAir(member);
						vaporized.add(member);
						destroyed = true;
						pressure = pressure.add(ray.scale(1d/motherfucker.size()));
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
				remains.add(member);
				pressure = pressure.subtract(ray.scale(1d/motherfucker.size()));
				/*
				if (block instanceof PWRComponentBlock) {
					world.setBlockState(member,((world.rand.nextInt(3) == 0) ? ModBlocks.pribris_burning : ModBlocks.pribris).getDefaultState());
				} else if (block.isFullBlock(state) && !block.isPassable(world,member)) {
					// almost broken
					if (counter >= threshold-2) {
						//world.setBlockState(member,ModBlocks.pribris_radiating.getDefaultState()); // Test
						placeWrecks.add(member);
					}
				}*/
				//world.setBlockState(member,fuckyou.getDefaultState());
			}
		}
		for (BlockPos pos : vaporized) {
			if (placeWrecks.contains(pos)) continue; // Somehow
			boolean converted = false;
			for (EnumFacing face : EnumFacing.values()) {
				if (remains.contains(pos.offset(face))) {
					placeWrecks.add(pos);
					allDebris.add(pos);
					converted = true;
					break;
				}
			}
			if (!converted) {
				Block block = world.getBlockState(pos).getBlock();
				if (!(block instanceof IFluidBlock) && (!block.isPassable(world,pos))) {
					Vec3d ray = new Vec3d(pos).addVector(0.5,0.5,0.5).subtract(centerPoint);
					EntityPWRDebris debris = new EntityPWRDebris(world,pos.getX() + 0.5D,pos.getY() + 0.5,pos.getZ() + 0.5D,world.getBlockState(pos));
					debris.motionX = signedPow(ray.x,1)/reactorSize*(1+world.rand.nextDouble()) + signedPow(pressure.x,0.8)/2;
					debris.motionY = signedPow(ray.y,1)/reactorSize*(1+world.rand.nextDouble()) + signedPow(pressure.y,0.8)/2;
					debris.motionZ = signedPow(ray.z,1)/reactorSize*(1+world.rand.nextDouble()) + signedPow(pressure.z,0.8)/2;
					world.spawnEntity(debris);
				}
				world.setBlockToAir(pos);
			}
		}
		// TODO: add cam shake
		for (BlockPos pos : remains) {
			boolean buried = true;
			for (EnumFacing face : EnumFacing.values()) {
				if (vaporized.contains(pos.offset(face)) || world.getBlockState(pos.offset(face)).getBlock().isPassable(world,pos)) {
					buried = false;
					break;
				}
			}
			if (!buried)
				allDebris.add(pos);
		}
		for (BlockPos member : allDebris) {
			IBlockState state = world.getBlockState(member);
			Block block = state.getBlock();
			SoundType soundType = block.getSoundType();
			Material material = block.getMaterial(state);
			Vec3d ray = new Vec3d(member).addVector(0.5,0.5,0.5).subtract(centerPoint);

			if (block instanceof MachinePWRControl) {
				world.setBlockState(member,ModBlocks.block_electrical_scrap.getDefaultState());
				//continue;
			}
			double heatBase = MathHelper.clamp(Math.pow(MathHelper.clamp(1-ray.lengthVector()/(reactorSize/2),0,1),0.45)*8,0,7);
			int heat = (int)heatBase;
			int heatRand = world.rand.nextInt(3);
			if (heatRand == 1)
				heat = (int)Math.round(heatBase);
			else if (heatRand == 2)
				heat = (int)Math.ceil(heatBase);
			boolean defaultPlacement = true;
			if (placeWrecks.contains(member)) {
				defaultPlacement = false;
				EnumFacing face = EnumFacing.UP;
				double absX = Math.abs(ray.x);
				double absY = Math.abs(ray.y);
				double absZ = Math.abs(ray.z);
				if ((absX > absY) && (absX > absZ))
					face = (ray.x > 0) ? EnumFacing.WEST : EnumFacing.EAST;
				else if ((absY > absX) && (absY > absZ))
					face = (ray.y > 0) ? EnumFacing.DOWN : EnumFacing.UP;
				else if ((absZ > absX) && (absZ > absY))
					face = (ray.z > 0) ? EnumFacing.NORTH : EnumFacing.SOUTH;
				EnumFacing most = null;
				int spaces = -1;
				int reliableSurround = 0;
				boolean surrounded = false;
				for (int i = 0; i < 7; i++) {
					EnumFacing curFace = (i > 0) ? EnumFacing.values()[i-1] : face;
					if (i > 0 && curFace.equals(face)) continue; // skip if duplicate
					if (!world.getBlockState(member.offset(curFace,-1)).isFullBlock()) continue;
					if (placeWrecks.contains(member.offset(curFace,-1))) continue;
					if (!world.getBlockState(member.offset(curFace)).getBlock().isPassable(world,member.offset(curFace))) continue;
					if (placeWrecks.contains(member.offset(curFace))) continue;
					int mySpaces = 0;
					EnumFacing[] sides = null;
					switch(curFace.getAxis()) {
						case X: sides = new EnumFacing[]{EnumFacing.UP,EnumFacing.DOWN,EnumFacing.NORTH,EnumFacing.SOUTH}; break;
						case Y: sides = EnumFacing.HORIZONTALS; break;
						case Z: sides = new EnumFacing[]{EnumFacing.UP,EnumFacing.DOWN,EnumFacing.EAST,EnumFacing.WEST}; break;
					}
					if (sides == null) continue;
					int surround = 0;
					int reliable = 0;
					for (EnumFacing side : sides) {
						if (placeWrecks.contains(member.offset(side)))
							surround++;
						else if (world.getBlockState(member.offset(side)).isFullBlock()) {
							surround++;
							reliable++;
						}
						if (!world.getBlockState(member.offset(curFace).offset(side)).getBlock().isPassable(world,member.offset(curFace).offset(side))) continue;
						if (placeWrecks.contains(member.offset(curFace).offset(side))) continue;
						mySpaces++;
					}
					if (mySpaces > spaces) {
						spaces = mySpaces;
						most = curFace;
						surrounded = surround >= 4;
						reliableSurround = reliable;
					}
				}
				if (most != null) {
					// TODO: make RUBBLE model
					// RUBBLE should be used when (most != face)
					// Actually use it when (!surrounded)
					// TODO: use SLIGHT if verysurrounded
					Erosion erosion = PWRMeshedWreck.Erosion.NORMAL;
					if (reliableSurround >= 2) erosion = Erosion.SLIGHT;
					else if (!surrounded) erosion = Erosion.RUBBLE;
					if (material.equals(Material.IRON)) {
						if (soundType.equals(SoundType.STONE))
							ModBlocks.PWR.wreck_stone.create(world,member,most,state,erosion,heat);
						else
							ModBlocks.PWR.wreck_metal.create(world,member,most,state,erosion,heat);
					} else
						defaultPlacement = true;
				}
			}
			if (defaultPlacement) {
				Block[] sellafieldLevels = new Block[]{
						ModBlocks.sellafield_slaked,
						ModBlocks.sellafield_0,
						ModBlocks.sellafield_1,
						ModBlocks.sellafield_2,
						ModBlocks.sellafield_3,
						ModBlocks.sellafield_4,
						ModBlocks.sellafield_core
				};
				if (heat > 0) {
					if (block instanceof BlockGrass)
						world.setBlockState(member,ModBlocks.waste_earth.getStateFromMeta(Math.min(heat,6)));
					else if (block instanceof BlockGravel)
						world.setBlockState(member,ModBlocks.waste_gravel.getStateFromMeta(Math.min(heat,6)));
					else if (block instanceof BlockDirt || block == Blocks.FARMLAND)
						world.setBlockState(member,ModBlocks.waste_dirt.getStateFromMeta(Math.min(heat,6)));
					else if (block instanceof BlockSnow)
						world.setBlockState(member,ModBlocks.waste_snow.getStateFromMeta(Math.min(heat,6)));
					else if (block instanceof BlockSnowBlock)
						world.setBlockState(member,ModBlocks.waste_snow_block.getStateFromMeta(Math.min(heat,6)));
					else if (block instanceof BlockMycelium)
						world.setBlockState(member,ModBlocks.waste_mycelium.getStateFromMeta(Math.min(heat,6)));
					else if (block instanceof BlockRedSandstone)
						world.setBlockState(member,ModBlocks.waste_sandstone_red.getStateFromMeta(Math.min(heat,6)));
					else if (block instanceof BlockSandStone)
						world.setBlockState(member,ModBlocks.waste_sandstone.getStateFromMeta(Math.min(heat,6)));
					else if (block instanceof BlockHardenedClay || block instanceof BlockStainedHardenedClay)
						world.setBlockState(member,ModBlocks.waste_terracotta.getStateFromMeta(Math.min(heat,6)));
					else if (block instanceof BlockSand) {
						BlockSand.EnumType meta = state.getValue(BlockSand.VARIANT);
						world.setBlockState(member,((meta == BlockSand.EnumType.SAND) ? ModBlocks.waste_sand : ModBlocks.waste_sand_red).getStateFromMeta(Math.min(heat,6)));
					}
					else {
						int level = -1;
						if (block == Blocks.COBBLESTONE || block == Blocks.STONE || block instanceof BlockStone || block == sellafieldLevels[0])
							level = 0;
						else {
							for (int i = 1; i < sellafieldLevels.length; i++) {
								if (block == sellafieldLevels[i]) {
									level = i;
									break;
								}
							}
						}
						if ((level >= 0) && (heat >= level))
							world.setBlockState(member,sellafieldLevels[Math.min(heat,6)].getStateFromMeta(world.rand.nextInt(4)));
						else
							ModBlocks.PWR.wreck_stone.create(world,member,EnumFacing.UP,state,Erosion.NONE,heat);
					}
				}
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
		ExplosionNukeGeneric.waste(world, (int)centerPoint.x, (int)centerPoint.y, (int)centerPoint.z, 35);
		RadiationSavedData.incrementRad(world, new BlockPos(centerPoint), 3000F, 4000F);
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
			return new PWRData(entity).readFromNBT((NBTTagCompound) value);
		else
			return null;
	}

	void sendControlPositions() {
		LeafiaPacket._start(companion)
				.__write(29,writeControlPositions())
				.__write(28,masterControl)
				.__sendToAffectedClients();
	}

	@Override
	public String getPacketIdentifier() { throw new LeafiaDevFlaw("Method PWRData.getPacketIdentifier() is not supposed to be used! Spaghetti coding moment."); }
	@SideOnly(Side.CLIENT)
	@Override
	public void onReceivePacketLocal(byte key,Object value) {
		if (key == 30) { // Tank packets
			if (!value.getClass().isArray()) {
				Minecraft.getMinecraft().player.sendMessage(new TextWarningLeafia("Malformed PWR tank packet! (Given value wasn't Array)"));
				return;
			}
			if (Array.getLength(value) != 11) {
				Minecraft.getMinecraft().player.sendMessage(new TextWarningLeafia("Malformed PWR tank packet! (Array length must be 5, got "+Array.getLength(value)+")"));
				return;
			}
			int readIndex = 0;
			compression = (int)Array.get(value,readIndex++);
			for (int i = 0; i < 5; i++)
				tanks[i].setCapacity((int)Array.get(value,readIndex++));
			for (int i = 0; i < 5; i++)
				tanks[i].setFluid(new FluidStack(tankTypes[i],(int)Array.get(value,readIndex++)));
			// if we somehow got non-int values in the array, well... #ripbozo
		} else if (key == 29) { // Rod sync packets
			if (value instanceof NBTTagCompound) {
				readControlPositions((NBTTagCompound)value);
			}
		} else if (key == 31) {
			new PWRDiagnosis(companion.getWorld(),companion.getPos()).addPosition(companion.getPos());
		} else if (key == 28) { // Master rod sync
			masterControl = (double)value;
		}
	}
	@Override
	public void onReceivePacketServer(byte key,Object value,EntityPlayer plr) {
		if (key == 30) { // control rods request
			if (value instanceof NBTTagCompound) {
				NBTTagCompound nbt = (NBTTagCompound)value;
				if (nbt.hasKey("name")) {
					controlDemand.put(nbt.getString("name"),nbt.getDouble("level"));
					manipulateRod(nbt.getString("name"));
				} else {
					masterControl = nbt.getDouble("level");
					manipulateRod(null);
				}
				sendControlPositions();
			}
		} else if (key == 29) {
			if (value instanceof Integer) {
				compression = Math.floorMod((int)value,3);
			}
		}
	}
	@Override
	public void onPlayerValidate(EntityPlayer plr) {
		addDataToPacket(LeafiaPacket._start(companion),this).__sendToClient(plr);
	}
	void manipulateRod(String name) {
		for (BlockPos pos : controls) {
			TileEntity entity = getWorld().getTileEntity(pos);
			if (entity instanceof TileEntityPWRControl) {
				TileEntityPWRControl control = (TileEntityPWRControl)entity;
				double newTarget = 0;
				if (controlDemand.containsKey(control.name))
					newTarget = controlDemand.get(control.name)*masterControl;
				if (name != null) {
					if (!control.name.equals(name)) continue;
				}
				control.targetPosition = newTarget;
			}
		}
	}
}
