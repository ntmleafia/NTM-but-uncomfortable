package com.leafia.contents.machines.reactors.msr.element;

import com.hbm.blocks.BlockContainerBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MSRElementBlock extends BlockContainerBase {
	public MSRElementBlock(Material m,String s) {
		super(m,s);
	}
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	@Override
	public @Nullable TileEntity createNewTileEntity(World worldIn,int meta) {
		return null;
	}
}
