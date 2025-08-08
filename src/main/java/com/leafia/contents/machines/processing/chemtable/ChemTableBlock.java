package com.leafia.contents.machines.processing.chemtable;

import com.hbm.blocks.BlockDummyable;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ChemTableBlock extends BlockDummyable {
	public ChemTableBlock(Material materialIn,String s) {
		super(materialIn,s);
	}

	@Override
	public int[] getDimensions() {
		return new int[]{1,0,0,0,0,1};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public @Nullable TileEntity createNewTileEntity(World worldIn,int meta) {
		if (meta >= 12)
			return new ChemTableTE();
		else
			return null;
	}
}
