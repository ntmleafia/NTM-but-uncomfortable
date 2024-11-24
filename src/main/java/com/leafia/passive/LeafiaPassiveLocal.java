package com.leafia.passive;

import com.leafia.dev.LeafiaDebug.Tracker.TrackerLocal;
import com.leafia.dev.optimization.diagnosis.RecordablePacket;
import com.llib.math.MathLeafia;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class LeafiaPassiveLocal {
	// "LeaviaPassiveLoval" (my typing is awesome)
	static short t0 = 0;
	static int packetRecordTimer1s = 1_000;
	static int packetRecordTimer1m = 60;
	public static void onTick(World world) {
		TrackerLocal.localTick(Minecraft.getMinecraft().player);
	}
	public static void priorTick(World world) {
		RecordablePacket.previousByteUsage = RecordablePacket.bytesUsage;
		RecordablePacket.bytesUsage = 0;
		short t1 = MathLeafia.getTime32s();
		int dT = MathLeafia.getTimeDifference32s(t0,t1);
		t0 = t1;
		packetRecordTimer1s -= dT;
		if (packetRecordTimer1s < 0) {
			packetRecordTimer1s = Math.floorMod(packetRecordTimer1s,1000);
			packetRecordTimer1m--;
			RecordablePacket.previousByteUsageSec = RecordablePacket.bytesUsageSec;
			RecordablePacket.bytesUsageSec = 0;
			if (packetRecordTimer1m <= 0) {
				packetRecordTimer1m = 60;
				RecordablePacket.previousByteUsageMin = RecordablePacket.bytesUsageMin;
				RecordablePacket.bytesUsageMin = 0;
			}
		}
	}
}
