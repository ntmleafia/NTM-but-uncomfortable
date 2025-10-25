package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelArmorDigamma extends ModelArmorBase {

	private final ModelRendererObj cassette;

	public ModelArmorDigamma(int type) {
		super(type);

		head = new ModelRendererObj(ResourceManager.armor_fau, "Head");
		body = new ModelRendererObj(ResourceManager.armor_fau, "Body");
		cassette = new ModelRendererObj(ResourceManager.armor_fau, "Cassette");
		leftArm = new ModelRendererObj(ResourceManager.armor_fau, "LeftArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
		rightArm = new ModelRendererObj(ResourceManager.armor_fau, "RightArm").setRotationPoint(5.0F, 2.0F, 0.0F);
		leftLeg = new ModelRendererObj(ResourceManager.armor_fau, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
		rightLeg = new ModelRendererObj(ResourceManager.armor_fau, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
		leftFoot = new ModelRendererObj(ResourceManager.armor_fau, "LeftBoot").setRotationPoint(1.9F, 12.0F, 0.0F);
		rightFoot = new ModelRendererObj(ResourceManager.armor_fau, "RightBoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	public void renderArmor(Entity par1Entity, float par7) {

		body.copyTo(cassette);

		switch (type) {
			case 3 -> {
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.fau_helmet);
				head.render(par7 * 1.1F);
			}
			case 2 -> {
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.fau_chest);
				body.render(par7 * 1.1F);
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.fau_cassette);
				cassette.render(par7 * 1.1F);
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.fau_arm);
				leftArm.render(par7 * 1.1F);
				rightArm.render(par7 * 1.1F);
			}
			case 1 -> {
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.fau_leg);
				leftLeg.render(par7 * 1.1F);
				rightLeg.render(par7 * 1.1F);
			}
			case 0 -> {
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.fau_leg);
				leftFoot.render(par7 * 1.1F);
				rightFoot.render(par7 * 1.1F);
			}
		}
	}
}