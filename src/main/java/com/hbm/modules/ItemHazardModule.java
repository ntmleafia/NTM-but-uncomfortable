package com.hbm.modules;

import java.util.List;

import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.ArmorUtil;
import com.hbm.inventory.BreederRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.I18nUtil;

import com.leafia.dev.MultiRad;
import com.leafia.dev.MultiRad.RadiationType;
import com.leafia.dev.items.LeafiaDynamicHazard;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

public class ItemHazardModule {
	/**
	 * Dependency injection: It's fun for boys and girls!
	 * All this interface-pattern-wish-wash only exists for three reasons:
	 * -it lets me add item hazards with ease by using self-returning setters
	 * -it's agnositc and also works with ItemBlocks or whatever implementation I want it to work
	 * -it makes the system truly centralized and I don't have to add new cases to 5 different classes when adding a new hazard
	 */
	public MultiRad radiation = new MultiRad();
	public float digamma;
	public int fire;
	public int cryogenic;
	public int toxic;
	public boolean blinding;
	public int asbestos;
	public int coal;
	public boolean hydro;
	public float explosive;
	public float sharp;
	
	public float tempMod = 1F;

	public void setMod(float tempMod) {
		this.tempMod = tempMod;
	}

	static ItemHazardModule bufferModule = new ItemHazardModule();

	public ItemHazardModule reflect(ItemHazardModule copyFrom) {
		this.radiation.alpha = copyFrom.radiation.alpha;
		this.radiation.beta = copyFrom.radiation.beta;
		this.radiation.gamma = copyFrom.radiation.gamma;
		this.radiation.neutrons = copyFrom.radiation.neutrons;
		this.radiation.activation = copyFrom.radiation.activation;
		this.digamma = copyFrom.digamma;
		this.fire = copyFrom.fire;
		this.cryogenic = copyFrom.cryogenic;
		this.toxic = copyFrom.toxic;
		this.blinding = copyFrom.blinding;
		this.asbestos = copyFrom.asbestos;
		this.coal = copyFrom.coal;
		this.hydro = copyFrom.hydro;
		this.explosive = copyFrom.explosive;
		this.sharp = copyFrom.sharp;
		this.tempMod = copyFrom.tempMod;
		return this;
	}

	public boolean isRadioactive() {
		return this.radiation.isRadioactive();
	}

	@Deprecated
	public void addRadiation(float radiation) {
		this.radiation.neutrons = radiation;
	}
	
	public void addDigamma(float digamma) {
		this.digamma = digamma;
	}
	
	public void addFire(int fire) {
		this.fire = fire;
	}

	public void addCryogenic(int cryogenicLvl) {
		this.cryogenic = cryogenicLvl;
	}

	public void addToxic(int toxicLvl) {
		this.toxic = toxicLvl;
	}
	
	public void addCoal(int coal) {
		this.coal = coal;
	}
	
	public void addAsbestos(int asbestos) {
		this.asbestos = asbestos;
	}
	
	public void addBlinding() {
		this.blinding = true;
	}
	
	public void addHydroReactivity() {
		this.hydro = true;
	}

	public void addSharp(float sharpness) { this.sharp = sharpness; }
	
	public void addExplosive(float bang) {
		this.explosive = bang;
	}

	public void applyEffects(EntityLivingBase entity, float mod, int slot, boolean currentItem, EnumHand hand) {
			
		boolean reacher = false;
		
		if(entity instanceof EntityPlayer && !GeneralConfig.enable528)
			reacher = Library.checkForHeld((EntityPlayer) entity, ModItems.reacher);
			
		if(this.radiation.total() * tempMod > 0) {
			float total = this.radiation.total();
			float rad = total * tempMod * mod / 20F;
			
			if(reacher)
				rad = (float) Math.min(Math.sqrt(rad), rad); //to prevent radiation from going up when being <1

			float finalRad = rad;

			float health = entity.getHealth()/entity.getMaxHealth();

			this.radiation.forEach((type,value)->{
				ContaminationUtil.contaminate(
						entity,
						type.equals(RadiationType.NEUTRONS) ? HazardType.RADIATION : HazardType.ACTIVATION,
						ContaminationType.CREATIVE,
						finalRad*(value/total)*type.modFunction.apply(entity,value) //*(RadiationConfig.enableHealthMod ? type.healthMod : 1)
				);
			});
		}

		if(this.digamma * tempMod > 0)
			ContaminationUtil.contaminate(entity, HazardType.DIGAMMA, ContaminationType.DIGAMMA, this.digamma * tempMod * mod / 20F);

		

		if(this.cryogenic > 0 && !reacher){
			if(entity instanceof EntityLivingBase){
				EntityLivingBase livingCEntity = (EntityLivingBase) entity;
				boolean isProtected = entity instanceof EntityPlayer && ArmorUtil.checkForHazmat((EntityPlayer)entity);
				if(!isProtected){
					livingCEntity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 110, this.cryogenic-1));
					livingCEntity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 110, Math.min(4, this.cryogenic-1)));
					livingCEntity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 110, this.cryogenic-1));
					if(this.cryogenic > 4){
						livingCEntity.addPotionEffect(new PotionEffect(MobEffects.WITHER, 110, this.cryogenic-3));
						entity.extinguish();
					}
				}
			}
		}

		if(this.fire > 0 && !reacher && (!(entity instanceof EntityPlayer) || (entity instanceof EntityPlayer && !ArmorUtil.checkForAsbestos((EntityPlayer)entity)))){
			entity.setFire(this.fire);
		}

		if(this.toxic > 0){
			if(entity instanceof EntityLivingBase){
				EntityLivingBase livingTEntity = (EntityLivingBase) entity;
				boolean hasToxFilter = false;
				boolean hasHazmat = false;
				if(entity instanceof EntityPlayer){
					if(ArmorRegistry.hasProtection(livingTEntity, EntityEquipmentSlot.HEAD, HazardClass.NERVE_AGENT)){
						ArmorUtil.damageGasMaskFilter(livingTEntity, 1);
						hasToxFilter = true;
					}
					hasHazmat = ArmorUtil.checkForHazmat((EntityPlayer)entity);
				}

				if(!hasToxFilter && !hasHazmat){
					livingTEntity.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 110, this.toxic-1));
					
					if(this.toxic > 2)
						livingTEntity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 110, Math.min(4, this.toxic-4)));
					if(this.toxic > 4)
						livingTEntity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 110, this.toxic));
					if(this.toxic > 6){
						if(entity.world.rand.nextInt((int)(2000/this.toxic)) == 0){
							livingTEntity.addPotionEffect(new PotionEffect(MobEffects.POISON, 110, this.toxic-4));
						}
					}
				}
				if(!(hasHazmat && hasToxFilter)){
					if(this.toxic > 8)
						livingTEntity.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 110, this.toxic-8));
					if(this.toxic > 16)
						livingTEntity.addPotionEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 110, this.toxic-16));
				}
			}
		}

		if(this.asbestos > 0 && GeneralConfig.enableAsbestos) {
			ContaminationUtil.applyAsbestos(entity, (int) (this.asbestos * mod), 1, (int)(1000/(this.asbestos * mod))); 
		}

		if(this.coal > 0 && GeneralConfig.enableCoal) {
			ContaminationUtil.applyCoal(entity, (int) (this.coal * mod), 1, (int)(1000/(this.coal * mod))); 
		}

		if(this.hydro && currentItem) {

			if(!entity.world.isRemote && entity.isInWater() && entity instanceof EntityPlayer) {
				
				EntityPlayer player = (EntityPlayer) entity;
				ItemStack held = player.getHeldItem(hand);
				
				player.inventory.mainInventory.set(player.inventory.currentItem, held.getItem().getContainerItem(held));
				player.inventoryContainer.detectAndSendChanges();
				player.world.newExplosion(null, player.posX, player.posY + player.getEyeHeight() - player.getYOffset(), player.posZ, 2F, true, true);
			}
		}

		if(this.explosive > 0 && currentItem) {

			if(!entity.world.isRemote && entity.isBurning() && entity instanceof EntityPlayer) {
				
				EntityPlayer player = (EntityPlayer) entity;
				ItemStack held = player.getHeldItem(hand);
				
				player.inventory.mainInventory.set(player.inventory.currentItem, held.getItem().getContainerItem(held));
				player.inventoryContainer.detectAndSendChanges();
				player.world.newExplosion(null, player.posX, player.posY + player.getEyeHeight() - player.getYOffset(), player.posZ, this.explosive, true, true);
			}
		}

		if(this.blinding && !ArmorRegistry.hasProtection(entity, EntityEquipmentSlot.HEAD, HazardClass.LIGHT)) {
			((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 110, 0));
		}
	}

	public static float getNewValue(float radiation){
		if(radiation < 1000000){
			return radiation;
		} else if(radiation < 1000000000){
			return radiation * 0.000001F;
		} else{
			return radiation * 0.000000001F;
		}
	}

	public static String getSuffix(float radiation){
		if(radiation < 1000000){
			return "";
		} else if(radiation < 1000000000){
			return I18nUtil.resolveKey("desc.mil");
		} else{
			return I18nUtil.resolveKey("desc.bil");
		}
	}

	public static float sharpStackNerf = 0.75f;
	
	public void addInformation(ItemStack stack, List<String> list, ITooltipFlag flagIn) {
		bufferModule.reflect(this);
		ItemHazardModule module = bufferModule;

		if (stack.getItem() instanceof LeafiaDynamicHazard) {
			module = ((LeafiaDynamicHazard)stack.getItem()).getHazards(module,stack);
		}

		if(module.radiation.total() * module.tempMod > 0) {
			list.add(TextFormatting.GREEN + "[" + I18nUtil.resolveKey("trait._hazarditem.radioactive") + "]");
			module.radiation.forEach((type,rad)->{
				if (rad > 0)
					list.add(TextFormatting.GREEN+" -::" + type.color + I18nUtil.resolveKey(type.translationKey) + " " + (Library.roundFloat(getNewValue(rad), 3)+ getSuffix(rad) + " " + I18nUtil.resolveKey("desc.rads")));
			});
			if(stack.getCount() > 1) {
				float stackRad = module.radiation.total() * module.tempMod * stack.getCount();
				list.add(TextFormatting.GREEN+" -::" + TextFormatting.GOLD + I18nUtil.resolveKey("desc.stack")+" " + Library.roundFloat(getNewValue(stackRad), 3) + getSuffix(stackRad) + " " + I18nUtil.resolveKey("desc.rads"));
			}
		}
		
		if(module.fire > 0) {
			list.add(TextFormatting.GOLD + "[" + I18nUtil.resolveKey("trait._hazarditem.hot") + "]");
		}

		if(module.cryogenic > 0) {
			list.add(TextFormatting.AQUA + "[" + I18nUtil.resolveKey("trait._hazarditem.cryogenic") + "]");
		}

		if(module.toxic > 0) {
			if(module.toxic > 16)
				list.add(TextFormatting.GREEN + "[" + I18nUtil.resolveKey("trait._hazarditem.toxic.max") + "]");
			else if(module.toxic > 8)
				list.add(TextFormatting.GREEN + "[" + I18nUtil.resolveKey("trait._hazarditem.toxic.16") + "]");
			else if(module.toxic > 4)
				list.add(TextFormatting.GREEN + "[" + I18nUtil.resolveKey("trait._hazarditem.toxic.8") + "]");
			else if(module.toxic > 2)
				list.add(TextFormatting.GREEN + "[" + I18nUtil.resolveKey("trait._hazarditem.toxic.4") + "]");
			else
				list.add(TextFormatting.GREEN + "[" + I18nUtil.resolveKey("trait._hazarditem.toxic.2") + "]");
		}
		
		if(module.blinding) {
			list.add(TextFormatting.DARK_AQUA + "[" + I18nUtil.resolveKey("trait._hazarditem.blinding") + "]");
		}

		if (module.sharp > 0) {
			list.add(TextFormatting.DARK_RED + "[" + I18nUtil.resolveKey("trait._hazarditem.sharp") + "]");
			list.add(TextFormatting.DARK_RED+" -::" + TextFormatting.RED + "" + I18nUtil.resolveKey("trait._hazarditem.sharp.add",Math.round(module.sharp*100)+"%"));
			if(stack.getCount() > 1) {
				list.add(TextFormatting.DARK_RED+" -::" + TextFormatting.RED + I18nUtil.resolveKey("desc.stack") + " " + Math.round((module.sharp*stack.getCount()*(1-sharpStackNerf)+module.sharp*sharpStackNerf)*100)+"%");
			}
		}
		
		if(module.asbestos > 0 && GeneralConfig.enableAsbestos) {
			list.add(TextFormatting.WHITE + "[" + I18nUtil.resolveKey("trait._hazarditem.asbestos") + "]");
		}
		
		if(module.coal > 0 && GeneralConfig.enableCoal) {
			list.add(TextFormatting.DARK_GRAY + "[" + I18nUtil.resolveKey("trait._hazarditem.coal") + "]");
		}
		
		if(module.hydro) {
			list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait._hazarditem.hydro") + "]");
		}
		
		if(module.explosive > 0) {
			list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait._hazarditem.explosive") + "]");
		}
		
		if(module.digamma * tempMod > 0) {
			list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait._hazarditem.digamma") + "]");
			list.add(TextFormatting.RED+" -::" + TextFormatting.DARK_RED + "" + Library.roundFloat(module.digamma * module.tempMod * 1000F, 2) + " " + I18nUtil.resolveKey("desc.digammaed"));
			if(stack.getCount() > 1) {
				list.add(TextFormatting.RED+" -::" + TextFormatting.DARK_RED + I18nUtil.resolveKey("desc.stack") + " " + Library.roundFloat(module.digamma * module.tempMod * stack.getCount() * 1000F, 2) + " " + I18nUtil.resolveKey("desc.digammaed"));
			}
		}
		
		int[] breeder = BreederRecipes.getFuelValue(stack);
		
		if(breeder != null) {
			list.add(BreederRecipes.getHEATString("[" + I18nUtil.resolveKey("trait.heat", breeder[0]) + "]", breeder[0]));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.breeding", breeder[1]));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.furnace", (breeder[0] * breeder[1] * 5)));
		}
	}

	public boolean onEntityItemUpdate(EntityItem item) {
		
		if(!item.world.isRemote) {
			if(this.hydro && (item.isInWater() || item.world.isRainingAt(new BlockPos((int)item.posX, (int)item.posY, (int)item.posZ)) || item.world.getBlockState(new BlockPos((int)item.posX, (int)item.posY, (int)item.posZ)).getMaterial() == Material.WATER)) {

				item.setDead();
				item.world.newExplosion(item, item.posX, item.posY, item.posZ, 2F, true, true);
				return true;
			}
			
			if(this.explosive > 0 && item.isBurning()) {

				item.setDead();
				item.world.newExplosion(item, item.posX, item.posY, item.posZ, this.explosive, true, true);
				return true;
			}
		}
		
		return false;
	}
}
