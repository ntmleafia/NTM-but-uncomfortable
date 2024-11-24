package com.leafia.passive;

import com.leafia.contents.machines.reactors.pwr.PWRDiagnosis;
import com.leafia.dev.LeafiaDebug.Tracker;
import net.minecraft.world.World;

public class LeafiaPassiveServer {
	public static void onTick(World world) {
		PWRDiagnosis.preventScan.clear();
		Tracker.postTick(world);
	}
	public static void priorTick(World world) {
		Tracker.preTick(world);
	}
}
