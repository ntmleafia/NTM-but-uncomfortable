package com.leafia.transformer;

import com.leafia.dev.optimization.diagnosis.RecordablePacket;
import com.llib.math.SiPfx;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class LeafiaOverlayDebug {
	public static void injectDebugInfoLeft(List<String> list) {
		int index = -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals("")) {
				index = i;
				break;
			}
		}
		if (index >= 0) {
			list.add(index,"NTM packet network: "+SiPfx.format("%01.2f",RecordablePacket.previousByteUsageSec,true).toLowerCase()+"bytes/sec");
			list.add(index+1,"("+SiPfx.format("%01.2f",RecordablePacket.previousByteUsageMin,true).toLowerCase()+"bytes/min, "+SiPfx.format("%01.2f",RecordablePacket.previousByteUsage,true).toLowerCase()+"bytes/tick)");
		}
	}
	public static void injectWackySplashes(List<String> splash) {
		splash.add("Floppenheimer!");
		splash.add("i should dip my balls in sulfuric acid");
		splash.add("All answers are popbob!");
		splash.add("None may enter The Orb!");
		splash.add("Wacarb was here");
		splash.add("SpongeBoy me Bob I am overdosing on keramine agagagagaga");
		splash.add(TextFormatting.RED+"I know where you live, "+System.getProperty("user.name")+".");
		splash.add("Nice toes, now hand them over.");
		splash.add("I smell burnt toast!");
		splash.add("There are bugs under your skin!");
		splash.add("Fentanyl!");
		splash.add("Do drugs!");
		splash.add("Imagine being scared by splash texts!");
		splash.add("Redditors aren't people!");
		splash.add("Can someone tell me what corrosive fumes the people on Reddit are huffing so I can avoid those more effectively?");
		splash.add("Is playing fire as a grasstype ethically wrong");
		splash.add("As one born with fur, I can confirm furry haters are basically racists.");
		splash.add("Extra information on F3 debug screen! Did you know?");
		splash.add("This mod sends string characters in 5 bits!");
	}
}
