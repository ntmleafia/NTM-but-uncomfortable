package com.hbm.tileentity.leafia;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.LeafiaQuickModel;
import com.hbm.render.tileentity.RenderAcidizer;
import com.hbm.tileentity.machine.CrystallizerCopyBase;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class TileEntityMachineAcidizer extends CrystallizerCopyBase implements LeafiaQuickModel {
	public TileEntityMachineAcidizer() {
		super();
	}

	@Override
	public String _resourcePath() {
		return "acidizer";
	}

	@Override
	public String _assetPath() {
		return "machines/acidizer";
	}

	@Override
	public TileEntitySpecialRenderer<TileEntity> _renderer() {
		return new RenderAcidizer();
	}

	@Override
	public Block _block() {
		return ModBlocks.machine_acidizer;
	}

	@Override
	public double _sizeReference() {
		return 5;
	}

	@Override
	public double _itemYoffset() {
		return 3;
	}
}