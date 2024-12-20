package com.leafia.contents.machines.reactors.pwr.blocks.components.vent.element;

import com.hbm.hfr.render.loader.HFRWavefrontObject;
import com.hbm.lib.RefStrings;
import com.hbm.render.amlfrom1710.IModelCustom;
import com.leafia.contents.machines.reactors.pwr.blocks.components.vent.MachinePWRVentBase;
import com.leafia.transformer.LeafiaGls;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderPWRVentElement extends TileEntitySpecialRenderer<TileEntityPWRVentElement> {

	static final ResourceLocation base =
			new ResourceLocation(RefStrings.MODID, "textures/blocks/pwr/pwr_vent_side_base.png");
	static final ResourceLocation blankLower =
			new ResourceLocation(RefStrings.MODID, "textures/blocks/pwr/pwr_vent_side_concrete_lower.png");
	static final ResourceLocation blankUpper =
			new ResourceLocation(RefStrings.MODID, "textures/blocks/pwr/pwr_vent_side_concrete_upper.png");
	static final ResourceLocation connectLower =
			new ResourceLocation(RefStrings.MODID, "textures/blocks/pwr/pwr_vent_side_lower.png");
	static final ResourceLocation connectUpper =
			new ResourceLocation(RefStrings.MODID, "textures/blocks/pwr/pwr_vent_side_upper.png");
	static final ResourceLocation neon =
			new ResourceLocation(RefStrings.MODID, "textures/blocks/pwr/pwr_vent_side_overlay_half.png");
	static final ResourceLocation frost =
			new ResourceLocation(RefStrings.MODID, "textures/blocks/pwr/frost.png");
	static final ResourceLocation concrete =
			new ResourceLocation(RefStrings.MODID, "textures/blocks/concrete_gray.png");
	static final IModelCustom mesh = new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/leafia/pwrventelement.obj"));
	@Override
	public boolean isGlobalRenderer(TileEntityPWRVentElement entity) {
		return true;
	}
	@Override
	public void render(TileEntityPWRVentElement entity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		LeafiaGls.pushMatrix();
		LeafiaGls._push();
		LeafiaGls.translate(x+0.5,y+0.5,z+0.5);
		IBlockState state = entity.getWorld().getBlockState(entity.getPos());
		if (state.getBlock() instanceof MachinePWRVentBase) {
			EnumFacing face = state.getValue(MachinePWRVentBase.FACING);
			if (face.getFrontOffsetY() != 0) {
				if (face.equals(EnumFacing.DOWN)) LeafiaGls.rotate(-180,1,0,0);
			} else {
				LeafiaGls.rotate(180-face.getHorizontalAngle(),0,1,0);
				LeafiaGls.rotate(-90,1,0,0);
			}
		}
		LeafiaGls.enableLighting();
		LeafiaGls.disableCull();
		bindTexture(concrete);
		if (!entity.topConnected)
			mesh.renderPart("Top");
		if (!entity.btmConnected)
			mesh.renderPart("Bottom");
		bindTexture(base);
		mesh.renderPart("Side");
		bindTexture(entity.topConnected ? connectUpper : blankUpper);
		mesh.renderPart("Side");
		bindTexture(entity.btmConnected ? connectLower : blankLower);
		mesh.renderPart("Side");

		bindTexture(neon);
		LeafiaGls.enableAlpha();
		LeafiaGls.enableBlend();
		LeafiaGls.disableDepth();
		LeafiaGls.blendFunc(SourceFactor.SRC_ALPHA,DestFactor.ONE);
		LeafiaGls.color(1,1,1,0);
		mesh.renderPart("SideOverlay");

		bindTexture(frost);
		LeafiaGls.blendFunc(SourceFactor.SRC_ALPHA,DestFactor.ONE_MINUS_SRC_ALPHA);
		LeafiaGls.color(1,1,1,0);
		mesh.renderPart("SideFrost");

		LeafiaGls.enableCull();
		LeafiaGls.shadeModel(GL11.GL_FLAT);
		LeafiaGls._pop();
		LeafiaGls.popMatrix();
	}
}