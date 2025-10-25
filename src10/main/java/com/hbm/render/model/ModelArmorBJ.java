package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class ModelArmorBJ extends ModelArmorBase {

	private final ModelRendererObj jetpack;

	public ModelArmorBJ(int type) {
		super(type);
		head = new ModelRendererObj(ResourceManager.armor_bj, "Head");
		body = new ModelRendererObj(ResourceManager.armor_bj, "Body");
		jetpack = new ModelRendererObj(ResourceManager.armor_bj, "Jetpack");
		leftArm = new ModelRendererObj(ResourceManager.armor_bj, "LeftArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
		rightArm = new ModelRendererObj(ResourceManager.armor_bj, "RightArm").setRotationPoint(5.0F, 2.0F, 0.0F);
		leftLeg = new ModelRendererObj(ResourceManager.armor_bj, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
		rightLeg = new ModelRendererObj(ResourceManager.armor_bj, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
		leftFoot = new ModelRendererObj(ResourceManager.armor_bj, "LeftFoot").setRotationPoint(1.9F, 12.0F, 0.0F);
		rightFoot = new ModelRendererObj(ResourceManager.armor_bj, "RightFoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	public void renderArmor(Entity par1Entity, float par7) {
		body.copyTo(jetpack);
		switch (type) {
			case 0 -> {
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.bj_eyepatch);
				head.render(par7 * 1.001F);
			}
			case 1, 5 -> {
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.bj_chest);
				body.render(par7);

				if (type == 5) {
					Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.bj_jetpack);
					jetpack.render(par7);
				}

				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.bj_arm);
				leftArm.render(par7);
				rightArm.render(par7);
			}
			case 2 -> {
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.bj_leg);
				leftLeg.render(par7);
				rightLeg.render(par7);
			}
			case 3 -> {
				Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.bj_leg);
				leftFoot.render(par7);
				rightFoot.render(par7);
			}
		}
	}

}