package com.leafia.contents.machines.elevators.car.chips;

import com.hbm.lib.HBMSoundEvents;
import com.leafia.contents.machines.elevators.car.ElevatorEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class EvChipSkylift extends EvChipBase {
	public EvChipSkylift(ElevatorEntity entity) {
		super(entity);
		if (entity.doorOpen) doorLevel = 1;
	}
	@Override
	public String getType() { return "s6"; }
	@Override
	public void onButtonServer(String id,EntityPlayer player,EnumHand hand) {
		if (id.startsWith("floor") && !entity.enabledButtons.contains(id)) {
			int floor = Integer.parseInt(id.substring(5));
			if (entity.doorOpen && floor == entity.getDataInteger(ElevatorEntity.FLOOR)) return;

			entity.enabledButtons.add(id);
			entity.world.playSound(null,entity.posX,entity.posY,entity.posZ,HBMSoundEvents.s6beep,SoundCategory.BLOCKS,0.35f,1);
			entity.targetFloors = entity.getTargetFloorsFromEnabledButtons();
		}
	}
	int doorOpenTimer = 0;
	int doorOpenTime = 20;
	float doorLevel = 0;
	@Override
	public void onUpdate() {
		Integer floor = entity.getFloorAtPos(new BlockPos(entity.posX+0.5,entity.posY+0.5,entity.posZ+0.5));
		double ratio = getSpeedRatio();
		if (Math.abs(ratio) <= 0.001 && entity.getDataInteger(ElevatorEntity.FLOOR).equals(floor)) {
			if (doorOpenTimer < doorOpenTime) {

			}
		}
	}
}
