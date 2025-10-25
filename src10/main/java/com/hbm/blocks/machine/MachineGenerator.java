package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class MachineGenerator extends Block {

	public MachineGenerator(Material m, String s) {
		super(m);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.machineTab);

		ModBlocks.ALL_BLOCKS.add(this);
	}
}
