package com.leafia.contents.machines.processing.mixingvat;

import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.TileEntityMachineBase;
import com.leafia.dev.blockitems.LeafiaQuickModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

public class MixingVatTE extends TileEntityMachineBase implements LeafiaQuickModel, IFluidHandler {
	public MixingVatTE() {
		super(9+4);
	}

	@Override
	public String getName() {
		return "tile.mixingvat.name";
	}

	@Override
	public String _resourcePath() {
		return "mixingvat";
	}

	@Override
	public String _assetPath() {
		return "xenoulexi/mixingvat";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TileEntitySpecialRenderer<TileEntity> _renderer() {
		return new MixingVatRender();
	}

	@Override
	public Block _block() {
		return ModBlocks.mixingvat;
	}

	@Override
	public double _sizeReference() {
		return 5;
	}

	@Override
	public double _itemYoffset() {
		return -0.07;
	}

	@Override
	public <T> T getCapability(Capability<T> capability,EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		} else {
			return super.getCapability(capability, facing);
		}
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[0];
	}

	@Override
	public int fill(FluidStack resource,boolean doFill) {
		return 0;
	}

	@Override
	public @Nullable FluidStack drain(FluidStack resource,boolean doDrain) {
		return null;
	}

	@Override
	public @Nullable FluidStack drain(int maxDrain,boolean doDrain) {
		return null;
	}
}
