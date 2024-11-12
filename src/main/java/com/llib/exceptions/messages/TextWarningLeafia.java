package com.llib.exceptions.messages;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TextWarningLeafia extends TextComponentString {
	public TextWarningLeafia(String msg) {
		super("ERROR ");
		this.appendSibling(new TextComponentString(">").setStyle(new Style().setColor(TextFormatting.RED)))
			.appendSibling(new TextComponentString(">").setStyle(new Style().setColor(TextFormatting.DARK_RED)))
			.appendSibling(new TextComponentString(" "+msg).setStyle(new Style().setColor(TextFormatting.RED)));
	}
}
