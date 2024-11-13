package com.leafia.unsorted.recipe_book.profiles;

import com.hbm.inventory.AssemblerRecipes;
import com.hbm.inventory.RecipesCommon;
import com.leafia.unsorted.recipe_book.system.LeafiaDummyRecipe;
import com.leafia.unsorted.recipe_book.LeafiaRecipeBookProfile;
import com.llib.math.range.RangeInt;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.creativetab.CreativeTabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipeBookAssembler extends LeafiaRecipeBookProfile {
    @Override
    public List<RecipeCategory> getCategories() {
        List<RecipeCategory> cats = new ArrayList<>();
        cats.add(new RecipeCategory(MainRegistry.partsTab,I18nUtil.resolveKey(MainRegistry.partsTab.getTranslatedTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.controlTab,I18nUtil.resolveKey(MainRegistry.controlTab.getTranslatedTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.templateTab,I18nUtil.resolveKey(MainRegistry.templateTab.getTranslatedTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.resourceTab,I18nUtil.resolveKey(MainRegistry.resourceTab.getTranslatedTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.blockTab,I18nUtil.resolveKey(MainRegistry.blockTab.getTranslatedTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.machineTab,I18nUtil.resolveKey(MainRegistry.machineTab.getTranslatedTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.nukeTab,I18nUtil.resolveKey(MainRegistry.nukeTab.getTranslatedTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.missileTab,I18nUtil.resolveKey(MainRegistry.missileTab.getTranslatedTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.weaponTab,I18nUtil.resolveKey(MainRegistry.weaponTab.getTranslatedTabLabel())));
        cats.add(new RecipeCategory(MainRegistry.consumableTab,I18nUtil.resolveKey(MainRegistry.consumableTab.getTranslatedTabLabel())));
        for (RecipesCommon.ComparableStack stack : AssemblerRecipes.recipeList) {
            CreativeTabs tab = stack.item.getCreativeTab();
            if (cats.contains(tab)) {
                RecipeCategory category = cats.get(cats.indexOf(tab));
                category.addItem(stack.item,stack.count(),new LeafiaDummyRecipe(stack.item,AssemblerRecipes.recipes.get(stack)));
            }
        }
        return cats;
    }
    @Override
    public Map<RangeInt,RecipesCommon.AStack[]> showRecipe(LeafiaDummyRecipe recipe) {
        return null;
    }
}
