package com.leafia.contents.machines.reactors.pwr.blocks.components.element;

import com.hbm.blocks.ModBlocks;
import com.hbm.saveddata.RadiationSavedData;
import com.hbm.util.Tuple.*;
import com.leafia.contents.machines.reactors.pwr.blocks.MachinePWRReflector;
import com.leafia.contents.machines.reactors.pwr.blocks.MachinePWRSource;
import com.leafia.contents.machines.reactors.pwr.blocks.components.PWRComponentEntity;
import com.leafia.contents.machines.reactors.pwr.blocks.components.control.MachinePWRControl;
import com.leafia.contents.machines.reactors.pwr.blocks.components.control.TileEntityPWRControl;
import com.hbm.interfaces.IRadResistantBlock;
import com.leafia.contents.machines.reactors.pwr.PWRData;
import com.leafia.dev.container_utility.LeafiaPacket;
import com.leafia.dev.container_utility.LeafiaPacketReceiver;
import com.leafia.contents.control.fuel.nuclearfuel.ItemLeafiaRod;
import com.hbm.lib.InventoryHelper;
import com.hbm.tileentity.TileEntityInventoryBase;
import com.hbm.util.I18nUtil;
import com.llib.group.LeafiaMap;
import com.llib.math.range.RangeDouble;
import com.llib.math.range.RangeInt;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityPWRElement extends TileEntityInventoryBase implements PWRComponentEntity, ITickable, LeafiaPacketReceiver {
	BlockPos corePos = null;
	PWRData data = null;
	int height = 1;

	@Override
	public void onDiagnosis() {
		height = getHeight();
		// call those ultimately complex functions o-o
		updateCornerMap();
		updateLinearMap();
	}

	public int getHeight() {
		int height = 1;
		for (BlockPos p = pos.down(); world.isValid(p); p = p.down()) {
			if (world.getBlockState(p).getBlock() instanceof MachinePWRElement)
				height++;
			else
				break;
		}
		return height;
	}
	static abstract class MapConsumer {
		int i = 0;
		abstract HeatRetrival accept(BlockPos fuelPos,Map<BlockPos,Pair<RangeDouble,RangeDouble>> controls,Set<RangeDouble> areas);
		public MapConsumer() {}
		public MapConsumer(int i) {
			this.i = i;
		}
	}

	public final Set<HeatRetrival> linearFuelMap = new HashSet<>();
	public final Set<HeatRetrival> cornerFuelMap = new HashSet<>();
	void updateCornerMap() {
		cornerFuelMap.clear();
		RangeInt range = new RangeInt(0,height-1);
		for (int bin = 0; bin <= 0b11; bin++) {
			final Map<BlockPos,Pair<RangeDouble,RangeDouble>> controls = new HashMap<>();
			List<RangeInt> areas = new ArrayList<>();
			areas.add(range);
			Set<Integer> moderatedRows = new HashSet<>();
			linetraceNeutrons(pos.add(((bin>>1)&1)*2-1,0,0),areas,controls);
			linetraceNeutrons(pos.add(0,0,(bin&1)*2-1),areas,controls);
			searchFuelAndAdd(pos.add(((bin>>1)&1)*2-1,0,(bin&1)*2-1),areas,controls,moderatedRows,new MapConsumer() {
				@Override
				HeatRetrival accept(BlockPos fuelPos,Map<BlockPos,Pair<RangeDouble,RangeDouble>> controls,Set<RangeDouble> areas) {
					HeatRetrival retrival = new HeatRetrival(pos,controls,areas);
					cornerFuelMap.add(retrival);
					return retrival;
				}
			});
		}
	}
	void updateLinearMap() {
		linearFuelMap.clear();
		RangeInt range = new RangeInt(0,height-1);
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			final LeafiaMap<BlockPos,Pair<RangeDouble,RangeDouble>> controls = new LeafiaMap<>();
			List<RangeInt> areas = new ArrayList<>();
			areas.add(range);
			Set<Integer> moderatedRows = new HashSet<>();
			for (int i = 1; (areas.size() > 0) && (i < 20); i++) {
				BlockPos basePos = pos.add(facing.getFrontOffsetX()*i,0,facing.getFrontOffsetZ()*i);
				if (!world.isValid(basePos)) break;
				linetraceNeutrons(basePos,areas,controls);
				searchFuelAndAdd(basePos,new ArrayList<>(areas),controls.clone(),moderatedRows,new MapConsumer(i) {
					@Override
					HeatRetrival accept(BlockPos fuelPos,Map<BlockPos,Pair<RangeDouble,RangeDouble>> controls,Set<RangeDouble> areas) {
						HeatRetrival retrival = new HeatRetrival(pos,controls,areas,this.i);
						linearFuelMap.add(retrival);
						return retrival;
					}
				});
			}
		}
	}
	Set<RangeDouble> intersectRanges(Set<RangeDouble> a,Set<RangeDouble> b) {
		Set<RangeDouble> intersection = new HashSet<>();
		for (RangeDouble rangeA : a) {
			for (RangeDouble rangeB : b) {
				RangeDouble range = new RangeDouble(Math.max(rangeA.min,rangeB.min),Math.min(rangeA.max,rangeB.max));
				if (range.min <= range.max)
					intersection.add(range);
			}
		}
		return intersection;
	}
	void searchFuelAndAdd(BlockPos basePos,List<RangeInt> areas,Map<BlockPos,Pair<RangeDouble,RangeDouble>> controls,Set<Integer> moderatedRows,MapConsumer callback) {
		Set<RangeDouble> scaledAreas = new HashSet<>();
		for (RangeInt area : areas) {
			scaledAreas.add(new RangeDouble(area.min / (double) height,area.max / (double) height));
			for (Integer depth : area) {
				BlockPos searchPos = basePos.down(depth);
				if (world.isValid(searchPos)) {
					Block block = world.getBlockState(searchPos).getBlock();
					if (block.getRegistryName() != null) {
						if (("_"+block.getRegistryName().getResourcePath()+"_").matches(".*[^a-z]graphite[^a-z].*"))
							moderatedRows.add(depth);
					}
				}
			}
		}
		List<Integer> blocked = new ArrayList<>();
		//int neutronSources = 0; The "Heat Function" is DESIGNED to omit the need of neutron sources.
		//                        Yeah, there's no way this is getting along.
		boolean[] reflectors = new boolean[height];
		boolean doReflect = false;
		for (RangeInt area : areas) {
			for (Integer depth : area) {
				int curDepth = depth;
				while (!blocked.contains(curDepth) && world.isValid(basePos.down(curDepth))) {
					blocked.add(curDepth);
					Block block = world.getBlockState(basePos.down(curDepth)).getBlock();
					if (block instanceof MachinePWRElement) {
						if (((MachinePWRElement) block).tileEntityShouldCreate(world,basePos.down(curDepth))) {
							int bottomDepth = depth;
							while (world.isValid(basePos.down(bottomDepth+1))) {
								if (world.getBlockState(basePos.down(bottomDepth + 1)).getBlock() instanceof MachinePWRElement) {
									bottomDepth++;
									blocked.add(bottomDepth);
								} else
									break;
							}
							int elementHeight = bottomDepth-curDepth+1;
							int moderation = 0;
							for (int depthMod = curDepth; depthMod <= bottomDepth; depthMod++) {
								if (moderatedRows.contains(depthMod))
									moderation++;
							}
							Set<RangeDouble> myArea = new HashSet<>();
							myArea.add(new RangeDouble(curDepth/(double)height,bottomDepth/(double)height));
							callback.accept(basePos.down(curDepth),controls,intersectRanges(scaledAreas,myArea)).moderation = moderation/(double)elementHeight;
							break;
						}
					} else
						break;
					curDepth--;
				}
				BlockPos movePos = basePos.down(depth);
				if (world.isValid(movePos)) {
					Block block = world.getBlockState(movePos).getBlock();
					if (block instanceof MachinePWRSource) {
						// nope
					} else if (block instanceof MachinePWRReflector) {
						reflectors[depth] = true;
						doReflect = true;
					}
				}
			}
		}
		if (doReflect) {
			int moderation = 0;
			int totalReflectors = 0;
			List<RangeInt> reflect = new ArrayList<>();
			boolean create = true;
			for (int depth = 0; depth < height; depth++) {
				boolean ref = reflectors[depth];
				if (ref) {
					totalReflectors++;
					if (moderatedRows.contains(depth))
						moderation++;
					if (create) {
						reflect.add(new RangeInt(depth,depth));
						create = false;
					} else
						reflect.get(reflect.size() - 1).max = depth;
				} else
					create = true;
			}
			Set<RangeDouble> reflectSet = new HashSet<>(); // have to create again because modifying elements of Set corrupts it >:// (Java moment)
			for (RangeInt ref : reflect) {
				reflectSet.add(new RangeDouble(ref.min / (double) height,ref.max / (double) height));
			}
			callback.accept(pos,controls,intersectRanges(scaledAreas,reflectSet)).moderation = moderation/(double)totalReflectors;
		}
	}
	void linetraceNeutrons(BlockPos basePos,List<RangeInt> areas,Map<BlockPos,Pair<RangeDouble,RangeDouble>> controls) {
		RangeInt range = new RangeInt(0,height-1);
		/* carving out neutron rays */ {
			for (Integer depth : range) {
				Block block = world.getBlockState(basePos.down(depth)).getBlock();
				if (block instanceof IRadResistantBlock) {
					if (!(((IRadResistantBlock) block).isRadResistant(world,basePos.down(depth))))
						continue;
				} else
					continue;

				RangeInt subject = null;
				for (RangeInt area : areas) { if (area.isInRange(depth)) { subject = area; break; } }
				if (subject == null) continue;
				areas.remove(subject);
				int condition = ((subject.min == depth) ? 0b10 : 0)+
						((subject.max == depth) ? 0b01 : 0);
				switch(condition) {
					case 0b00:
						areas.add(new RangeInt(subject.min,depth-1));
						areas.add(new RangeInt(depth+1,subject.max));
						break;
					case 0b01:
						subject.max--;
						areas.add(subject);
						break;
					case 0b10:
						subject.min++;
						areas.add(subject);
						break;
					case 0b11:
						break;
				}
			}
		}

		/* detect rods */ {
			Integer rodTop = null;
			if (world.getBlockState(basePos).getBlock() instanceof MachinePWRControl) {
				rodTop = 0;
				for (BlockPos searchPos = basePos.up(); world.isValid(searchPos); searchPos = searchPos.up()) {
					if (world.getBlockState(searchPos).getBlock() instanceof MachinePWRControl)
						rodTop -= 1;
					else
						break;
				}
			}
			int depth = 0;
			for (BlockPos searchPos = basePos; (world.isValid(searchPos) || (rodTop != null)); searchPos = searchPos.down()) {
				boolean isControl = world.isValid(searchPos); if (isControl) isControl = world.getBlockState(searchPos).getBlock() instanceof MachinePWRControl;
				if (rodTop == null) {
					if (isControl)
						rodTop = depth;
				} else {
					if (!isControl) {
						int rodBottom = depth-1;
						double rodHeight = rodBottom-rodTop+1;
						if (rodBottom < height+rodHeight) {
							int rodUnderneath = rodBottom - height;
							RangeDouble rangeBottom = new RangeDouble(
									rodUnderneath / rodHeight,
									(rodUnderneath + height) / rodHeight
							);
							RangeDouble rangeTop = new RangeDouble(
									1 - rodTop / rodHeight,
									1 - (rodTop + height) / rodHeight
							);
							controls.put(new BlockPos(basePos.getX(),basePos.getY() - rodTop,basePos.getZ()),new Pair<>(rangeBottom,rangeTop));
						}
						rodTop = null;
					}
				}
				depth++;
			}
		}
	}
	public static class HeatRetrival {
		public final Map<BlockPos,Pair<RangeDouble,RangeDouble>> controls;
		public final BlockPos fuelPos;
		public final double divisor;
		public final Set<RangeDouble> areas;
		public double moderation;
		public HeatRetrival(BlockPos fuelPos,Map<BlockPos,Pair<RangeDouble,RangeDouble>> controls,Set<RangeDouble> areas,int distance) {
			this.fuelPos = fuelPos;
			this.divisor = Math.pow(2,distance/2d-1);
			this.areas = areas;
			this.controls = controls;
		}
		public HeatRetrival(BlockPos fuelPos,Map<BlockPos,Pair<RangeDouble,RangeDouble>> controls,Set<RangeDouble> areas) {
			this.fuelPos = fuelPos;
			this.divisor = 2;
			this.areas = areas;
			this.controls = controls;
		}
		public double getControlMin(World world) {
			/*double control = 1;
			for (BlockPos pos : controls) {
				control = Math.min(control,getControl(world,pos));
			}*/
			double control = 0;
			for (RangeDouble area : areas) {
				double localRatioB = 1;
				double localRatioT = 1;
				for (Map.Entry<BlockPos,Pair<RangeDouble,RangeDouble>> entry : controls.entrySet()) {
					double rodPos = getControl(world,entry.getKey());
					{
						double normalizedPos = entry.getValue().getA().ratio(rodPos);
						localRatioB = Math.min(localRatioB,MathHelper.clamp(normalizedPos,0,1));
					}
					{
						double normalizedPos = entry.getValue().getB().ratio(rodPos);
						localRatioT = Math.min(localRatioT,MathHelper.clamp(normalizedPos,0,1));
					}
				}
				control += Math.min(localRatioB+localRatioT,1)*(area.max-area.min);
			}
			return control;
		}
		public double getControlAvg(World world) {
			/*
			double control = 0;
			for (BlockPos pos : controls) {
				control += getControl(world,pos);
			}*/
			double control = 0;
			for (RangeDouble area : areas) {
				double localRatio = 0;
				int cnt = 0;
				for (Map.Entry<BlockPos,Pair<RangeDouble,RangeDouble>> entry : controls.entrySet()) {
					cnt++;
					double rodPos = getControl(world,entry.getKey());
					double normalizedPosA = entry.getValue().getA().ratio(rodPos);
					double normalizedPosB = entry.getValue().getB().ratio(rodPos);
					localRatio += MathHelper.clamp(Math.max(normalizedPosA,normalizedPosB),0,1);
				}
				if (cnt <= 0) {
					localRatio = 1;
					cnt = 1;
				}
				control += localRatio/cnt*(area.max-area.min);
			}
			return control;
		}
		public double getControl(World world,BlockPos pos) {
			TileEntity entity = world.getTileEntity(pos);
			if (entity != null) {
				if (entity instanceof TileEntityPWRControl) {
					return ((TileEntityPWRControl) entity).position;
				}
			}
			if (world.getBlockState(pos).getBlock() instanceof IRadResistantBlock) {
				return ((IRadResistantBlock) world.getBlockState(pos).getBlock()).isRadResistant(world,pos) ? 0 : 1;
			}
			return 1;
		}
	}
	public double getHeatFromHeatRetrival(HeatRetrival retrival,ItemLeafiaRod rod) {
		BlockPos pos = retrival.fuelPos;
		if (world.getBlockState(pos).getBlock() instanceof MachinePWRElement) {
			if (((MachinePWRElement) world.getBlockState(pos).getBlock()).tileEntityShouldCreate(world,pos)) {
				TileEntity entity = world.getTileEntity(pos);
				if (entity != null) {
					if (entity instanceof TileEntityPWRElement) {
						ItemStackHandler items = ((TileEntityPWRElement) entity).inventory;
						if (items != null) {
							return rod.getFlux(items.getStackInSlot(0))*(1-retrival.moderation)+rod.getFlux(items.getStackInSlot(0),true)*retrival.moderation;
						}
					}
				}
			}
		}
		return 0;
	}

	public TileEntityPWRElement() {
		super(1);
	}

	public void connectUpper() { // For clients, called only on validate()
		if (!this.isInvalid() && world.isBlockLoaded(pos)) {
			Chunk chunk = world.getChunkFromBlockCoords(pos);
			if (world.isRemote) { // Keep in mind that neighborChanged in Block does NOT get called for Remotes
				if (world.getBlockState(pos.down()).getBlock() instanceof MachinePWRElement) {
					TileEntity entityBelow = chunk.getTileEntity(pos.down(),Chunk.EnumCreateEntityType.CHECK);
					if (entityBelow != null) {
						if (entityBelow instanceof TileEntityPWRElement) {
							((TileEntityPWRElement)entityBelow).connectUpper();
						}
					}
				}
				if (world.getBlockState(pos.up()).getBlock() instanceof MachinePWRElement) {
					inventory.setStackInSlot(0,ItemStack.EMPTY);
					invalidate();
				}
				return;
			}
			BlockPos upPos = pos.up();
			boolean mustTransmit = false;
			TileEntityPWRElement target = null;
			while (world.isValid(upPos)) {
				if (world.getBlockState(upPos).getBlock() instanceof MachinePWRElement) {
					mustTransmit = true;
					TileEntity entity = chunk.getTileEntity(upPos,Chunk.EnumCreateEntityType.CHECK);
					target = null;
					if (entity != null) {
						if (entity instanceof TileEntityPWRElement) {
							if (!entity.isInvalid()) {
								target = (TileEntityPWRElement) entity;
								if (!target.inventory.getStackInSlot(0).isEmpty())
									target = null;
							}
						}
					}
				} else
					break;
				upPos = upPos.up();
			}
			if (mustTransmit) {
				if (target != null) {
					target.inventory.setStackInSlot(0,inventory.getStackInSlot(0));
					this.inventory.setStackInSlot(0,ItemStack.EMPTY);
				} else
					InventoryHelper.dropInventoryItems(world,pos,this);
				this.invalidate();
			}
		}
	}
	@Override
	public void setCoreLink(@Nullable BlockPos pos) {
		corePos = pos;
	}

	@Override
	public PWRData getLinkedCore() {
		return PWRComponentEntity.getCoreFromPos(world,corePos);
	}

	@Override
	public void assignCore(@Nullable PWRData data) {
		if (this.data != data) {
			PWRData.addDataToPacket(LeafiaPacket._start(this),data).__sendToAffectedClients();
		}
		this.data = data;
	}
	@Override
	public PWRData getCore() {
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("corePosX"))
			corePos = new BlockPos(
					compound.getInteger("corePosX"),
					compound.getInteger("corePosY"),
					compound.getInteger("corePosZ")
			);
		super.readFromNBT(compound);
		if (compound.hasKey("data")) { // DO NOT MOVE THIS ABOVE SUPER CALL! super.readFromNBT() is where this.pos gets initialized!!
			data = new PWRData(this);
			data.readFromNBT(compound);
		}
	}

	@Override
	public String getName() {
		return I18nUtil.resolveKey("tile.pwr_element.name");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (corePos != null) {
			compound.setInteger("corePosX",corePos.getX());
			compound.setInteger("corePosY",corePos.getY());
			compound.setInteger("corePosZ",corePos.getZ());
		}
		if (data != null) {
			data.writeToNBT(compound);
		}
		return super.writeToNBT(compound);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!world.isRemote)
			syncLocals();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (this.data != null)
			this.data.invalidate(world);
	}

	@Override
	public void validate() {
		super.validate();
		//if (world.isRemote) { // so long lol
		//if (!compound.hasKey("_isSyncSignal")) {
		//LeafiaPacket._validate(this);
		//LeafiaPacket._start(this).__write((byte)0,true).__setTileEntityQueryType(Chunk.EnumCreateEntityType.CHECK).__sendToServer();
		//}
		//}
		connectUpper();
	}
	@Nullable
	PWRData gatherData() {
		/*
		if (this.corePos != null) {
			TileEntity entity = world.getTileEntity(corePos);
			if (entity != null) {
				if (entity instanceof PWRComponentEntity) {
					return ((PWRComponentEntity) entity).getCore();
				}
			}
		}
		return null;*/
		return this.getLinkedCore();
	}
	@Override
	public void update() {
		if (this.data != null)
			this.data.update();
		if (!world.isRemote) {
			ItemStack stack = this.inventory.getStackInSlot(0);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ItemLeafiaRod) {
					int height = getHeight();
					double coolin = 0;
					PWRData gathered = gatherData();
					if (gathered != null) {
						coolin = Math.pow(gathered.tanks[0].getFluidAmount()/(double)Math.max(gathered.tanks[0].getCapacity(),1),0.4)
								*(gathered.tanks[0].getCapacity()/128_000d);
					}
					ItemLeafiaRod rod = (ItemLeafiaRod)(stack.getItem());
					double heatDetection = 0;
					for (HeatRetrival retrival : cornerFuelMap)
						heatDetection += getHeatFromHeatRetrival(retrival,rod)*retrival.getControlAvg(world)*height;
					for (HeatRetrival retrival : linearFuelMap)
						heatDetection += getHeatFromHeatRetrival(retrival,rod)*retrival.getControlMin(world)*height;
					double rad = Math.pow(heatDetection,0.65)/2;
					RadiationSavedData.incrementRad(world,pos,(float)rad/8,(float)rad);

					rod.HeatFunction(stack,true,heatDetection,coolin,20,400);
					rod.decay(stack,inventory,0);
					NBTTagCompound data = stack.getTagCompound();
					double cooled = 0;
					if (data != null) {
						if (data.getInteger("spillage") > 100) {
							if (rod.meltdownPriority > 0) {
								if (gathered != null)
									gathered.explode(world,stack);
							} else {
								inventory.setStackInSlot(0,ItemStack.EMPTY);
								//world.destroyBlock(pos,false);
								world.playEvent(2001, pos, Block.getStateId(world.getBlockState(pos)));
								world.setBlockState(pos,ModBlocks.corium_block.getDefaultState());
								BlockPos nextPos = pos.down();
								while (world.isValid(nextPos)) {
									if (world.getBlockState(nextPos).getBlock() instanceof MachinePWRElement)
										world.setBlockState(nextPos,ModBlocks.corium_block.getDefaultState());
									else
										break;
									nextPos = nextPos.down();
								}
								return;
							}
						}
						cooled = data.getDouble("cooled");
					}
					if (gathered != null)
						cooled += Math.pow(gathered.coriums*2727,0.1);
					if (cooled > 0 && gathered != null) {
						int hotType = 1;
						int drain = (int)Math.ceil(cooled/10000*gathered.tanks[0].getCapacity());
						if (gathered.tanks[0].getFluidAmount() > 0) {
							if (gathered.tanks[1].getFluidAmount() >= gathered.tanks[1].getCapacity())
								hotType = 2;
							gathered.tanks[hotType].fill(new FluidStack(gathered.tankTypes[hotType],drain),true);
							if (gathered.tanks[2].getFluidAmount() >= gathered.tanks[2].getCapacity())
								gathered.explode(world,stack);
						}
						gathered.tanks[0].drain(drain,true);
					}
					NBTTagCompound nbt = stack.getTagCompound();
					LeafiaPacket packet = LeafiaPacket._start(this);
					if (nbt.hasKey("heat"))
						packet.__write(1,nbt.getDouble("heat"));
					if (nbt.hasKey("depletion"))
						packet.__write(2,nbt.getDouble("depletion"));
					if (nbt.hasKey("incoming"))
						packet.__write(3,nbt.getDouble("incoming"));
					if (nbt.hasKey("melting"))
						packet.__write(4,nbt.getBoolean("melting"));
					packet.__sendToAffectedClients();
				}
			}
		}
	}

	@Override
	public String getPacketIdentifier() {
		return "PWRElement";
	}
	public LeafiaPacket generateSyncPacket() {
		NBTTagCompound nbt = writeToNBT(new NBTTagCompound());
		if (nbt.hasKey("data"))
			nbt.removeTag("data");
		return LeafiaPacket._start(this).__write((byte)0,nbt);
	}
	public void syncLocals() {
		generateSyncPacket().__sendToAffectedClients();//.__setTileEntityQueryType(Chunk.EnumCreateEntityType.CHECK).__sendToAllInDimension();
	}
	@SideOnly(Side.CLIENT)
	@Override
	public void onReceivePacketLocal(byte key,Object value) {
		switch(key) {
			case 0:
				if (value instanceof NBTTagCompound) {
					NBTTagCompound nbt = (NBTTagCompound)value;
					nbt.setBoolean("_isSyncSignal",true);
					readFromNBT(nbt);
				}
				break;
			case 1:
			case 2:
			case 3:
				String[] doubleArray = new String[]{"heat","depletion","incoming"};
				if (value instanceof Double) {
					if (inventory != null) {
						if (!inventory.getStackInSlot(0).isEmpty()) {
							if (inventory.getStackInSlot(0).getItem() instanceof ItemLeafiaRod) {
								NBTTagCompound nbt = inventory.getStackInSlot(0).getTagCompound();
								if (nbt == null) nbt = new NBTTagCompound();
								nbt.setDouble(doubleArray[key-1],(double)value);
								inventory.getStackInSlot(0).setTagCompound(nbt);
							}
						}
					}
				}
			case 4:
				if (value instanceof Boolean) {
					if (inventory != null) {
						if (!inventory.getStackInSlot(0).isEmpty()) {
							if (inventory.getStackInSlot(0).getItem() instanceof ItemLeafiaRod) {
								NBTTagCompound nbt = inventory.getStackInSlot(0).getTagCompound();
								if (nbt == null) nbt = new NBTTagCompound();
								nbt.setBoolean("melting",(boolean)value);
								inventory.getStackInSlot(0).setTagCompound(nbt);
							}
						}
					}
				}
			case 31:
				data = PWRData.tryLoadFromPacket(this,value);
		}
		if (this.data != null)
			this.data.onReceivePacketLocal(key,value);
	}
	@Override
	public void onReceivePacketServer(byte key,Object value,EntityPlayer plr) {/*
        if (key == 0) {
            if (value.equals(true)) {
            }
        }*/
		if (this.data != null)
			this.data.onReceivePacketServer(key,value,plr);
	}
	@Override
	public void onPlayerValidate(EntityPlayer plr) {
		LeafiaPacket packet = generateSyncPacket();
		if (this.data != null) {
			PWRData.addDataToPacket(packet,this.data);
		}
		packet.__sendToClient(plr);
	}
}
