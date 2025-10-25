package com.hbm.items.food;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.potion.HbmPotion;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.item.ItemSoup;

public class ItemFoodSoup extends ItemSoup {

	public ItemFoodSoup(int i, String s) {
		super(i);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn){
		if(this == ModItems.glowing_stew) {
            list.add("Removes 80 RAD");
    	}
		super.addInformation(stack, world, list, flagIn);
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		if(stack.getItem() == ModItems.glowing_stew){
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 2 * 20, 0));
			player.addPotionEffect(new PotionEffect(HbmPotion.radaway, 4 * 20, 0));
		}
		if(stack.getItem() == ModItems.balefire_scrambled){
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 15 * 60 * 20, 10));
			player.addPotionEffect(new PotionEffect(HbmPotion.radaway, 15 * 60 * 20, 4));

			EntityBalefire bf = new EntityBalefire(worldIn);
			bf.posX = player.posX;
			bf.posY = player.posX;
			bf.posZ = player.posZ;
			bf.destructionRange = (int) 25;
			worldIn.spawnEntity(bf);
			if(BombConfig.enableNukeClouds) {
				EntityNukeTorex.statFac(worldIn, player.posX, player.posY, player.posZ, 25);
			}
		}
		if(stack.getItem() == ModItems.balefire_and_ham){
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60 * 60 * 20, 10));
			player.addPotionEffect(new PotionEffect(HbmPotion.radaway, 60 * 60 * 20, 16));

			EntityBalefire bf = new EntityBalefire(worldIn);
			bf.posX = player.posX;
			bf.posY = player.posX;
			bf.posZ = player.posZ;
			bf.destructionRange = (int) 50;
			worldIn.spawnEntity(bf);
			if(BombConfig.enableNukeClouds) {
				EntityNukeTorex.statFac(worldIn, player.posX, player.posY, player.posZ, 50);
			}
		}
	}
}
