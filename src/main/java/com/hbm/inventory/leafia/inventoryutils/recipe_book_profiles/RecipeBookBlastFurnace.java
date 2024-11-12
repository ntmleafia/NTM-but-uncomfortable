package com.hbm.inventory.leafia.inventoryutils.recipe_book_profiles;

import com.hbm.inventory.DiFurnaceRecipes;
import com.hbm.inventory.RecipesCommon.*;
import com.hbm.inventory.leafia.inventoryutils.recipebooks.LeafiaDummyRecipe;
import com.hbm.inventory.leafia.inventoryutils.LeafiaRecipeBookProfile;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemHazard;
import com.llib.math.RangeInt;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple.Pair;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;

public class RecipeBookBlastFurnace extends LeafiaRecipeBookProfile {
    @Override
    public List<RecipeCategory> getCategories() {
        List<RecipeCategory> cats = new ArrayList<>();
        cats.add(new RecipeCategory("Basic Alloys",ModItems.ingot_steel,ModItems.ingot_advanced_alloy));
        cats.add(new RecipeCategory("Hazardous Alloys",ModItems.ingot_magnetized_tungsten));
        cats.add(new RecipeCategory("Machine Parts",ModItems.neutron_reflector));
        cats.add(new RecipeCategory("Miscellaneous",Items.LAVA_BUCKET));
        Set<Pair<AStack,AStack>> blockedPairs = new HashSet<>();
        for (Pair<AStack,AStack> pair : DiFurnaceRecipes.diRecipes.keySet()) {
            if (blockedPairs.contains(pair)) continue;
            blockedPairs.add(new Pair<>(pair.getValue(),pair.getKey()));
            ItemStack stack = DiFurnaceRecipes.diRecipes.get(pair);
            int count = stack.getCount();
            Item item = stack.getItem();
            int index = -1;
            if (item.getCreativeTab() == MainRegistry.partsTab) {
                if (item.getRegistryName().getResourcePath().startsWith("ingot_")) {
                    if (item instanceof ItemHazard)
                        index = 1;
                    else
                        index = 0;
                } else
                    index = 2;
            } else
                index = 3;
            if (index >= 0)
                cats.get(index).addItem(item,count,new LeafiaDummyRecipe(item,count,pair.getKey(),pair.getValue()));
        }
        return cats;
    }
    @Override
    public Map<RangeInt,AStack[]> showRecipe(LeafiaDummyRecipe recipe) {
        Map<RangeInt,AStack[]> slotMap = new HashMap<>();
        slotMap.put(new RangeInt(0,0),new AStack[]{recipe.input[0]});
        slotMap.put(new RangeInt(1,1),new AStack[]{recipe.input[1]});
        return slotMap;
    }
}
