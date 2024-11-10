package com.hbm.tileentity.leafia.pwr;

import com.hbm.blocks.leafia.pwr.MachinePWRElement;
import com.hbm.inventory.leafia.inventoryutils.LeafiaPacket;
import com.hbm.inventory.leafia.inventoryutils.LeafiaPacketReceiver;
import com.hbm.lib.InventoryHelper;
import com.hbm.tileentity.TileEntityInventoryBase;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TileEntityPWRElement extends TileEntityInventoryBase implements PWRBase, ITickable, LeafiaPacketReceiver {
    BlockPos corePos = null;
    PWRData data = null;

    public TileEntityPWRElement() {
        super(1);
    }

    public void connectUpper() { // For clients, called only on validate()
        if (!this.isInvalid() && world.isBlockLoaded(pos)) {
            Chunk chunk = world.getChunkFromBlockCoords(pos);
            if (world.isRemote) { // Keep in mind that neighborChanged in Block does NOT get called for Remotes
                if (world.getBlockState(pos.down()).getBlock() instanceof MachinePWRElement) {
                    TileEntity entityBelow = chunk.getTileEntity(pos.down(),Chunk.EnumCreateEntityType.CHECK);
                    if (entityBelow != null) {
                        if (entityBelow instanceof TileEntityPWRElement) {
                            ((TileEntityPWRElement)entityBelow).connectUpper();
                        }
                    }
                }
                if (world.getBlockState(pos.up()).getBlock() instanceof MachinePWRElement) {
                    inventory.setStackInSlot(0,ItemStack.EMPTY);
                    invalidate();
                }
                return;
            }
            BlockPos upPos = pos.up();
            boolean mustTransmit = false;
            TileEntityPWRElement target = null;
            while (world.isValid(upPos)) {
                if (world.getBlockState(upPos).getBlock() instanceof MachinePWRElement) {
                    mustTransmit = true;
                    TileEntity entity = chunk.getTileEntity(upPos,Chunk.EnumCreateEntityType.CHECK);
                    target = null;
                    if (entity != null) {
                        if (entity instanceof TileEntityPWRElement) {
                            if (!entity.isInvalid()) {
                                target = (TileEntityPWRElement) entity;
                                if (!target.inventory.getStackInSlot(0).isEmpty())
                                    target = null;
                            }
                        }
                    }
                } else
                    break;
                upPos = upPos.up();
            }
            if (mustTransmit) {
                if (target != null) {
                    target.inventory.setStackInSlot(0,inventory.getStackInSlot(0));
                    this.inventory.setStackInSlot(0,ItemStack.EMPTY);
                } else
                    InventoryHelper.dropInventoryItems(world,pos,this);
                this.invalidate();
            }
        }
    }
    @Override
    public void setCore(BlockPos pos) {
        corePos = pos;
    }

    @Override
    public void setData(@Nullable PWRData data) {
        this.data = data;
    }
    @Override
    public PWRData getData() {
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("corePosX"))
            corePos = new BlockPos(
                    compound.getInteger("corePosX"),
                    compound.getInteger("corePosY"),
                    compound.getInteger("corePosZ")
            );
        if (compound.hasKey("data")) {
            data = new PWRData();
            data.readFromNBT(compound);
        }
        super.readFromNBT(compound);
    }

    @Override
    public String getName() {
        return I18nUtil.resolveKey("tile.pwr_element.name");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (corePos != null) {
            compound.setInteger("corePosX",corePos.getX());
            compound.setInteger("corePosY",corePos.getY());
            compound.setInteger("corePosZ",corePos.getZ());
        }
        if (data != null) {
            data.writeToNBT(compound);
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!world.isRemote)
            syncLocals();
    }

    @Override
    public void validate() {
        super.validate();
        //if (world.isRemote) { // so long lol
            //if (!compound.hasKey("_isSyncSignal")) {
            //LeafiaPacket._validate(this);
            //LeafiaPacket._start(this).__write((byte)0,true).__setTileEntityQueryType(Chunk.EnumCreateEntityType.CHECK).__sendToServer();
            //}
        //}
        connectUpper();
    }

    @Override
    public void update() {
        if (this.data != null)
            this.data.update();
    }

    @Override
    public String getPacketIdentifier() {
        return "PWRElement";
    }
    public LeafiaPacket generateSyncPacket() {
        return LeafiaPacket._start(this).__write((byte)0,writeToNBT(new NBTTagCompound()));
    }
    public void syncLocals() {
        generateSyncPacket().__sendToAffectedClients();//.__setTileEntityQueryType(Chunk.EnumCreateEntityType.CHECK).__sendToAllInDimension();
    }
    @SideOnly(Side.CLIENT)
    @Override
    public void onReceivePacketLocal(byte key,Object value) {
        if (key == 0) {
            if (value instanceof NBTTagCompound) {
                NBTTagCompound nbt = (NBTTagCompound)value;
                nbt.setBoolean("_isSyncSignal",true);
                readFromNBT(nbt);
            }
        }
    }
    @Override
    public void onReceivePacketServer(byte key,Object value,EntityPlayer plr) {/*
        if (key == 0) {
            if (value.equals(true)) {
            }
        }*/
    }
    @Override
    public void onPlayerValidate(EntityPlayer plr) {
        generateSyncPacket().__sendToClient(plr);
    }
}
