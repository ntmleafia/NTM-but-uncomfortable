package com.hbm.inventory;

import java.util.*;

import static com.hbm.inventory.OreDictManager.*;


import com.hbm.blocks.ModBlocks;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class ArcWelderRecipes {
	
	public static List<ArcWelderRecipe> recipes = new ArrayList();

	public static void registerDefaults() {

		//Parts
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.motor, 2), 100, 200L,
				new OreDictStack(IRON.plate(), 2), new ComparableStack(ModItems.coil_copper), new ComparableStack(ModItems.coil_copper_torus)));
		
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.motor, 2), 100, 400L,
				new OreDictStack(STEEL.plate(), 1), new ComparableStack(ModItems.coil_copper), new ComparableStack(ModItems.coil_copper_torus)));
		
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.low_density_element, 1), 200, 5_000L,
				new OreDictStack(AL.plate(), 4), new OreDictStack(FIBER.ingot(), 4), new OreDictStack(ANY_HARDPLASTIC.ingot())));
		
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.low_density_element, 1), 200, 10_000L,
				new OreDictStack(TI.plate(), 2), new OreDictStack(FIBER.ingot(), 4), new OreDictStack(ANY_HARDPLASTIC.ingot())));

		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.heavy_duty_element, 1), 600, 50_000_000L, new FluidStack(ModForgeFluids.UU_MATTER, 2_000),
				new OreDictStack(CMB.plateWelded(), 2), new OreDictStack(STEEL.heavyComp(), 1), new OreDictStack(ZR.plateWelded())));

		//Dense Wires
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.wire_dense, 1, Mats.MAT_ALLOY.id), 100, 10_000L,
				new OreDictStack(ALLOY.wire(), 8)));
		
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.wire_dense, 1, Mats.MAT_GOLD.id), 100, 10_000L,
				new OreDictStack(GOLD.wire(), 8)));

		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.wire_dense, 1, Mats.MAT_TUNGSTEN.id), 100, 10_000L,
				new OreDictStack(W.wire(), 8)));

		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.wire_dense, 1, Mats.MAT_SCHRABIDIUM.id), 100, 10_000L,
				new OreDictStack(SA326.wire(), 8)));


        //Shells
        recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.shell, 1, Mats.MAT_TITANIUM.id), 200, 1_000L,
                new OreDictStack(TI.plate(), 4)));
        recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.shell, 1, Mats.MAT_COPPER.id), 100, 1_000L,
                new OreDictStack(CU.plate(), 4)));
        recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.shell, 1, Mats.MAT_ALUMINIUM.id), 100, 1_000L,
                new OreDictStack(AL.plate(), 4)));
        recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.shell, 1, Mats.MAT_STEEL.id), 100, 1_500L,
                new OreDictStack(STEEL.plate(), 4)));

        recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.pipe, 1, Mats.MAT_COPPER.id), 100, 1_000L,
                new OreDictStack(CU.plate(), 3)));
        recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.pipe, 1, Mats.MAT_ALUMINIUM.id), 100, 1_000L,
                new OreDictStack(AL.plate(), 3)));
        recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.pipe, 1, Mats.MAT_LEAD.id), 100, 1_000L,
                new OreDictStack(PB.plate(), 3)));
        recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.pipe, 1, Mats.MAT_STEEL.id), 100, 1_500L,
                new OreDictStack(STEEL.plate(), 3)));



        //earlygame welded parts
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_IRON.id), 100, 100L,
				new OreDictStack(IRON.plateCast(), 2)));
		//high-demand mid-game parts
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_STEEL.id), 100, 500L,
				new OreDictStack(STEEL.plateCast(), 2)));
		//literally just the combination oven
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_COPPER.id), 200, 1_000L,
				new OreDictStack(CU.plateCast(), 2)));
		//mid-game, single combustion engine running on LPG
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_TITANIUM.id), 600, 50_000L,
				new OreDictStack(TI.plateCast(), 2)));
		//mid-game PWR
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_ZIRCONIUM.id), 600, 10_000L,
				new OreDictStack(ZR.plateCast(), 2)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_ALUMINIUM.id), 300, 10_000L,
				new OreDictStack(AL.plateCast(), 2)));
		//late-game fusion
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_TCALLOY.id), 1_200, 1_000_000L, new FluidStack(ModForgeFluids.OXYGEN, 1_000),
				new OreDictStack(TCALLOY.plateCast(), 2)));
		
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_CDALLOY.id), 1_200, 1_000_000L, new FluidStack(ModForgeFluids.OXYGEN, 1_000),
				new OreDictStack(CDALLOY.plateCast(), 2)));
		
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_TUNGSTEN.id), 1_200, 250_000L, new FluidStack(ModForgeFluids.OXYGEN, 1_000),
				new OreDictStack(W.plateCast(), 2)));
		
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_CMB.id), 1_200, 10_000_000L, new FluidStack(ModForgeFluids.REFORMGAS, 1_000),
				new OreDictStack(CMB.plateCast(), 2)));
		//pre-DFC
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.plate_welded, 1, Mats.MAT_OSMIRIDIUM.id), 6_000, 20_000_000L, new FluidStack(ModForgeFluids.REFORMGAS, 16_000),
				new OreDictStack(OSMIRIDIUM.plateCast(), 2)));
		
		//Missile Parts
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.thruster_small), 60, 1_000L, new OreDictStack(STEEL.plate(), 4), new OreDictStack(AL.wire(), 4), new OreDictStack(CU.plate(), 4)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.thruster_medium), 100, 2_000L, new OreDictStack(STEEL.plate(), 8), new ComparableStack(ModItems.motor, 1), new OreDictStack(GRAPHITE.ingot(), 8)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.thruster_large), 200, 5_000L, new OreDictStack(DURA.ingot(), 10), new ComparableStack(ModItems.motor, 1), new OreDictStack(OreDictManager.getReflector(), 12)));

		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.fuel_tank_small), 60, 1_000L, new FluidStack(ModForgeFluids.KEROSENE, 6_000), new OreDictStack(AL.plate(), 6), new OreDictStack(CU.plate(), 4), new ComparableStack(ModBlocks.steel_scaffold, 4)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.fuel_tank_medium), 100, 2_000L, new FluidStack(ModForgeFluids.KEROSENE, 8_000), new OreDictStack(AL.plateCast(), 4), new OreDictStack(TI.plate(), 8), new ComparableStack(ModBlocks.steel_scaffold, 12)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.fuel_tank_large), 200, 5_000L, new FluidStack(ModForgeFluids.KEROSENE, 12_000), new OreDictStack(AL.plateWelded(), 8), new OreDictStack(BIGMT.plate(), 12), new ComparableStack(ModBlocks.steel_scaffold, 16)));

		//Missiles
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_anti_ballistic), 100, 5_000L, new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 3), new ComparableStack(ModItems.missile_assembly), new ComparableStack(ModItems.thruster_small, 4)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_generic), 100, 5_000L, new ComparableStack(ModItems.warhead_generic_small), new ComparableStack(ModItems.fuel_tank_small), new ComparableStack(ModItems.thruster_small)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_incendiary), 100, 5_000L, new ComparableStack(ModItems.warhead_incendiary_small), new ComparableStack(ModItems.fuel_tank_small), new ComparableStack(ModItems.thruster_small)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_cluster), 100, 5_000L, new ComparableStack(ModItems.warhead_cluster_small), new ComparableStack(ModItems.fuel_tank_small), new ComparableStack(ModItems.thruster_small)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_buster), 100, 5_000L, new ComparableStack(ModItems.warhead_buster_small), new ComparableStack(ModItems.fuel_tank_small), new ComparableStack(ModItems.thruster_small)));
//		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_decoy), 60, 2_500L, new OreDictStack(STEEL.ingot()), new ComparableStack(ModItems.fuel_tank_small), new ComparableStack(ModItems.thruster_small)));

		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_strong), 200, 10_000L, new ComparableStack(ModItems.warhead_generic_medium), new ComparableStack(ModItems.fuel_tank_medium), new ComparableStack(ModItems.thruster_medium)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_incendiary_strong), 200, 10_000L, new ComparableStack(ModItems.warhead_incendiary_medium), new ComparableStack(ModItems.fuel_tank_medium), new ComparableStack(ModItems.thruster_medium)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_cluster_strong), 200, 10_000L, new ComparableStack(ModItems.warhead_cluster_medium), new ComparableStack(ModItems.fuel_tank_medium), new ComparableStack(ModItems.thruster_medium)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_buster_strong), 200, 10_000L, new ComparableStack(ModItems.warhead_buster_medium), new ComparableStack(ModItems.fuel_tank_medium), new ComparableStack(ModItems.thruster_medium)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_emp_strong), 200, 10_000L, new ComparableStack(ModBlocks.emp_bomb, 3), new ComparableStack(ModItems.fuel_tank_medium), new ComparableStack(ModItems.thruster_medium)));

		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_burst), 300, 25_000L, new ComparableStack(ModItems.warhead_generic_large), new ComparableStack(ModItems.fuel_tank_medium, 2), new ComparableStack(ModItems.thruster_medium, 4)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_inferno), 300, 25_000L, new ComparableStack(ModItems.warhead_incendiary_large), new ComparableStack(ModItems.fuel_tank_medium, 2), new ComparableStack(ModItems.thruster_medium, 4)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_rain), 300, 25_000L, new ComparableStack(ModItems.warhead_cluster_large), new ComparableStack(ModItems.fuel_tank_medium, 2), new ComparableStack(ModItems.thruster_medium, 4)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_drill), 300, 25_000L, new ComparableStack(ModItems.warhead_buster_large), new ComparableStack(ModItems.fuel_tank_medium, 2), new ComparableStack(ModItems.thruster_medium, 4)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_endo), 300, 25_000L, new ComparableStack(ModBlocks.therm_endo), new ComparableStack(ModItems.fuel_tank_medium, 2), new ComparableStack(ModItems.thruster_medium, 4)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_exo), 300, 25_000L, new ComparableStack(ModBlocks.therm_exo), new ComparableStack(ModItems.fuel_tank_medium, 2), new ComparableStack(ModItems.thruster_medium, 4)));

		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_n2), 600, 50_000L, new ComparableStack(ModItems.warhead_n2), new ComparableStack(ModItems.fuel_tank_large), new ComparableStack(ModItems.thruster_large, 3)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_nuclear), 600, 50_000L, new ComparableStack(ModItems.warhead_nuclear), new ComparableStack(ModItems.fuel_tank_large), new ComparableStack(ModItems.thruster_large, 3)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_nuclear_cluster), 600, 50_000L, new ComparableStack(ModItems.warhead_mirv), new ComparableStack(ModItems.fuel_tank_large), new ComparableStack(ModItems.thruster_large, 3)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_volcano), 600, 50_000L, new ComparableStack(ModItems.warhead_volcano), new ComparableStack(ModItems.fuel_tank_large), new ComparableStack(ModItems.thruster_large, 3)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.missile_doomsday), 600, 50_000L, new ComparableStack(ModItems.warhead_cluster_large, 8), new ComparableStack(ModItems.fuel_tank_large), new ComparableStack(ModItems.thruster_large, 3)));

		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.sat_mapper), 600, 10_000L, new ComparableStack(ModItems.sat_base), new ComparableStack(ModItems.sat_head_mapper)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.sat_scanner), 600, 10_000L, new ComparableStack(ModItems.sat_base), new ComparableStack(ModItems.sat_head_scanner)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.sat_radar), 600, 10_000L, new ComparableStack(ModItems.sat_base), new ComparableStack(ModItems.sat_head_radar)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.sat_laser), 600, 50_000L, new ComparableStack(ModItems.sat_base), new ComparableStack(ModItems.sat_head_laser)));
		recipes.add(new ArcWelderRecipe(new ItemStack(ModItems.sat_resonator), 600, 50_000L, new ComparableStack(ModItems.sat_base), new ComparableStack(ModItems.sat_head_resonator)));
	}
	
//	public static HashMap getRecipes() {
//
//		HashMap<Object, Object> recipes = new HashMap<Object, Object>();
//		
//		for(ArcWelderRecipe recipe : ArcWelderRecipes.recipes) {
//			
//			int size = recipe.ingredients.length + (recipe.fluid != null ? 1 : 0);
//			Object[] array = new Object[size];
//			
//			for(int i = 0; i < recipe.ingredients.length; i++) {
//				array[i] = recipe.ingredients[i];
//			}
//			
//			if(recipe.fluid != null) array[size - 1] = ItemFluidIcon.make(recipe.fluid);
//			
//			recipes.put(array, recipe.output);
//		}
//		
//		return recipes;
//	}
	
	public static ArcWelderRecipe getRecipe(ItemStack... inputs) {
		
		outer:
		for(ArcWelderRecipe recipe : recipes) {

            List<AStack> recipeItemList = new ArrayList<>(Arrays.asList(recipe.ingredients));

            for (ItemStack inputStack : inputs) {

                if (!inputStack.isEmpty()) {

                    boolean hasMatch = false;

                    for (AStack recipeStack : recipeItemList) {
                        if (recipeStack.matchesRecipe(inputStack, true) && inputStack.getCount() >= recipeStack.stacksize) {
                            hasMatch = true;
                            recipeItemList.remove(recipeStack);
                            break;
                        }
                    }

                    if (!hasMatch) {
                        continue outer;
                    }
                }
            }
			
			if(recipeItemList.isEmpty()) return recipe;
		}
		
		return null;
	}

	public static HashSet<Fluid> fluids = new HashSet<>();

	public static class ArcWelderRecipe {
		
		public AStack[] ingredients;
		public FluidStack fluid;
		public ItemStack output;
		public int duration;
		public long consumption;
		
		public ArcWelderRecipe(ItemStack output, int duration, long consumption, FluidStack fluid, AStack... ingredients) {
			this.ingredients = ingredients;
			this.fluid = fluid;
			this.output = output;
			this.duration = duration;
			this.consumption = consumption;
			if(fluid != null) fluids.add(fluid.getFluid());
		}
		
		public ArcWelderRecipe(ItemStack output, int duration, long consumption, AStack... ingredients) {
			this(output, duration, consumption, null, ingredients);
		}
	}
}
