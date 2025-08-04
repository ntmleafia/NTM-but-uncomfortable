package com.leafia.contents.machines.reactors.msr.arbitrary;

import com.hbm.tileentity.TileEntityInventoryBase;
import com.hbm.util.I18nUtil;
import com.leafia.contents.control.fuel.nuclearfuel.LeafiaRodItem;
import com.leafia.contents.machines.reactors.pwr.PWRData;
import com.leafia.dev.container_utility.LeafiaPacket;
import com.leafia.dev.container_utility.LeafiaPacketReceiver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MSRArbitraryTE extends TileEntityInventoryBase implements LeafiaPacketReceiver {
	public MSRArbitraryTE() {
		super(1);
	}
	@Override
	public String getName() {
		return I18nUtil.resolveKey("tile.msr_arbitrary.name");
	}
	@Override
	public String getPacketIdentifier() {
		return "MSRArbitrary";
	}
	public LeafiaPacket generateSyncPacket() {
		return LeafiaPacket._start(this).__write((byte)0,writeToNBT(new NBTTagCompound()));
	}
	public void syncLocals() {
		generateSyncPacket().__sendToAffectedClients();//.__setTileEntityQueryType(Chunk.EnumCreateEntityType.CHECK).__sendToAllInDimension();
	}
	@Override
	public void markDirty() {
		super.markDirty();
		if (!world.isRemote)
			syncLocals();
	}
	@SideOnly(Side.CLIENT)
	@Override
	public void onReceivePacketLocal(byte key,Object value) {
		if (key == 0) {
			if (value instanceof NBTTagCompound nbt)
				readFromNBT(nbt);
		}
	}
	@Override
	public void onReceivePacketServer(byte key,Object value,EntityPlayer plr) { }
	@Override
	public void onPlayerValidate(EntityPlayer plr) {
		generateSyncPacket().__sendToClient(plr);
	}
}
