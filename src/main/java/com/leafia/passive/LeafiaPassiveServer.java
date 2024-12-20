package com.leafia.passive;

import com.hbm.items.ModItems;
import com.leafia.contents.machines.reactors.pwr.PWRDiagnosis;
import com.leafia.dev.LeafiaDebug.Tracker;
import com.leafia.eventbuses.LeafiaServerListener;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class LeafiaPassiveServer {
	static final List<Runnable> queue = new ArrayList<>();
	public static void onTick(World world) {
		PWRDiagnosis.preventScan.clear();
		Tracker.postTick(world);
	}
	public static void priorTick(World world) {
		if (ModItems.wand_leaf.darnit != null)
			ModItems.wand_leaf.darnit.run();
		Tracker.preTick(world);
		LeafiaServerListener.damageCache.clear();
		List<Runnable> running = new ArrayList<>(queue);
		queue.clear();
		for (Runnable callback : running)
			callback.run();
	}
	public static void queueFunction(Runnable callback) {
		queue.add(callback);
	}
}
