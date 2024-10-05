package com.hbm.items.ohno;

import com.hbm.config.GeneralConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.handler.ArmorUtil;
import com.hbm.interfaces.IHasCustomModel;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemCustomLore;
import com.hbm.items.special.ItemHazard;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.saveddata.RadiationSavedData;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemLeafiaRod extends ItemHazard implements IHasCustomModel {
	public enum ItemType {
		VOID,
		BILLET
	}
	public enum Purity {
		RAW,		// no icon
		ISOTOPE,	// "I" icon
		FUEL,		// "F" icon
		SOURCE,		// "S" icon
		BREEDER,	// "B" icon
		UNSTABLE	// "X" icon
	}
	double lerp(double a, double b, double t) {
		return a+(b-a)*t;
	}
	public Item baseItem = null;
	public ItemType baseItemType = ItemType.VOID;
	public Purity purity = Purity.RAW;
	public String functionId = "null";
	public double life = 0;
	public double meltingPoint = 1538;
	public String label = "ERROR!";
	public Item newFuel = null;
	public boolean splitIntoFast = true;
	public boolean splitWithFast = false;
	public boolean splitWithAny = false;
	public static ItemStack comparePriority(ItemStack a,@Nullable ItemStack b) {
		/*
		NBTTagCompound data = a.getTagCompound();
		if (data == null) {
			return b;
		} else {
			if (data.getBoolean("melting")) {
				if (b != null) {
					NBTTagCompound data2 = b.getTagCompound();
					if (data2 == null)
						return a;
					else {
						if (data2.getBoolean("melting")) {
							boolean validA = a.getItem() instanceof ItemLeafiaRod;
							boolean validB = b.getItem() instanceof ItemLeafiaRod;
							if (!validA && !validB)
								return null;
							else if (validA && !validB)
								return a;
							else if (!validA && validB)
								return b;
							int myPriority = ((ItemLeafiaRod) a.getItem()).meltdownPriority;
							if (((ItemLeafiaRod) b.getItem()).meltdownPriority > myPriority)
								return b;
							else if (myPriority > 0)
								return a;
							else return null;
						} else return a;
					}
				} else return a;
			} else return b;
		}*/
		boolean validA = a.getItem() instanceof ItemLeafiaRod;
		if (b == null) {
			if (validA) return a;
			else return null;
		} else {
			boolean validB = b.getItem() instanceof ItemLeafiaRod;
			if (!validA && !validB)
				return null;
			else if (validA && !validB)
				return a;
			else if (!validA && validB)
				return b;
			int myPriority = ((ItemLeafiaRod) a.getItem()).meltdownPriority;
			if (((ItemLeafiaRod) b.getItem()).meltdownPriority > myPriority)
				return b;
			else
				return a;
		}
	}
	public int meltdownPriority = 0;

	public float detonateRadius = 5;
	public boolean detonateNuclear = false;
	public boolean detonateVisualsOnly = false;
	public String detonateConfiguration = "default";
	public ItemLeafiaRod resetDetonate() {
		detonateRadius = 5;
		detonateNuclear = false;
		detonateVisualsOnly = false;
		detonateConfiguration = "default";
		return this;
	}
	public float detonate(@Nullable World world, @Nullable BlockPos pos) {
		boolean explode = (world != null);
		switch(functionId) {
			case "flashgold": case "flashlead":
				meltdownPriority = 10;
				if (explode) {
					float x = pos.getX()+0.5f;
					float y = pos.getY()+0.5f;
					float z = pos.getZ()+0.5f;
					detonateRadius *= 1.5f;
					EntityNukeTorex.statFac(world,x,y,z,detonateRadius);
					if (detonateNuclear && !detonateVisualsOnly)
						world.spawnEntity(EntityNukeExplosionMK5.statFac(world,(int)detonateRadius,x,y,z));
				}
				break;
			case "balefire":
				meltdownPriority = 20;
				if (explode) {
					float x = pos.getX()+0.5f;
					float y = pos.getY()+0.5f;
					float z = pos.getZ()+0.5f;
					detonateRadius *= 2.5f;
					EntityNukeTorex.statFacBale(world,x,y,z,detonateRadius);
					if (detonateNuclear && !detonateVisualsOnly) {
						EntityBalefire bf = new EntityBalefire(world);
						bf.posX = x;
						bf.posY = y;
						bf.posZ = z;
						bf.destructionRange = (int)detonateRadius;
						world.spawnEntity(bf);
					}
				}
				break;
			default:
				break;
		}
		if (explode && !detonateVisualsOnly) {
			RadiationSavedData.incrementRad(world, pos, 1000F, 2000F);
			if (!detonateNuclear) {
				world.createExplosion(null, pos.getX(), pos.getY(), pos.getZ(), detonateRadius * 1.5f, true);
				ExplosionNukeGeneric.waste(world, pos.getX(), pos.getY(), pos.getZ(), (int)detonateRadius*2);
			}
		}
		return detonateRadius;
	}
	public String HeatFunction(@Nullable ItemStack stack, boolean updateHeat, double x, double cool, double desiredTemp, double coolingRate) {
		NBTTagCompound data = null;
		String flux = TextFormatting.RED+"0°C"+TextFormatting.YELLOW;
		String temp = TextFormatting.GOLD+"ERROR°C"+TextFormatting.YELLOW;
		double heat = 0;
		if (stack != null) {
			data = stack.getTagCompound();
			if (data != null) {
				heat = data.getDouble("heat")-20;
				flux = TextFormatting.RED+String.format("%01.2f",data.getDouble("incoming"))+"°C"+TextFormatting.YELLOW;
				temp = TextFormatting.GOLD+"("+String.format("%01.2f",data.getDouble("heat"))+"°C-20)"+TextFormatting.YELLOW;
			}
		}
		String n = "0";
		double y = 0; // x = 20+~~
		switch(functionId) {
			case "depleteduranium":
				y = 80;
				n = "80";
				break;
			case "depletedplutonium":
				y = 90;
				n = "90";
				break;
			case "u238":
				y = Math.pow(x*6,0.69)*0.5;
				n = "("+flux+"*6)^0.69 * 0.5 "+TextFormatting.DARK_AQUA+"(POOR)";
				newFuel = fromResourceMap.get("leafia_rod_hepu239");
				break;
			case "nu":
				y = Math.pow(x*6,0.5)*4;
				n = "("+flux+"*6)^0.5 * 4 "+TextFormatting.DARK_AQUA+"(POOR)";
				newFuel = fromResourceMap.get("leafia_rod_npu");
				break;
			case "meu235":
				y = Math.pow(x*6,0.6)*3;
				n = "("+flux+"*6)^0.6 * 3 "+TextFormatting.DARK_GREEN+"(FINE)";
				newFuel = fromResourceMap.get("leafia_rod_depleteduranium");
				break;
			case "heu235":
				y = Math.pow(x*6,0.6)*4;
				n = "("+flux+"*6)^0.6 * 4 "+TextFormatting.DARK_GREEN+"(FINE)";
				newFuel = fromResourceMap.get("leafia_rod_depleteduranium");
				break;
			case "heu233":
				y = Math.pow(x*6,0.65)*3;
				n = "("+flux+"*6)^0.65 * 3 "+TextFormatting.GOLD+"(RISKY)";
				newFuel = fromResourceMap.get("leafia_rod_heu235");
				break;

			case "pu238":
				y = 400+Math.pow(x*2,0.75);
				n = "400 + ("+flux+"*2)^0.75 "+TextFormatting.DARK_GREEN+"(FINE)";
				newFuel = fromResourceMap.get("leafia_rod_lepu239");
				break;
			case "pu240":
				y = Math.pow(x*5,0.5)*4.5;
				n = "("+flux+"*5)^0.5 * 4.5 "+TextFormatting.DARK_AQUA+"(POOR)";
				newFuel = fromResourceMap.get("leafia_rod_hepu241");
				break;
			case "npu":
				y = Math.pow(x*5,0.5)*6.5;
				n = "("+flux+"*5)^0.5 * 6.5 "+TextFormatting.DARK_GREEN+"(FINE)";
				newFuel = fromResourceMap.get("leafia_rod_depletedplutonium");
				break;
			case "lepu239":
				y = Math.pow(x*4,0.6)*5;
				n = "("+flux+"*4)^0.6 * 5 "+TextFormatting.DARK_GREEN+"(FINE)";
				newFuel = fromResourceMap.get("leafia_rod_depletedplutonium");
				break;
			case "mepu239":
				y = Math.pow(x*4,0.65)*3;
				n = "("+flux+"*4)^0.65 * 3 "+TextFormatting.DARK_GREEN+"(FINE)";
				newFuel = fromResourceMap.get("leafia_rod_depletedplutonium");
				break;
			case "hepu239":
				y = Math.pow(x*4,0.75)*1.5;
				n = "("+flux+"*4)^0.75 * 1.5 "+TextFormatting.GOLD+"(RISKY)";
				newFuel = fromResourceMap.get("leafia_rod_pu240");
				break;

			case "th232":
				y = heat*0.9+Math.pow(x,0.1);
				n = temp+"*0.9 + "+flux+"^0.1 "+TextFormatting.DARK_AQUA+"(LIKE, REALLY POOR)";
				newFuel = fromResourceMap.get("leafia_rod_thmeu");
				break;
			case "po210": case "po210be":
				y = 700+Math.pow(x*2,0.69);
				n = "700 + ("+flux+"*2)^0.69 "+TextFormatting.GOLD+"(RISKY)";
				newFuel = fromResourceMap.get("leafia_rod_lead");
				break;
			case "au198":
				y = 1580+Math.pow(x,0.75)*2.5;
				n = "1580 + "+flux+"^0.75 * 2.5 "+TextFormatting.GOLD+"(RISKY)";
				newFuel = ModItems.bottle_mercury;
				break;
			case "pb209":
				y = 2300+x*0.2;
				n = "2300 + "+flux+"* 0.2 "+TextFormatting.DARK_RED+"(DANGEROUS)";
				newFuel = fromResourceMap.get("leafia_rod_bi209");
				break;
		}
		if (updateHeat) {
			if(data == null) {
				data = new NBTTagCompound();
				stack.setTagCompound(data);
				data.setDouble("heat",20);
				data.setDouble("depletion",0);
				data.setBoolean("melting",false);
				data.setInteger("generosityTimer",90);
				data.setInteger("spillage",0);
			}
			boolean meltdown = data.getBoolean("melting");
			data.setDouble("incoming",x);
			if (meltdown) {
				y = heat+20;
				cool = 0; // it's only a matter of time until your machine explodes >:)
				data.setInteger("spillage",data.getInteger("spillage")+1);
			}
			heat = data.getDouble("heat");
			double heatMg = Math.pow(Math.abs((20+y)-heat)+1,0.25)-1;
			if (heat > 20+y)
				heatMg = heatMg * -1;
			else if ((heat >= meltingPoint) && (meltingPoint != 0) && !meltdown)
				heatMg = heatMg * Math.max(lerp(1,0,(heat-meltingPoint)/(Math.pow(meltingPoint,0.75)+200)),0);
			if (!meltdown) {
				double curDepletion = data.getDouble("depletion") + Math.max(heatMg / 2, 0) + Math.pow(x / 2 + 1, 0.1) - 1; // +y is preferred but it doesnt really work with inert materials like lithium soo
				data.setDouble("depletion", curDepletion);
			}
			double newTemp = heat+heatMg;
			double cooled = (Math.pow(Math.max(newTemp-desiredTemp,0)+1,Math.pow(coolingRate,0.5)/100)-1)*cool;
			double newCooledTemp = Math.max(newTemp-cooled,20);
			data.setDouble("cooled",cooled);
			data.setDouble(
					"heat",
					newCooledTemp
			);
			if (!meltdown && (meltingPoint != 0)) {
				int timer = data.getInteger("generosityTimer");
				int initial = timer;
				if (newCooledTemp >= meltingPoint) { // oh no!
					if (newCooledTemp - heat >= 0)
						timer = timer - 1;
					else
						timer = Math.min(timer + 20, 90);
					if (timer <= 0) {
						timer = 0;
						data.setBoolean("melting", true);
					}
				} else
					timer = 90;
				if (timer != initial)
					data.setInteger("generosityTimer", timer);
			}
		}
		return n;
	}
	@Override
	public boolean showDurabilityBar(ItemStack stack) { return (getDurabilityForDisplay(stack) > 0); }
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		NBTTagCompound data = stack.getTagCompound();
		if (data != null)
			return Math.min(data.getDouble("depletion")/life,1);
		return 0;
	}
	public double getFlux(@Nullable ItemStack stack) {
		if (stack == null)
			return 0;
		if(stack.getItem() instanceof ItemLeafiaRod) {
			double compat = 1;
			if (this.splitWithAny || (((ItemLeafiaRod) stack.getItem()).splitIntoFast == this.splitWithFast))
				compat = 2;
			NBTTagCompound data = stack.getTagCompound();
			if (data != null)
				return data.getDouble("heat")*compat;
		}
		return 0;
	}
	public ItemStack getDecayProduct(ItemStack stack) {
		NBTTagCompound data = stack.getTagCompound();
		if (data == null)
			return null;
		else {
			if (data.getBoolean("melting")) {
				// TODO: add molten fuel rods
			} else {
				boolean isDepleted = (data.getDouble("depletion") >= life);
				if (isDepleted) {
					if (newFuel != null) {
						NBTTagCompound newData = data.copy();
						newData.setDouble("depletion", 0);
						ItemStack newStack = new ItemStack(newFuel, 1, 0, newData);
						newStack.setTagCompound(newData);
						return newStack;
					}
				}
			}
		}
		return null;
	}
	public boolean decay(ItemStack stack, ItemStackHandler inventory, int slot) {
		ItemStack newFuel = getDecayProduct(stack);
		if (newFuel != null) {
			inventory.setStackInSlot(slot,newFuel);
			return true;
		}
		return false;
	}
	int meltdownFlash = 0;
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		String res = this.getRegistryName().getResourcePath();
		ItemLeafiaRod item = ItemLeafiaRod.fromResourceMap.get(res);
		NBTTagCompound data = stack.getTagCompound();
		double heat = 20;
		double depletion = 0;
		boolean meltdown = false;
		if(data != null) {
			heat = data.getDouble("heat");
			depletion = data.getDouble("depletion");
			meltdown = data.getBoolean("melting");
		}
		list.add(TextFormatting.DARK_GRAY + I18nUtil.resolveKey(item.baseItem.getUnlocalizedName()+".name") + ((life != 0) ? ("  " + TextFormatting.DARK_GREEN + "["+(int)Math.max(Math.ceil((1-depletion/life)*100),0)+"%]") : ""));
		if (newFuel != null) {
			if (newFuel instanceof ItemLeafiaRod)
				list.add(TextFormatting.DARK_GRAY + "  Decays into: " + TextFormatting.GRAY + ((ItemLeafiaRod)newFuel).label);
			else
				list.add(TextFormatting.DARK_GRAY + "  Decays into: " + TextFormatting.GRAY + I18nUtil.resolveKey(newFuel.getUnlocalizedName()+".name"));
		}
		if (life != 0)
			list.add(TextFormatting.DARK_GREEN + "  Life: About "+life+"°C");
		if (splitWithAny)
			list.add(TextFormatting.AQUA + "  Prefers all neutrons");
		else if (splitWithFast)
			list.add(TextFormatting.LIGHT_PURPLE + "  Prefers fast neutrons");
		if (!splitIntoFast)
			list.add(TextFormatting.AQUA + "  Moderated");
		super.addInformation(stack,worldIn,list,flagIn);
		list.add("");
		list.add(TextFormatting.YELLOW + "Heat Function: "+item.HeatFunction(stack,false,0,0,0,0));
		list.add(TextFormatting.GOLD + "Temperature: "+String.format("%01.1f",heat)+"°C");
		if (meltingPoint != 0) {
			list.add(TextFormatting.DARK_RED + "Melting Point: "+String.format("%01.1f",meltingPoint));
			double percent = heat/meltingPoint;
			int barLength = 60;
			String bar = "";
			boolean dark = false;
			for (int i = 0; i < barLength; i++) {
				if ((i >= Math.floor(barLength*percent)) && !dark) {
					dark = true;
					bar = bar + TextFormatting.DARK_GRAY;
				}
				bar = bar + "|";
			}
			int status = 0;
			if (heat > 300)
				status = 1;
			if (percent > 0.65)
				status = 2;
			if (meltdown)
				status = 3;
			switch(status) {
				case 0:
					list.add(TextFormatting.LIGHT_PURPLE+"["+bar+TextFormatting.LIGHT_PURPLE+"]");
					list.add(TextFormatting.LIGHT_PURPLE+"  UNOPTIMAL");
					break;
				case 1:
					list.add(TextFormatting.GREEN+"["+bar+TextFormatting.GREEN+"]");
					list.add(TextFormatting.GREEN+"  OPTIMAL");
					break;
				case 2:
					list.add(TextFormatting.RED+"["+bar+TextFormatting.RED+"]");
					list.add(TextFormatting.RED+"  OVERHEAT");
					break;
				case 3:
					list.add(TextFormatting.DARK_RED+"["+bar+TextFormatting.DARK_RED+"]");
					meltdownFlash = Math.floorMod(meltdownFlash+1,20);
					list.add((meltdownFlash >= 11) ? "" : TextFormatting.DARK_RED +"  MELTDOWN");
					break;
			}
		}
	}
	public static final ModelResourceLocation rodModel = new ModelResourceLocation(
			RefStrings.MODID + ":leafia_rod", "bakeMe");
	public static final Map<String,ItemLeafiaRod> fromResourceMap = new HashMap<>();

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entity, int itemSlot, boolean isSelected){
		super.onUpdate(stack,worldIn,entity,itemSlot,isSelected);
		this.HeatFunction(stack,true,0,0,0,0);

		NBTTagCompound data = stack.getTagCompound();
		if (data != null) {
			double heat = data.getDouble("heat");
			boolean reacher = false;
			if(entity instanceof EntityPlayer && !GeneralConfig.enable528 && (heat < 900))
				reacher = Library.checkForHeld((EntityPlayer) entity, ModItems.reacher);
			if (heat >= 1500)
				entity.attackEntityFrom(DamageSource.ON_FIRE,6);
			if (heat >= 3200)
				entity.attackEntityFrom(DamageSource.ON_FIRE,9);
			if((heat >= 80) && !reacher && (!(entity instanceof EntityPlayer) || (entity instanceof EntityPlayer && !ArmorUtil.checkForAsbestos((EntityPlayer)entity)))){
				entity.setFire(2+(int)Math.floor(heat/100));
			}
		}

		ItemStack nextItem = this.getDecayProduct(stack);
		if (nextItem != null)
			entity.replaceItemInInventory(itemSlot,nextItem);
	}

	public ItemLeafiaRod(String s, double heatGenerated, double meltingPoint) {
		super("leafia_rod_" + s.replace("-","").replace(" ","").toLowerCase());
		this.label = s;
		s = s.replace("-","").replace(" ","").toLowerCase();
		this.functionId = s;
		s = "leafia_rod_" + s;
		fromResourceMap.put(s,this);

		//this.setHasSubtypes(true);
		this.setMaxStackSize(1);
		this.life = heatGenerated;
		this.meltingPoint = meltingPoint;

		this.setContainerItem(ModItems.leafRod);

		detonate(null,null);
	}
	public ItemLeafiaRod setAppearance(Item baseItem, ItemType baseItemType, Purity purity) {
		this.baseItem = baseItem;
		this.baseItemType = baseItemType;
		this.purity = purity;
		return this;
	}
	public ItemLeafiaRod setModerated() { this.splitIntoFast = false; return this; }
	public ItemLeafiaRod preferFast() { this.splitWithFast = true; return this; }
	public ItemLeafiaRod preferAny() { this.splitWithAny = true; return this; }
	
	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		return I18nUtil.resolveKey("item.leafiarod",label);
	}

	@Override
	public ModelResourceLocation getResourceLocation() {
		return rodModel;
	}

	public static class EmptyLeafiaRod extends ItemCustomLore {

		public EmptyLeafiaRod() {
			super("leafia_rod");
		}
		@Override
		@SideOnly(Side.CLIENT)
		public String getItemStackDisplayName(ItemStack stack) {
			return I18nUtil.resolveKey("item.leafiarodempty");
		}
		@Override
		public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
			super.addInformation(stack,worldIn,list,flagIn);
			list.add(TextFormatting.DARK_GRAY + I18nUtil.resolveKey("info.leafiarod.empty"));
		}
	}
}
