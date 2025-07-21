package com.leafia.contents.machines.elevators.car.chips;

import com.hbm.lib.HBMSoundEvents;
import com.leafia.contents.machines.elevators.car.ElevatorEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;

public class EvChipS6 extends EvChipBase {
	public EvChipS6(ElevatorEntity entity) {
		super(entity);
	}
	@Override
	public String getType() { return "s6"; }
	@Override
	public void onButtonServer(String id,EntityPlayer player,EnumHand hand) {
		if (id.startsWith("floor") && !entity.enabledButtons.contains(id)) {
			entity.enabledButtons.add(id);
			entity.world.playSound(null,entity.posX,entity.posY,entity.posZ,HBMSoundEvents.s6beep,SoundCategory.BLOCKS,0.35f,1);
			entity.targetFloors = entity.getTargetFloorsFromEnabledButtons();
			//int floor = Integer.parseInt(id.substring(5));
		}
	}
	@Override
	public void onUpdate() {

	}
}
