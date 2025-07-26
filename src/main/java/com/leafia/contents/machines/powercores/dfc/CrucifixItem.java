package com.leafia.contents.machines.powercores.dfc;

import com.hbm.items.special.ItemCustomLore;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class CrucifixItem extends ItemCustomLore {
	public CrucifixItem(String s) {
		super(s);
	}
	@Override
	public void addInformation(ItemStack stack,World world,List<String> list,ITooltipFlag flagIn) {
		list.add(TextFormatting.GRAY+"Throw this into a collapsing DFC to fix it");
		super.addInformation(stack,world,list,flagIn);
	}
	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		if (!entityItem.isDead)
			entityItem.setEntityInvulnerable(true);
		return super.onEntityItemUpdate(entityItem);
	}
}
