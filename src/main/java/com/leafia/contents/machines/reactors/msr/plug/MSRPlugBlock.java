package com.leafia.contents.machines.reactors.msr.plug;

import com.hbm.blocks.machine.BlockMachineBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;

public class MSRPlugBlock extends BlockMachineBase {
	public MSRPlugBlock(Material materialIn,String s) {
		super(materialIn,0,s);
	}
	@Override
	protected boolean rotatable() {
		return true;
	}
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
