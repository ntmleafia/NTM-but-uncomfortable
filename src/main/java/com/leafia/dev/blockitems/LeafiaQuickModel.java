package com.leafia.dev.blockitems;

import com.hbm.lib.RefStrings;
import com.hbm.render.amlfrom1710.AdvancedModelLoader;
import com.hbm.render.amlfrom1710.IModelCustom;
import com.hbm.render.item.ItemRenderBase;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

import static com.hbm.render.RenderHelper.bindTexture;

public interface LeafiaQuickModel {
    // automate this shit already
    @SideOnly(Side.CLIENT)
    Map<String,ResourceLocation> texMap = new HashMap<>();
    @SideOnly(Side.CLIENT)
    Map<String,IModelCustom> mdlMap = new HashMap<>();


    /**
     * @return Should return the path for the TE to be registered.
     */
    @SideOnly(Side.CLIENT)
    String _resourcePath();

    /**
     * @return Should return the path for assets, both textures and model assuming they're in the same location.
     */
    @SideOnly(Side.CLIENT)
    String _assetPath();

    /**
     * @return Just return a new instance of corresponding renderer.
     */
    @SideOnly(Side.CLIENT)
    TileEntitySpecialRenderer<TileEntity> _renderer();

    /**
     * @return Should return the block under ModBlocks.
     */
    Block _block();

    @SideOnly(Side.CLIENT)
    default ResourceLocation __getTexture() {
        if (!LeafiaQuickModel.texMap.containsKey(this._resourcePath()))
            throw new RuntimeException("Leafia: Texture "+this._resourcePath()+" not registered in QuickModel");
        return LeafiaQuickModel.texMap.get(this._resourcePath());
    }
    @SideOnly(Side.CLIENT)
    default IModelCustom __getModel() {
        if (!LeafiaQuickModel.mdlMap.containsKey(this._resourcePath()))
            throw new RuntimeException("Leafia: Model "+this._resourcePath()+" not registered in QuickModel");
        return LeafiaQuickModel.mdlMap.get(this._resourcePath());
    }
    @SideOnly(Side.CLIENT)
    static void _loadResources(LeafiaQuickModel subject, String asset) {
        String path = subject._resourcePath();
        if (!texMap.containsKey(path)) {
            texMap.put(path,new ResourceLocation(RefStrings.MODID, "textures/models/"+asset+".png"));
        }
        if (!mdlMap.containsKey(path)) {
            mdlMap.put(path, AdvancedModelLoader.loadModel(new ResourceLocation(RefStrings.MODID, "models/"+asset+".obj")));
        }
    }
    @SideOnly(Side.CLIENT)
    double _sizeReference();
    @SideOnly(Side.CLIENT)
    double _itemYoffset();
    @SideOnly(Side.CLIENT)
    default ItemRenderBase _itemRenderer() {
        return new ItemRenderBase() {
            public void renderInventory() {
                GL11.glRotated(-65,1,0,0);
                GL11.glRotated(25,0,0,1);
                GL11.glTranslated(0,-_itemYoffset(),0);
                GL11.glScaled(22.5/_sizeReference(), 22.5/_sizeReference(), 22.5/_sizeReference());
            }
            public void renderCommon() {
                GL11.glScaled(0.5, 0.5, 0.5);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(__getTexture());
                __getModel().renderAll();
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
