package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemRBMKPellet extends Item {
	
	public String fullName = "";

	public ItemRBMKPellet(String fullName, String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.fullName = fullName;
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setCreativeTab(MainRegistry.controlTab);
		
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items){
		if(tab == CreativeTabs.SEARCH || tab == this.getCreativeTab()){
			for(int i = 0; i < 10; ++i) {
				items.add(new ItemStack(this, 1, i));
			}
		}
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);

		tooltip.add(TextFormatting.ITALIC + this.fullName);
		tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "Pellet for recycling");
		
		int meta = rectify(stack.getItemDamage());
		
		switch(meta % 5) {
		case 0: tooltip.add(TextFormatting.GOLD + "Brand New"); break;
		case 1: tooltip.add(TextFormatting.YELLOW + "Barely Depleted"); break;
		case 2: tooltip.add(TextFormatting.GREEN + "Moderately Depleted"); break;
		case 3: tooltip.add(TextFormatting.DARK_GREEN + "Highly Depleted"); break;
		case 4: tooltip.add(TextFormatting.DARK_GRAY + "Fully Depleted"); break;
		}
		
		if(hasXenon(meta))
			tooltip.add(TextFormatting.DARK_PURPLE + "High Xenon Poison");
	}
	
	public static boolean hasXenon(int meta) {
		return rectify(meta) >= 5;
	}
	
	public static int rectify(int meta) {
		return Math.abs(meta) % 10;
	}
}