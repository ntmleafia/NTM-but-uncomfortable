package com.hbm.inventory;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.MachineConfig;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.Spaghetti;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.items.ModItems;
import com.hbm.items.ModItems.Materials.Ingots;
import com.hbm.items.ModItems.Materials.Nuggies;
import com.hbm.items.ModItems.Materials.Powders;
import com.hbm.items.machine.ItemChemistryTemplate;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.hbm.inventory.OreDictManager.*;

//TODO: clean this shit up
@Spaghetti("everything")
public class ChemplantRecipes {

	public static HashMap<Integer, AStack[]> recipeItemInputs = new HashMap<>();
	public static HashMap<Integer, FluidStack[]> recipeFluidInputs = new HashMap<>();
	public static HashMap<Integer, AStack[]> recipeItemOutputs = new HashMap<>();
	public static HashMap<Integer, FluidStack[]> recipeFluidOutputs = new HashMap<>();
	public static HashMap<Integer, Integer> recipeDurations = new HashMap<>();
	public static HashMap<Integer, String> recipeNames = new HashMap<>();

	public static void registerRecipes() {
		if(MachineConfig.chemplantKeepOilProcessing){
			makeRecipe(0, "FP_HEAVYOIL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.HEAVYOIL, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.BITUMEN, 300), new FluidStack(ModForgeFluids.SMEAR, 700) }, 50);
			
			makeRecipe(10, "FP_SMEAR", null, new FluidStack[]{ new FluidStack(ModForgeFluids.SMEAR, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.HEATINGOIL, 600), new FluidStack(ModForgeFluids.LUBRICANT, 400) }, 50);
			
			makeRecipe(20, "FP_NAPHTHA", null, new FluidStack[]{ new FluidStack(ModForgeFluids.NAPHTHA, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.HEATINGOIL, 400), new FluidStack(ModForgeFluids.DIESEL, 600) }, 50);
			
			makeRecipe(30, "FP_LIGHTOIL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.LIGHTOIL, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.DIESEL, 400), new FluidStack(ModForgeFluids.KEROSENE, 600) }, 50);
		}
		makeRecipe(40, "FR_REOIL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.SMEAR, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.RECLAIMED, 800) }, 30);
		
		makeRecipe(50, "FR_PETROIL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.RECLAIMED, 800), new FluidStack(ModForgeFluids.LUBRICANT, 200) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROIL, 1000) }, 30);
		
		makeRecipe(60, "FC_BITUMEN", null, new FluidStack[]{ new FluidStack(ModForgeFluids.BITUMEN, 1200), new FluidStack(ModForgeFluids.STEAM, 2400) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.OIL, 1000), new FluidStack(ModForgeFluids.PETROLEUM, 200) }, 100);
		
		makeRecipe(70, "FC_I_NAPHTHA", null, new FluidStack[]{ new FluidStack(ModForgeFluids.SMEAR, 1400), new FluidStack(FluidRegistry.WATER, 800) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.NAPHTHA, 800) }, 150);
		
		makeRecipe(80, "FC_GAS_PETROLEUM", null, new FluidStack[]{ new FluidStack(ModForgeFluids.GAS, 1800), new FluidStack(FluidRegistry.WATER, 1200) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROLEUM, 800) }, 100);
		
		makeRecipe(90, "FC_DIESEL_KEROSENE", null, new FluidStack[]{ new FluidStack(ModForgeFluids.DIESEL, 1200), new FluidStack(ModForgeFluids.STEAM, 2000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.KEROSENE, 400) }, 150);
		
		makeRecipe(100, "FC_KEROSENE_PETROLEUM", null, new FluidStack[]{ new FluidStack(ModForgeFluids.KEROSENE, 1400), new FluidStack(ModForgeFluids.STEAM, 2000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROLEUM, 800) }, 150);
		
		makeRecipe(110, "CC_OIL", new AStack[] { new OreDictStack(COAL.dust(), 8), new ComparableStack(ModItems.oil_tar, 4) }, new FluidStack[]{ new FluidStack(ModForgeFluids.OIL, 600), new FluidStack(ModForgeFluids.STEAM, 1400) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.OIL, 2000) }, 150);
		
		makeRecipe(120, "CC_I", new AStack[] { new OreDictStack(COAL.dust(), 6), new ComparableStack(ModItems.oil_tar, 4) }, new FluidStack[]{ new FluidStack(ModForgeFluids.SMEAR, 800), new FluidStack(FluidRegistry.WATER, 1800) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.SMEAR, 1600) }, 200);
		
		makeRecipe(130, "CC_HEATING", new AStack[] { new OreDictStack(COAL.dust(), 6), new ComparableStack(ModItems.oil_tar, 4) }, new FluidStack[]{ new FluidStack(ModForgeFluids.HEATINGOIL, 800), new FluidStack(ModForgeFluids.STEAM, 2000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.HEATINGOIL, 1800) }, 250);
		
		makeRecipe(140, "CC_HEAVY", new AStack[] { new OreDictStack(COAL.dust(), 8), new ComparableStack(ModItems.oil_tar, 4) }, new FluidStack[]{ new FluidStack(ModForgeFluids.HEAVYOIL, 600), new FluidStack(FluidRegistry.WATER, 1400) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.HEAVYOIL, 1800) }, 200);
		
		makeRecipe(150, "CC_NAPHTHA", new AStack[] { new OreDictStack(COAL.dust(), 8), new ComparableStack(ModItems.oil_tar, 4) }, new FluidStack[]{ new FluidStack(ModForgeFluids.NAPHTHA, 1200), new FluidStack(ModForgeFluids.STEAM, 2400) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.NAPHTHA, 2000) }, 300);
		
		makeRecipe(160, "SF_OIL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.OIL, 350) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(170, "SF_HEAVYOIL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.HEAVYOIL, 250) }, new AStack[] { new ComparableStack(ModItems.oil_tar, 2) }, null, 20);
		
		makeRecipe(180, "SF_SMEAR", null, new FluidStack[]{ new FluidStack(ModForgeFluids.SMEAR, 200) }, new AStack[] { new ComparableStack(ModItems.oil_tar, 2) }, null, 20);
		
		makeRecipe(190, "SF_HEATINGOIL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.HEATINGOIL, 100) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(200, "SF_RECLAIMED", null, new FluidStack[]{ new FluidStack(ModForgeFluids.RECLAIMED, 200) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(210, "SF_PETROIL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROIL, 250) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(220, "SF_LUBRICANT", null, new FluidStack[]{ new FluidStack(ModForgeFluids.LUBRICANT, 250) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(230, "SF_NAPHTHA", null, new FluidStack[]{ new FluidStack(ModForgeFluids.NAPHTHA, 300) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(240, "SF_DIESEL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.DIESEL, 400) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(250, "SF_LIGHTOIL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.LIGHTOIL, 450) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(260, "SF_KEROSENE", null, new FluidStack[]{ new FluidStack(ModForgeFluids.KEROSENE, 550) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(270, "SF_GAS", null, new FluidStack[]{ new FluidStack(ModForgeFluids.GAS, 750) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(280, "SF_PETROLEUM", null, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROLEUM, 600) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(290, "SF_BIOGAS", null, new FluidStack[]{ new FluidStack(ModForgeFluids.BIOGAS, 400) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(300, "SF_BIOFUEL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.BIOFUEL, 300) }, new AStack[] { new ComparableStack(ModItems.solid_fuel, 2) }, null, 20);
		
		makeRecipe(310, "BP_BIOGAS", new AStack[] { new ComparableStack(ModItems.biomass, 16) }, null, null, new FluidStack[]{ new FluidStack(ModForgeFluids.BIOGAS, 4000) }, 200);
		
		makeRecipe(320, "BP_BIOFUEL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.BIOGAS, 1500), new FluidStack(ModForgeFluids.ETHANOL, 250) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.BIOFUEL, 1000) }, 100);
		
		makeRecipe(330, "OIL_SAND", new AStack[] { new ComparableStack(ModBlocks.ore_oil_sand, 16), new OreDictStack(KEY_OIL_TAR, 1) }, null, new AStack[] { new ComparableStack(Blocks.SAND, 16) }, new FluidStack[]{ new FluidStack(ModForgeFluids.BITUMEN, 1000) }, 200);
		
		makeRecipe(340, "ASPHALT", new AStack[] { new OreDictStack(KEY_GRAVEL, 2), new OreDictStack(KEY_SAND, 6) }, new FluidStack[]{ new FluidStack(ModForgeFluids.BITUMEN, 8000) }, new AStack[] { new ComparableStack(ModBlocks.asphalt, 16) }, null, 100);
		
		makeRecipe(350, "GNEISS_GAS", new AStack[] { new ComparableStack(ModBlocks.ore_gneiss_gas, 2) }, new FluidStack[]{ new FluidStack(ModForgeFluids.SUPERHOTSTEAM, 1000) }, new AStack[]{ new ComparableStack(ModBlocks.stone_gneiss, 2) }, new FluidStack[]{ new FluidStack(ModForgeFluids.GAS, 1000), new FluidStack(ModForgeFluids.SPENTSTEAM, 1000) }, 100);
		
		makeRecipe(360, "COOLANT", new AStack[] { new OreDictStack(KNO.dust(), 1) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 1800) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.COOLANT, 2000) }, 50);
		
		makeRecipe(370, "CRYOGEL", new AStack[] { new ComparableStack(Powders.powder_ice, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.COOLANT, 1800) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.CRYOGEL, 2000) }, 50);

		makeRecipe(371, "CO2", new AStack[] { }, new FluidStack[]{ new FluidStack(ModForgeFluids.GAS, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.CARBONDIOXIDE, 1000) }, 50);

		if(GeneralConfig.enableBabyMode) {
			makeRecipe(380, "DESH", new AStack[] { new ComparableStack(Powders.powder_desh_mix, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.LIGHTOIL, 200) }, new AStack[] { new ComparableStack(Ingots.ingot_desh, 1) }, null, 300);
		} else {
			makeRecipe(380, "DESH", new AStack[] { new ComparableStack(Powders.powder_desh_mix, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.MERCURY, 200), new FluidStack(ModForgeFluids.LIGHTOIL, 200) }, new AStack[] { new ComparableStack(Ingots.ingot_desh, 1) }, null, 300);
		}
		
		makeRecipe(390, "NITAN", new AStack[] { new ComparableStack(Powders.powder_nitan_mix, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.KEROSENE, 600), new FluidStack(ModForgeFluids.MERCURY, 200) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.NITAN, 1000) }, 50);
		
		makeRecipe(400, "PEROXIDE", null, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.ACID, 800) }, 50);
		
		makeRecipe(410, "SULFURIC_ACID", new AStack[] { new OreDictStack(S.dust()) }, new FluidStack[]{ new FluidStack(ModForgeFluids.ACID, 800) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.SULFURIC_ACID, 500) }, 50);
		
		makeRecipe(420, "NITRIC_ACID", new AStack[] { new OreDictStack(KNO.dust()) }, new FluidStack[]{ new FluidStack(ModForgeFluids.SULFURIC_ACID, 500) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.NITRIC_ACID, 500) }, 50);
		
		makeRecipe(430, "SOLVENT", null, new FluidStack[]{ new FluidStack(ModForgeFluids.NAPHTHA, 800), new FluidStack(ModForgeFluids.AROMATICS, 300) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.SOLVENT, 1000) }, 50);
		
		makeRecipe(440, "RADIOSOLVENT", null, new FluidStack[]{ new FluidStack(ModForgeFluids.SOLVENT, 500), new FluidStack(ModForgeFluids.MUD_FLUID, 500) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.RADIOSOLVENT, 1000) }, 50);
		
		makeRecipe(450, "CIRCUIT_4", new AStack[] { new ComparableStack(ModItems.circuit_red_copper, 1), new ComparableStack(ModItems.wire_gold, 4), new OreDictStack(LAPIS.dust(), 1), new OreDictStack(ANY_PLASTIC.ingot(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.ACID, 400) }, new AStack[] { new ComparableStack(ModItems.circuit_gold, 1) }, null, 200);
		
		makeRecipe(460, "CIRCUIT_5", new AStack[] { new ComparableStack(ModItems.circuit_gold, 1), new ComparableStack(ModItems.wire_schrabidium, 4), new OreDictStack(DIAMOND.dust(), 1), new OreDictStack(DESH.ingot(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.ACID, 800), new FluidStack(ModForgeFluids.PETROLEUM, 400) }, new AStack[] { new ComparableStack(ModItems.circuit_schrabidium, 1) }, null, 250);
		
		makeRecipe(470, "POLYMER", new AStack[] { new OreDictStack(COAL.gem(), 2), new OreDictStack(F.dust(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROLEUM, 600) }, new AStack[] { new ComparableStack(Ingots.ingot_polymer, 1) }, null, 100);
		
		makeRecipe(480, "BAKELITE", null, new FluidStack[]{ new FluidStack(ModForgeFluids.AROMATICS, 500), new FluidStack(ModForgeFluids.PETROLEUM, 500) }, new AStack[] { new ComparableStack(Ingots.ingot_bakelite, 1) }, null, 100);
		
		makeRecipe(490, "RUBBER", new AStack[] { new OreDictStack(S.dust(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.UNSATURATEDS, 500) }, new AStack[] { new ComparableStack(Ingots.ingot_rubber, 1) }, null, 100);
		
		makeRecipe(500, "DYNAMITE", new AStack[] { new ComparableStack(Items.SUGAR), new OreDictStack(KNO.dust()), new OreDictStack(KEY_SAND) }, new FluidStack[]{ new FluidStack(ModForgeFluids.SULFURIC_ACID, 1000) }, new AStack[] { new ComparableStack(ModItems.ball_dynamite, 2) }, null, 50);
		
		makeRecipe(510, "TNT", new AStack[] { new OreDictStack(KNO.dust(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.AROMATICS, 500) }, new AStack[] { new ComparableStack(ModItems.ball_tnt, 4) }, null, 150);

		makeRecipe(520, "C4", new AStack[] { new OreDictStack(KNO.dust(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.UNSATURATEDS, 500) }, new AStack[] { new ComparableStack(ModItems.ingot_c4, 4) }, null, 150);

		makeRecipe(530, "HEAVY_ELECTROLYSIS", null, new FluidStack[]{ new FluidStack(ModForgeFluids.HEAVYWATER, 8000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.DEUTERIUM, 400), new FluidStack(ModForgeFluids.OXYGEN, 400) }, 150);
		
		makeRecipe(540, "DEUTERIUM", new AStack[] { new OreDictStack(S.dust(), 2) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 4000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.DEUTERIUM, 500) }, 200);
		
		makeRecipe(550, "STEAM", null, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.STEAM, 1000) }, 20);
		
		makeRecipe(560, "ALGE", new AStack[] { new ComparableStack(ModItems.biomass, 4), new OreDictStack(KNO.dust(), 1), new OreDictStack(COAL.dustTiny(), 2) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 8000) }, new AStack[] { new ComparableStack(ModItems.biomass, 36) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 1000) }, 20*60);
		
		makeRecipe(570, "YELLOWCAKE", new AStack[] { new OreDictStack(U.dust(), 1), new OreDictStack(S.dust(), 2) }, new FluidStack[]{ new FluidStack(ModForgeFluids.ACID, 500) }, new AStack[] { new ComparableStack(Powders.powder_yellowcake, 1) }, null, 250);
		
		makeRecipe(580, "UF6", new AStack[] { new ComparableStack(Powders.powder_yellowcake, 1), new OreDictStack(F.dust(), 3) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.UF6, 1000) }, 100);
		
		makeRecipe(590, "PUF6", new AStack[] { new OreDictStack(PU.dust(), 1), new OreDictStack(F.dust(), 3) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.PUF6, 1000) }, 150);
		
		makeRecipe(600, "SAS3", new AStack[] { new OreDictStack(SA326.dust(), 1), new OreDictStack(S.dust(), 2) }, new FluidStack[]{ new FluidStack(ModForgeFluids.ACID, 2000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.SAS3, 1000) }, 200);
		
		makeRecipe(610, "DYN_SCHRAB", new AStack[] {new ComparableStack(ModItems.particle_higgs, 1), new OreDictStack(U.ingot(), 8), new ComparableStack(ModItems.catalyst_clay, 8) }, new FluidStack[]{ new FluidStack(ModForgeFluids.COOLANT, 1000) }, new AStack[] { new ComparableStack(ModItems.particle_empty, 1), new ComparableStack(Ingots.ingot_schrabidium, 8) }, new FluidStack[]{ new FluidStack(ModForgeFluids.MUD_FLUID, 50) }, 20*30);
		
		makeRecipe(620, "DYN_STR", new AStack[] {new ComparableStack(ModItems.particle_strange, 1), new ComparableStack(Nuggies.nugget_radspice, 8), new ComparableStack(ModItems.catalyst_clay, 8) }, new FluidStack[]{ new FluidStack(ModForgeFluids.CRYOGEL, 1000) }, new AStack[] { new ComparableStack(ModItems.particle_empty, 1), new ComparableStack(ModItems.egg_balefire, 4) }, new FluidStack[]{ new FluidStack(ModForgeFluids.MUD_FLUID, 200) }, 20*60);
		
		makeRecipe(630, "DYN_EUPH", new AStack[] {new ComparableStack(ModItems.particle_dark, 1), new OreDictStack(SA327.ingot(), 8), new ComparableStack(ModItems.catalyst_clay, 16) }, new FluidStack[]{ new FluidStack(ModForgeFluids.CRYOGEL, 2000) }, new AStack[] { new ComparableStack(ModItems.particle_empty, 1), new ComparableStack(Ingots.ingot_euphemium, 8) }, new FluidStack[]{ new FluidStack(ModForgeFluids.MUD_FLUID, 100) }, 20*60*2);
		
		makeRecipe(640, "DYN_DNT", new AStack[] {new ComparableStack(ModItems.particle_sparkticle, 1), new OreDictStack(SBD.ingot(), 8), new ComparableStack(ModItems.catalyst_clay, 32) }, new FluidStack[]{ new FluidStack(ModForgeFluids.CRYOGEL, 4000) }, new AStack[] { new ComparableStack(ModItems.particle_empty, 1), new ComparableStack(Ingots.ingot_dineutronium, 8) }, new FluidStack[]{ new FluidStack(ModForgeFluids.MUD_FLUID, 400) }, 20*60*5);
		
		makeRecipe(650, "DYN_EL", new AStack[] {new ComparableStack(ModItems.particle_digamma, 1), new OreDictStack(DNT.ingot(), 16), new ComparableStack(ModItems.catalyst_clay, 64) }, new FluidStack[]{ new FluidStack(ModForgeFluids.CRYOGEL, 8000) }, new AStack[] { new ComparableStack(ModItems.particle_empty, 1), new ComparableStack(Ingots.ingot_electronium, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.MUD_FLUID, 800) }, 20*60*10);
		
		makeRecipe(660, "CORDITE", new AStack[] {new OreDictStack(KNO.dust(), 2), new OreDictStack(KEY_PLANKS, 1), new ComparableStack(Items.SUGAR, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.HEATINGOIL, 200) }, new AStack[] { new ComparableStack(ModItems.cordite, 4) }, null, 40);
		
		makeRecipe(670, "KEVLAR", new AStack[] {new OreDictStack(KNO.dust(), 2), new OreDictStack(KEY_BRICK, 1), new OreDictStack(COAL.gem(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROLEUM, 100) }, new AStack[] { new ComparableStack(ModItems.plate_kevlar, 4) }, null, 40);
		
		makeRecipe(680, "CONCRETE", new AStack[] { new OreDictStack(KEY_GRAVEL, 8), new OreDictStack(KEY_SAND, 8) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 2000) }, new AStack[] { new ComparableStack(ModBlocks.concrete_smooth, 16) }, null, 100);
		
		makeRecipe(690, "CONCRETE_ASBESTOS", new AStack[] { new OreDictStack(KEY_GRAVEL, 2), new OreDictStack(KEY_SAND, 2), new OreDictStack(ASBESTOS.ingot(), 4) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 2000) }, new AStack[] { new ComparableStack(ModBlocks.concrete_asbestos, 16) }, null, 100);
		
		makeRecipe(700, "DUCRETE", new AStack[] { new OreDictStack(KEY_SAND, 8), new OreDictStack(U238.billet(), 2), new ComparableStack(Items.CLAY_BALL, 4) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 2000) }, new AStack[] { new ComparableStack(ModBlocks.ducrete, 8) }, null, 200);
		
		makeRecipe(710, "SOLID_FUEL", new AStack[] {new ComparableStack(ModItems.solid_fuel, 2), new OreDictStack(KNO.dust(), 1), new OreDictStack(REDSTONE.dust(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROLEUM, 200) }, new AStack[] { new ComparableStack(ModItems.rocket_fuel, 4) }, null, 200);
		
		makeRecipe(720, "ELECTROLYSIS", null, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 8000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.HYDROGEN, 800), new FluidStack(ModForgeFluids.OXYGEN, 800) }, 150);
		
		makeRecipe(730, "XENON", null, null, null, new FluidStack[]{ new FluidStack(ModForgeFluids.XENON, 50) }, 300);
		
		makeRecipe(740, "XENON_OXY", null, new FluidStack[]{ new FluidStack(ModForgeFluids.OXYGEN, 250) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.XENON, 50) }, 20);
		
		makeRecipe(750, "SATURN", new AStack[] {new OreDictStack(DURA.dust(), 1), new OreDictStack(P_RED.dust(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.ACID, 100), new FluidStack(ModForgeFluids.MERCURY, 50) }, new AStack[] { new ComparableStack(Ingots.ingot_saturnite, 2) }, null, 60);
		
		makeRecipe(760, "BALEFIRE", new AStack[] {new ComparableStack(ModItems.egg_balefire_shard, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.KEROSENE, 6000) }, new AStack[] { new ComparableStack(Powders.powder_balefire, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.BALEFIRE, 8000) }, 100);
		
		makeRecipe(770, "SCHRABIDIC", new AStack[] {new ComparableStack(ModItems.pellet_charged, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.SAS3, 8000), new FluidStack(ModForgeFluids.ACID, 6000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.SCHRABIDIC, 16000) }, 100);
		
		makeRecipe(780, "SCHRABIDATE", new AStack[] {new OreDictStack(IRON.dust(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.SCHRABIDIC, 250) }, new AStack[] { new ComparableStack(Powders.powder_schrabidate, 1) }, null, 600);
		
		makeRecipe(790, "COLTAN_CLEANING", new AStack[] {new OreDictStack(COLTAN.dust(), 2), new OreDictStack(COAL.dust(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.ACID, 250), new FluidStack(ModForgeFluids.HYDROGEN, 500) }, new AStack[] { new ComparableStack(Powders.powder_coltan, 1), new ComparableStack(Powders.powder_niobium, 1), new ComparableStack(ModItems.dust, 1) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 500) }, 60);
		
		makeRecipe(800, "COLTAN_PAIN", new AStack[] {new ComparableStack(Powders.powder_coltan, 1), new OreDictStack(F.dust(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.GAS, 1000), new FluidStack(ModForgeFluids.OXYGEN, 500) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.PAIN, 1000) }, 120);
		
		makeRecipe(810, "COLTAN_CRYSTAL", null, new FluidStack[]{ new FluidStack(ModForgeFluids.PAIN, 1000), new FluidStack(ModForgeFluids.ACID, 500) }, new AStack[] { new ComparableStack(ModItems.gem_tantalium, 1), new ComparableStack(ModItems.dust, 3) }, new FluidStack[]{ new FluidStack(FluidRegistry.WATER, 250) }, 80);
		
		makeRecipe(820, "ARSENIC", new AStack[] { new ComparableStack(ModItems.scrap_oil, 64), new ComparableStack(ModItems.scrap_oil, 64), new ComparableStack(ModItems.scrap_oil, 64), new ComparableStack(ModItems.scrap_oil, 64) }, new FluidStack[]{ new FluidStack(ModForgeFluids.SULFURIC_ACID, 1000) }, new AStack[] { new ComparableStack(Nuggies.nugget_arsenic, 1), new ComparableStack(ModItems.sulfur, 2) }, new FluidStack[]{ new FluidStack(ModForgeFluids.HEAVYOIL, 1500) }, 1200);

		makeRecipe(830, "VIT_LIQUID", new AStack[] {new ComparableStack(ModBlocks.sand_lead, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.WASTEFLUID, 1000) }, new AStack[] {new ComparableStack(ModItems.nuclear_waste_vitrified, 1) }, null, 100);
		
		makeRecipe(840, "VIT_GAS", new AStack[] {new ComparableStack(ModBlocks.sand_lead, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.WASTEGAS, 1000) }, new AStack[] {new ComparableStack(ModItems.nuclear_waste_vitrified, 1) }, null, 100);
		
		makeRecipe(850, "TEL", new AStack[] {new OreDictStack(KEY_OIL_TAR, 1), new OreDictStack(PB.dust(), 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROLEUM, 100), new FluidStack(ModForgeFluids.STEAM, 1000) }, new AStack[] { new ComparableStack(ModItems.antiknock, 1) }, null, 40);
		
		makeRecipe(860, "GASOLINE", new AStack[] {new ComparableStack(ModItems.antiknock, 1) }, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROIL, 10000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.GASOLINE, 12000) }, 40);
		
		makeRecipe(870, "FRACKSOL", new AStack[] { new OreDictStack(S.dust()) }, new FluidStack[]{ new FluidStack(ModForgeFluids.PETROLEUM, 100), new FluidStack(FluidRegistry.WATER, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.FRACKSOL, 1000) }, 20);
		
		makeRecipe(880, "OSMIRIDIUM_DEATH", new AStack[] { new ComparableStack(Powders.powder_paleogenite), new OreDictStack(F.dust(), 8), new ComparableStack(Nuggies.nugget_bismuth, 4) }, new FluidStack[]{ new FluidStack(ModForgeFluids.SULFURIC_ACID, 1000), new FluidStack(ModForgeFluids.SAS3, 1000) }, null, new FluidStack[]{ new FluidStack(ModForgeFluids.LIQUID_OSMIRIDIUM, 1000) }, 240);
		
		// makeRecipe(0, "FP_SMEAR, new AStack[] { new ComparableStack() }, new FluidStack[]{ new FluidStack() }, null, null, 100);
		
	}

	public static void makeRecipe(int index, String name, AStack[] itemInputs, FluidStack[] fluidInputs, AStack[] outputItems, FluidStack[] outputFluids, int duration) {
		if(itemInputs != null)
			recipeItemInputs.put(index, itemInputs);
		if(fluidInputs != null)
			recipeFluidInputs.put(index, fluidInputs);
		if(outputItems != null)
			recipeItemOutputs.put(index, outputItems);
		if(outputFluids != null)
			recipeFluidOutputs.put(index, outputFluids);
		if(duration > 1)
			recipeDurations.put(index, duration);
		if(name != null)
			recipeNames.put(index, name);
	}

	public static void removeRecipe(int index) {
		recipeItemInputs.remove(index);
		recipeFluidInputs.remove(index);
		recipeItemOutputs.remove(index);
		recipeFluidOutputs.remove(index);
		recipeDurations.remove(index);
		recipeNames.remove(index);
	}

	public static List<AStack> getChemInputFromTempate(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemChemistryTemplate))
			return null;
		AStack[] inputs = recipeItemInputs.get(stack.getItemDamage());
		if(inputs != null)
			return Arrays.asList(inputs);
		return null;
	}


	public static FluidStack[] getFluidInputFromTempate(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemChemistryTemplate))
			return null;
		return recipeFluidInputs.get(stack.getItemDamage());
	}


	public static ItemStack[] getChemOutputFromTempate(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemChemistryTemplate))
			return null;
		AStack[] outputs = recipeItemOutputs.get(stack.getItemDamage());
		if(outputs != null){
			ItemStack[] stackOutputs = new ItemStack[outputs.length];
			for(int i=0; i<stackOutputs.length; i++){
				stackOutputs[i] = outputs[i].getStack();
			}
			return stackOutputs;
		}
		return null;
	}


	public static FluidStack[] getFluidOutputFromTempate(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemChemistryTemplate))
			return null;
		return recipeFluidOutputs.get(stack.getItemDamage());
	}


	public static int getProcessTime(ItemStack stack) {	
    	if(!(stack.getItem() instanceof ItemChemistryTemplate))
    		return 100;
        Integer time = recipeDurations.get(stack.getItemDamage());
        if(time != null)
        	return time;
        return 100;
    }

    public static String getName(ItemStack stack) {	
    	if(!(stack.getItem() instanceof ItemChemistryTemplate))
    		return null;
        String name = recipeNames.get(stack.getItemDamage());
        if(name != null)
        	return name;
        return "BAD_RECIPE";
    }

    public static String getName(int i) {	
        String name = recipeNames.get(i);
        if(name != null)
        	return name;
        return "";
    }

    public static boolean hasRecipe(ItemStack stack) {	
    	if(stack == null || !(stack.getItem() instanceof ItemChemistryTemplate))
    		return false;
       	return recipeNames.containsKey(stack.getItemDamage());
    }
}
