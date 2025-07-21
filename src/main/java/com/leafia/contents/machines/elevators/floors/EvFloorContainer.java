package com.leafia.contents.machines.elevators.floors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class EvFloorContainer extends Container {
	public EvFloorContainer(EvFloorTE te) {
	}
	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer) {
		return true;
	}
}
