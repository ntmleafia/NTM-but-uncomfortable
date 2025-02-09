package com.leafia.contents.machines.manfacturing.soldering;

import api.hbm.energy.IEnergyUser;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.tileentity.TileEntityMachineBase;
import com.leafia.dev.blockitems.LeafiaQuickModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class SolderingTE extends TileEntityMachineBase implements IEnergyUser, IFluidHandler, ITankPacketAcceptor, LeafiaQuickModel {
	public long power;
	public long maxPower = 2_000;
	public long consumption;
	public boolean collisionPrevention = false;

	public int progress;
	public int processTime = 1;

	public FluidTank tank;
	public ItemStack display;

	public SolderingTE() {
		super(11);
		tank = new FluidTank(8000);
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public void recievePacket(NBTTagCompound[] tags) {

	}

	@Override
	public String getName() {
		return "container.machineSoldering";
	}

	@Override
	public String _resourcePath() {
		return "soldering";
	}

	@Override
	public String _assetPath() {
		return "machines/cat1/soldering_station";
	}

	@Override
	public TileEntitySpecialRenderer<TileEntity> _renderer() {
		return new SolderingRender();
	}

	@Override
	public Block _block() {
		return ModBlocks.machine_soldering;
	}

	@Override
	public double _sizeReference() {
		return 5;
	}

	@Override
	public double _itemYoffset() {
		return 3;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[0];
	}

	@Override
	public int fill(FluidStack resource,boolean doFill) {
		return 0;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource,boolean doDrain) {
		return null;
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain,boolean doDrain) {
		return null;
	}
}
