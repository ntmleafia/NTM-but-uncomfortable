package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class ModelGlasses extends ModelArmorBase {

	public ModelGlasses(int type) {
		super(type);

		head = new ModelRendererObj(ResourceManager.armor_goggles);
		body = new ModelRendererObj(ResourceManager.armor_bj, "Body");
		leftArm = new ModelRendererObj(ResourceManager.armor_bj, "LeftArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
		rightArm = new ModelRendererObj(ResourceManager.armor_bj, "RightArm").setRotationPoint(5.0F, 2.0F, 0.0F);
		leftLeg = new ModelRendererObj(ResourceManager.armor_bj, "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
		rightLeg = new ModelRendererObj(ResourceManager.armor_bj, "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
		leftFoot = new ModelRendererObj(ResourceManager.armor_bj, "LeftFoot").setRotationPoint(1.9F, 12.0F, 0.0F);
		rightFoot = new ModelRendererObj(ResourceManager.armor_bj, "RightFoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	public void renderArmor(Entity par1Entity, float par7) {
		if(type == 0) {
			Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.goggles);
			head.render(par7*1.001F);
		}
	}
}