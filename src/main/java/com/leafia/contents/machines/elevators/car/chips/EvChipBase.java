package com.leafia.contents.machines.elevators.car.chips;

import com.leafia.contents.machines.elevators.car.ElevatorEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

public abstract class EvChipBase {
	final ElevatorEntity entity;

	public EvChipBase(ElevatorEntity entity) {
		this.entity = entity;
		readEntityFromNBT(entity.getEntityData());
	}
	public abstract String getType();

	public abstract void onButtonServer(String id,EntityPlayer player,EnumHand hand);
	public abstract void onUpdate();

	public void readEntityFromNBT(NBTTagCompound compound) {}
	public void writeEntityToNBT(NBTTagCompound compound) {}
}
