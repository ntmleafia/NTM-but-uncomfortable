package com.leafia.contents.machines.processing.advcent;

import com.hbm.blocks.ModBlocks;
import com.leafia.dev.blockitems.LeafiaQuickModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class AdvCentTE extends TileEntity implements LeafiaQuickModel {
	@Override
	public String _resourcePath() {
		return "advcent";
	}

	@Override
	public String _assetPath() {
		return "xenoulexi/adv_centrifuge";
	}

	@Override
	public TileEntitySpecialRenderer<TileEntity> _renderer() {
		return new AdvCentRender();
	}

	@Override
	public Block _block() {
		return ModBlocks.machine_advcent;
	}

	@Override
	public double _sizeReference() {
		return 6.13;
	}

	@Override
	public double _itemYoffset() {
		return -0.15;
	}
}
