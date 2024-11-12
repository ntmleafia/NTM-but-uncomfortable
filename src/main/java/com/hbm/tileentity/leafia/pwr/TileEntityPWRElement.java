package com.hbm.tileentity.leafia.pwr;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.leafia.pwr.MachinePWRElement;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.inventory.leafia.inventoryutils.LeafiaPacket;
import com.hbm.inventory.leafia.inventoryutils.LeafiaPacketReceiver;
import com.hbm.items.ohno.ItemLeafiaRod;
import com.hbm.lib.InventoryHelper;
import com.hbm.tileentity.TileEntityInventoryBase;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TileEntityPWRElement extends TileEntityInventoryBase implements PWRBase, ITickable, LeafiaPacketReceiver {
	BlockPos corePos = null;
	PWRData data = null;

	public final Set<HeatRetrival> linearFuelMap = new HashSet<>();
	public final Set<HeatRetrival> cornerFuelMap = new HashSet<>();
	public static class HeatRetrival {
		public final List<BlockPos> controls = new ArrayList<>();
		public final BlockPos fuelPos;
		public final double divisor;
		public HeatRetrival(BlockPos fuelPos,int distance) {
			this.fuelPos = fuelPos;
			this.divisor = Math.pow(2,distance/2d-1);
		}
		public HeatRetrival(BlockPos fuelPos) {
			this.fuelPos = fuelPos;
			this.divisor = 2;
		}
		public double getControlMin(World world) {
			double control = 1;
			for (BlockPos pos : controls) {
				control = Math.min(control,getControl(world,pos));
			}
			return control;
		}
		public double getControlAvg(World world) {
			if (controls.size() <= 0) return 1;
			double control = 0;
			for (BlockPos pos : controls) {
				control += getControl(world,pos);
			}
			return control/controls.size();
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
	public double getHeatFromBlockPos(BlockPos pos,ItemLeafiaRod rod) {
		if (world.getBlockState(pos).getBlock() instanceof MachinePWRElement) {
			if (((MachinePWRElement) world.getBlockState(pos).getBlock()).tileEntityShouldCreate(world,pos)) {
				TileEntity entity = world.getTileEntity(pos);
				if (entity != null) {
					if (entity instanceof TileEntityPWRElement) {
						ItemStackHandler items = ((TileEntityPWRElement) entity).inventory;
						if (items != null) {
							return rod.getFlux(items.getStackInSlot(0));
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
	public void setCore(@Nullable BlockPos pos) {
		corePos = pos;
	}

	@Override
	public void setData(@Nullable PWRData data) {
		if (this.data != data) {
			PWRData.addDataToPacket(LeafiaPacket._start(this),data).__sendToAffectedClients();
		}
		this.data = data;
	}
	@Override
	public PWRData getData() {
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
		if (this.corePos != null) {
			TileEntity entity = world.getTileEntity(corePos);
			if (entity != null) {
				if (entity instanceof PWRBase) {
					return ((PWRBase) entity).getData();
				}
			}
		}
		return null;
	}
	@Override
	public void update() {
		if (this.data != null)
			this.data.update();
		if (!world.isRemote) {
			ItemStack stack = this.inventory.getStackInSlot(0);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ItemLeafiaRod) {
					double coolin = 0;
					PWRData gathered = gatherData();
					if (gathered != null) {
						coolin = Math.pow(gathered.tanks[0].getFluidAmount()/(double)gathered.tanks[0].getCapacity(),0.4);
					}
					ItemLeafiaRod rod = (ItemLeafiaRod)(stack.getItem());
					double heatDetection = 0;
					for (HeatRetrival retrival : cornerFuelMap) {
						heatDetection += getHeatFromBlockPos(retrival.fuelPos,rod)*retrival.getControlAvg(world);
					}
					for (HeatRetrival retrival : linearFuelMap) {
						heatDetection += getHeatFromBlockPos(retrival.fuelPos,rod)*retrival.getControlMin(world);
					}
					rod.HeatFunction(stack,true,heatDetection,coolin,20,400);
					rod.decay(stack,inventory,0);
					NBTTagCompound data = stack.getTagCompound();
					double cooled = 0;
					if (data != null) {
						if (data.getInteger("spillage") > 100) {
							world.destroyBlock(pos,false);
							world.setBlockState(pos,ModBlocks.corium_block.getDefaultState());
							if (gathered != null)
								gathered.explode(world,stack);
						}
						cooled = data.getDouble("cooled");
					}
					if (cooled > 0 && gathered != null) {
						int hotType = 1;
						int drain = (int)Math.ceil(cooled/10000*gathered.tanks[0].getCapacity()); // idk wtf is going on here >:( i hate math
						gathered.tanks[0].drain(drain,true);
						if (gathered.tanks[1].getFluidAmount() >= gathered.tanks[1].getCapacity())
							hotType = 2;
						gathered.tanks[hotType].fill(new FluidStack(gathered.tankTypes[hotType],drain),true);
						if (gathered.tanks[2].getFluidAmount() >= gathered.tanks[2].getCapacity())
							gathered.explode(world,stack);
					}
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
		if (key == 0) {
			if (value instanceof NBTTagCompound) {
				NBTTagCompound nbt = (NBTTagCompound)value;
				nbt.setBoolean("_isSyncSignal",true);
				readFromNBT(nbt);
			}
		} else if (key == 31) {
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
