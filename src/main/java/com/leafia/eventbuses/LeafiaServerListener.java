package com.leafia.eventbuses;

import com.leafia.contents.machines.reactors.pwr.blocks.components.element.TileEntityPWRElement;
import com.leafia.dev.LeafiaDebug;
import com.llib.group.LeafiaSet;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map.Entry;

public class LeafiaServerListener {
	@SubscribeEvent
	public void onBlockNotify(NeighborNotifyEvent evt) {
		if (!evt.getWorld().isRemote) {
			LeafiaDebug.debugPos(evt.getWorld(),evt.getPos(),3,0xFF0000,"NeighborNotifyEvent");
			for (Entry<TileEntityPWRElement,LeafiaSet<BlockPos>> entry : TileEntityPWRElement.listeners.entrySet()) {
				if (entry.getKey().isInvalid()) {
					TileEntityPWRElement.listeners.remove(entry.getKey());
					continue;
				}
				if (entry.getValue().contains(evt.getPos()))
					entry.getKey().updateObstacleMappings();
			}
		}
	}
}
