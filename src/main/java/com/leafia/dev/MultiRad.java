package com.leafia.dev;

import com.hbm.config.RadiationConfig;
import com.hbm.potion.HbmPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class MultiRad {
	public enum RadiationType {
		ALPHA("alpha",TextFormatting.RED,(entity,base)->{
			if (base <= 0) return 0f;
			int amp = HbmPotion.getSkinDamage(entity);
			float health = entity.getHealth()/entity.getMaxHealth();
			float div = health/4*3+0.25f;
			if (amp >= 2) {
				if (entity.getRNG().nextInt(2000-Math.min((int)Math.floor(Math.pow(base/10,0.8))*100,950)) == 0)
					HbmPotion.hurtSkin(entity,3);
			}
			return accountConfig(amp>=3 ? 1.5f/div : 0);
		}),
		BETA("beta",TextFormatting.AQUA,(entity,base)->{
			if (base <= 0) return 0f;
			int amp = HbmPotion.getSkinDamage(entity);
			float health = entity.getHealth()/entity.getMaxHealth();
			if (entity.getRNG().nextInt(2000) == 0)
				HbmPotion.hurtSkin(entity,2);
			return accountConfig(amp>=2 ? (health < 0.5 ? 0.9f : 0.6f) : (amp+1)*(amp+1)*0.01f);
		}),
		GAMMA("gamma",TextFormatting.DARK_GREEN,(entity,base)->0.2f),
		NEUTRONS("neutrons",TextFormatting.YELLOW,(entity,base)->1.3f),
		ACTIVATION("activation",TextFormatting.DARK_GRAY,(entity,base)->1f), // Should be always 1
		X("x",TextFormatting.LIGHT_PURPLE,(entity,base)->0.05f);
		public final String translationKey;
		public final TextFormatting color;
		public final BiFunction<EntityLivingBase,Float,Float> modFunction;
		protected static float accountConfig(float value) { return RadiationConfig.enableHealthMod ? value : 1; }
		RadiationType(String suffix,TextFormatting color,BiFunction<EntityLivingBase,Float,Float> modFunction) {
			this.translationKey = "trait._hazarditem.radioactive."+suffix;
			this.color = color;
			this.modFunction = modFunction;
		}
	}
	public float alpha;
	public float beta;
	public float gamma;
	public float neutrons;
	public float activation;
	public float getAlpha() { return alpha; }
	public float getBeta() { return beta; }
	public float getGamma() { return gamma; }
	public float getNeutrons() { return neutrons; }
	public float getActivation() { return activation; }
	public void setAlpha(float alpha) { this.alpha = alpha; }
	public void setBeta(float beta) { this.beta = beta; }
	public void setGamma(float gamma) { this.gamma = gamma; }
	public void setNeutrons(float neutrons) { this.neutrons = neutrons; }
	public void setActivation(float activation) { this.activation = activation; }
	public void set(float value,RadiationType... types) {
		for (RadiationType type : types) {
			if (type == null) continue;
			switch(type) {
				case ALPHA: alpha = value; break;
				case BETA: beta = value; break;
				case GAMMA: gamma = value; break;
				case NEUTRONS: neutrons = value; break;
				case ACTIVATION: activation = value; break;
			}
		}
	}
	public float getT(RadiationType... types) {
		float value = 0;
		for (RadiationType type : types) {
			if (type == null) continue;
			switch(type) {
				case ALPHA: value += alpha; break;
				case BETA: value += beta; break;
				case GAMMA: value += gamma; break;
				case NEUTRONS: value += neutrons; break;
				case ACTIVATION: value += activation; break;
			}
		}
		return value;
	}
	public float getM(RadiationType... types) {
		float value = 0;
		for (RadiationType type : types) {
			if (type == null) continue;
			switch(type) {
				case ALPHA: value = Math.max(value,alpha); break;
				case BETA: value = Math.max(value,beta); break;
				case GAMMA: value = Math.max(value,gamma); break;
				case NEUTRONS: value = Math.max(value,neutrons); break;
				case ACTIVATION: value = Math.max(value,activation); break;
			}
		}
		return value;
	}
	@Nullable
	public RadiationType getMost(RadiationType... types) {
		RadiationType most = null;
		float buffer = 0;
		for (RadiationType type : types) {
			if (type == null) continue;
			float cmp = 0;
			switch(type) {
				case ALPHA: cmp = alpha; break;
				case BETA: cmp = beta; break;
				case GAMMA: cmp = gamma; break;
				case NEUTRONS: cmp = neutrons; break;
				case ACTIVATION: cmp = activation; break;
			}
			if (cmp > buffer) {
				most = type;
				buffer = cmp;
			}
		}
		return most;
	}
	public float total() {
		return alpha+beta+gamma+neutrons+activation;
	}
	public float max() {
		return getM(RadiationType.values());
	}
	public boolean isRadioactive() {
		return total() > 0;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MultiRad) {
			MultiRad other = (MultiRad)obj;
			return other.alpha == this.alpha && other.beta == this.beta && other.gamma == this.gamma && other.neutrons == this.neutrons && other.activation == this.activation;
		}
		return super.equals(obj);
	}
	public void forEach(BiConsumer<RadiationType,Float> callback) {
		callback.accept(RadiationType.ALPHA,alpha);
		callback.accept(RadiationType.BETA,beta);
		callback.accept(RadiationType.GAMMA,gamma);
		callback.accept(RadiationType.NEUTRONS,neutrons);
		callback.accept(RadiationType.ACTIVATION,activation);
	}
}
