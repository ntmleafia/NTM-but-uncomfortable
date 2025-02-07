package com.hbm.inventory;

import java.util.ArrayList;
import java.util.List;

import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.Spaghetti;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Materials.Nuggies;

import com.hbm.items.ModItems.Materials.Powders;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

//TODO: clean this shit up
//Alcater: on it
//Alcater: almost done yay
@Spaghetti("everything")
public class MachineRecipes {

	// return: Fluid, amount produced, amount required, HE produced
	public static Object[] getTurbineOutput(Fluid type) {

		if (type == ModForgeFluids.steam) {
			return new Object[] { ModForgeFluids.spentsteam, 5, 500, 50 };
		} else if (type == ModForgeFluids.hotsteam) {
			return new Object[] { ModForgeFluids.steam, 50, 5, 100 };
		} else if (type == ModForgeFluids.superhotsteam) {
			return new Object[] { ModForgeFluids.hotsteam, 50, 5, 150 };
		} else if(type == ModForgeFluids.ultrahotsteam){
			return new Object[] { ModForgeFluids.superhotsteam, 50, 5, 250 };
		}

		return null;
	}

	public static List<GasCentOutput> getGasCentOutput(Fluid fluid) {
		
		List<GasCentOutput> list = new ArrayList<GasCentOutput>();
		if(fluid == null){
			return null;
		} else if(fluid == ModForgeFluids.uf6){
			list.add(new GasCentOutput(1, new ItemStack(Nuggies.nugget_u235), 1));
			list.add(new GasCentOutput(19, new ItemStack(Nuggies.nugget_u238), 2));
			list.add(new GasCentOutput(7, new ItemStack(Nuggies.nugget_uranium_fuel), 3));
			list.add(new GasCentOutput(9, new ItemStack(ModItems.fluorite), 4));
			return list;
		} else if(fluid == ModForgeFluids.puf6){
			list.add(new GasCentOutput(3, new ItemStack(Nuggies.nugget_pu238), 1));
			list.add(new GasCentOutput(2, new ItemStack(Nuggies.nugget_pu239), 2));
			list.add(new GasCentOutput(4, new ItemStack(Nuggies.nugget_pu240), 3));
			list.add(new GasCentOutput(1, new ItemStack(ModItems.fluorite), 4));
			return list;
		} else if(fluid == ModForgeFluids.watz){
			list.add(new GasCentOutput(1, new ItemStack(Nuggies.nugget_solinium), 1));
			list.add(new GasCentOutput(1, new ItemStack(Nuggies.nugget_uranium), 2));
			list.add(new GasCentOutput(5, new ItemStack(Powders.powder_lead), 3));
			list.add(new GasCentOutput(10, new ItemStack(ModItems.dust), 4));
			return list;
		} else if(fluid == ModForgeFluids.sas3){
			list.add(new GasCentOutput(4, new ItemStack(Nuggies.nugget_schrabidium), 1));
			list.add(new GasCentOutput(4, new ItemStack(Nuggies.nugget_schrabidium), 2));
			list.add(new GasCentOutput(1, new ItemStack(ModItems.sulfur), 3));
			list.add(new GasCentOutput(1, new ItemStack(ModItems.sulfur), 4));
			return list;
		} else if(fluid == ModForgeFluids.coolant){
			list.add(new GasCentOutput(1, new ItemStack(ModItems.niter), 1));
			list.add(new GasCentOutput(1, new ItemStack(ModItems.niter), 2));
			list.add(new GasCentOutput(1, new ItemStack(ModItems.niter), 3));
			list.add(new GasCentOutput(1, new ItemStack(ModItems.niter), 4));
			return list;
		} else if(fluid == ModForgeFluids.cryogel){
			list.add(new GasCentOutput(1, new ItemStack(Powders.powder_ice), 1));
			list.add(new GasCentOutput(1, new ItemStack(Powders.powder_ice), 2));
			list.add(new GasCentOutput(1, new ItemStack(ModItems.niter), 3));
			list.add(new GasCentOutput(1, new ItemStack(ModItems.niter), 4));
			return list;
		} else if(fluid == ModForgeFluids.nitan){
			list.add(new GasCentOutput(1, new ItemStack(Powders.powder_nitan_mix), 1));
			list.add(new GasCentOutput(1, new ItemStack(Powders.powder_nitan_mix), 2));
			list.add(new GasCentOutput(1, new ItemStack(Powders.powder_nitan_mix), 3));
			list.add(new GasCentOutput(1, new ItemStack(Powders.powder_nitan_mix), 4));
			return list;
		} else if(fluid == ModForgeFluids.liquid_osmiridium){
			list.add(new GasCentOutput(1, new ItemStack(Powders.powder_impure_osmiridium), 1));
			list.add(new GasCentOutput(2, new ItemStack(Powders.powder_meteorite), 2));
			list.add(new GasCentOutput(4, new ItemStack(Powders.powder_meteorite_tiny), 3));
			list.add(new GasCentOutput(1, new ItemStack(Powders.powder_paleogenite_tiny), 4));
			return list;
		}
		
		return null;
	}
	
	public static class GasCentOutput {
		public int weight;
		public ItemStack output;
		public int slot;
		
		public GasCentOutput(int w, ItemStack s, int i) {
			weight = w;
			output = s;
			slot = i;
		}
	}


	public static int getFluidConsumedGasCent(Fluid fluid) {
		if(fluid == null)
			return 0;
		else if(fluid == FluidRegistry.LAVA)
			return 1000;
		else if(fluid == ModForgeFluids.uf6)
			return 100;
		else if(fluid == ModForgeFluids.puf6)
			return 100;
		else if(fluid == ModForgeFluids.watz)
			return 1000;
		else if(fluid == ModForgeFluids.sas3)
			return 100;
		else if(fluid == ModForgeFluids.coolant)
			return 2000;
		else if(fluid == ModForgeFluids.cryogel)
			return 1000;
		else if(fluid == ModForgeFluids.nitan)
			return 500;
		else if(fluid == ModForgeFluids.liquid_osmiridium)
			return 1000; //whose idea was 2000 heck nah
		else
			return 0;
	}
}
