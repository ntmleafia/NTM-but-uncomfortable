package com.leafia.contents.network.spk_cable;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.ILaserable;
import com.hbm.lib.RefStrings;
import com.hbm.render.amlfrom1710.AdvancedModelLoader;
import com.hbm.render.amlfrom1710.IModelCustom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class SPKCableRender extends TileEntitySpecialRenderer<SPKCableTE> {
	public static final IModelCustom mdl = AdvancedModelLoader.loadModel(new ResourceLocation(RefStrings.MODID, "models/leafia/cable_spk.obj"));
	public static final ResourceLocation tex = new ResourceLocation(RefStrings.MODID, "textures/blocks/leafia/cable_spk.png");

	@Override
	public void render(SPKCableTE te,double x,double y,double z,float partialTicks,int destroyStage,float alpha) {
		if (te.getBlockType() != ModBlocks.spk_cable) return;
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5F, y + 0.5F, z + 0.5F);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		bindTexture(tex);

		boolean pX = te.getWorld().getTileEntity(te.getPos().add(1, 0, 0)) instanceof ILaserable;
		boolean nX = te.getWorld().getTileEntity(te.getPos().add(-1, 0, 0)) instanceof ILaserable;
		boolean pY = te.getWorld().getTileEntity(te.getPos().add(0, 1, 0)) instanceof ILaserable;
		boolean nY = te.getWorld().getTileEntity(te.getPos().add(0, -1, 0)) instanceof ILaserable;
		boolean pZ = te.getWorld().getTileEntity(te.getPos().add(0, 0, 1)) instanceof ILaserable;
		boolean nZ = te.getWorld().getTileEntity(te.getPos().add(0, 0, -1)) instanceof ILaserable;

		if(pX && nX && !pY && !nY && !pZ && !nZ)
			mdl.renderPart("CX");
		else if(!pX && !nX && pY && nY && !pZ && !nZ)
			mdl.renderPart("CY");
		else if(!pX && !nX && !pY && !nY && pZ && nZ)
			mdl.renderPart("CZ");
		else{
			mdl.renderPart("Core");
			if(pX) mdl.renderPart("posX");
			if(nX) mdl.renderPart("negX");
			if(pY) mdl.renderPart("posY");
			if(nY) mdl.renderPart("negY");
			if(pZ) mdl.renderPart("negZ");
			if(nZ) mdl.renderPart("posZ");
		}

		GL11.glTranslated(-x - 0.5F, -y - 0.5F, -z - 0.5F);
		GL11.glPopMatrix();
	}
}
