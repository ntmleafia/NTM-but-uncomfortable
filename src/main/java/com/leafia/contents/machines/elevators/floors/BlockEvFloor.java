package com.leafia.contents.machines.elevators.floors;

import com.hbm.blocks.BlockDummyable;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockEvFloor extends BlockDummyable {
	public BlockEvFloor(Material materialIn,String s) {
		super(materialIn,s);
	}
	@Override
	public int[] getDimensions(){
		return new int[]{1, 0, 0, 0, 0, 0};
	}
	@Override
	public int getOffset() {
		return 0;
	}
	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn,int meta) {
		return null;
	}
}
