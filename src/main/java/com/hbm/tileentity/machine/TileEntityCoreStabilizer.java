package com.hbm.tileentity.machine;

import com.leafia.dev.container_utility.LeafiaPacket;
import com.leafia.dev.container_utility.LeafiaPacketReceiver;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemLens;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energy.IEnergyUser;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityCoreStabilizer extends TileEntityMachineBase implements ITickable, IEnergyUser, LeafiaPacketReceiver, SimpleComponent {

	public long power;
	public static final long maxPower = 10000000000000L;

	@Override
	public String getPacketIdentifier() {
		return "dfc_stabilizer";
	}
	@SideOnly(Side.CLIENT)
	public int outerColor = LensType.STANDARD.outerColor;
	@SideOnly(Side.CLIENT)
	public int innerColor = LensType.STANDARD.innerColor;
	@SideOnly(Side.CLIENT)
	@Override
	public void onReceivePacketLocal(byte key, Object value) {
		if (key == 0)
			this.beam = (int)value;
		if (key == 1)
			this.outerColor = (int)value;
		if (key == 2)
			this.innerColor = (int)value;
	}
	@Override
	public void onReceivePacketServer(byte key, Object value, EntityPlayer plr) {}

	@Override
	public void onPlayerValidate(EntityPlayer plr) {

	}

	@Override
	public String getComponentName() {
		return "dfc_communicator";
	}
	@Callback
	public Object[] analyze(Context context, Arguments args) {
		TileEntityCore core = getCore();
		if (isOn && core != null) {
			LinkedHashMap<String,Object> mop = new LinkedHashMap<>();
			mop.put("heat",core.heat);
			mop.put("restriction",core.field);
			mop.put("stress",core.heat/(float)core.field);
			mop.put("corruption",core.overload/60F);
			mop.put("fuelA",core.tanks[0].getFluidAmount());
			mop.put("fuelB",core.tanks[1].getFluidAmount());
			return new Object[] {mop};
		}
		return new Object[] {"COULDN'T CONNECT TO THE CORE"};
	}
	@Callback(doc = "setLevel(newLevel: number)->(previousLevel: number)")
	public Object[] setLevel(Context context, Arguments args) {
		Object[] prev = new Object[] {watts};
		watts = MathHelper.clamp(args.checkInteger(0),1,100);
		return prev;
	}
	@Callback(doc = "getLevel()->(level: number)")
	public Object[] getLevel(Context context, Arguments args) {
		return new Object[] {watts};
	}
	@Callback(doc = "validate()->(success: boolean) - Whether the stabilizer is working or not")
	public Object[] validate(Context context, Arguments args) {
		return new Object[] {isOn};
	}
	@Callback(doc = "durability()->(lensDurability: int, maximum: int) - Returns currently installed lens' durability, or 0 if missing.")
	public Object[] durability(Context context, Arguments args) {
		ItemStack stack = inventory.getStackInSlot(0);
		if(stack.getItem() instanceof ItemLens) {
			ItemLens lens = (ItemLens) inventory.getStackInSlot(0).getItem();
			return new Object[] {ItemLens.getLensDamage(stack),lens.maxDamage};
		}
		return new Object[] {0,0};
	}
	@Callback(doc = "getPower(); returns the current power level - long")
	public Object[] getPower(Context context, Arguments args) {
		return new Object[] {power};
	}
	@Callback(doc = "getMaxPower(); returns the maximum power level - long")
	public Object[] getMaxPower(Context context, Arguments args) {
		return new Object[] {getMaxPower()};
	}
	@Callback(doc = "getChargePercent(); returns the charge in percent - double")
	public Object[] getChargePercent(Context context, Arguments args) {
		return new Object[] {100D * getPower()/(double)getMaxPower()};
	}
	@Nullable
	TileEntityCore getCore() {
		EnumFacing dir = EnumFacing.getFront(this.getBlockMetadata());
		for(int i = 1; i <= range; i++) {
			int x = pos.getX() + dir.getFrontOffsetX() * i;
			int y = pos.getY() + dir.getFrontOffsetY() * i;
			int z = pos.getZ() + dir.getFrontOffsetZ() * i;
			BlockPos pos1 = new BlockPos(x, y, z);
			TileEntity te = world.getTileEntity(pos1);
			if(te instanceof TileEntityCore) {
				beam = i;
				return (TileEntityCore)te;
			}
			if(te instanceof TileEntityCoreStabilizer)
				continue;
			if(world.getBlockState(pos1).getBlock() != Blocks.AIR)
				break;
		}
		return null;
	}
	public enum LensType {
		STANDARD(0x0c222c,0x7F7F7F),
		BLANK(0x121212,0x646464),
		LIMITER(0x001733,0x7F7F7F),
		BOOSTER(0x4f1600,0x7F7F7F),
		OMEGA(0x64001e,0x9A9A9A);
		public int outerColor;
		public int innerColor;
		LensType(int outerColor,int innerColor) {
			this.outerColor = outerColor;
			this.innerColor = innerColor;
		}
	}
	public int watts;
	public int beam;
	public LensType lens = LensType.STANDARD;
	public boolean isOn;
	
	public static final int range = 15;
	
	public TileEntityCoreStabilizer() {
		super(1);
		
	}

	@Override
	public void update() {
		if(!world.isRemote) {

			this.updateStandardConnections(world, pos);
			
			watts = MathHelper.clamp(watts, 1, 100);
			long demand = (long) Math.pow(watts, 6);
			isOn = false;

			beam = 0;

			ItemLens lens = null;
			if(inventory.getStackInSlot(0).getItem() instanceof ItemLens){
				lens = (ItemLens) inventory.getStackInSlot(0).getItem();
				if (lens == ModItems.ams_focus_blank)
					this.lens = LensType.BLANK;
				else if (lens == ModItems.ams_lens)
					this.lens = LensType.STANDARD;
				else if (lens == ModItems.ams_focus_limiter)
					this.lens = LensType.LIMITER;
				else if (lens == ModItems.ams_focus_booster)
					this.lens = LensType.BOOSTER;
				else if (lens == ModItems.ams_focus_omega)
					this.lens = LensType.OMEGA;
			}

			if(lens != null && power >= demand * lens.drainMod) {
				isOn = true;
				TileEntityCore core = getCore();
				if (core != null) {
					//core.field += (int)(watts * lens.fieldMod);
					core.stabilization += lens.fieldMod*(watts/100d);
					core.energyMod += lens.energyMod;
					this.power -= (long)(demand * lens.drainMod);

					long dmg = ItemLens.getLensDamage(inventory.getStackInSlot(0));
					dmg += watts;

					if(dmg >= lens.maxDamage)
						inventory.setStackInSlot(0, ItemStack.EMPTY);
					else
						ItemLens.setLensDamage(inventory.getStackInSlot(0), dmg);
				}
			}
			//PacketDispatcher.wrapper.sendToAllTracking(new AuxGaugePacket(pos, beam, 0), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 250));
			LeafiaPacket._start(this)
					.__write((byte)0,this.beam)
					.__write((byte)1,this.lens.outerColor)
					.__write((byte)2,this.lens.innerColor)
					.__sendToClients(250);
		}
	}

	@Override
	public void networkUnpack(NBTTagCompound data) {
		power = data.getLong("power");
		watts = data.getInteger("watts");
		isOn = data.getBoolean("isOn");
	}
	
	@Override
	public String getName() {
		return "container.dfcStabilizer";
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}
	
	public int getWattsScaled(int i) {
		return (watts * i) / 100;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
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
		watts = compound.getInteger("watts");
		isOn = compound.getBoolean("isOn");
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("power", power);
		compound.setInteger("watts", watts);
		compound.setBoolean("isOn", isOn);
		return super.writeToNBT(compound);
	}
}
