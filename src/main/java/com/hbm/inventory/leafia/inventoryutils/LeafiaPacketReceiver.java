package com.hbm.inventory.leafia.inventoryutils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface LeafiaPacketReceiver {
    public String getPacketIdentifier();
    @SideOnly(Side.CLIENT)
    public void onReceivePacketLocal(byte key,Object value);
    public void onReceivePacketServer(byte key,Object value,EntityPlayer plr);
    public default double affectionRange() { return 32; }
    public void onPlayerValidate(EntityPlayer plr); // little attempt to make this mod less internet expensive..
}
