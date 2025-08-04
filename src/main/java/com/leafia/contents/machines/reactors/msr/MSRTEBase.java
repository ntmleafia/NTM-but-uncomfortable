package com.leafia.contents.machines.reactors.msr;

import com.hbm.util.Tuple.Pair;
import com.llib.group.LeafiaMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MSRTEBase extends TileEntity {
	FluidTank tank = new FluidTank(1000);
	NBTTagCompound nbtProtocol(NBTTagCompound tag) {
		if (tag == null) tag = new NBTTagCompound();
		if (!tag.hasKey("itemMixture"))
			tag.setTag("itemMixture",new NBTTagList());
		if (!tag.hasKey("heat"))
			tag.setDouble("heat",0);
		return tag;
	}
	Map<String,Double> readMixture(NBTTagCompound tag) {
		Map<String,Double> mixture = new LeafiaMap<>();
		NBTTagList list = tag.getTagList("itemMixture",10);
		for (NBTBase nbtBase : list) {
			if (nbtBase instanceof NBTTagCompound compound)
				mixture.put(compound.getString("item"),compound.getDouble("amount"));
		}
		return mixture;
	}
	NBTTagList writeMixture(Map<String,Double> mixture) {
		NBTTagList list = new NBTTagList();
		for (Entry<String,Double> entry : mixture.entrySet()) {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("item",entry.getKey());
			compound.setDouble("amount",entry.getValue());
			list.appendTag(compound);
		}
		return list;
	}
	void transferStats(FluidStack stack,double div) {
		if (tank.getFluid() == null) return;
		NBTTagCompound compound = nbtProtocol(tank.getFluid().tag);
		NBTTagCompound target = nbtProtocol(stack.tag);
		Map<String,Double> mixture0 = readMixture(compound);
		Map<String,Double> mixture1 = readMixture(target);
		for (String fluid : mixture0.keySet()) {
			double amount0 = mixture0.get(fluid);
			double amount1 = 0;
			if (mixture1.containsKey(fluid))
				amount1 = mixture1.get(fluid);
			double transfer = amount0-amount1;
			if (transfer > 0) {
				transfer /= div;
				amount0 -= transfer;
				amount1 += transfer;
				mixture0.put(fluid,amount0);
				mixture1.put(fluid,amount1);
			}
		}
		double heatTransfer = compound.getDouble("heat")-target.getDouble("heat");
		if (heatTransfer > 0) {
			heatTransfer /= div;
			compound.setDouble("heat",compound.getDouble("heat")-heatTransfer);
			target.setDouble("heat",target.getDouble("heat")+heatTransfer);
		}
	}
	public void sendFluids() {
		int demand = 0;
		List<MSRTEBase> list = new ArrayList<>();
		for (EnumFacing facing : EnumFacing.values()) {
			BlockPos target = pos.add(facing.getDirectionVec());
			if (world.getTileEntity(target) instanceof MSRTEBase te) {
				demand += te.tank.getCapacity()-te.tank.getFluidAmount();
				list.add(te);
			}
		}
		demand = Math.min(demand,tank.getFluidAmount());
		if (!list.isEmpty()) {
			demand = demand/list.size();
			for (MSRTEBase te : list) {
				transferStats(te.tank.getFluid(),list.size());
				FluidStack stack = tank.drain(demand,true);
				assert stack != null;
				te.tank.fill(new FluidStack(te.tank.getFluid() == null ? stack : te.tank.getFluid(),stack.amount),true);
			}
		}
	}
}
