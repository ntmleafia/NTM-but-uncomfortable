package com.leafia.eventbuses;

import com.hbm.interfaces.IItemHazard;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.modules.ItemHazardModule;
import com.hbm.potion.HbmPotion;
import com.leafia.contents.machines.reactors.pwr.blocks.components.element.TileEntityPWRElement;
import com.leafia.dev.LeafiaDebug;
import com.leafia.dev.optimization.LeafiaParticlePacket;
import com.leafia.dev.optimization.LeafiaParticlePacket.Sweat;
import com.leafia.passive.LeafiaPassiveServer;
import com.llib.group.LeafiaMap;
import com.llib.group.LeafiaSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.Map.Entry;

public class LeafiaServerListener {
	@SubscribeEvent
	public void onBlockNotify(NeighborNotifyEvent evt) {
		if (!evt.getWorld().isRemote) {
			LeafiaDebug.debugPos(evt.getWorld(),evt.getPos(),3,0xFF0000,"NeighborNotifyEvent");
			for (Entry<TileEntityPWRElement,LeafiaSet<BlockPos>> entry : TileEntityPWRElement.listeners.entrySet()) {
				if (entry.getKey().isInvalid()) {
					TileEntityPWRElement.listeners.remove(entry.getKey());
					continue;
				}
				if (entry.getValue().contains(evt.getPos()))
					entry.getKey().updateObstacleMappings();
			}
		}
	}
	public static LeafiaMap<Entity,Float> damageCache = new LeafiaMap<>();
	@SubscribeEvent
	public void onEntityHurt(LivingDamageEvent evt) {
		DamageSource src = evt.getSource();
		if (!src.equals(ModDamageSource.pointed))
			damageCache.put(evt.getEntity(),evt.getAmount());
		EntityLivingBase entity = evt.getEntityLiving();
		Random rng = entity.getRNG();
		if (src.isFireDamage()) {
			if (rng.nextInt(5+5*HbmPotion.getSkinDamage(entity)/*HbmPotion.getSkinDamage(entity)*/) == 0)
				HbmPotion.hurtSkin(entity,3);
		}
		if (src.equals(DamageSource.FALL))
			sharpDamageEntity(evt.getEntity(),evt.getAmount(),getItems(evt.getEntity()));
	}
	@SubscribeEvent
	public void onEntityHit(LivingAttackEvent evt) {
		Entity attacker = evt.getSource().getImmediateSource();
		if (attacker != null) {
			List<ItemStack> stacks = new ArrayList<>();
			for (ItemStack stack : attacker.getHeldEquipment())
				stacks.add(stack);
			sharpDamageEntity(evt.getEntity(),evt.getAmount(),stacks);
		}
	}
	@SubscribeEvent
	public void onEntityKnockback(LivingKnockBackEvent evt) {
		if (evt.getStrength() > 0) {
			if (damageCache.containsKey(evt.getEntity())) {
				float damage = damageCache.get(evt.getEntity());
				sharpDamageEntity(evt.getEntity(),damage,getItems(evt.getEntity()));
			}
		}
	}
	@SubscribeEvent
	public void onItemPickup(EntityItemPickupEvent evt) {
		sharpDamageEntity(evt.getEntity(),1,Collections.singletonList(evt.getItem().getItem()));
	}
	List<ItemStack> getItems(Entity entity) {
		List<ItemStack> stacks = new ArrayList<>();
		if (entity instanceof EntityPlayer) {
			InventoryPlayer inventory = ((EntityPlayer)entity).inventory;
			for (int i = 0; i < inventory.getSizeInventory(); i++)
				stacks.add(inventory.getStackInSlot(i));
		} else {
			for (ItemStack stack : entity.getEquipmentAndArmor())
				stacks.add(stack);
		}
		return stacks;
	}
	public void sharpDamageEntity(Entity entity,float baseDamage,List<ItemStack> stacks) {
		float modifier = 0;
		float max = 0;
		for (ItemStack stack : stacks) {
			if (!stack.isEmpty() && stack.getItem() instanceof IItemHazard) {
				IItemHazard hazard = (IItemHazard)stack.getItem();
				modifier += hazard.getModule().sharp*stack.getCount();
				max = Math.max(max,hazard.getModule().sharp);
			}
		}
		float additionalDamage = baseDamage*(modifier*(1-ItemHazardModule.sharpStackNerf)+max*ItemHazardModule.sharpStackNerf);
		if (additionalDamage > 0) {
			LeafiaPassiveServer.queueFunction(()->{
				if (entity.world == null) return;
				entity.world.playSound(null,entity.getPosition(),HBMSoundHandler.blood_splat,SoundCategory.MASTER,0.25f,entity.world.rand.nextFloat()*0.2f+0.9f);
				entity.world.playSound(null,entity.getPosition(),HBMSoundHandler.pointed,SoundCategory.MASTER,0.25f,entity.world.rand.nextFloat()*0.2f+0.9f);
				LeafiaParticlePacket.Sweat particle = new Sweat(entity,Blocks.REDSTONE_BLOCK.getDefaultState(),entity.world.rand.nextInt(4)+2);
				particle.emit(new Vec3d(entity.posX,entity.posY,entity.posZ),Vec3d.ZERO,entity.dimension);
				entity.hurtResistantTime = 0;
				entity.attackEntityFrom(ModDamageSource.pointed,additionalDamage);
			});
		}
	}
}
