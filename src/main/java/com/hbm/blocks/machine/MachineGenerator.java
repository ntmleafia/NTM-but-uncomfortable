package com.hbm.blocks.machine;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.leafia.dev.MachineTooltip;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class MachineGenerator extends Block {

	
	
	public MachineGenerator(Material m, String s) {
		super(m);
		this.setUnlocalizedName(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.machineTab);

		ModBlocks.ALL_BLOCKS.add(this);
	}
	@Override
	public void addInformation(ItemStack stack,@Nullable World player,List<String> tooltip,ITooltipFlag advanced) {
		MachineTooltip.addShit(tooltip);
		super.addInformation(stack,player,tooltip,advanced);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ModItems.circuit_targeting_tier3;
	}
	
}
