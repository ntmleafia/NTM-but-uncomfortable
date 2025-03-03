package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.explosion.ExplosionLarge;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.ILaserable;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energy.IEnergyGenerator;
import com.hbm.util.Tuple.Pair;
import com.leafia.contents.machines.powercores.dfc.DFCBaseTE;
import com.leafia.contents.machines.powercores.dfc.debris.AbsorberShrapnelEntity;
import com.leafia.contents.machines.powercores.dfc.debris.AbsorberShrapnelEntity.DebrisType;
import com.llib.LeafiaLib.NumScale;
import com.llib.math.FiaMatrix;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
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
public class TileEntityCoreReceiver extends DFCBaseTE implements ITickable, IEnergyGenerator, IFluidHandler, ILaserable, ITankPacketAcceptor, SimpleComponent {

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

	void spawnShrapnel(DebrisType type) {
		Vec3d center = new Vec3d(pos).addVector(0.5,0.5,0.5);
		FiaMatrix mat = new FiaMatrix(center,center.add(new Vec3d(EnumFacing.getFront(this.getBlockMetadata()).getDirectionVec())));
		AbsorberShrapnelEntity entity = new AbsorberShrapnelEntity(world,mat.getX(),mat.getY(),mat.getZ(),type);
		double forward = 0.35;
		double spread = 0.15;
		switch(type) {
			case CABLE:
				mat = mat.rotateX(90).rotateY(world.rand.nextInt(4)*90);
				spread = 0.2;
				break;
			case CORE:
				entity.motionX = world.rand.nextGaussian()*0.05;
				entity.motionY = world.rand.nextGaussian()*0.05;
				entity.motionZ = world.rand.nextGaussian()*0.05;
				world.spawnEntity(entity);
				return;
			case CORNER:
				mat = mat.rotateX(world.rand.nextInt(4)*90-45).rotateY(world.rand.nextInt(2)*90-45);
				break;
			case FRONT:
				spread = 0.2;
				break;
			case BEAM:
				int rand = world.rand.nextInt(12);
				if (rand < 8) mat = mat.rotateY(Math.floorDiv(rand,2)*90).rotateX(Math.floorMod(rand,2)*90-45);
				else mat = mat.rotateY((rand-8)*90-45);
				break;
		}
		Vec3d flyDirection = mat.frontVector.scale((world.rand.nextDouble()+1)/2*forward)
				.add(mat.upVector.scale(world.rand.nextGaussian()*spread)).add(mat.rightVector.scale(world.rand.nextGaussian()*spread));
		entity.setPosition(mat.getX()+mat.frontVector.x/3,mat.getY()+mat.frontVector.y/3,mat.getZ()+mat.frontVector.z/3);
		entity.motionX = flyDirection.x;
		entity.motionY = flyDirection.y;
		entity.motionZ = flyDirection.z;
		world.spawnEntity(entity);
	}

	void explode() {
		world.setBlockToAir(pos);
		for (int i = 0; i < 3; i++) spawnShrapnel(DebrisType.BEAM);
		for (int i = 0; i < 2; i++) spawnShrapnel(DebrisType.CORNER);
		spawnShrapnel(DebrisType.CABLE);
		spawnShrapnel(DebrisType.CORE);
		spawnShrapnel(DebrisType.FRONT);
		ExplosionLarge.spawnBurst(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 12, 3);
		this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.machineExplode, SoundCategory.BLOCKS, 10.0F, 1);
		world.newExplosion(null,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,2f,true,true);
	}

	@Override
	public void update() {
		core = null;
		EnumFacing facing = getFront();
		/*
		EnumFacing facing = EnumFacing.getFront(this.getBlockMetadata());
		for(int i = 1; i <= TileEntityCoreEmitter.range; i++) {
			BlockPos offs = pos.offset(facing,i);
			TileEntity te = world.getTileEntity(offs);
			if (te instanceof TileEntityCore) {
				core = (TileEntityCore)te;
				core.absorbers.add(this);
			}
			if (!world.getBlockState(offs).getMaterial().isReplaceable()) break;
		}*/
		core = getCore(TileEntityCoreEmitter.range);
		if (core != null)
			core.absorbers.add(this);
		if(!world.isRemote) {

			if (joules >= NumScale.PETA) {
				this.explode();
				return;
			}

			updateSPKConnections(world,pos);
			if(Long.MAX_VALUE-power < joules * 5000L)
				power = Long.MAX_VALUE;
			else
				power += joules * 5000L;

			this.sendPower(world, pos);

			long remaining = power/5000L;
			if (remaining > 0) {
				List<Pair<ILaserable,EnumFacing>> targets = new ArrayList<>();
				for (EnumFacing outFace : EnumFacing.values()) {
					if (outFace.getAxis().equals(facing.getAxis())) continue;
					TileEntity te = world.getTileEntity(pos.offset(outFace));
					if (te instanceof ILaserable) {
						ILaserable thing = (ILaserable)te;
						if (thing.isInputPreferable(outFace))
							targets.add(new Pair<>(thing,outFace));
					}
				}
				if (targets.size() > 0) {
					long transfer = remaining/targets.size();
					for (Pair<ILaserable,EnumFacing> target : targets)
						target.getA().addEnergy(transfer,target.getB());

					power -= transfer*targets.size()*5000L;
				}
			}

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
		if (isInputPreferable(dir)) {//dir.getOpposite().ordinal() == this.getBlockMetadata()) {
			if(Long.MAX_VALUE - joules < energy)
				joules = Long.MAX_VALUE;
			else
				joules += energy;
		} else {
			//world.destroyBlock(pos, false);
			//world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.5F, true);
			explode();
		}
	}

	@Override
	public boolean isInputPreferable(EnumFacing dir) {
		Vec3d unit = getDirection();
		double component;
		if (dir.getAxis() == Axis.X) component = unit.x;
		else if (dir.getAxis() == Axis.Y) component = unit.y;
		else component = unit.z;
		component *= dir.getOpposite().getAxisDirection().getOffset();
		return component > 0.707;//dir.getOpposite().ordinal() == this.getBlockMetadata();
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
		return "dfc_absorber";
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

	@Override
	public String getPacketIdentifier() {
		return "dfc_absorber";
	}
}
