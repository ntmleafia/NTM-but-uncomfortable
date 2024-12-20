package com.leafia.dev;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
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
	public static boolean isSolidVisibleCube(IBlockState state) {
		return state.isFullCube() && state.getMaterial().isSolid() && !state.getMaterial().isReplaceable() && state.getRenderType().equals(EnumBlockRenderType.MODEL);
	}
}