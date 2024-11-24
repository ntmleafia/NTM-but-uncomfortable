package com.leafia.eventbuses;

import com.leafia.transformer.LeafiaOverlayDebug;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LeafiaClientListener {
	/**
	 * Thank you forge for naming it like this
	 * <p>Yes, {@link RenderGameOverlayEvent.Text} is the event solely for debug screen, despite the radically confusing name just "Text".
	 * <p>Good job, forge. I'll kindly prepare 9800 schrabidium missiles to serve you.
	 */
	@SubscribeEvent
	public void dammit(RenderGameOverlayEvent.Text debug) {
		LeafiaOverlayDebug.injectDebugInfoLeft(debug.getLeft());
	}
}
