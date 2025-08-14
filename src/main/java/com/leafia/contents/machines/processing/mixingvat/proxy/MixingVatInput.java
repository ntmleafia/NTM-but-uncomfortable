package com.leafia.contents.machines.processing.mixingvat.proxy;

import com.hbm.tileentity.TileEntityProxyCombo;
import com.leafia.contents.machines.processing.mixingvat.MixingVatTE;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import org.jetbrains.annotations.Nullable;

public class MixingVatInput extends TileEntityProxyCombo implements IFluidHandler {

	@Override
	public <T> T getCapability(Capability<T> capability,EnumFacing facing) {
		if(tile == null) {
			tile = this.getTE();
			if(tile == null){
				return super.getCapability(capability, facing);
			}
		}
		if(inventory && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return tile.getCapability(capability, facing);
		}
		if(power && capability == CapabilityEnergy.ENERGY){
			return tile.getCapability(capability, facing);
		}
		if(fluid && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public @Nullable FluidStack drain(int maxDrain,boolean doDrain) {
		return null;
	}
	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[0];
	}
	@Override
	public int fill(FluidStack resource,boolean doFill) {
		return ((MixingVatTE)tile).fill(resource,doFill);
	}
	@Override
	public @Nullable FluidStack drain(FluidStack resource,boolean doDrain) {
		return null;
	}
}
