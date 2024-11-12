package com.llib;

import net.minecraft.util.text.TextFormatting;

public class LeafiaUtil {
	public static int colorFromTextFormat(TextFormatting formatting) {
		int index = formatting.getColorIndex();
		int offset = (index>>3)*0x55; // colors go 00AA 55FF, and 55+AA is FF so we can just do this
		return (offset<<16) + (offset<<8) + (offset)
				+ ((index>>2 &1)*0xAA<<16) // r
				+ ((index>>1 &1)*0xAA<<8) // g
				+ ((index &1)*0xAA); // b
	}
}
