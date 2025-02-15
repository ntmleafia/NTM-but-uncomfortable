package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.ILaserable;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.lib.Library;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energy.IEnergyGenerator;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityCoreReceiver extends TileEntityMachineBase implements ITickable, IEnergyGenerator, IFluidHandler, ILaserable, ITankPacketAcceptor, SimpleComponent {

	public long power;
	public long joules;
	//Because it get cleared after the te updates, it needs to be saved here for the container
	public long syncJoules;
	public FluidTank tank;

	public TileEntityCore core = null;
	public TileEntityCoreReceiver() {
		super(0);
		tank = new FluidTank(64000);
	}

	@Override
	public void update() {
		core = null;
		EnumFacing facing = EnumFacing.getFront(this.getBlockMetadata());
		for(int i = 1; i <= TileEntityCoreEmitter.range; i++) {
			BlockPos offs = pos.offset(facing,i);
			TileEntity te = world.getTileEntity(offs);
			if (te instanceof TileEntityCore) {
				core = (TileEntityCore)te;
				core.absorbers.add(this);
			}
		}
		if(!world.isRemote) {
			if(Long.MAX_VALUE-power < joules * 5000L)
				power = Long.MAX_VALUE;
			else
				power += joules * 5000L;

			this.sendPower(world, pos);

			if(joules > 0) {

				if(tank.getFluidAmount() >= 20) {
					tank.drain(20, true);
				} else {
					world.setBlockState(pos, Blocks.FLOWING_LAVA.getDefaultState());
					return;
				}
			}

			syncJoules = joules;
			
			joules = 0;
		}
	}

	@Override
	public String getName() {
		return "container.dfcReceiver";
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return tank.getTankProperties();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(resource == null || resource.getFluid() != ModForgeFluids.cryogel)
			return 0;
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public void addEnergy(long energy, EnumFacing dir) {
		// only accept lasers from the front
		if(dir.getOpposite().ordinal() == this.getBlockMetadata()) {
			if(Long.MAX_VALUE - joules < energy)
				joules = Long.MAX_VALUE;
			else
				joules += energy;
		} else {
			world.destroyBlock(pos, false);
			world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.5F, true);
		}
	}

	@Override
	public void recievePacket(NBTTagCompound[] tags) {
		if(tags.length == 1)
			tank.readFromNBT(tags[0]);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		power = compound.getLong("power");
		joules = compound.getLong("joules");
		tank.readFromNBT(compound.getCompoundTag("tank"));
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("power", power);
		compound.setLong("joules", joules);
		compound.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
		return super.writeToNBT(compound);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getMaxPower() {
		return 0;
	}

	@Override
	public String getComponentName() {
		return "dfc_receiver";
	}
	@Callback
	public Object[] incomingEnergy(Context context, Arguments args) {
		return new Object[] {syncJoules};
	}
	@Callback
	public Object[] outgoingPower(Context context, Arguments args) {
		return new Object[] {power};
	}
	@Callback
	public Object[] storedCoolnt(Context context, Arguments args) {
		return new Object[] {tank.getFluidAmount()};
	}
}
