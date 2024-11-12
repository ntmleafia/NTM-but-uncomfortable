package com.hbm.tileentity.leafia.pwr;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.leafia.LeafiaRegisterTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class TileEntityPWRPort extends TileEntity implements PWRBase, IFluidHandler, LeafiaRegisterTileEntity {
	static {
		MainRegistry.registerTileEntities.put(TileEntityPWRPort.class,"pwr_port"); // didnt work. I hate this game
	}
	BlockPos corePos = null;
	@Override
	public void setCore(@Nullable BlockPos pos) {
		corePos = pos;
	}
	@Override
	public void setData(@Nullable PWRData data) {}
	@Override
	public PWRData getData() { return null; }
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
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("corePosX"))
			corePos = new BlockPos(
					compound.getInteger("corePosX"),
					compound.getInteger("corePosY"),
					compound.getInteger("corePosZ")
			);
		super.readFromNBT(compound);
	}
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (corePos != null) {
			compound.setInteger("corePosX",corePos.getX());
			compound.setInteger("corePosY",corePos.getY());
			compound.setInteger("corePosZ",corePos.getZ());
		}
		return super.writeToNBT(compound);
	}

	// redirects
	@Override
	public IFluidTankProperties[] getTankProperties() {
		PWRData gathered = gatherData();
		return (gathered != null) ? gathered.getTankProperties() : new IFluidTankProperties[0];
	}
	@Override
	public int fill(FluidStack resource,boolean doFill) {
		PWRData gathered = gatherData();
		return (gathered != null) ? gathered.fill(resource,doFill) : 0;
	}
	@Nullable
	@Override
	public FluidStack drain(FluidStack resource,boolean doDrain) {
		PWRData gathered = gatherData();
		return (gathered != null) ? gathered.drain(resource,doDrain) : null;
	}
	@Nullable
	@Override
	public FluidStack drain(int maxDrain,boolean doDrain) {
		PWRData gathered = gatherData();
		return (gathered != null) ? gathered.drain(maxDrain,doDrain) : null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability,@Nullable EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability,facing);
	}
	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability,@Nullable EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		}
		return super.getCapability(capability,facing);
	}
}
