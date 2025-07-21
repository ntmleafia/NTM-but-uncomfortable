package com.leafia.contents.machines.elevators.floors;

import com.hbm.blocks.ModBlocks.Elevators;
import com.hbm.main.MainRegistry;
import com.leafia.dev.container_utility.LeafiaPacket;
import com.leafia.dev.container_utility.LeafiaPacketReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class EvFloorTE extends TileEntity implements LeafiaPacketReceiver {
	static byte idGui = 31;
	static byte idFloor = 0;

	public int floor = 1;
	public void openGui(EntityPlayer player) {
		LeafiaPacket._start(this).__write(idFloor,floor).__write(idGui,0).__sendToClient(player);
	}
	@Override
	public String getPacketIdentifier() {
		return "EV_FLOOR";
	}
	@Override
	public void onReceivePacketLocal(byte key,Object value) {
		if (key == idFloor) floor = (int)value;
		else if (key == idGui) Minecraft.getMinecraft().player.openGui(MainRegistry.instance,Elevators.guiIdFloor,world,pos.getX(),pos.getY(),pos.getZ());
	}
	@Override
	public void onReceivePacketServer(byte key,Object value,EntityPlayer plr) {
		if (key == 0) {
			floor = (int)value;
			LeafiaPacket._start(this).__write(idFloor,floor).__sendToAffectedClients();
		}
	}
	@Override
	public void onPlayerValidate(EntityPlayer plr) {}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setByte("floor",(byte)floor);
		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.floor = compound.getByte("floor");
	}
}
