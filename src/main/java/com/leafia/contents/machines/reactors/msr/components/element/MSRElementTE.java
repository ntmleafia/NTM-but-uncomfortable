package com.leafia.contents.machines.reactors.msr.components.element;

import com.leafia.contents.machines.reactors.msr.components.MSRTEBase;
import com.leafia.contents.machines.reactors.msr.components.arbitrary.MSRArbitraryBlock;
import com.leafia.contents.machines.reactors.msr.components.arbitrary.MSRArbitraryTE;
import com.leafia.contents.machines.reactors.msr.components.control.MSRControlTE;
import com.leafia.dev.LeafiaDebug.Tracker;
import com.leafia.dev.container_utility.LeafiaPacket;
import com.leafia.dev.math.FiaMatrix;
import com.llib.group.LeafiaMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.function.Function;

public class MSRElementTE extends MSRTEBase {
	public enum MSRFuels {
		MEU
		;
		Item[] items;
		String[] dicts;
		public String funcString;
		public Function<Double,Double> function;
	}
	Block getBlockArbitrary(BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof MSRArbitraryBlock) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof MSRArbitraryTE arbitrary) {
				if (arbitrary.inventory.getStackInSlot(0).getItem() instanceof ItemBlock b)
					block = b.getBlock();
			}
		}
		return block;
	}

	BlockPos toBlockPos(FiaMatrix mat) {
		return new BlockPos(mat.position);
	}

	FiaMatrix toFiaMatrix(BlockPos pos) {
		return new FiaMatrix(new Vec3d(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5));
	}

	boolean isModerator(Block block) {
		return ("_"+block.getRegistryName().getPath()+"_").matches(".*[^a-z]graphite[^a-z].*");
	}

	void reactCorners2D(FiaMatrix mat,Map<BlockPos,Block> blocks) {
		Tracker._startProfile(this,"reactCorners2D");
		for (int x = -1; x <= 2; x+=2) {
			for (int y = -1; y <= 2; y+=2) {
				BlockPos a = toBlockPos(mat.translate(x,0,0));
				BlockPos b = toBlockPos(mat.translate(0,y,0));
				BlockPos c = toBlockPos(mat.translate(x,y,0));
				Block blockA = blocks.getOrDefault(a,Blocks.AIR);
				Block blockB = blocks.getOrDefault(b,Blocks.AIR);
				boolean moderatedA = isModerator(blockA);
				boolean moderatedB = isModerator(blockB);
				Tracker._tracePosition(this,c,moderatedA,moderatedB);
			}
		}
		Tracker._endProfile(this);
	}

	void reactCorners() {
		Tracker._startProfile(this,"reactCorners");
		Map<BlockPos,Block> mop = new LeafiaMap<>();
		for (EnumFacing value : EnumFacing.values()) {
			BlockPos p = pos.offset(value);
			mop.put(p,getBlockArbitrary(p));
		}
		FiaMatrix mat = toFiaMatrix(pos);
		reactCorners2D(mat,mop);
		reactCorners2D(mat.rotateY(90),mop);
		reactCorners2D(mat.rotateX(90),mop);
		Tracker._endProfile(this);
	}

	@Override
	public String getPacketIdentifier() {
		return "MSRElement";
	}
	public MSRControlTE control = null;
	public double restriction = 0;

	@Override
	public void update() {
		super.update();
		if (!world.isRemote) {
			/*if (control != null && control.isInvalid()) {
				control = null;
				restriction = 0;
			}*/
			reactCorners();
			LeafiaPacket._start(this).__write(0,restriction).__sendToAffectedClients();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		restriction = compound.getDouble("restriction");
		if (compound.hasKey("controlX")) {
			BlockPos pos1 = new BlockPos(
					compound.getInteger("controlX"),
					compound.getInteger("controlY"),
					compound.getInteger("controlZ")
			);
			if (world.getTileEntity(pos1) instanceof MSRControlTE te)
				control = te;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setDouble("restriction",restriction);
		if (control != null) {
			compound.setInteger("controlX",control.getPos().getX());
			compound.setInteger("controlY",control.getPos().getY());
			compound.setInteger("controlZ",control.getPos().getZ());
		} else {
			compound.removeTag("controlX");
			compound.removeTag("controlY");
			compound.removeTag("controlZ");
		}
		return super.writeToNBT(compound);
	}

	@Override
	public void onReceivePacketLocal(byte key,Object value) {
		super.onReceivePacketLocal(key,value);
		if (key == 0)
			restriction = (double)value;
	}
}
