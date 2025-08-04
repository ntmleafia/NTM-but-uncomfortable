package com.leafia.contents.machines.reactors.pwr.blocks.components.control;

import com.hbm.hfr.render.loader.HFRWavefrontObject;
import com.hbm.lib.RefStrings;
import com.hbm.render.amlfrom1710.IModelCustom;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class PWRControlRender extends TileEntitySpecialRenderer<PWRControlTE> {

	public static final ResourceLocation controlSide = new ResourceLocation(RefStrings.MODID, "textures/blocks/pwr/pwr_control_side.png");
	public static final ResourceLocation controlTop = new ResourceLocation(RefStrings.MODID, "textures/blocks/pwr/pwr_control_top.png");
	public static final IModelCustom mesh = new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/leafia/pwr_control_final.obj"));
	@Override
	public boolean isGlobalRenderer(PWRControlTE entity) {
		return true;
	}

	@Override
	public void render(PWRControlTE entity,double x,double y,double z,float partialTicks,int destroyStage,float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);

		//GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glPushMatrix();
		bindTexture(controlSide);
		for (int i = 1; i <= entity.height; i++) {
			mesh.renderPart("FrameSide");
			GL11.glTranslated(0,-1,0);
		}
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		bindTexture(controlTop);
		for (int i = 1; i <= entity.height; i++) {
			mesh.renderPart("FrameEnd");
			if (i == 1)
				mesh.renderPart("FrameEndTop");
			if (i == entity.height)
				mesh.renderPart("FrameEndBtm");
			GL11.glTranslated(0,-1,0);
		}
		GL11.glPopMatrix();

		GL11.glTranslated(0,entity.position*entity.height,0);
		GL11.glPushMatrix();
		bindTexture(controlSide);
		for (int i = 1; i <= entity.height; i++) {
			mesh.renderPart("RodsSide");
			GL11.glTranslated(0,-1,0);
		}
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		bindTexture(controlTop);
		for (int i = 1; i <= entity.height; i++) {
			if ((MathHelper.positiveModulo(entity.position*entity.height,1) != 0) || (entity.position*entity.height-i+1 > 0))
				mesh.renderPart("RodsEnd");
			if (i == 1)
				mesh.renderPart("RodsEndTop");
			if (i == entity.height)
				mesh.renderPart("RodsEndBtm");
			GL11.glTranslated(0,-1,0);
		}
		GL11.glPopMatrix();

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glPopMatrix();
	}
}