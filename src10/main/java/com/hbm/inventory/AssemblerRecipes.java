package com.hbm.inventory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;

import static com.hbm.inventory.OreDictManager.*;
import static com.hbm.inventory.material.Mats.*;


import com.hbm.items.tool.ItemGasCanister;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;

import com.google.gson.Gson;
import com.hbm.blocks.ModBlocks;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NbtComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemCircuit.EnumCircuitType;
import com.hbm.items.machine.ItemAssemblyTemplate;
import com.hbm.items.machine.ItemFluidTank;
import com.hbm.items.special.ItemCell;
import com.hbm.items.tool.ItemFluidCanister;
import com.hbm.main.MainRegistry;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class AssemblerRecipes {

	public static File config;
	public static File template;
	private static final Gson gson = new Gson();
	private static IForgeRegistry<Item> itemRegistry;
	private static IForgeRegistry<Block> blockRegistry;
	public static LinkedHashMap<ComparableStack, AStack[]> recipes = new LinkedHashMap<>();
	public static HashMap<ComparableStack, Integer> time = new HashMap<>();
	public static List<ComparableStack> recipeList = new ArrayList<>();
	public static HashSet<ComparableStack> hidden = new HashSet<>();
	
	//Backup client recipes
	public static LinkedHashMap<ComparableStack, AStack[]> backupRecipes;
	public static HashMap<ComparableStack, Integer> backupTime;
	public static List<ComparableStack> backupRecipeList;
	public static HashSet<ComparableStack> backupHidden = new HashSet<>();

	/**
	 * Pre-Init phase: Finds the recipe config (if exists) and checks if a
	 * template is present, if not it generates one.
	 * 
	 * @param dir
	 *            The suggested config folder
	 */
	public static void preInit(File dir) {

		if(dir == null || !dir.isDirectory())
			return;

		template = dir;

		List<File> files = Arrays.asList(dir.listFiles());
		for(File file : files) {
			if(file.getName().equals("hbmAssembler.json")) {
				config = file;
			}
		}
	}

	public static void loadRecipes() {
		registerDefaults();
		loadRecipesFromConfig();
		generateList();
	}

	public static ItemStack getOutputFromTempate(ItemStack stack) {

		if(stack != null && stack.getItem() instanceof ItemAssemblyTemplate) {

			int i = ItemAssemblyTemplate.getRecipeIndex(stack);
			if(i >= 0 && i < recipeList.size()) {
				return recipeList.get(i).toStack();
			}
		}

		return null;
	}

	public static List<AStack> getRecipeFromTempate(ItemStack stack) {

		if(stack != null && stack.getItem() instanceof ItemAssemblyTemplate) {

			int i = ItemAssemblyTemplate.getRecipeIndex(stack);

			if(i >= 0 && i < recipeList.size()) {
				ItemStack out = recipeList.get(i).toStack();

				if(out != null) {
					ComparableStack comp = new ComparableStack(out);
					AStack[] ret = recipes.get(comp);
					return Arrays.asList(ret);
				}
			}
		}

		return null;
	}

	/**
	 * Generates an ordered list of outputs, used by the template item to
	 * generate subitems
	 */
	public static void generateList() {

		List<ComparableStack> list = new ArrayList<>(recipes.keySet());
		Collections.sort(list);
		recipeList = list;
	}

	/**
	 * Registers regular recipes if there's no custom confiuration
	 */

	//FFS 7 you need to become more precise and thorough.
	private static void registerDefaults() {
		//NEWCRAFT
		makeRecipe(new ComparableStack(ModBlocks.watz_element, 3), new AStack[] {new OreDictStack(STEEL.plateCast(), 2), new OreDictStack(ZR.ingot(), 2), new OreDictStack(BIGMT.ingot(), 2), new OreDictStack(ANY_HARDPLASTIC.ingot(), 4), new OreDictStack(PB.pipe(), 2)},200);
		makeRecipe(new ComparableStack(ModBlocks.watz_cooler, 3), new AStack[] {new OreDictStack(STEEL.plateCast(), 2), new OreDictStack(CU.plateCast(), 4), new OreDictStack(RUBBER.ingot(), 2), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.watz_casing, 3), new AStack[] {new OreDictStack(ANY_RESISTANTALLOY.plateWelded()), new OreDictStack(B.ingot(), 3), new OreDictStack(STEEL.plateWelded(), 2), }, 100);

		makeRecipe(new ComparableStack(ModItems.plate_iron, 2), new AStack[] { new OreDictStack(IRON.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_gold, 2), new AStack[] { new OreDictStack(GOLD.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_titanium, 2), new AStack[] { new OreDictStack(TI.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_aluminium, 2), new AStack[] { new OreDictStack(AL.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_steel, 2), new AStack[] { new OreDictStack(STEEL.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_lead, 2), new AStack[] { new OreDictStack(PB.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_copper, 2), new AStack[] { new OreDictStack(CU.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_advanced_alloy, 2), new AStack[] { new OreDictStack(ALLOY.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_schrabidium, 2), new AStack[] { new OreDictStack(SA326.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_combine_steel, 2), new AStack[] { new OreDictStack(CMB.ingot(), 3), }, 30);
		makeRecipe(new ComparableStack(ModItems.plate_saturnite, 2), new AStack[] { new OreDictStack(BIGMT.ingot(), 3), }, 30);

		makeRecipe(new ComparableStack(ModItems.plate_mixed, 4), new AStack[] {new OreDictStack(ALLOY.plate(), 2), new OreDictStack(OreDictManager.getReflector(), 1), new OreDictStack(BIGMT.plate(), 1) },50);
		makeRecipe(new ComparableStack(ModItems.hazmat_cloth, 4), new AStack[] { new OreDictStack(PB.dust(), 4), new ComparableStack(Items.STRING, 8), }, 50);
		makeRecipe(new ComparableStack(ModItems.asbestos_cloth, 4), new AStack[] { new OreDictStack(ASBESTOS.ingot(), 2), new ComparableStack(Items.STRING, 6), new ComparableStack(Blocks.WOOL, 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.filter_coal, 1), new AStack[] { new OreDictStack(COAL.dust(), 4), new ComparableStack(Items.STRING, 6), new ComparableStack(Items.PAPER, 1), }, 50);


		makeRecipe(new ComparableStack(ModItems.centrifuge_element, 1), new AStack[] {new OreDictStack(STEEL.plate(), 4), new OreDictStack(TI.plate(), 4), new ComparableStack(ModItems.motor, 1), }, 100);
		makeRecipe(new ComparableStack(ModItems.reactor_core, 1), new AStack[] {new OreDictStack(PB.plateCast(), 4), new OreDictStack(BE.ingot(), 8), new OreDictStack(OreDictManager.getReflector(), 8), new OreDictStack(FIBER.ingot(), 4) },100);
		makeRecipe(new ComparableStack(ModItems.rtg_unit, 1), new AStack[] {new ComparableStack(ModItems.thermo_element, 2), new OreDictStack(CU.plate(), 4), new OreDictStack(PB.plate(), 2), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CAPACITOR.ordinal()), },100);

		makeRecipe(new ComparableStack(ModItems.thermo_unit_empty, 1), new AStack[] { new ComparableStack(ModItems.coil_copper_torus, 3), new OreDictStack(STEEL.ingot(), 3), new OreDictStack(TI.plate(), 6), new ComparableStack(ModItems.plate_polymer, 12), new OreDictStack(BR.ingot(), 2), }, 100);
		makeRecipe(new ComparableStack(ModItems.levitation_unit, 1), new AStack[] { new ComparableStack(ModItems.coil_copper, 4), new ComparableStack(ModItems.coil_tungsten, 2), new OreDictStack(TI.plate(), 6), new OreDictStack(SA326.nugget(), 2), }, 100);
		makeRecipe(new ComparableStack(ModItems.drill_titanium, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 2), new OreDictStack(DURA.ingot(), 2), new OreDictStack(DURA.bolt(), 4), new OreDictStack(TI.plate(), 6), },100);

		makeRecipe(new ComparableStack(ModBlocks.machine_excavator, 1), new AStack[] {
				new ComparableStack(Blocks.STONEBRICK, 8),
				new OreDictStack(STEEL.ingot(), 8),
				new OreDictStack(IRON.ingot(), 8),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ANALOG.ordinal())
		}, 300);
		makeRecipe(new ComparableStack(ModItems.drillbit_steel), new AStack[] {
				new OreDictStack(STEEL.ingot(), 12),
				new OreDictStack(W.ingot(), 4)
			}, 200);
		makeRecipe(new ComparableStack(ModItems.drillbit_steel_diamond), new AStack[] {
				new ComparableStack(ModItems.drillbit_steel),
				new OreDictStack(DIAMOND.dust(), 16)
			}, 100);
		
		makeRecipe(new ComparableStack(ModItems.drillbit_hss), new AStack[] {
				new OreDictStack(DURA.ingot(), 12),
				new OreDictStack(ANY_PLASTIC.ingot(), 12),
				new OreDictStack(TI.ingot(), 8)
			}, 200);
		makeRecipe(new ComparableStack(ModItems.drillbit_hss_diamond), new AStack[] {
				new ComparableStack(ModItems.drillbit_hss),
				new OreDictStack(DIAMOND.dust(), 24)
			}, 100);
		
		makeRecipe(new ComparableStack(ModItems.drillbit_desh), new AStack[] {
				new OreDictStack(DESH.ingot(), 16),
				new OreDictStack(RUBBER.ingot(), 12),
				new OreDictStack(NB.ingot(), 4)
			}, 200);
		makeRecipe(new ComparableStack(ModItems.drillbit_desh_diamond), new AStack[] {
				new ComparableStack(ModItems.drillbit_desh),
				new OreDictStack(DIAMOND.dust(), 32)
			}, 100);
		
		makeRecipe(new ComparableStack(ModItems.drillbit_tcalloy), new AStack[] {
				new OreDictStack(TCALLOY.ingot(), 20),
				new OreDictStack(DESH.ingot(), 12),
				new OreDictStack(RUBBER.ingot(), 8)
			}, 200);
		makeRecipe(new ComparableStack(ModItems.drillbit_tcalloy_diamond), new AStack[] {
				new ComparableStack(ModItems.drillbit_tcalloy),
				new OreDictStack(DIAMOND.dust(), 48)
			}, 100);
		
		makeRecipe(new ComparableStack(ModItems.drillbit_ferro), new AStack[] {
				new OreDictStack(FERRO.ingot(), 24),
				new OreDictStack(CDALLOY.ingot(), 12),
				new OreDictStack(ANY_BISMOID.ingot(), 4),
			}, 200);
		makeRecipe(new ComparableStack(ModItems.drillbit_ferro_diamond), new AStack[] {
				new ComparableStack(ModItems.drillbit_ferro),
				new OreDictStack(DIAMOND.dust(), 64)
			}, 100);
		makeRecipe(new ComparableStack(ModItems.drillbit_dnt), new AStack[] {
				new ComparableStack(ModItems.ingot_dineutronium, 32),
				new OreDictStack(GH336.ingot(), 24),
				new ComparableStack(ModItems.ingot_chainsteel, 8),
			}, 2000);
		//NEWCRAFT

		makeRecipe(new ComparableStack(ModItems.telepad, 1), new AStack[] { new OreDictStack(ANY_PLASTIC.ingot(), 12), new OreDictStack(SA326.plateCast(), 2), new OreDictStack(CMB.plate(), 4), new OreDictStack(STEEL.plate(), 2), new ComparableStack(ModItems.wire, 6, MAT_GOLD.id), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.QUANTUM), }, 300);
		makeRecipe(new ComparableStack(ModItems.entanglement_kit, 1), new AStack[] {new OreDictStack(STEEL.plate(), 8), new OreDictStack(DURA.ingot(), 4), new OreDictStack(CU.plate(), 24), new OreDictStack(GOLD.wireDense(), 16), new NbtComparableStack(ItemFluidTank.getFullTank(ModForgeFluids.XENON))},200);
		makeRecipe(new ComparableStack(ModItems.dysfunctional_reactor, 1), new AStack[] {new OreDictStack(STEEL.plate(), 15), new OreDictStack(PB.ingot(), 5), new ComparableStack(ModItems.rod_quad_empty, 10), new OreDictStack("dyeBrown", 3), },200);

		makeRecipe(new ComparableStack(ModItems.missile_assembly, 1), new AStack[] {new OreDictStack(AL.pipe(), 2), new OreDictStack(AL.shell(), 2), new OreDictStack(TI.plate(), 8), new OreDictStack(ANY_PLASTIC.ingot(), 8), new ComparableStack(ModItems.rocket_fuel, 8), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.BASIC.ordinal()), }, 200);
		makeRecipe(new ComparableStack(ModItems.missile_carrier, 1), new AStack[] { new NbtComparableStack(ItemFluidTank.getFullBarrel(ModForgeFluids.KEROSENE, 16)), new ComparableStack(ModItems.thruster_medium, 4), new ComparableStack(ModItems.thruster_large, 1), new ComparableStack(ModItems.hull_big_titanium, 6), new ComparableStack(ModItems.hull_big_steel, 2), new ComparableStack(ModItems.hull_small_aluminium, 12), new OreDictStack(TI.plate(), 24), new OreDictStack(ANY_RUBBER.ingot(), 64), new OreDictStack(ANY_RUBBER.ingot(), 64), new ComparableStack(ModBlocks.det_cord, 8), new ComparableStack(ModItems.circuit, 12, EnumCircuitType.ADVANCED), new ComparableStack(ModItems.circuit, 3, EnumCircuitType.CHIP_BISMOID), }, 4800);
		makeRecipe(new ComparableStack(ModItems.warhead_generic_small, 1), new AStack[] {new OreDictStack(TI.plate(), 5), new OreDictStack(STEEL.plate(), 3), new ComparableStack(ModItems.ball_dynamite, 2), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CHIP) },100);
		makeRecipe(new ComparableStack(ModItems.warhead_generic_medium, 1), new AStack[] {new OreDictStack(TI.plate(), 8), new OreDictStack(STEEL.plate(), 5), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 4), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.BASIC) },150);
		makeRecipe(new ComparableStack(ModItems.warhead_generic_large, 1), new AStack[] {new OreDictStack(TI.plate(), 15), new OreDictStack(STEEL.plate(), 8), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 8), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED) },200);
		makeRecipe(new ComparableStack(ModItems.warhead_incendiary_small, 1), new AStack[] {new ComparableStack(ModItems.warhead_generic_small, 1), new OreDictStack(P_RED.dust(), 4), },100);
		makeRecipe(new ComparableStack(ModItems.warhead_incendiary_medium, 1), new AStack[] {new ComparableStack(ModItems.warhead_generic_medium, 1), new OreDictStack(P_RED.dust(), 8), },150);
		makeRecipe(new ComparableStack(ModItems.warhead_incendiary_large, 1), new AStack[] {new ComparableStack(ModItems.warhead_generic_large, 1), new OreDictStack(P_RED.dust(), 16), },200);
		makeRecipe(new ComparableStack(ModItems.warhead_cluster_small, 1), new AStack[] {new ComparableStack(ModItems.warhead_generic_small, 1), new ComparableStack(ModItems.pellet_cluster, 4), },100);
		makeRecipe(new ComparableStack(ModItems.warhead_cluster_medium, 1), new AStack[] {new ComparableStack(ModItems.warhead_generic_medium, 1), new ComparableStack(ModItems.pellet_cluster, 8), },150);
		makeRecipe(new ComparableStack(ModItems.warhead_cluster_large, 1), new AStack[] {new ComparableStack(ModItems.warhead_generic_large, 1), new ComparableStack(ModItems.pellet_cluster, 16), },200);
		makeRecipe(new ComparableStack(ModItems.warhead_buster_small, 1), new AStack[] {new ComparableStack(ModItems.warhead_generic_small, 1), new ComparableStack(ModBlocks.det_cord, 8), },100);
		makeRecipe(new ComparableStack(ModItems.warhead_buster_medium, 1), new AStack[] {new ComparableStack(ModItems.warhead_generic_medium, 1), new ComparableStack(ModBlocks.det_cord, 4), new ComparableStack(ModBlocks.det_charge, 4), },150);
		makeRecipe(new ComparableStack(ModItems.warhead_buster_large, 1), new AStack[] {new ComparableStack(ModItems.warhead_generic_large, 1), new ComparableStack(ModBlocks.det_charge, 8), },200);
		makeRecipe(new ComparableStack(ModItems.warhead_nuclear, 1), new AStack[] {new ComparableStack(ModItems.boy_shielding, 1), new ComparableStack(ModItems.boy_target, 1), new ComparableStack(ModItems.boy_bullet, 1), new ComparableStack(ModItems.boy_propellant, 1), new OreDictStack(TI.plateCast(), 12), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER) },300);
		makeRecipe(new ComparableStack(ModItems.warhead_mirv, 1), new AStack[] {new OreDictStack(TI.plateCast(), 12), new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModItems.man_core, 1), new ComparableStack(ModItems.ball_tatb, 8), new OreDictStack(LI.ingot(), 8), new NbtComparableStack(ItemCell.getFullCell(ModForgeFluids.DEUTERIUM, 8)), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CONTROLLER_ADVANCED) },500);
		makeRecipe(new ComparableStack(ModItems.warhead_volcano, 1), new AStack[] {new OreDictStack(TI.plate(), 24), new OreDictStack(STEEL.plate(), 16), new ComparableStack(ModBlocks.det_nuke, 3), new OreDictStack(U238.block(), 24), new ComparableStack(ModItems.circuit, 5, EnumCircuitType.CAPACITOR_BOARD.ordinal()) }, 600);
		makeRecipe(new ComparableStack(ModItems.warhead_n2, 1), new AStack[] { new ComparableStack(ModItems.n2_charge, 5), new ComparableStack(ModItems.hull_big_steel, 2), new ComparableStack(ModItems.hull_small_steel, 1), new ComparableStack(ModItems.wire, 6, MAT_COPPER.id), new OreDictStack(STEEL.plate(), 20), new OreDictStack(TI.plate(), 12), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER), }, 300);
		makeRecipe(new ComparableStack(ModItems.warhead_mirvlet, 1), new AStack[] { new OreDictStack(W.ingot(), 1), new OreDictStack(STEEL.plate(), 3), new OreDictStack(PU239.ingot(), 1), new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 1) }, 100);
		makeRecipe(new ComparableStack(ModItems.warhead_thermo_endo, 1), new AStack[] { new ComparableStack(ModBlocks.therm_endo, 2), new OreDictStack(TI.plate(), 12), new OreDictStack(STEEL.plate(), 6), }, 300);
		makeRecipe(new ComparableStack(ModItems.warhead_thermo_exo, 1), new AStack[] { new ComparableStack(ModBlocks.therm_exo, 2), new OreDictStack(TI.plate(), 12), new OreDictStack(STEEL.plate(), 6), }, 300);
		makeRecipe(new ComparableStack(ModItems.thruster_nuclear, 1), new AStack[] {new OreDictStack(DURA.ingot(), 32), new OreDictStack(B.ingot(), 8), new OreDictStack(PB.plate(), 16), new ComparableStack(ModItems.pipes_steel), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER_ADVANCED.ordinal()) },600);
		makeRecipe(new ComparableStack(ModItems.sat_base, 1), new AStack[] {
				new OreDictStack(RUBBER.ingot(), 12),
				new OreDictStack(TI.shell(), 3),
				new ComparableStack(ModItems.thruster_large, 1),
                new ComparableStack(ModItems.low_density_element, 8),
				new ComparableStack(ModItems.plate_desh, 4),
				new NbtComparableStack(ItemFluidTank.getFullBarrel(ModForgeFluids.KEROSENE)),
				new ComparableStack(ModItems.photo_panel, 24),
				new ComparableStack(ModItems.circuit, 12, EnumCircuitType.BASIC.ordinal()),
				new ComparableStack(ModBlocks.machine_lithium_battery, 1)
		},500);
		makeRecipe(new ComparableStack(ModItems.sat_head_mapper, 1), new AStack[] {
				new OreDictStack(STEEL.shell(), 3),
				new ComparableStack(ModItems.plate_desh, 4),
				new ComparableStack(ModItems.circuit, 4, EnumCircuitType.ADVANCED.ordinal()),
				new ComparableStack(ModBlocks.glass_quartz, 8),
		},400);
		makeRecipe(new ComparableStack(ModItems.sat_head_scanner, 1), new AStack[] {
				new OreDictStack(STEEL.shell(), 3),
				new OreDictStack(TI.plateCast(), 8),
				new ComparableStack(ModItems.plate_desh, 4),
				new ComparableStack(ModItems.magnetron, 8),
				new ComparableStack(ModItems.circuit, 8, EnumCircuitType.ADVANCED.ordinal())
		},400);
		makeRecipe(new ComparableStack(ModItems.sat_head_radar, 1), new AStack[] {
				new OreDictStack(STEEL.shell(), 3),
				new OreDictStack(TI.plateCast(), 12),
				new ComparableStack(ModItems.magnetron, 12),
				new ComparableStack(ModItems.coil_gold, 16),
				new ComparableStack(ModItems.circuit, 4, EnumCircuitType.ADVANCED.ordinal())
		},400);
		makeRecipe(new ComparableStack(ModItems.sat_head_laser, 1), new AStack[] {
				new OreDictStack(STEEL.shell(), 6),
				new OreDictStack(CU.plateCast(), 24),
				new OreDictStack(ANY_HARDPLASTIC.ingot(), 16),
				new ComparableStack(ModItems.circuit, 8, EnumCircuitType.CONTROLLER_ADVANCED.ordinal()),
				new ComparableStack(ModItems.circuit, 16, EnumCircuitType.CAPACITOR_BOARD.ordinal()),
				new ComparableStack(ModItems.crystal_diamond, 8),
				new ComparableStack(ModBlocks.glass_quartz, 8)
		},450);
		makeRecipe(new ComparableStack(ModItems.sat_head_resonator, 1), new AStack[] {
				new OreDictStack(STEEL.plateCast(), 6),
				new OreDictStack(STAR.ingot(), 12),
				new OreDictStack(ANY_PLASTIC.ingot(), 48),
				new ComparableStack(ModItems.crystal_xen, 1),
				new ComparableStack(ModItems.circuit, 16, EnumCircuitType.ADVANCED.ordinal())
		},1000);
		makeRecipe(new ComparableStack(ModItems.sat_foeq, 1), new AStack[] {
				new OreDictStack(TI.shell(), 3),
				new ComparableStack(ModItems.plate_desh, 8),
				new NbtComparableStack(ItemGasCanister.getFullCanister(ModForgeFluids.HYDROGEN)),
				new ComparableStack(ModItems.photo_panel, 16),
				new ComparableStack(ModItems.thruster_nuclear, 1),
				new ComparableStack(ModItems.ingot_uranium_fuel, 6),
				new ComparableStack(ModItems.circuit, 24, EnumCircuitType.BASIC.ordinal()),
				new ComparableStack(ModItems.magnetron, 3),
				new ComparableStack(ModBlocks.machine_lithium_battery, 1)
		},1200);
		makeRecipe(new ComparableStack(ModItems.sat_miner, 1), new AStack[] {
				new OreDictStack(BIGMT.plate(), 24),
				new ComparableStack(ModItems.motor_desh, 2),
				new ComparableStack(ModItems.drill_titanium, 2),
				new ComparableStack(ModItems.circuit, 12, EnumCircuitType.ADVANCED.ordinal()),
				new NbtComparableStack(ItemFluidCanister.getFullCanister(ModForgeFluids.KEROSENE)),
				new ComparableStack(ModItems.thruster_small, 1),
				new ComparableStack(ModItems.photo_panel, 12),
				new ComparableStack(ModItems.centrifuge_element, 4),
				new ComparableStack(ModBlocks.machine_lithium_battery, 1)
		},600);
		makeRecipe(new ComparableStack(ModItems.chopper_head, 1), new AStack[] { new ComparableStack(ModBlocks.reinforced_glass, 2), new ComparableStack(ModBlocks.fwatz_computer, 1), new OreDictStack(CMB.ingot(), 22), new ComparableStack(ModItems.wire, 4, MAT_MAGTUNG.id), }, 300);
		makeRecipe(new ComparableStack(ModItems.chopper_gun, 1), new AStack[] { new OreDictStack(CMB.plate(), 4), new OreDictStack(CMB.ingot(), 2), new ComparableStack(ModItems.wire, 6, MAT_TUNGSTEN.id), new ComparableStack(ModItems.coil_magnetized_tungsten, 1), new ComparableStack(ModItems.motor, 1), }, 150);
		makeRecipe(new ComparableStack(ModItems.chopper_torso, 1), new AStack[] { new OreDictStack(CMB.ingot(), 26), new ComparableStack(ModBlocks.fwatz_computer, 1), new ComparableStack(ModItems.wire, 4, MAT_MAGTUNG.id), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.chopper_blades, 2), }, 350);
		makeRecipe(new ComparableStack(ModItems.chopper_tail, 1), new AStack[] { new OreDictStack(CMB.plate(), 8), new OreDictStack(CMB.ingot(), 5), new ComparableStack(ModItems.wire, 4, MAT_MAGTUNG.id), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.chopper_blades, 2), }, 200);
		makeRecipe(new ComparableStack(ModItems.chopper_wing, 1), new AStack[] { new OreDictStack(CMB.plate(), 6), new OreDictStack(CMB.ingot(), 3), new ComparableStack(ModItems.wire, 2, MAT_MAGTUNG.id), }, 150);
		makeRecipe(new ComparableStack(ModItems.chopper_blades, 1), new AStack[] { new OreDictStack(CMB.plate(), 8), new OreDictStack(STEEL.plate(), 2), new OreDictStack(CMB.ingot(), 2), }, 200);
		makeRecipe(new ComparableStack(ModItems.tritium_deuterium_cake, 1), new AStack[] { new NbtComparableStack(ItemCell.getFullCell(ModForgeFluids.DEUTERIUM, 6)), new NbtComparableStack(ItemCell.getFullCell(ModForgeFluids.TRITIUM, 2)), new ComparableStack(ModItems.lithium, 4), }, 150);
		makeRecipe(new ComparableStack(ModItems.pellet_cluster, 1), new AStack[] { new OreDictStack(STEEL.plate(), 4), new ComparableStack(Blocks.TNT, 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.pellet_buckshot, 1), new AStack[] { new OreDictStack(PB.nugget(), 6), }, 50);
		makeRecipe(new ComparableStack(ModItems.australium_iii, 1), new AStack[] { new ComparableStack(ModItems.rod_australium, 1), new OreDictStack(STEEL.ingot(), 1), new OreDictStack(STEEL.plate(), 6), new OreDictStack(CU.plate(), 2), new ComparableStack(ModItems.wire, 6, MAT_COPPER.id), }, 150);
		makeRecipe(new ComparableStack(ModItems.magnetron, 1), new AStack[] {new OreDictStack(CU.plate(), 3), new OreDictStack(W.wire(), 4), }, 40);

		makeRecipe(new ComparableStack(ModItems.redcoil_capacitor, 1), new AStack[] {new OreDictStack(GOLD.plate(), 3), new ComparableStack(ModItems.fuse, 1), new OreDictStack(ALLOY.wire(), 4), new ComparableStack(ModItems.coil_advanced_alloy, 6), new ComparableStack(Blocks.REDSTONE_BLOCK, 2), },200);
		makeRecipe(new ComparableStack(ModItems.part_lithium, 4), new AStack[] { new OreDictStack(ANY_RUBBER.ingot(), 1), new OreDictStack(LI.dust(), 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.part_beryllium, 4), new AStack[] { new OreDictStack(ANY_RUBBER.ingot(), 1), new OreDictStack(BE.dust(), 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.part_carbon, 4), new AStack[] { new OreDictStack(ANY_RUBBER.ingot(), 1), new OreDictStack(COAL.dust(), 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.part_copper, 4), new AStack[] { new OreDictStack(ANY_RUBBER.ingot(), 1), new OreDictStack(CU.dust(), 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.part_plutonium, 4), new AStack[] { new OreDictStack(ANY_RUBBER.ingot(), 1), new OreDictStack(PU.dust(), 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.thermo_element, 1), new AStack[] {new OreDictStack(STEEL.plate(), 1), new OreDictStack(MINGRADE.wire(), 2), new OreDictStack(NETHERQUARTZ.dust(), 2), }, 60);
		makeRecipe(new ComparableStack(ModItems.plate_dalekanium, 1), new AStack[] { new ComparableStack(ModBlocks.block_meteor, 1), }, 50);
		makeRecipe(new ComparableStack(ModBlocks.block_meteor, 1), new AStack[] { new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 36) }, 500);

		makeRecipe(new ComparableStack(ModBlocks.cmb_brick, 8), new AStack[] {new OreDictStack(ANY_CONCRETE.any(), 4), new OreDictStack(CMB.plate(), 4), },100);
		makeRecipe(new ComparableStack(ModBlocks.cmb_brick_reinforced, 8), new AStack[] {new ComparableStack(ModBlocks.block_magnetized_tungsten, 4), new ComparableStack(ModBlocks.brick_concrete, 4), new ComparableStack(ModBlocks.cmb_brick, 1), new OreDictStack(STEEL.plate(), 4), },200);
		makeRecipe(new ComparableStack(ModBlocks.seal_frame, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 3), new OreDictStack(AL.wire(), 4), new OreDictStack(REDSTONE.dust(), 2), new ComparableStack(ModBlocks.steel_roof, 5), },50);
		makeRecipe(new ComparableStack(ModBlocks.seal_controller, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 3), new OreDictStack(ANY_PLASTIC.ingot(), 4), new OreDictStack(MINGRADE.ingot(), 1), new OreDictStack(REDSTONE.dust(), 4), new ComparableStack(ModBlocks.steel_roof, 5), },100);
		makeRecipe(new ComparableStack(ModBlocks.vault_door, 1), new AStack[] {
				new OreDictStack(STEEL.ingot(), 32),
				new OreDictStack(W.ingot(), 32),
				new OreDictStack(PB.plate(), 16),
				new OreDictStack(ALLOY.plate(), 4),
				new OreDictStack(ANY_RUBBER.ingot(), 4),
				new OreDictStack(W.bolt(), 16),
				new OreDictStack(DURA.bolt(), 16),
				new ComparableStack(ModItems.motor, 3),
		}, 200);

		makeRecipe(new ComparableStack(ModBlocks.blast_door, 1), new AStack[] {
				new OreDictStack(STEEL.ingot(), 8),
				new OreDictStack(W.ingot(), 8),
				new OreDictStack(PB.plate(), 6),
				new OreDictStack(ALLOY.plate(), 3),
				new OreDictStack(ANY_RUBBER.ingot(), 3),
				new OreDictStack(W.bolt(), 4),
				new OreDictStack(DURA.bolt(), 4),
				new ComparableStack(ModItems.motor, 1),
		}, 300);
		makeRecipe(new ComparableStack(ModBlocks.sliding_blast_door_2, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 16), new OreDictStack(W.ingot(), 8), new ComparableStack(ModItems.circuit, 3, EnumCircuitType.BASIC), new OreDictStack(ANY_RUBBER.ingot(), 4), new ComparableStack(ModItems.bolt, 16, MAT_DURA.id), new ComparableStack(ModItems.motor, 2), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.sliding_blast_door, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 16), new OreDictStack(W.ingot(), 8), new ComparableStack(ModBlocks.reinforced_glass, 4), new OreDictStack(ANY_RUBBER.ingot(), 4), new ComparableStack(ModItems.bolt, 16, MAT_DURA.id), new ComparableStack(ModItems.motor, 2), }, 300);

		makeRecipe(new ComparableStack(ModBlocks.machine_centrifuge, 1), new AStack[] {new ComparableStack(ModItems.centrifuge_element, 1), new OreDictStack(ANY_PLASTIC.ingot(), 2), new OreDictStack(STEEL.plate(), 8), new OreDictStack(CU.plate(), 8), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ANALOG.ordinal()), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_gascent, 1), new AStack[] {new ComparableStack(ModItems.centrifuge_element, 4), new OreDictStack(ANY_PLASTIC.ingot(), 4), new OreDictStack(DESH.ingot(), 2), new OreDictStack(STEEL.plate(), 8), new ComparableStack(ModItems.coil_tungsten, 4), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED.ordinal()) }, 300);
		makeRecipe(new ComparableStack(ModBlocks.machine_reactor, 1), new AStack[] { new ComparableStack(ModItems.reactor_core, 1), new OreDictStack(STEEL.ingot(), 12), new OreDictStack(PB.plate(), 16), new ComparableStack(ModBlocks.reinforced_glass, 4), new OreDictStack(ASBESTOS.ingot(), 4) }, 150);
		makeRecipe(new ComparableStack(ModBlocks.machine_rtg_furnace_off, 1), new AStack[] { new ComparableStack(Blocks.FURNACE, 1), new ComparableStack(ModItems.rtg_unit, 3), new OreDictStack(PB.plate(), 6), new OreDictStack(OreDictManager.getReflector(), 4), new OreDictStack(CU.plate(), 2), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.machine_difurnace_rtg_off, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_difurnace_off, 1),
				new ComparableStack(ModItems.rtg_unit, 3),
				new OreDictStack(DESH.ingot(), 4),
				new OreDictStack(PB.plate(), 6),
				new OreDictStack(OreDictManager.getReflector(), 8),
				new OreDictStack(CU.plateCast(), 4)
			}, 150);
		makeRecipe(new ComparableStack(ModBlocks.machine_radgen, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 8), new OreDictStack(STEEL.plate(), 32), new ComparableStack(ModItems.coil_magnetized_tungsten, 6), new ComparableStack(ModItems.wire, 24, MAT_MAGTUNG.id), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.BASIC), new ComparableStack(ModItems.reactor_core, 3), new OreDictStack(STAR.ingot(), 1), new OreDictStack(KEY_RED, 1), }, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_diesel, 1), new AStack[] {new OreDictStack(STEEL.shell(), 1), new ComparableStack(ModItems.piston_selenium, 1), new OreDictStack(STEEL.plateCast(), 1), new ComparableStack(ModItems.coil_copper, 4), }, 60);
		makeRecipe(new ComparableStack(ModBlocks.machine_selenium, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(TI.plate(), 6), new OreDictStack(CU.plate(), 8), new ComparableStack(ModItems.hull_big_steel, 1), new ComparableStack(ModItems.hull_small_steel, 9), new ComparableStack(ModItems.pedestal_steel, 1), new ComparableStack(ModItems.coil_copper, 4), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.machine_reactor_small, 1), new AStack[] {
				new ComparableStack(ModItems.hull_big_steel, 4),
				new ComparableStack(ModBlocks.steel_scaffold, 4),
				new ComparableStack(ModBlocks.brick_compound, 16),
				new ComparableStack(ModBlocks.deco_pipe_quad, 8),
				new ComparableStack(ModItems.motor, 4),
				new OreDictStack(B.ingot(), 8),
				new OreDictStack(GRAPHITE.ingot(), 16),
				new ComparableStack(ModItems.circuit, 3, EnumCircuitType.BASIC)
			}, 600);
		makeRecipe(new ComparableStack(ModBlocks.machine_rtg_grey, 1), new AStack[] { new ComparableStack(ModItems.rtg_unit, 3), new OreDictStack(STEEL.plate(), 4), new ComparableStack(ModItems.wire, 4, MAT_MINGRADE.id), new OreDictStack(ANY_PLASTIC.ingot(), 3), }, 200);
		
		//Batteries
		makeRecipe(new ComparableStack(ModBlocks.machine_battery, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(S.dust(), 12), new OreDictStack(PB.dust(), 12), new OreDictStack(MINGRADE.ingot(), 2), new ComparableStack(ModItems.wire, 4, MAT_MINGRADE.id), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_lithium_battery, 1), new AStack[] { 
			new OreDictStack(POLYMER.ingot(), 6), 
			new OreDictStack(CO.dust(), 12), 
			new OreDictStack(LI.dust(), 12), 
			new OreDictStack(ALLOY.ingot(), 3), 
			new ComparableStack(ModItems.wire, 4, MAT_MINGRADE.id), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.machine_desh_battery, 1), new AStack[] { 
			new OreDictStack(DESH.ingot(), 12), 
			new OreDictStack(BAKELITE.dust(), 12), 
			new OreDictStack(P_RED.dust(), 12), 
			new OreDictStack(GRAPHITE.ingot(), 6), 
			new OreDictStack(RA226.nugget(), 2), }, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_saturnite_battery, 1), new AStack[] { 
			new OreDictStack(BIGMT.ingot(), 8), 
			new OreDictStack(DURA.dust(), 12), 
			new OreDictStack(ZR.dust(), 12), 
			new OreDictStack(PU238.ingot(), 4), 
			new OreDictStack(TC99.nugget(), 2), }, 600);
		makeRecipe(new ComparableStack(ModBlocks.machine_schrabidium_battery, 1), new AStack[] { 
			new OreDictStack(SA326.ingot(), 16), 
			new OreDictStack(SA326.dust(), 12), 
			new OreDictStack(NP237.dust(), 12), 
			new OreDictStack(REIIUM.ingot(), 8), 
			new OreDictStack(ANY_BISMOID.nugget(), 2), }, 800);
		makeRecipe(new ComparableStack(ModBlocks.machine_euphemium_battery, 1), new AStack[] { 
			new OreDictStack(EUPH.ingot(), 24), 
			new OreDictStack(XE135.dust(), 16), 
			new OreDictStack(SBD.dust(), 16), 
			new OreDictStack(STAR.ingot(), 12), 
			new OreDictStack(GH336.nugget(), 8), }, 1600);
		makeRecipe(new ComparableStack(ModBlocks.machine_radspice_battery, 1), new AStack[] { 
			new ComparableStack(ModItems.ingot_radspice, 32), 
			new ComparableStack(ModItems.powder_chlorophyte, 24), 
			new ComparableStack(ModItems.powder_balefire, 24), 
			new OreDictStack(VERTICIUM.ingot(), 16), 
			new OreDictStack(AS.nugget(), 8), }, 3200);
		makeRecipe(new ComparableStack(ModBlocks.machine_dineutronium_battery, 1), new AStack[] { 
			new OreDictStack(DNT.ingot(), 48), 
			new ComparableStack(ModItems.powder_spark_mix, 32), 
			new ComparableStack(ModItems.battery_spark_cell_1000, 1), 
			new OreDictStack(CMB.ingot(), 24), 
			new ComparableStack(ModItems.coil_magnetized_tungsten, 12), }, 4800);
		makeRecipe(new ComparableStack(ModBlocks.machine_electronium_battery, 1), new AStack[] { 
			new ComparableStack(ModItems.ingot_electronium, 64), 
			new OreDictStack(OSMIRIDIUM.dust(), 48), 
			new ComparableStack(ModItems.battery_spark_cell_10000, 1), 
			new OreDictStack(AU198.ingot(), 32), 
			new ComparableStack(ModItems.nugget_u238m2, 16), }, 6400);

		makeRecipe(new ComparableStack(ModBlocks.machine_shredder, 1), new AStack[] {new OreDictStack(STEEL.plate(), 8), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModBlocks.steel_beam, 2), new ComparableStack(Blocks.IRON_BARS, 2) },200);
		makeRecipe(new ComparableStack(ModBlocks.machine_well, 1), new AStack[] {new ComparableStack(ModBlocks.steel_scaffold, 20), new ComparableStack(ModItems.tank_steel, 2), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.pipes_steel, 1), new ComparableStack(ModItems.drill_titanium, 1) }, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_pumpjack, 1), new AStack[] {new ComparableStack(ModBlocks.steel_scaffold, 8), new OreDictStack(STEEL.plateWelded(), 8), new ComparableStack(ModItems.pipes_steel, 4), new ComparableStack(ModItems.tank_steel, 4), new OreDictStack(STEEL.plate(), 32), new ComparableStack(ModItems.drill_titanium, 1), new ComparableStack(ModItems.motor_desh) }, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_fracking_tower), new AStack[] {
				new ComparableStack(ModBlocks.steel_scaffold, 40),
				new ComparableStack(ModBlocks.concrete_smooth, 64),
				new ComparableStack(ModItems.drill_titanium),
				new ComparableStack(ModItems.motor_desh, 2),
				new ComparableStack(ModItems.plate_desh, 6),
				new ComparableStack(ModItems.circuit, 16, EnumCircuitType.CAPACITOR.ordinal()),
				new OreDictStack(STEEL.shell(), 16),
				new OreDictStack(STEEL.pipe(), 12)
		}, 600);
		makeRecipe(new ComparableStack(ModBlocks.machine_catalytic_cracker), new AStack[] {
				new ComparableStack(ModBlocks.steel_scaffold, 16),
				new OreDictStack(STEEL.shell(), 6),
				new OreDictStack(ANY_PLASTIC.ingot(), 4),
				new OreDictStack(NB.ingot(), 2),
				new OreDictStack(LA.ingot(), 1),
				new ComparableStack(ModItems.catalyst_clay, 12),
		}, 300);
		makeRecipe(new ComparableStack(ModBlocks.machine_liquefactor), new AStack[] {
				new OreDictStack(STEEL.ingot(), 8),
				new OreDictStack(CU.plateCast(), 6),
				new OreDictStack(ANY_TAR.any(), 8),
				new OreDictStack(STEEL.shell(), 3),
				new ComparableStack(ModItems.circuit, 12, EnumCircuitType.CAPACITOR),
				new ComparableStack(ModItems.catalyst_clay, 4),
				new ComparableStack(ModItems.coil_tungsten, 8)
		}, 200);

		makeRecipe(new ComparableStack(ModBlocks.machine_solidifier), new AStack[] {
				new OreDictStack(ANY_CONCRETE.any(), 8),
				new OreDictStack(AL.plateCast(), 6),
				new OreDictStack(ANY_PLASTIC.ingot(), 4),
				new OreDictStack(STEEL.shell(), 3),
				new ComparableStack(ModItems.circuit, 12, EnumCircuitType.CAPACITOR),
				new ComparableStack(ModItems.catalyst_clay, 4),
				new ComparableStack(ModItems.coil_copper, 4)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_vacuum_distill, 1), new AStack[] {
				new OreDictStack(STEEL.plateCast(), 16),
				new OreDictStack(CU.plate(), 16),
				new OreDictStack(ANY_RESISTANTALLOY.ingot(), 4),
				new ComparableStack(ModItems.sphere_steel, 1),
				new OreDictStack(STEEL.pipe(), 12),
				new ComparableStack(ModItems.motor_desh, 3),
				new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CHIP_BISMOID)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_catalytic_reformer, 1), new AStack[] {
				new OreDictStack(STEEL.plateCast(), 12),
				new OreDictStack(CU.plate(), 8),
				new OreDictStack(NB.ingot(), 8),
				new OreDictStack(ANY_RESISTANTALLOY.ingot(), 4),
				new OreDictStack(STEEL.shell(), 3),
				new OreDictStack(STEEL.pipe(), 8),
				new ComparableStack(ModItems.motor, 1),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_hydrotreater, 1), new AStack[] {
				new OreDictStack(STEEL.plateWelded(), 8),
				new OreDictStack(CU.plateCast(), 4),
				new OreDictStack(NB.ingot(), 8),
				new OreDictStack(ANY_RESISTANTALLOY.ingot(), 4),
				new OreDictStack(STEEL.shell(), 2),
				new OreDictStack(STEEL.pipe(), 8),
				new ComparableStack(ModItems.motor_desh, 2),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_flare, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 12), new OreDictStack(IRON.pipe(), 3), new OreDictStack(CU.plate(), 4), new ComparableStack(ModItems.tank_steel, 1), new OreDictStack(STEEL.pipe(), 8), new OreDictStack(STEEL.shell(), 4), new ComparableStack(ModItems.thermo_element, 3), },200);
		makeRecipe(new ComparableStack(ModBlocks.machine_coker, 1), new AStack[] {new OreDictStack(STEEL.plateWelded(), 3), new OreDictStack(IRON.pipe(), 4), new OreDictStack(CU.plate(), 8), new OreDictStack(RUBBER.ingot(), 4), new ComparableStack(ModItems.tank_steel, 2), new ComparableStack(ModBlocks.steel_grate, 4) },200);
		makeRecipe(new ComparableStack(ModBlocks.machine_refinery, 1), new AStack[] {new OreDictStack(STEEL.plateWelded(), 3), new OreDictStack(CU.plate(), 16), new OreDictStack(STEEL.shell(), 6), new OreDictStack(STEEL.pipe(), 12), new ComparableStack(ModItems.plate_polymer, 8), new ComparableStack(ModItems.circuit, 3, EnumCircuitType.ANALOG.ordinal()) },350);
		makeRecipe(new ComparableStack(ModBlocks.machine_epress, 1), new AStack[] {new OreDictStack(STEEL.plate(), 8), new OreDictStack(ANY_RUBBER.ingot(), 4), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.BASIC.ordinal()) }, 100);
		makeRecipe(new ComparableStack(ModBlocks.machine_chemplant, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 8), new OreDictStack(CU.pipe(), 2), new ComparableStack(ModItems.plate_polymer, 16), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.coil_tungsten, 2), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ANALOG.ordinal()) }, 200);
		makeRecipe(new ComparableStack(ModBlocks.machine_chemfac, 1), new AStack[] {
				new OreDictStack(DURA.ingot(), 16),
				new OreDictStack(ANY_RESISTANTALLOY.ingot(), 8),
				new OreDictStack(RUBBER.ingot(), 16),
				new OreDictStack(STEEL.shell(), 12),
				new OreDictStack(CU.pipe(), 8),
				new ComparableStack(ModItems.motor_desh, 4),
				new ComparableStack(ModItems.coil_tungsten, 16),
				new ComparableStack(ModItems.circuit, 16, EnumCircuitType.BASIC)
		}, 400);
		makeRecipe(new ComparableStack(ModBlocks.machine_crystallizer, 1), new AStack[] {new OreDictStack(STEEL.plateWelded(), 2), new OreDictStack(TI.shell(), 3), new OreDictStack(DESH.ingot(), 4), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.BASIC.ordinal()), new ComparableStack(ModItems.blades_advanced_alloy, 2) },200);
		makeRecipe(new ComparableStack(ModBlocks.machine_fluidtank, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 2), new OreDictStack(STEEL.plate(), 6), new OreDictStack(STEEL.shell(), 4), new OreDictStack(ANY_TAR.any(), 4), },150);
		makeRecipe(new ComparableStack(ModBlocks.machine_mining_laser, 1), new AStack[] {new ComparableStack(ModItems.tank_steel, 3), new OreDictStack(STEEL.plate(), 16), new ComparableStack(ModItems.crystal_redstone, 3), new ComparableStack(Items.DIAMOND, 3), new OreDictStack(ANY_PLASTIC.ingot(), 4), new ComparableStack(ModItems.motor, 3), new OreDictStack(DURA.ingot(), 4), new OreDictStack(DURA.bolt(), 8), new ComparableStack(ModBlocks.machine_battery, 3), },400);
		makeRecipe(new ComparableStack(ModBlocks.machine_turbofan, 1), new AStack[] {new OreDictStack(TI.shell(), 8), new OreDictStack(DURA.pipe(), 4), new OreDictStack(ANY_PLASTIC.ingot(), 12), new ComparableStack(ModItems.turbine_tungsten, 1), new OreDictStack(GOLD.wireDense(), 12), new ComparableStack(ModItems.circuit, 3, EnumCircuitType.BASIC.ordinal()) }, 300);
		makeRecipe(new ComparableStack(ModBlocks.machine_teleporter, 1), new AStack[] {new OreDictStack(TI.ingot(), 8), new OreDictStack(ALLOY.plate(), 12), new OreDictStack(GOLD.wire(), 32), new ComparableStack(ModItems.entanglement_kit, 1), new ComparableStack(ModItems.telepad, 1), new ComparableStack(ModBlocks.machine_schrabidium_battery, 1) },300);
		makeRecipe(new ComparableStack(ModBlocks.machine_schrabidium_transmutator, 1), new AStack[] {new OreDictStack(MAGTUNG.ingot(), 1), new OreDictStack(TI.ingot(), 24), new OreDictStack(ALLOY.plate(), 18), new OreDictStack(STEEL.plateWelded(), 12), new ComparableStack(ModItems.plate_desh, 6), new OreDictStack(RUBBER.ingot(), 8), new ComparableStack(ModBlocks.machine_battery, 5), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.ADVANCED.ordinal()), },500);

		makeRecipe(new ComparableStack(ModBlocks.factory_advanced_hull, 1), new AStack[] { new OreDictStack(ALLOY.ingot(), 4), new OreDictStack(ALLOY.plate(), 4), new OreDictStack(POLYMER.ingot(), 2), new ComparableStack(ModItems.wire, 6, MAT_ALLOY.id), }, 50);
		makeRecipe(new ComparableStack(ModBlocks.factory_advanced_furnace, 1), new AStack[] { new OreDictStack(ALLOY.ingot(), 4), new OreDictStack(ALLOY.plate(), 4), new OreDictStack(STEEL.plate(), 8), new OreDictStack(POLYMER.ingot(), 4), new ComparableStack(ModItems.coil_advanced_alloy, 2), }, 100);
		makeRecipe(new ComparableStack(ModBlocks.factory_advanced_core, 1), new AStack[] { new OreDictStack(ALLOY.ingot(), 6), new OreDictStack(ALLOY.plate(), 6), new OreDictStack(STEEL.plate(), 8), new ComparableStack(ModItems.coil_advanced_alloy, 2), new ComparableStack(ModItems.motor, 16), new ComparableStack(Blocks.PISTON, 6), new OreDictStack(POLYMER.block(), 4) }, 100);
		makeRecipe(new ComparableStack(ModBlocks.factory_advanced_conductor, 1), new AStack[] { new OreDictStack(ALLOY.ingot(), 8), new OreDictStack(ALLOY.plate(), 6), new OreDictStack(POLYMER.ingot(), 4), new ComparableStack(ModItems.wire, 4, MAT_ALLOY.id), new ComparableStack(ModItems.fuse, 6), }, 50);
		makeRecipe(new ComparableStack(ModBlocks.reactor_element, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 2), new OreDictStack(OreDictManager.getReflector(), 4), new OreDictStack(PB.plate(), 2), new ComparableStack(ModItems.rod_empty, 8), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.reactor_control, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(PB.ingot(), 6), new ComparableStack(ModItems.bolt, 6, MAT_TUNGSTEN.id), new ComparableStack(ModItems.motor, 1), }, 100);
		makeRecipe(new ComparableStack(ModBlocks.reactor_hatch, 1), new AStack[] { new ComparableStack(ModBlocks.brick_concrete, 1), new OreDictStack(STEEL.plate(), 6), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.reactor_conductor, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(CU.plate(), 12), new ComparableStack(ModItems.wire, 4, MAT_TUNGSTEN.id), }, 130);
		makeRecipe(new ComparableStack(ModBlocks.reactor_computer, 1), new AStack[] { new ComparableStack(ModBlocks.reactor_conductor, 2), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CONTROLLER), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CRT_TUBE), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.fusion_conductor, 3), new AStack[] {new OreDictStack(ND.ingot()) , new ComparableStack(ModBlocks.deco_pipe, 1), new ComparableStack(ModItems.coil_advanced_alloy, 10), }, 150);
		makeRecipe(new ComparableStack(ModBlocks.fusion_center, 1), new AStack[] {new OreDictStack(ANY_HARDPLASTIC.ingot(), 4), new OreDictStack(STEEL.plate(), 6), new OreDictStack(ALLOY.wire(), 24), },200);
		makeRecipe(new ComparableStack(ModBlocks.fusion_motor, 1), new AStack[] {new OreDictStack(TI.ingot(), 4), new OreDictStack(STEEL.ingot(), 2), new ComparableStack(ModItems.motor, 4), },250);
		makeRecipe(new ComparableStack(ModBlocks.fusion_heater, 4), new AStack[] {new OreDictStack(W.plateWelded(), 2), new OreDictStack(STEEL.plateWelded(), 2), new OreDictStack(OreDictManager.getReflector(), 2), new ComparableStack(ModItems.magnetron, 2) }, 200);
		makeRecipe(new ComparableStack(ModBlocks.fwatz_hatch, 1), new AStack[] { new ComparableStack(ModBlocks.fwatz_scaffold, 4), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CHIP_BISMOID), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CHIP), new ComparableStack(ModItems.wire, 16, MAT_MINGRADE.id), new OreDictStack(CMB.plate(), 16), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.fwatz_computer, 1), new AStack[] { new ComparableStack(ModBlocks.block_meteor, 1), new ComparableStack(ModItems.wire, 16, MAT_MAGTUNG.id), new OreDictStack(SBD.dust(), 4), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.BISMOID), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.fwatz_core, 1), new AStack[] {new ComparableStack(ModBlocks.block_meteor, 1), new ComparableStack(ModItems.wire, 24, MAT_MAGTUNG.id), new OreDictStack(DIAMOND.dust(), 8), new OreDictStack(MAGTUNG.dust(), 12), new OreDictStack(DESH.heavyComp()), new ComparableStack(ModItems.upgrade_screm, 1), new ComparableStack(ModItems.upgrade_overdrive_3, 1), new ComparableStack(ModItems.circuit, 8, EnumCircuitType.QUANTUM)},450);

		makeRecipe(new ComparableStack(ModBlocks.nuke_gadget, 1), new AStack[] {new ComparableStack(ModItems.sphere_steel, 1), new ComparableStack(ModItems.fins_flat, 2), new ComparableStack(ModItems.pedestal_steel, 1), new ComparableStack(ModItems.circuit, 3, EnumCircuitType.CONTROLLER.ordinal()), new OreDictStack("dyeGray", 8), },300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_boy, 1), new AStack[] {new OreDictStack(STEEL.shell(), 2), new ComparableStack(ModItems.fins_small_steel, 1), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CONTROLLER.ordinal()), new OreDictStack("dyeBlue", 4), },300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_man, 1), new AStack[] {new ComparableStack(ModItems.sphere_steel, 1), new OreDictStack(STEEL.shell(), 2), new ComparableStack(ModItems.fins_big_steel, 1), new ComparableStack(ModItems.circuit, 3, EnumCircuitType.CONTROLLER.ordinal()), new OreDictStack("dyeYellow", 6), },300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_mike, 1), new AStack[] {new ComparableStack(ModItems.sphere_steel, 1), new OreDictStack(AL.shell(), 4), new ComparableStack(ModItems.circuit, 8, EnumCircuitType.CONTROLLER_ADVANCED.ordinal()), new OreDictStack("dyeLightGray", 16), },300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_tsar, 1), new AStack[] {new ComparableStack(ModItems.sphere_steel, 1), new OreDictStack(TI.shell(), 6), new OreDictStack(STEEL.shell(), 2), new ComparableStack(ModItems.fins_tri_steel, 1), new ComparableStack(ModItems.circuit, 16, EnumCircuitType.CONTROLLER_ADVANCED.ordinal()), new OreDictStack("dyeBlack", 8), },600);
		makeRecipe(new ComparableStack(ModBlocks.nuke_prototype, 1), new AStack[] {new ComparableStack(ModItems.dysfunctional_reactor, 1), new OreDictStack(STEEL.shell(), 2), new ComparableStack(ModItems.ingot_euphemium, 3), new ComparableStack(ModItems.circuit, 8, EnumCircuitType.CONTROLLER_ADVANCED.ordinal()) },500);
		makeRecipe(new ComparableStack(ModBlocks.nuke_fleija, 1), new AStack[] {new OreDictStack(AL.shell(), 1), new ComparableStack(ModItems.fins_quad_titanium, 1), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER.ordinal()), new OreDictStack("dyeWhite", 4), },400);
		makeRecipe(new ComparableStack(ModBlocks.nuke_solinium, 1), new AStack[] {new OreDictStack(STEEL.shell(), 2), new ComparableStack(ModItems.fins_quad_titanium, 1), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER.ordinal()), new OreDictStack("dyeGray", 8), },400);
		makeRecipe(new ComparableStack(ModBlocks.nuke_n2, 1), new AStack[] {new OreDictStack(STEEL.shell(), 6), new OreDictStack(MAGTUNG.wire(), 12), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CONTROLLER.ordinal()), new OreDictStack("dyeBlack", 8), },300);
		makeRecipe(new ComparableStack(ModBlocks.nuke_fstbmb, 1), new AStack[] { new ComparableStack(ModItems.sphere_steel, 1), new ComparableStack(ModItems.hull_big_titanium, 6), new ComparableStack(ModItems.fins_big_steel, 1), new ComparableStack(ModItems.powder_magic, 8), new ComparableStack(ModItems.wire, 12, MAT_GOLD.id), new ComparableStack(ModItems.circuit, 8, EnumCircuitType.CONTROLLER_ADVANCED.ordinal()), new OreDictStack(KEY_GRAY, 6), }, 600);
		makeRecipe(new ComparableStack(ModBlocks.nuke_custom, 1), new AStack[] {new OreDictStack(STEEL.shell(), 2), new ComparableStack(ModItems.fins_small_steel, 1), new ComparableStack(ModItems.circuit, 8, EnumCircuitType.CONTROLLER_ADVANCED.ordinal()), new OreDictStack("dyeGray", 4), },300);
		makeRecipe(new ComparableStack(ModBlocks.float_bomb, 1), new AStack[] { new OreDictStack(TI.plate(), 12), new ComparableStack(ModItems.levitation_unit, 1), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.ADVANCED), new ComparableStack(ModItems.wire, 6, MAT_GOLD.id), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.therm_endo, 1), new AStack[] { new OreDictStack(TI.plate(), 12), new ComparableStack(ModItems.thermo_unit_endo, 1), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.ADVANCED), new ComparableStack(ModItems.wire, 6, MAT_GOLD.id), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.therm_exo, 1), new AStack[] { new OreDictStack(TI.plate(), 12), new ComparableStack(ModItems.thermo_unit_exo, 1), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.ADVANCED), new ComparableStack(ModItems.wire, 6, MAT_GOLD.id), }, 250);

		makeRecipe(new ComparableStack(ModBlocks.launch_pad, 1), new AStack[] {
				new OreDictStack(STEEL.plateWelded(), 8),
				new OreDictStack(ANY_CONCRETE.any(), 8),
				new OreDictStack(ANY_HARDPLASTIC.ingot(), 16),
				new ComparableStack(ModItems.circuit, 4, EnumCircuitType.ADVANCED.ordinal())
		}, 400);
		makeRecipe(new ComparableStack(ModItems.spawn_chopper, 1), new AStack[] { new ComparableStack(ModItems.chopper_blades, 5), new ComparableStack(ModItems.chopper_gun, 1), new ComparableStack(ModItems.chopper_head, 1), new ComparableStack(ModItems.chopper_tail, 1), new ComparableStack(ModItems.chopper_torso, 1), new ComparableStack(ModItems.chopper_wing, 2), }, 300);

		makeRecipe(new ComparableStack(ModBlocks.turret_light, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 6), new ComparableStack(ModItems.pipes_steel, 2), new OreDictStack(MINGRADE.ingot(), 2), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.BASIC), }, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_heavy, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 8), new OreDictStack(AL.ingot(), 4), new ComparableStack(ModItems.pipes_steel, 2), new ComparableStack(ModItems.hull_small_steel, 1), new OreDictStack(MINGRADE.ingot(), 4), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.circuit, 3, EnumCircuitType.BASIC), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.turret_rocket, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 12), new OreDictStack(TI.ingot(), 4), new ComparableStack(ModItems.hull_small_steel, 8), new OreDictStack(MINGRADE.ingot(), 6), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.ANALOG), }, 300);
		makeRecipe(new ComparableStack(ModBlocks.turret_flamer, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 8), new OreDictStack(W.ingot(), 2), new ComparableStack(ModItems.pipes_steel, 1), new ComparableStack(ModItems.tank_steel, 2), new OreDictStack(MINGRADE.ingot(), 4), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.ANALOG), }, 250);
		makeRecipe(new ComparableStack(ModBlocks.turret_tau, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 16), new OreDictStack(TI.ingot(), 8), new OreDictStack(ALLOY.plate(), 4), new ComparableStack(ModItems.redcoil_capacitor, 3), new OreDictStack(MINGRADE.ingot(), 12), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.ADVANCED), }, 350);
		makeRecipe(new ComparableStack(ModBlocks.turret_spitfire, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 6), new OreDictStack(MINGRADE.ingot(), 6), new OreDictStack(STEEL.plate(), 16), new OreDictStack(IRON.plate(), 8), new ComparableStack(ModItems.hull_small_steel, 4), new ComparableStack(ModItems.pipes_steel, 2), new ComparableStack(ModItems.motor, 3), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.BASIC), }, 350);
		makeRecipe(new ComparableStack(ModBlocks.turret_cwis, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 6), new OreDictStack(MINGRADE.ingot(), 8), new OreDictStack(STEEL.plate(), 10), new OreDictStack(TI.plate(), 4), new ComparableStack(ModItems.hull_small_aluminium, 2), new ComparableStack(ModItems.pipes_steel, 6), new ComparableStack(ModItems.motor, 4), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.ADVANCED), new ComparableStack(ModItems.magnetron, 3), }, 400);
		makeRecipe(new ComparableStack(ModBlocks.turret_cheapo, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 4), new OreDictStack(IRON.plate(), 4), new ComparableStack(ModItems.pipes_steel, 3), new ComparableStack(ModItems.motor, 3), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CHIP)}, 200);

		makeRecipe(new ComparableStack(ModItems.gun_defabricator, 1), new AStack[] { new OreDictStack(STEEL.ingot(), 2), new OreDictStack(ANY_PLASTIC.ingot(), 8), new OreDictStack(IRON.plateWelded(), 5), new ComparableStack(ModItems.mechanism_special, 3), new ComparableStack(ModItems.laser_crystal_dem, 1), new ComparableStack(ModItems.plate_dalekanium, 3), }, 200);
		makeRecipe(new ComparableStack(ModItems.gun_osipr_ammo, 24), new AStack[] { new OreDictStack(STEEL.plate(), 2), new ComparableStack(Items.REDSTONE, 1), new ComparableStack(Items.GLOWSTONE_DUST, 1), }, 50);
		makeRecipe(new ComparableStack(ModItems.gun_osipr_ammo2, 1), new AStack[] { new OreDictStack(CMB.plate(), 4), new ComparableStack(Items.REDSTONE, 7), new ComparableStack(ModItems.powder_power, 3), }, 200);
		
		//AMMO CLIP CRAFTING
		makeRecipe(new ComparableStack(ModItems.clip_revolver_iron), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_iron_ammo, 24) }, 40);	
		makeRecipe(new ComparableStack(ModItems.clip_revolver), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_gold), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_gold_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_lead), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_lead_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_schrabidium), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_schrabidium_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_cursed), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_cursed_ammo, 17) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_nightmare), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_nightmare_ammo, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_nightmare2), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_revolver_nightmare2_ammo, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_pip), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_44_pip, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_revolver_nopip), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_44, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_rpg), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_rocket, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_stinger), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_stinger_ammo, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_fatman), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_nuke, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_mirv), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_mirv, 3) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_bf), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_bf_ammo, 2) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_mp40), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_9mm, 32) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_uzi), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_22lr, 32) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_uboinik), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_12gauge, 24) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_lever_action), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_20gauge, 24) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_bolt_action), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_20gauge_slug, 24) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_osipr), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_osipr_ammo, 30) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_immolator), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_immolator_ammo, 64) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_cryolator), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_cryolator_ammo, 64) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_mp), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.ammo_566_gold, 40) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_xvl1456), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_xvl1456_ammo, 64) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_emp), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_emp_ammo, 6) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_jack), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_jack_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_spark), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_spark_ammo, 4) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_hp), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_hp_ammo, 12) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_euthanasia), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_euthanasia_ammo, 16) }, 40);
		makeRecipe(new ComparableStack(ModItems.clip_defabricator), new AStack[] { new ComparableStack(ModItems.plate_polymer), new ComparableStack(ModItems.gun_defabricator_ammo, 16) }, 40);

		makeRecipe(new ComparableStack(ModItems.grenade_fire, 1), new AStack[] { new ComparableStack(ModItems.grenade_frag, 1), new OreDictStack(P_RED.dust(), 1), new OreDictStack(CU.plate(), 2), }, 150);
		makeRecipe(new ComparableStack(ModItems.grenade_fire, 1), new AStack[] {new ComparableStack(ModItems.grenade_frag, 1), new OreDictStack(P_RED.dust(), 1), new OreDictStack(CU.plate(), 2), },150);
		makeRecipe(new ComparableStack(ModItems.grenade_shrapnel, 1), new AStack[] {new ComparableStack(ModItems.grenade_frag, 1), new ComparableStack(ModItems.pellet_buckshot, 1), new OreDictStack(STEEL.plate(), 2), },150);
		makeRecipe(new ComparableStack(ModItems.grenade_cluster, 1), new AStack[] {new ComparableStack(ModItems.grenade_frag, 1), new ComparableStack(ModItems.pellet_cluster, 1), new OreDictStack(STEEL.plate(), 2), },200);
		makeRecipe(new ComparableStack(ModItems.grenade_flare, 1), new AStack[] {new ComparableStack(ModItems.grenade_generic, 1), new ComparableStack(Items.GLOWSTONE_DUST, 1), new OreDictStack(AL.plate(), 2), new OreDictStack(CS.plate(), 1) },100);
		makeRecipe(new ComparableStack(ModItems.grenade_electric, 1), new AStack[] {new ComparableStack(ModItems.grenade_generic, 1), new ComparableStack(ModItems.circuit, 3, EnumCircuitType.CAPACITOR.ordinal()), new OreDictStack(GOLD.plate(), 2), },200);
		makeRecipe(new ComparableStack(ModItems.grenade_pulse, 4), new AStack[] {new OreDictStack(STEEL.plate(), 1), new OreDictStack(IRON.plate(), 3), new OreDictStack(MINGRADE.wire(), 6), new ComparableStack(Items.DIAMOND, 1), },300);
		makeRecipe(new ComparableStack(ModItems.grenade_plasma, 2), new AStack[] {new OreDictStack(STEEL.plate(), 3), new OreDictStack(ALLOY.plate(), 1), new ComparableStack(ModItems.coil_advanced_torus, 1), new NbtComparableStack(ItemCell.getFullCell(ModForgeFluids.DEUTERIUM, 1)), new NbtComparableStack(ItemCell.getFullCell(ModForgeFluids.TRITIUM, 1)), },300);
		makeRecipe(new ComparableStack(ModItems.grenade_tau, 2), new AStack[] {new OreDictStack(PB.plate(), 3), new OreDictStack(ALLOY.plate(), 1), new ComparableStack(ModItems.coil_advanced_torus, 1), new ComparableStack(ModItems.gun_xvl1456_ammo, 1) },300);
		makeRecipe(new ComparableStack(ModItems.grenade_schrabidium, 1), new AStack[] {new ComparableStack(ModItems.grenade_flare, 1), new OreDictStack(SA326.dust(), 1), new OreDictStack(OreDictManager.getReflector(), 2), },300);
		makeRecipe(new ComparableStack(ModItems.grenade_nuclear, 1), new AStack[] {new OreDictStack(IRON.plate(), 1), new OreDictStack(STEEL.plate(), 1), new OreDictStack(PU239.nugget(), 2), new OreDictStack(MINGRADE.wire(), 2), },200);
		makeRecipe(new ComparableStack(ModItems.grenade_zomg, 1), new AStack[] {new ComparableStack(ModItems.plate_paa, 3), new OreDictStack(OreDictManager.getReflector(), 1), new ComparableStack(ModItems.coil_magnetized_tungsten, 3), new ComparableStack(ModItems.powder_power, 3), new ComparableStack(ModItems.nugget_radspice, 1) },300);
		makeRecipe(new ComparableStack(ModItems.grenade_black_hole, 1), new AStack[] {new OreDictStack(ANY_PLASTIC.ingot(), 6), new OreDictStack(OreDictManager.getReflector(), 3), new ComparableStack(ModItems.coil_magnetized_tungsten, 2), new ComparableStack(ModItems.black_hole, 1), },500);
		makeRecipe(new ComparableStack(ModItems.multitool_dig, 1), new AStack[] { new ComparableStack(ModItems.rod_reiium, 1), new ComparableStack(ModItems.rod_weidanium, 1), new ComparableStack(ModItems.rod_australium, 1), new ComparableStack(ModItems.rod_verticium, 1), new ComparableStack(ModItems.rod_unobtainium, 1), new ComparableStack(ModItems.rod_daffergon, 1), new OreDictStack(ANY_PLASTIC.ingot(), 4), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED), new ComparableStack(ModItems.ducttape, 1), }, 600);
		

		makeRecipe(new ComparableStack(ModItems.gadget_explosive, 4), new AStack[] {new OreDictStack(AL.plate(), 4), new OreDictStack(GOLD.wire(), 8), new ComparableStack(ModBlocks.det_cord, 4), new OreDictStack(CU.plate(), 1), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 10), new OreDictStack(ANY_PLASTIC.ingot(), 2)}, 200); //8 HE lenses (polymer inserts since no baratol) w/ bridge-wire detonators, aluminum pushers, & duraluminum shell
		makeRecipe(new ComparableStack(ModItems.gadget_wireing, 1), new AStack[] {new OreDictStack(IRON.plate(), 1), new OreDictStack(GOLD.wire(), 12), },100);
		makeRecipe(new ComparableStack(ModItems.gadget_core, 1), new AStack[] {new OreDictStack(PU239.nugget(), 7), new OreDictStack(U238.nugget(), 3), },200);
		makeRecipe(new ComparableStack(ModItems.boy_shielding, 1), new AStack[] { new OreDictStack(OreDictManager.getReflector(), 12), new OreDictStack(STEEL.plate(), 4), }, 150);
		makeRecipe(new ComparableStack(ModItems.boy_target, 1), new AStack[] { new OreDictStack(U235.nugget(), 7), }, 200);
		makeRecipe(new ComparableStack(ModItems.boy_bullet, 1), new AStack[] { new OreDictStack(U235.nugget(), 3), }, 100);
		makeRecipe(new ComparableStack(ModItems.boy_propellant, 1), new AStack[] {new ComparableStack(ModItems.cordite, 8), new OreDictStack(IRON.plate(), 8), new OreDictStack(AL.plate(), 4), new OreDictStack(MINGRADE.wire(), 4), },100);
		makeRecipe(new ComparableStack(ModItems.boy_igniter, 1), new AStack[] {new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 1), new OreDictStack(AL.plate(), 6), new OreDictStack(STEEL.plate(), 1), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED.ordinal()), new OreDictStack(MINGRADE.wire(), 3), },150); //HE for gating purposes

		makeRecipe(new ComparableStack(ModItems.man_explosive, 4), new AStack[] {new OreDictStack(AL.plate(), 4), new ComparableStack(ModItems.wire, 8, MAT_MINGRADE.id), new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 2), new OreDictStack(CU.plate(), 1), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 8), new OreDictStack(RUBBER.ingot(), 1)}, 250); //8 HE (To use 16 PBX ingots; rubber inserts) lenses w/ improved bridge-wire detonators, thin aluminum pushers, & duraluminum shell
		makeRecipe(new ComparableStack(ModItems.man_igniter, 1), new AStack[] {new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED.ordinal()), new OreDictStack(MINGRADE.wire(), 9), },150);
		makeRecipe(new ComparableStack(ModItems.man_core, 1), new AStack[] { new OreDictStack(PU239.nugget(), 8), new OreDictStack(BE.nugget(), 2), }, 150);
		makeRecipe(new ComparableStack(ModItems.mike_core, 1), new AStack[] { new OreDictStack(U238.nugget(), 24), new OreDictStack(PB.ingot(), 6), }, 250);
		makeRecipe(new ComparableStack(ModItems.mike_deut, 1), new AStack[] { new OreDictStack(IRON.plate(), 12), new OreDictStack(STEEL.plate(), 16), new NbtComparableStack(ItemCell.getFullCell(ModForgeFluids.DEUTERIUM, 10)), }, 250);
		makeRecipe(new ComparableStack(ModItems.mike_cooling_unit, 1), new AStack[] { new OreDictStack(IRON.plate(), 8), new ComparableStack(ModItems.coil_copper, 5), new ComparableStack(ModItems.coil_tungsten, 5), new ComparableStack(ModItems.motor, 2), }, 200);
		makeRecipe(new ComparableStack(ModItems.fleija_igniter, 1), new AStack[] {new OreDictStack(TI.plate(), 6), new OreDictStack(SA326.wire(), 2), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED.ordinal()), },300);
		makeRecipe(new ComparableStack(ModItems.fleija_core, 1), new AStack[] { new OreDictStack(U235.nugget(), 8), new OreDictStack(NP237.nugget(), 2), new OreDictStack(BE.nugget(), 4), new ComparableStack(ModItems.coil_copper, 2), }, 500);
		makeRecipe(new ComparableStack(ModItems.fleija_propellant, 1), new AStack[] { new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 3), new OreDictStack(SA326.plate(), 8), new ComparableStack(ModItems.ingot_tennessine, 3), }, 400);
		makeRecipe(new ComparableStack(ModItems.solinium_igniter, 1), new AStack[] {new OreDictStack(TI.plate(), 4), new OreDictStack(ALLOY.wire(), 2), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED.ordinal()), new ComparableStack(ModItems.coil_gold, 1), },400);
		makeRecipe(new ComparableStack(ModItems.solinium_core, 1), new AStack[] { new OreDictStack(SA327.ingot(), 3), new OreDictStack(EUPH.nugget(), 4), new ComparableStack(ModItems.ingot_tennessine, 2), }, 400);
		makeRecipe(new ComparableStack(ModItems.solinium_propellant, 1), new AStack[] {new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 3), new OreDictStack(OreDictManager.getReflector(), 2), new ComparableStack(ModItems.plate_polymer, 6), new OreDictStack(W.wire(), 6), new ComparableStack(ModItems.biomass_compressed, 4), },350);
		makeRecipe(new ComparableStack(ModItems.schrabidium_hammer, 1), new AStack[] {new OreDictStack(SA326.block(), 35), new ComparableStack(ModItems.billet_yharonite, 64), new ComparableStack(ModItems.billet_yharonite, 64), new ComparableStack(ModItems.coin_ufo, 1), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64), new ComparableStack(ModItems.fragment_meteorite, 64) },1000);
		makeRecipe(new ComparableStack(ModItems.component_limiter, 1), new AStack[] { new ComparableStack(ModItems.hull_big_steel, 2), new OreDictStack(STEEL.plate(), 32), new OreDictStack(TI.plate(), 18), new ComparableStack(ModItems.plate_desh, 12), new ComparableStack(ModItems.pipes_steel, 4), new ComparableStack(ModItems.circuit, 8, EnumCircuitType.ADVANCED), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CONTROLLER_ADVANCED), new OreDictStack(STAR.ingot(), 14), new ComparableStack(ModItems.plate_dalekanium, 5), new ComparableStack(ModItems.powder_magic, 16), new ComparableStack(ModBlocks.fwatz_computer, 3), }, 2500);
		makeRecipe(new ComparableStack(ModItems.component_emitter, 1), new AStack[] { new ComparableStack(ModItems.hull_big_steel, 3), new ComparableStack(ModItems.hull_big_titanium, 2), new OreDictStack(STEEL.plate(), 32), new OreDictStack(PB.plate(), 24), new ComparableStack(ModItems.plate_desh, 24), new ComparableStack(ModItems.pipes_steel, 8), new ComparableStack(ModItems.circuit, 12, EnumCircuitType.ADVANCED), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CONTROLLER_ADVANCED), new OreDictStack(STAR.ingot(), 26), new ComparableStack(ModItems.powder_magic, 48), new ComparableStack(ModBlocks.fwatz_computer, 2), new ComparableStack(ModItems.crystal_xen, 1), }, 2500);

		makeRecipe(new ComparableStack(ModBlocks.ams_limiter, 1), new AStack[] { new ComparableStack(ModItems.component_limiter, 5), new OreDictStack(AL.heavyComp(), 1), new OreDictStack(ALLOY.heavyComp(), 1), new ComparableStack(ModItems.plate_dineutronium, 16), new ComparableStack(ModItems.circuit, 6, EnumCircuitType.CONTROLLER_ADVANCED), new ComparableStack(ModItems.pipes_steel, 16), new ComparableStack(ModItems.motor, 12), new ComparableStack(ModItems.coil_advanced_torus, 12), new ComparableStack(ModItems.entanglement_kit, 1), }, 6000);
		makeRecipe(new ComparableStack(ModBlocks.ams_emitter, 1), new AStack[] { new ComparableStack(ModItems.component_emitter, 16), new OreDictStack(W.heavyComp(), 1), new OreDictStack(TI.heavyComp(), 2), new ComparableStack(ModItems.plate_dineutronium, 32), new ComparableStack(ModItems.circuit, 12, EnumCircuitType.CONTROLLER_ADVANCED), new ComparableStack(ModItems.coil_advanced_torus, 24), new ComparableStack(ModItems.entanglement_kit, 3), new ComparableStack(ModItems.crystal_horn, 1), new ComparableStack(ModBlocks.fwatz_core, 1), }, 6000);
		makeRecipe(new ComparableStack(ModBlocks.machine_radar, 1), new AStack[] {new OreDictStack(STEEL.plate(), 8), new OreDictStack(ANY_PLASTIC.ingot(), 8), new OreDictStack(ANY_RUBBER.ingot(), 8), new ComparableStack(ModItems.magnetron, 3), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.circuit, 8, EnumCircuitType.BASIC.ordinal()), new ComparableStack(ModItems.coil_copper, 12), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CRT_TUBE), },300);
		makeRecipe(new ComparableStack(ModBlocks.machine_forcefield, 1), new AStack[] {new OreDictStack(ALLOY.plate(), 8), new ComparableStack(ModItems.plate_desh, 4), new ComparableStack(ModItems.coil_gold_torus, 6), new ComparableStack(ModItems.coil_magnetized_tungsten, 12), new ComparableStack(ModItems.motor, 1), new ComparableStack(ModItems.upgrade_radius, 1), new ComparableStack(ModItems.upgrade_health, 1), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED), new ComparableStack(ModBlocks.machine_transformer, 1), },1000);

		makeRecipe(new ComparableStack(ModItems.mp_thruster_10_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.pipes_steel, 1), new OreDictStack(W.ingot(), 4), new OreDictStack(STEEL.plate(), 4), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_10_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.coil_tungsten, 1), new OreDictStack(DURA.ingot(), 4), new OreDictStack(STEEL.plate(), 4), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_10_xenon, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 4), new ComparableStack(ModItems.pipes_steel, 2), new ComparableStack(ModItems.arc_electrode, 4), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.pipes_steel, 4), new OreDictStack(W.ingot(), 8), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_kerosene_dual, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.pipes_steel, 2), new OreDictStack(W.ingot(), 4), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DESH.ingot(), 1), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_kerosene_triple, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.pipes_steel, 3), new OreDictStack(W.ingot(), 6), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DESH.ingot(), 2), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DURA.ingot(), 6), new ComparableStack(ModItems.coil_tungsten, 3), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_solid_hexdecuple, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DURA.ingot(), 12), new ComparableStack(ModItems.coil_tungsten, 6), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_hydrogen, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.pipes_steel, 4), new OreDictStack(W.ingot(), 8), new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.tank_steel, 1), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_hydrogen_dual, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.pipes_steel, 2), new OreDictStack(W.ingot(), 4), new OreDictStack(STEEL.plate(), 6), new ComparableStack(ModItems.tank_steel, 1), new OreDictStack(DESH.ingot(), 1), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_balefire_short, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.plate_polymer, 8), new ComparableStack(ModBlocks.reactor_element, 1), new OreDictStack(DESH.ingot(), 8), new OreDictStack(BIGMT.plate(), 12), new OreDictStack(CU.plateWelded(), 2), new ComparableStack(ModItems.ingot_uranium_fuel, 4), new ComparableStack(ModItems.pipes_steel, 2), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_balefire, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.plate_polymer, 16), new ComparableStack(ModBlocks.reactor_element, 2), new OreDictStack(DESH.ingot(), 16), new OreDictStack(BIGMT.plate(), 24), new OreDictStack(CU.plateWelded(), 4), new ComparableStack(ModItems.ingot_uranium_fuel, 8), new ComparableStack(ModItems.pipes_steel, 2), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_15_balefire_large, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.plate_polymer, 16), new ComparableStack(ModBlocks.reactor_element, 2), new OreDictStack(DESH.ingot(), 24), new OreDictStack(BIGMT.plate(), 32), new OreDictStack(CU.plateWelded(), 4), new ComparableStack(ModItems.ingot_uranium_fuel, 8), new ComparableStack(ModItems.pipes_steel, 2), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.pipes_steel, 8), new OreDictStack(W.ingot(), 16), new OreDictStack(STEEL.plate(), 12), new OreDictStack(DESH.ingot(), 8), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_kerosene_dual, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.pipes_steel, 4), new OreDictStack(W.ingot(), 8), new OreDictStack(STEEL.plate(), 6), new OreDictStack(DESH.ingot(), 4), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_kerosene_triple, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.pipes_steel, 6), new OreDictStack(W.ingot(), 12), new OreDictStack(STEEL.plate(), 8), new OreDictStack(DESH.ingot(), 6), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.coil_tungsten, 8), new OreDictStack(DURA.ingot(), 16), new OreDictStack(STEEL.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_solid_multi, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.coil_tungsten, 12), new OreDictStack(DURA.ingot(), 18), new OreDictStack(STEEL.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_thruster_20_solid_multier, 1), new AStack[] { new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModItems.coil_tungsten, 16), new OreDictStack(DURA.ingot(), 20), new OreDictStack(STEEL.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 2), new ComparableStack(ModBlocks.steel_scaffold, 3), new OreDictStack(TI.plate(), 12), new OreDictStack(STEEL.plate(), 3), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 2), new ComparableStack(ModBlocks.steel_scaffold, 3), new OreDictStack(TI.plate(), 12), new OreDictStack(AL.plate(), 3), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_xenon, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 2), new ComparableStack(ModBlocks.steel_scaffold, 3), new OreDictStack(TI.plate(), 12), new OreDictStack(CU.plateWelded(), 3), }, 100);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_long_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 2), new ComparableStack(ModBlocks.steel_scaffold, 6), new OreDictStack(TI.plate(), 24), new OreDictStack(STEEL.plate(), 6), }, 200);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_long_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 2), new ComparableStack(ModBlocks.steel_scaffold, 6), new OreDictStack(TI.plate(), 24), new OreDictStack(AL.plate(), 6), }, 200);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_15_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModBlocks.steel_scaffold, 9), new OreDictStack(TI.plate(), 36), new OreDictStack(STEEL.plate(), 9), }, 300);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_15_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModBlocks.steel_scaffold, 9), new OreDictStack(TI.plate(), 36), new OreDictStack(AL.plate(), 9), }, 300);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_15_hydrogen, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModBlocks.steel_scaffold, 9), new OreDictStack(TI.plate(), 36), new OreDictStack(IRON.plate(), 9), }, 300);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_10_15_balefire, 1), new AStack[] { new ComparableStack(ModItems.seg_10, 1), new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModBlocks.steel_scaffold, 9), new OreDictStack(TI.plate(), 36), new OreDictStack(BIGMT.plate(), 9), }, 300);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 2), new ComparableStack(ModBlocks.steel_scaffold, 12), new OreDictStack(TI.plate(), 48), new OreDictStack(STEEL.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 2), new ComparableStack(ModBlocks.steel_scaffold, 12), new OreDictStack(TI.plate(), 48), new OreDictStack(AL.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_hydrogen, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 2), new ComparableStack(ModBlocks.steel_scaffold, 12), new OreDictStack(TI.plate(), 48), new OreDictStack(IRON.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_balefire, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 2), new ComparableStack(ModBlocks.steel_scaffold, 12), new OreDictStack(TI.plate(), 48), new OreDictStack(BIGMT.plate(), 12), }, 500);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_20_kerosene, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModBlocks.steel_scaffold, 16), new OreDictStack(TI.plate(), 64), new OreDictStack(STEEL.plate(), 16), }, 600);
		makeRecipe(new ComparableStack(ModItems.mp_fuselage_15_20_solid, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new ComparableStack(ModItems.seg_20, 1), new ComparableStack(ModBlocks.steel_scaffold, 16), new OreDictStack(TI.plate(), 64), new OreDictStack(AL.plate(), 16), }, 600);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_he, 1), new AStack[] {new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 6), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 3), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.BASIC), },100);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_incendiary, 1), new AStack[] {new ComparableStack(ModItems.seg_10, 1), new OreDictStack(TI.plate(), 4), new OreDictStack(P_RED.dust(), 3), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 2), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.BASIC), },100);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_buster, 1), new AStack[] {new ComparableStack(ModItems.seg_10, 1), new OreDictStack(TI.plate(), 4), new ComparableStack(ModBlocks.det_charge, 1), new ComparableStack(ModBlocks.det_cord, 4), new OreDictStack(CU.plateCast(), 4), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.BASIC), },100);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_nuclear, 1), new AStack[] {new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 6), new OreDictStack(PU239.ingot(), 1), new OreDictStack(OreDictManager.getReflector(), 2), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 4), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CONTROLLER), },200);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_nuclear_large, 1), new AStack[] {new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 8), new OreDictStack(AL.plate(), 4), new OreDictStack(PU239.ingot(), 2), new ComparableStack(ModBlocks.det_charge, 4), new ComparableStack(ModItems.circuit,3, EnumCircuitType.CONTROLLER_ADVANCED), },300);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_taint, 1), new AStack[] {new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModBlocks.det_cord, 2), new ComparableStack(ModItems.powder_magic, 12), new ComparableStack(ModItems.ingot_mud, 1), },100);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_10_cloud, 1), new AStack[] {new ComparableStack(ModItems.seg_10, 1), new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModBlocks.det_cord, 2), new ComparableStack(ModItems.grenade_pink_cloud, 2), },100);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_he, 1), new AStack[] {new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 16), new ComparableStack(ModBlocks.det_charge, 4), new ComparableStack(ModItems.circuit, 3, EnumCircuitType.BASIC), },200);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_incendiary, 1), new AStack[] {new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 16), new ComparableStack(ModBlocks.det_charge, 2), new OreDictStack(P_RED.dust(), 8), new ComparableStack(ModItems.circuit, 3, EnumCircuitType.BASIC), },200);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_nuclear, 1), new AStack[] {new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 24), new OreDictStack(TI.plate(), 12), new OreDictStack(PU239.ingot(), 3), new ComparableStack(ModBlocks.det_charge, 6), new ComparableStack(ModItems.circuit, 5, EnumCircuitType.CONTROLLER_ADVANCED), },500);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_thermo, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 32), new OreDictStack(TI.plate(), 16), new ComparableStack(ModBlocks.det_nuke, 2), new ComparableStack(ModItems.mike_deut, 1), new ComparableStack(ModItems.mike_core, 1), new ComparableStack(ModItems.circuit, 8, EnumCircuitType.CONTROLLER_ADVANCED), }, 600);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_volcano, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 32), new OreDictStack(TI.plate(), 16), new ComparableStack(ModBlocks.det_nuke, 4), new OreDictStack(U238.block(), 32), new ComparableStack(ModItems.circuit, 6, EnumCircuitType.CAPACITOR_TANTALIUM), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED) }, 600);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_mirv, 1), new AStack[] { new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 24), new OreDictStack(TI.plate(), 16), new ComparableStack(ModItems.warhead_mirvlet, 7), new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 2), new ComparableStack(ModItems.hull_small_steel, 1), new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER_ADVANCED), }, 600);
		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_n2, 1), new AStack[] {new ComparableStack(ModItems.seg_15, 1), new OreDictStack(STEEL.plate(), 8), new OreDictStack(TI.plate(), 20), new ComparableStack(ModBlocks.det_charge, 24), new ComparableStack(Blocks.REDSTONE_BLOCK, 12), new OreDictStack(MAGTUNG.dust(), 6), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CONTROLLER), },400);

		makeRecipe(new ComparableStack(ModItems.missile_soyuz0, 1), new AStack[] {
				new OreDictStack(TI.shell(), 32),
				new OreDictStack(RUBBER.ingot(), 64),
				new ComparableStack(ModItems.rocket_fuel, 64),
				new ComparableStack(ModItems.thruster_small, 12),
				new ComparableStack(ModItems.thruster_medium, 12),
				new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CONTROLLER.ordinal()),
                new ComparableStack(ModItems.low_density_element, 32)
		},1200);
		makeRecipe(new ComparableStack(ModItems.missile_soyuz_lander, 1), new AStack[] {
                new ComparableStack(ModItems.rocket_fuel, 10),
                new ComparableStack(ModItems.thruster_small, 3),
                new ComparableStack(ModItems.tank_steel, 2),
                new ComparableStack(ModItems.circuit, 4, EnumCircuitType.BASIC),
                new OreDictStack(ANY_RUBBER.ingot(), 32),
                new ComparableStack(ModItems.hull_big_aluminium, 2),
                new ComparableStack(ModItems.sphere_steel, 1),
                new ComparableStack(ModItems.low_density_element, 12), }, 600);

		makeRecipe(new ComparableStack(ModItems.upgrade_overdrive_1, 1), new AStack[] {
				new ComparableStack(ModItems.upgrade_speed_3, 1),
				new ComparableStack(ModItems.upgrade_effect_3, 1),
				new OreDictStack(BIGMT.ingot(), 16),
				new OreDictStack(ANY_HARDPLASTIC.ingot(), 16),
				new ComparableStack(ModItems.circuit, 16, EnumCircuitType.ADVANCED.ordinal()),
		}, 200);
		makeRecipe(new ComparableStack(ModItems.upgrade_overdrive_2, 1), new AStack[] {
				new ComparableStack(ModItems.upgrade_overdrive_1, 1),
				new ComparableStack(ModItems.upgrade_speed_3, 1),
				new ComparableStack(ModItems.upgrade_effect_3, 1),
				new OreDictStack(BIGMT.ingot(), 16),
//				new ComparableStack(ModItems.ingot_cft, 8),
				new ComparableStack(ModItems.circuit, 16, EnumCircuitType.CAPACITOR_BOARD.ordinal()),
		}, 300);
		makeRecipe(new ComparableStack(ModItems.upgrade_overdrive_3, 1), new AStack[] {
				new ComparableStack(ModItems.upgrade_overdrive_2, 1),
				new ComparableStack(ModItems.upgrade_speed_3, 1),
				new ComparableStack(ModItems.upgrade_effect_3, 1),
				new OreDictStack(ANY_BISMOID.ingot(), 16),
//				new ComparableStack(ModItems.ingot_cft, 16),
				new ComparableStack(ModItems.circuit, 16, EnumCircuitType.BISMOID.ordinal()),
		}, 500);

		makeRecipe(new ComparableStack(ModItems.fusion_shield_tungsten, 1), new AStack[] { new OreDictStack(W.block(), 32), new OreDictStack(OreDictManager.getReflector(), 64), new OreDictStack(OreDictManager.getReflector(), 32) }, 600);
		makeRecipe(new ComparableStack(ModItems.fusion_shield_desh, 1), new AStack[] { new OreDictStack(DESH.block(), 16), new OreDictStack(CO.block(), 16), new OreDictStack(BIGMT.plate(), 64), new OreDictStack(BIGMT.plate(), 32) }, 600);
		makeRecipe(new ComparableStack(ModItems.fusion_shield_chlorophyte, 1), new AStack[] { new OreDictStack(W.block(), 16), new ComparableStack(ModBlocks.block_dura_steel, 16), new OreDictStack(OreDictManager.getReflector(), 48), new ComparableStack(ModItems.powder_chlorophyte, 48) }, 600);
		makeRecipe(new ComparableStack(ModItems.fusion_shield_vaporwave, 1), new AStack[] { new OreDictStack(DNT.block(), 16), new ComparableStack(ModBlocks.block_euphemium_cluster, 16), new OreDictStack(SBD.dust(), 64), new ComparableStack(ModItems.powder_spark_mix, 48), new ComparableStack(ModItems.powder_daffergon, 48) }, 4800);

		makeRecipe(new ComparableStack(ModBlocks.machine_fensu, 1), new AStack[] { new ComparableStack(ModItems.ingot_electronium, 32), new ComparableStack(ModBlocks.machine_dineutronium_battery, 16), new OreDictStack(ANY_RESISTANTALLOY.heavyComp(), 16), new OreDictStack(STAR.block(), 64), new ComparableStack(ModBlocks.machine_transformer_dnt, 8), new ComparableStack(ModItems.coil_magnetized_tungsten, 24), new ComparableStack(ModItems.powder_magic, 64), new ComparableStack(ModItems.plate_dineutronium, 24), new ComparableStack(ModItems.ingot_u238m2), new OreDictStack(FIBER.ingot(), 64), new ComparableStack(ModItems.ingot_radspice, 64) }, 1200);
		makeRecipe(new ComparableStack(ModBlocks.struct_iter_core, 1), new AStack[] {
				new OreDictStack(ANY_RESISTANTALLOY.plateWelded(), 6),
				new OreDictStack(W.plateWelded(), 6),
				new OreDictStack(OreDictManager.getReflector(), 12),
				new ComparableStack(ModItems.coil_advanced_alloy, 12),
				new OreDictStack(ANY_PLASTIC.ingot(), 8),
				new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CONTROLLER_ADVANCED.ordinal()),
		}, 600);
		makeRecipe(new ComparableStack(ModBlocks.machine_large_turbine, 1), new AStack[] {
				new ComparableStack(ModItems.generator_steel, 1),
				new OreDictStack(STEEL.plate(), 12),
				new OreDictStack(RUBBER.ingot(), 4),
				new ComparableStack(ModItems.turbine_titanium, 3),
				new OreDictStack(GOLD.wireDense(), 6),
				new OreDictStack(DURA.pipe(), 3),
				new OreDictStack(STEEL.pipe(), 4),
				new ComparableStack(ModItems.circuit, 4, EnumCircuitType.BASIC.ordinal()),
		}, 200);

		makeRecipe(new ComparableStack(ModItems.pellet_chlorophyte, 2), new AStack[] { new ComparableStack(ModItems.powder_chlorophyte, 1), new OreDictStack(PB.nugget(), 12), }, 50);
		makeRecipe(new ComparableStack(ModItems.pellet_mercury, 2), new AStack[] { new ComparableStack(ModItems.nugget_mercury, 1), new OreDictStack(PB.nugget(), 12), }, 50);
		makeRecipe(new ComparableStack(ModItems.pellet_meteorite, 2), new AStack[] { new ComparableStack(ModItems.powder_meteorite, 1), new OreDictStack(PB.nugget(), 12), }, 50);
		makeRecipe(new ComparableStack(ModItems.pellet_canister, 2), new AStack[] { new OreDictStack(IRON.ingot(), 3), }, 50);

		makeRecipe(new ComparableStack(ModItems.mp_warhead_15_balefire, 1), new AStack[] {new ComparableStack(ModItems.seg_15, 1), new OreDictStack(OreDictManager.getReflector(), 16), new ComparableStack(ModItems.powder_magic, 6), new ComparableStack(ModItems.egg_balefire_shard, 4), new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 8), new ComparableStack(ModItems.circuit, 8, EnumCircuitType.ADVANCED), }, 60);

		makeRecipe(new ComparableStack(ModBlocks.machine_cyclotron, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_desh_battery, 3),
				new OreDictStack(ND.wireDense(), 32),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(STEEL.plate(), 32),
				new OreDictStack(AL.plate(), 32),
				new OreDictStack(ANY_PLASTIC.ingot(), 24),
				new OreDictStack(RUBBER.ingot(), 24),
				new OreDictStack(CU.plateCast(), 8),
				new ComparableStack(ModItems.circuit, 16, EnumCircuitType.BASIC.ordinal())
		}, 600);

		makeRecipe(new ComparableStack(ModBlocks.uu_gigafactory, 1), new AStack[] { 
				new ComparableStack(ModBlocks.machine_orbus, 1), 
				new ComparableStack(ModBlocks.machine_saturnite_battery, 2), 
				new ComparableStack(ModBlocks.hadron_analysis, 16), 
				new ComparableStack(ModItems.laser_crystal_cmb, 1),
				new ComparableStack(ModItems.powder_nitan_mix, 8),
				new OreDictStack(AL.heavyComp(), 1),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CONTROLLER_QUANTUM), }, 900);

		makeRecipe(new ComparableStack(ModItems.gun_zomg, 1), new AStack[] { new ComparableStack(ModItems.crystal_xen, 2), new ComparableStack(ModItems.singularity_counter_resonant, 1), new ComparableStack(ModItems.mechanism_special, 3), new ComparableStack(ModItems.plate_paa, 12), new OreDictStack(OreDictManager.getReflector(), 8), new ComparableStack(ModItems.coil_magnetized_tungsten, 5), new ComparableStack(ModItems.powder_magic, 4), new OreDictStack(ASBESTOS.ingot(), 8) }, 200);
		
		makeRecipe(new ComparableStack(ModBlocks.machine_industrial_generator, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_coal_off, 2),
				new ComparableStack(ModBlocks.machine_boiler_off, 2),
				new ComparableStack(ModBlocks.machine_large_turbine, 1),
				new ComparableStack(ModBlocks.machine_transformer, 1),
				new ComparableStack(ModBlocks.steel_scaffold, 20),
				new OreDictStack(STEEL.ingot(), 12),
				new OreDictStack(PB.plate(), 8),
				new OreDictStack(AL.plate(), 12),
				new ComparableStack(ModItems.pipes_steel, 1)
			}, 200);
		makeRecipe(new ComparableStack(ModItems.euphemium_capacitor, 1), new AStack[] {
				new OreDictStack(NB.ingot(), 4),
				new ComparableStack(ModItems.redcoil_capacitor, 1),
				new ComparableStack(ModItems.ingot_euphemium, 4),
				new ComparableStack(ModItems.circuit, 8, EnumCircuitType.CAPACITOR_BOARD),
				new ComparableStack(ModItems.powder_nitan_mix, 18),
			}, 600);
		makeRecipe(new ComparableStack(ModBlocks.block_cap_nuka, 1), new AStack[] { new ComparableStack(ModItems.cap_nuka, 64), new ComparableStack(ModItems.cap_nuka, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap_quantum, 1), new AStack[] { new ComparableStack(ModItems.cap_quantum, 64), new ComparableStack(ModItems.cap_quantum, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap_sparkle, 1), new AStack[] { new ComparableStack(ModItems.cap_sparkle, 64), new ComparableStack(ModItems.cap_sparkle, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap_rad, 1), new AStack[] { new ComparableStack(ModItems.cap_rad, 64), new ComparableStack(ModItems.cap_rad, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap_korl, 1), new AStack[] { new ComparableStack(ModItems.cap_korl, 64), new ComparableStack(ModItems.cap_korl, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap_fritz, 1), new AStack[] { new ComparableStack(ModItems.cap_fritz, 64), new ComparableStack(ModItems.cap_fritz, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap_sunset, 1), new AStack[] { new ComparableStack(ModItems.cap_sunset, 64), new ComparableStack(ModItems.cap_sunset, 64) }, 10);
		makeRecipe(new ComparableStack(ModBlocks.block_cap_star, 1), new AStack[] { new ComparableStack(ModItems.cap_star, 64), new ComparableStack(ModItems.cap_star, 64) }, 10);
		
		makeRecipe(new ComparableStack(ModItems.ammo_75bolt, 2), new AStack[] {
				new OreDictStack(STEEL.plate(), 2),
				new OreDictStack(CU.plate(), 1),
				new ComparableStack(ModItems.primer_50, 5),
				new ComparableStack(ModItems.casing_50, 5),
				new OreDictStack(ANY_HIGHEXPLOSIVE.ingot(), 5),
				new ComparableStack(ModItems.cordite, 5),
				new OreDictStack(REDSTONE.dust(), 3)
			}, 60);
		makeRecipe(new ComparableStack(ModItems.ammo_75bolt_incendiary, 2), new AStack[] {
				new OreDictStack(STEEL.plate(), 2),
				new OreDictStack(CU.plate(), 1),
				new ComparableStack(ModItems.primer_50, 5),
				new ComparableStack(ModItems.casing_50, 5),
				new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 3),
				new ComparableStack(ModItems.cordite, 3),
				new OreDictStack(P_WHITE.ingot(), 3)
			}, 60);

		makeRecipe(new ComparableStack(ModItems.ammo_75bolt_he, 2), new AStack[] {
				new OreDictStack(STEEL.plate(), 2),
				new OreDictStack(CU.plate(), 1),
				new ComparableStack(ModItems.primer_50, 5),
				new ComparableStack(ModItems.casing_50, 5),
				new OreDictStack(ANY_PLASTICEXPLOSIVE.ingot(), 5),
				new ComparableStack(ModItems.cordite, 5),
				new ComparableStack(Items.REDSTONE, 3)
			}, 60);

		makeRecipe(new ComparableStack(ModItems.spawn_worm, 1), new AStack[] {
				new OreDictStack(W.heavyComp(), 1),
				new ComparableStack(ModItems.motor, 64),
				new ComparableStack(ModBlocks.glass_trinitite, 25),
				new OreDictStack(REDSTONE.dust(), 40),
				new OreDictStack(GOLD.wireDense(), 32),
				new OreDictStack(PO210.block(), 10),
				new ComparableStack(ModItems.plate_armor_titanium, 50),
				new ComparableStack(ModItems.coin_worm, 1)
		}, 1200);

		makeRecipe(new ComparableStack(ModBlocks.turret_chekhov, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new ComparableStack(ModItems.motor, 3),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED),
				new OreDictStack(STEEL.pipe(), 3),
				new ComparableStack(ModBlocks.crate_iron, 1),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CRT_TUBE)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_friendly, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new ComparableStack(ModItems.motor, 3),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.BASIC),
				new OreDictStack(STEEL.pipe(), 3),
				new ComparableStack(ModBlocks.crate_iron, 1),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CRT_TUBE)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_jeremy, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_saturnite_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED),
				new ComparableStack(ModItems.motor_desh, 1),
				new OreDictStack(STEEL.shell(), 3),
				new ComparableStack(ModBlocks.crate_steel, 1),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CRT_TUBE)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_tauon, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_saturnite_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new OreDictStack(ANY_PLASTIC.ingot(), 4),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED),
				new ComparableStack(ModItems.motor_desh, 1),
				new OreDictStack(CU.ingot(), 32),
				new ComparableStack(ModItems.battery_lithium, 1),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CRT_TUBE)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_richard, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_saturnite_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED),
				new OreDictStack(ANY_PLASTIC.ingot(), 2),
				new OreDictStack(STEEL.shell(), 8),
				new ComparableStack(ModBlocks.crate_steel, 1),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CRT_TUBE)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_howard, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_battery, 1),
				new OreDictStack(STEEL.ingot(), 24),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.motor_desh, 2),
				new ComparableStack(ModItems.circuit, 3, EnumCircuitType.ADVANCED),
				new OreDictStack(STEEL.pipe(), 10),
				new ComparableStack(ModBlocks.crate_steel, 1),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CRT_TUBE)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_maxwell, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_saturnite_battery, 1),
				new OreDictStack(STEEL.ingot(), 24),
				new ComparableStack(ModItems.motor, 2),
				new ComparableStack(ModItems.circuit, 2, EnumCircuitType.ADVANCED),
				new OreDictStack(STEEL.pipe(), 4),
				new ComparableStack(ModItems.magnetron, 16),
				new OreDictStack(ANY_RESISTANTALLOY.ingot(), 8),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CRT_TUBE)
		}, 200);
		makeRecipe(new ComparableStack(ModBlocks.turret_fritz, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_battery, 1),
				new OreDictStack(STEEL.ingot(), 16),
				new ComparableStack(ModItems.motor, 3),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED),
				new OreDictStack(STEEL.pipe(), 8),
				new ComparableStack(ModBlocks.barrel_steel, 1),
				new ComparableStack(ModItems.circuit, 1, EnumCircuitType.CRT_TUBE)
		}, 200);
		makeRecipe(new ComparableStack(ModItems.gun_egon, 1), new AStack[] {new ComparableStack(ModItems.mechanism_special, 4), new OreDictStack(STEEL.plate(), 16), new OreDictStack(PB.plate(), 24), new ComparableStack(ModItems.coil_advanced_torus, 32), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.CHIP_QUANTUM), new ComparableStack(ModItems.plate_polymer, 8), new ComparableStack(ModBlocks.machine_schrabidium_battery, 2), new ComparableStack(ModBlocks.machine_waste_drum, 1), new ComparableStack(ModItems.wire, 8, MAT_COPPER.id)}, 256);
		makeRecipe(new ComparableStack(ModBlocks.silo_hatch, 1), new AStack[]{new OreDictStack(STEEL.plateWelded(), 4), new ComparableStack(ModItems.plate_polymer, 4), new ComparableStack(ModItems.motor, 2), new OreDictStack(STEEL.bolt(), 16), new OreDictStack(KEY_GRAY, 4)}, 200);

		makeRecipe(new ComparableStack(ModItems.sat_gerald, 1), new AStack[] {
				new ComparableStack(ModItems.heavy_duty_element, 32),
                new ComparableStack(ModBlocks.det_nuke, 64),
                new ComparableStack(ModItems.circuit, 64, EnumCircuitType.CONTROLLER_QUANTUM),
				new ComparableStack(ModItems.coin_ufo, 1),
                new ComparableStack(ModBlocks.machine_radspice_battery, 2),
                new ComparableStack(ModBlocks.dfc_core, 1),
                new ComparableStack(ModItems.mp_thruster_15_balefire_large_rad, 1),
                new ComparableStack(ModItems.mp_thruster_15_balefire_large_rad, 1),
			}, 1200);

		makeRecipe(new ComparableStack(ModBlocks.machine_chungus, 1), new AStack[] {
				new ComparableStack(ModItems.generator_steel, 6),
				new OreDictStack(STEEL.plateWelded(), 16),
				new OreDictStack(TI.plate(), 12),
				new OreDictStack(ANY_RESISTANTALLOY.ingot(), 16),
				new ComparableStack(ModItems.turbine_tungsten, 5),
				new ComparableStack(ModItems.turbine_titanium, 3),
				new ComparableStack(ModItems.flywheel_beryllium, 1),
        new OreDictStack(GOLD.wireDense(), 48),
				new OreDictStack(DURA.pipe(), 16),
				new OreDictStack(STEEL.pipe(), 16)
		}, 600);

		makeRecipe(new ComparableStack(ModBlocks.machine_condenser_powered, 1), new AStack[] {
				new OreDictStack(STEEL.plateWelded(), 8),
				new OreDictStack(ANY_RESISTANTALLOY.plateWelded(), 4),
                new OreDictStack(CU.plate(), 16),
				new ComparableStack(ModItems.motor_desh, 3),
				new OreDictStack(STEEL.pipe(), 24),
				new NbtComparableStack(ItemFluidCanister.getFullCanister(ModForgeFluids.LUBRICANT, 4))
		}, 600);
		
		makeRecipe(new ComparableStack(ModBlocks.machine_silex, 1), new AStack[] {
				new ComparableStack(ModBlocks.glass_quartz, 16),
				new OreDictStack(STEEL.plateCast(), 8),
				new OreDictStack(DESH.ingot(), 4),
				new OreDictStack(RUBBER.ingot(), 8),
				new OreDictStack(STEEL.pipe(), 8),
				new OreDictStack(DIAMOND.crystal(), 1)
		}, 400);

		makeRecipe(new ComparableStack(ModBlocks.machine_fel, 1), new AStack[] {
				new ComparableStack(ModBlocks.machine_saturnite_battery, 1),
				new OreDictStack(ALLOY.wireDense(), 64),
				new OreDictStack(STEEL.plateCast(), 12),
				new OreDictStack(ANY_PLASTIC.ingot(), 16),
				new ComparableStack(ModItems.circuit, 16, EnumCircuitType.CAPACITOR),
				new ComparableStack(ModItems.circuit, 4, EnumCircuitType.BASIC)
		}, 400);

		makeRecipe(new ComparableStack(ModBlocks.rbmk_blank, 1), new AStack[] {
				new ComparableStack(ModBlocks.concrete_asbestos, 4),
				new OreDictStack(STEEL.plateCast(), 4),
				new OreDictStack(CU.ingot(), 4),
				new ComparableStack(ModItems.plate_polymer, 4)
		}, 100);

		makeRecipe(new ComparableStack(ModItems.multitool_hit, 1), new AStack[] {
				new OreDictStack(ANY_RESISTANTALLOY.ingot(), 4),
				new OreDictStack(STEEL.plate(), 4),
				new OreDictStack(GOLD.wire(), 12),
				new ComparableStack(ModItems.motor, 4),
				new ComparableStack(ModItems.circuit, 16, EnumCircuitType.CAPACITOR_BOARD)
		}, 100);

		makeRecipe(new ComparableStack(ModBlocks.machine_bat9000, 1), new AStack[] {new OreDictStack(STEEL.plate(), 16), new OreDictStack(TI.plateWelded(), 2), new ComparableStack(ModBlocks.steel_scaffold, 16), new OreDictStack(ANY_TAR.any(), 16), },150);
		makeRecipe(new ComparableStack(ModBlocks.machine_orbus, 1), new AStack[] {new OreDictStack(STEEL.ingot(), 12), new OreDictStack(ANY_RESISTANTALLOY.plateWelded(), 8), new OreDictStack(BIGMT.plate(), 12), new ComparableStack(ModItems.coil_advanced_alloy, 12), new ComparableStack(ModItems.battery_sc_polonium, 1) }, 200);

		makeRecipe(new ComparableStack(ModBlocks.large_vehicle_door, 1), new AStack[]{new OreDictStack(STEEL.plateCast(), 16), new ComparableStack(ModItems.plate_polymer, 4), new ComparableStack(ModItems.motor, 4), new OreDictStack(DURA.bolt(), 16), new OreDictStack("dyeGreen", 4)}, 400);
		makeRecipe(new ComparableStack(ModBlocks.water_door, 1), new AStack[]{new OreDictStack(STEEL.plate(), 16), new OreDictStack(DURA.bolt(), 4), new OreDictStack("dyeRed", 1)}, 200);
		makeRecipe(new ComparableStack(ModBlocks.qe_containment, 1), new AStack[]{new OreDictStack(STEEL.plateCast(), 4), new OreDictStack(ALLOY.plate(), 4), new ComparableStack(ModItems.plate_polymer, 8), new ComparableStack(ModItems.motor, 2), new OreDictStack(DURA.bolt(), 32), new OreDictStack("dyeBlack", 4)}, 400);
		makeRecipe(new ComparableStack(ModBlocks.qe_sliding_door, 1), new AStack[]{new OreDictStack(STEEL.plate(), 4), new ComparableStack(ModItems.plate_polymer, 4), new ComparableStack(ModItems.motor, 2), new OreDictStack(DURA.bolt(), 4), new OreDictStack("dyeWhite", 4), new ComparableStack(Blocks.GLASS, 4)}, 200);
		makeRecipe(new ComparableStack(ModBlocks.fire_door, 1), new AStack[]{new OreDictStack(PB.plate(), 12), new OreDictStack(STEEL.plate(), 36), new OreDictStack(ASBESTOS.ingot(), 12), new ComparableStack(ModItems.plate_polymer, 6), new OreDictStack(STEEL.block(), 4), new ComparableStack(ModItems.motor, 4), new ComparableStack(ModItems.bolt, 6, MAT_DURA.id), new OreDictStack(KEY_RED, 8)}, 500);
		makeRecipe(new ComparableStack(ModBlocks.small_hatch, 1), new AStack[]{new OreDictStack(PB.plate(), 4), new OreDictStack(STEEL.plate(), 8), new OreDictStack(ALLOY.plate(), 2), new ComparableStack(ModItems.bolt, 1, MAT_DURA.id), new ComparableStack(ModBlocks.brick_concrete, 1), new ComparableStack(ModBlocks.ladder_red, 1)}, 200);
		makeRecipe(new ComparableStack(ModBlocks.round_airlock_door, 1), new AStack[]{new OreDictStack(PB.plate(), 16), new OreDictStack(STEEL.plate(), 32), new OreDictStack(ALLOY.plate(), 12), new ComparableStack(ModItems.plate_polymer, 12), new OreDictStack(STEEL.block(), 6), new ComparableStack(ModItems.motor, 6), new ComparableStack(ModItems.bolt, 12, MAT_DURA.id), new OreDictStack(KEY_GRAY, 4)}, 500);
		makeRecipe(new ComparableStack(ModBlocks.secure_access_door, 1), new AStack[]{new OreDictStack(STEEL.plateCast(), 12), new OreDictStack(ALLOY.plate(), 16), new ComparableStack(ModItems.plate_polymer, 8), new ComparableStack(ModItems.motor, 4), new OreDictStack(DURA.bolt(), 32), new OreDictStack("dyeCyan", 8)}, 400);
		makeRecipe(new ComparableStack(ModBlocks.sliding_seal_door, 1), new AStack[]{new OreDictStack(STEEL.plate(), 12), new ComparableStack(ModItems.plate_polymer, 4), new ComparableStack(ModItems.motor, 2), new OreDictStack(DURA.bolt(), 4), new OreDictStack("dyeWhite", 2)}, 200);
		makeRecipe(new ComparableStack(ModBlocks.sliding_gate_door, 1), new AStack[]{new OreDictStack(PB.plate(), 4), new OreDictStack(STEEL.plate(), 12), new OreDictStack(ALLOY.plate(), 4), new ComparableStack(ModItems.plate_polymer, 2), new OreDictStack(STEEL.block(), 1), new ComparableStack(ModItems.motor, 2), new ComparableStack(ModItems.bolt, 2, MAT_DURA.id), new OreDictStack(KEY_WHITE, 2)}, 500);
		makeRecipe(new ComparableStack(ModBlocks.transition_seal, 1), new AStack[]{
				new ComparableStack(ModBlocks.cmb_brick_reinforced, 16),
				new OreDictStack(STEEL.plate(), 64),
				new OreDictStack(ALLOY.plate(), 40),
				new OreDictStack(ANY_RUBBER.ingot(), 36),
				new OreDictStack(STEEL.block(), 24),
				new ComparableStack(ModItems.motor_desh, 16),
				new OreDictStack(DURA.bolt(), 16),
				new OreDictStack(KEY_YELLOW, 4)
		}, 1200);
		makeRecipe(new ComparableStack(ModBlocks.control_panel_custom, 1), new AStack[]{new ComparableStack(ModItems.circuit, 1, EnumCircuitType.ADVANCED), new OreDictStack(STEEL.block(), 1), new ComparableStack(ModItems.wire, 24, MAT_COPPER.id), new ComparableStack(ModBlocks.pole_top)}, 100);
		makeRecipe(new ComparableStack(ModBlocks.railgun_plasma, 1), new AStack[]{new OreDictStack(STEEL.plate(), 24), new ComparableStack(ModItems.hull_big_steel, 2), new ComparableStack(ModItems.hull_small_steel, 6), new ComparableStack(ModItems.pipes_steel, 2), new ComparableStack(ModBlocks.machine_schrabidium_battery, 1), new ComparableStack(ModItems.coil_copper, 16), new ComparableStack(ModItems.coil_copper_torus, 8), new ComparableStack(ModItems.plate_desh, 4), new ComparableStack(ModItems.circuit, 4, EnumCircuitType.ADVANCED), new ComparableStack(ModItems.circuit, 2, EnumCircuitType.CAPACITOR), new OreDictStack(ANY_PLASTIC.ingot(), 4)}, 500);
		
		/// HIDDEN ///
		hidden.add(new ComparableStack(ModBlocks.machine_radgen, 1));

		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.shapes.contains(MaterialShapes.CASTPLATE) && mat.shapes.contains(MaterialShapes.HEAVY_COMPONENT)) {
				makeRecipe(new ComparableStack(ModItems.heavy_component, 1, mat.id), new AStack[] { new OreDictStack(MaterialShapes.CASTPLATE.name() + mat.names[0], 64), new OreDictStack(MaterialShapes.CASTPLATE.name() + mat.names[0], 64), new OreDictStack(MaterialShapes.CASTPLATE.name() + mat.names[0], 64), new OreDictStack(MaterialShapes.CASTPLATE.name() + mat.names[0], 64) }, 12_000);
			}
		}
	}
	
	public static void makeRecipe(ComparableStack out, AStack[] in, int duration) {

		if(out == null || Item.REGISTRY.getNameForObject(out.item) == null) {
			MainRegistry.logger.error("Canceling assembler registration, item was null!");
			return;
		}

		recipes.put(out, in);
		time.put(out, duration);
	}

	public static void removeRecipe(ComparableStack out){
		if(out == null || Item.REGISTRY.getNameForObject(out.item) == null) {
			MainRegistry.logger.error("Canceling assembler recipe removal, item was null!");
			return;
		}
		recipes.remove(out);
		time.remove(out);
		recipeList.remove(out);
	}

	public static void loadRecipesFromConfig() {
		itemRegistry = GameRegistry.findRegistry(Item.class);
		blockRegistry = GameRegistry.findRegistry(Block.class);
		
		File recipeConfig = new File(MainRegistry.proxy.getDataDir().getPath() + "/config/hbm/assemblerConfig.cfg");
		if (!recipeConfig.exists())
			try {
				recipeConfig.getParentFile().mkdirs();
				FileWriter write = new FileWriter(recipeConfig);
				write.write("# Format: time;itemName,meta,amount|nextItemName,meta,amount;productName,meta,amount\n"
						  + "# One line per recipe.\n"
						  + "# For an oredict input item, replace the mod id with oredict, like oredict:plateSteel. These do not require metatdata\n"
						  + "# Example for iron plates: 30;minecraft:iron_ingot,0,3;oredict:plateIron,2\n"
						  + "# For an NBT item, use a 4th item parameter with the nbt string of the tag.\n"
						  + "# The NBT string format is the same as used in commands\n"
						  + "# Example for turning kerosene canisters into steel plates:\n"
						  + "# 20;hbm:canister_fuel,0,2,{HbmFluidKey:{FluidName:\"kerosene\",Amount:1000}};hbm:plate_steel,0,32\n"
						  + "#\n"
						  + "# To remove a recipe, use the format: \n"
						  + "# remove hbm:plate_iron,0,2\n"
						  + "# This will remove any recipe with the output of two iron plates");
				addConfigRecipes(write);
				write.close();
				
			} catch (IOException e) {
				MainRegistry.logger.log(Level.ERROR, "ERROR: Could not create config file: " + recipeConfig.getAbsolutePath());
				e.printStackTrace();
				return;
			}
		
		
		
		BufferedReader read = null;
		try {
			read = new BufferedReader(new FileReader(recipeConfig));
			String currentLine = null;
			int lineCount = 0;
			
			while((currentLine = read.readLine()) != null){
				lineCount ++;
				if(currentLine.startsWith("#") || currentLine.isEmpty())
					continue;
				if(currentLine.startsWith("remove"))
					parseRemoval(currentLine, lineCount);
				else
					parseRecipe(currentLine, lineCount);
			}
		} catch (FileNotFoundException e) {
			MainRegistry.logger.log(Level.ERROR, "Could not find assembler config file! This should never happen.");
			e.printStackTrace();
		} catch (IOException e){
			MainRegistry.logger.log(Level.ERROR, "Error reading assembler config!");
			e.printStackTrace();
		} finally {
			if(read != null)
				try {
					read.close();
				} catch (IOException e) {}
		}
		
	}
	
	public static void parseRemoval(String currentLine, int line){
		String[] parts = currentLine.split(" ");
		if(parts.length != 2){
			MainRegistry.logger.log(Level.WARN, "Could not parse assembler recipe removal on line " + line + ": does not have two parts. Skipping...");
			return;
		}
		AStack stack = parseAStack(parts[1], 64);
		if(stack == null){
			MainRegistry.logger.log(Level.WARN, "Could not parse assembler output itemstack from \"" + parts[1] + "\" on line " + line + ". Skipping...");
			return;
		}
		if(!(stack instanceof ComparableStack)){
			MainRegistry.logger.log(Level.WARN, "Oredict stacks are not allowed as assembler outputs! Line number: " + line + " Skipping...");
			return;
		}
		ComparableStack cStack = (ComparableStack) stack;
		recipes.remove(cStack);
		time.remove(cStack);
		recipeList.remove(cStack);
	}

	private static void parseRecipe(String currentLine, int line) {
		String[] parts = currentLine.split(";");
		if(parts.length != 3){
			MainRegistry.logger.log(Level.WARN, "Could not parse assembler recipe on line " + line + ": does not have three parts. Skipping...");
			return;
		}
		int recipeTime = 0;
		try {
			recipeTime = Integer.parseInt(parts[0]);
		} catch (NumberFormatException e){
			MainRegistry.logger.log(Level.WARN, "Could not parse assembler process time from \"" + parts[0] + "\" on line " + line + ". Skipping...");
			return;
		}
		List<AStack> input = new ArrayList<>();
		for(String s : parts[1].split("\\|")){
			AStack stack = parseAStack(s, 12*64);
			if(stack == null){
				MainRegistry.logger.log(Level.WARN, "Could not parse assembler input itemstack from \"" + s + "\" on line " + line + ". Skipping...");
				return;
			}
			input.add(stack);
		}
		AStack output = parseAStack(parts[2], 64);
		if(output == null){
			MainRegistry.logger.log(Level.WARN, "Could not parse assembler output itemstack from \"" + parts[2] + "\" on line " + line + ". Skipping...");
			return;
		}
		if(!(output instanceof ComparableStack)){
			MainRegistry.logger.log(Level.WARN, "Oredict stacks are not allowed as assembler outputs! Line number: " + line + " Skipping...");
			return;
		}
		if(recipes.containsKey(output)){
			MainRegistry.logger.log(Level.WARN, "Found duplicate assembler recipe outputs! This is not allowed! Line number: " + line + " Skipping...");
		}
		recipes.put((ComparableStack) output, input.toArray(new AStack[0]));
		time.put((ComparableStack) output, recipeTime);
		recipeList.add((ComparableStack) output);
	}
	
	//The whole point of these two methods below is to ignore the part inside braces for nbt tags.
	//I'm sure there's a cleaner way to do this, but I'm not going to spend more time trying to figure it out.
	private static String readSplitPart(int idx, String s){
		StringBuilder build = new StringBuilder();
		int braceCount = 0;
		for(int i = idx; i < s.length(); i ++){
			char c = s.charAt(i);
			if(c == '{'){
				braceCount ++;
			} else if(c == '}'){
				braceCount --;
			}
			if(braceCount == 0 && (c == '|' || c == ',' || c == ';'))
				break;
			build.append(c);
		}
		if(build.length() == 0)
			return null;
		return build.toString();
	}
	
	private static String[] splitStringIgnoreBraces(String s){
		List<String> list = new ArrayList<>();
		int idx = 0;
		while(idx < s.length()){
			String part = readSplitPart(idx, s);
			if(part != null)
				list.add(part);
			else
				break;
			if(list.size() >= 4)
				break;
			idx += part.length()+1;
		}
		return list.toArray(new String[0]);
	}

	private static AStack parseAStack(String s, int maxSize){
		String[] parts = splitStringIgnoreBraces(s);
		if(parts.length == 3 || parts.length == 4){
			Block block = null;
			Item item = itemRegistry.getValue(new ResourceLocation(parts[0]));
			if(item == null)
				block = blockRegistry.getValue(new ResourceLocation(parts[0]));
			if(item == null && block == null){
				MainRegistry.logger.log(Level.WARN, "Could not parse item or block from \"" + parts[0] + "\", probably isn't a registered item. Skipping...");
				return null;
			}
			int meta = 0;
			try {
				meta = Integer.parseInt(parts[1]);
			} catch (NumberFormatException e) {
				MainRegistry.logger.log(Level.WARN, "Could not parse item metadata from \"" + parts[1] + "\". Skipping...");
				return null;
			}
			if(meta < 0){
				MainRegistry.logger.log(Level.WARN, "Bad item meta: " + meta + ". Skipping...");
				return null;
			}
			int amount = 0;
			try {
				amount = Integer.parseInt(parts[2]);
			} catch (NumberFormatException e){
				MainRegistry.logger.log(Level.WARN, "Could not parse item amount from \"" + parts[2] + "\". Skipping...");
				return null;
			}
			if(amount < 0 || amount > maxSize){
				MainRegistry.logger.log(Level.WARN, "Bad item amount: " + amount + ". Skipping...");
				return null;
			}
			if(parts.length == 4){
				String name = parts[3];
				name.trim();
				NBTTagCompound tag = parseNBT(name);
				if(tag == null){
					MainRegistry.logger.log(Level.WARN, "Failed to parse NBT tag: " + parts[3] + ". Skipping...");
					return null;
				}
				ItemStack stack;
				if(item == null)
					stack = new ItemStack(block, amount, meta);
				else
					stack = new ItemStack(item, amount, meta);
				stack.setTagCompound(tag);
				return new NbtComparableStack(stack);
			} else {
				if(item == null)
					return new ComparableStack(block, amount, meta);
				return new ComparableStack(item, amount, meta);
			}
		}
		if(parts.length == 2){
			String[] ore = parts[0].split(":");
			if(ore.length == 2 && ore[0].equals("oredict")){
				String name = ore[1];
				int amount = 0;
				try {
					amount = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e){
					MainRegistry.logger.log(Level.WARN, "Could not parse item amount from \"" + parts[1] + "\". Skipping...");
					return null;
				}
				if(amount < 0 || amount > 12*64){
					MainRegistry.logger.log(Level.WARN, "Bad item amount: " + amount + ". Skipping...");
					return null;
				}
				return new OreDictStack(name, amount);
			} else {
				MainRegistry.logger.log(Level.WARN, "Could not parse ore dict name from \"" + parts[0] + "\". Skipping...");
			}
		}
		return null;
	}
	
	private static NBTTagCompound parseNBT(String json){
		try {
			return JsonToNBT.getTagFromJson(json);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private static void addConfigRecipes(FileWriter write) throws IOException {
			write.write("\n");
	}
}