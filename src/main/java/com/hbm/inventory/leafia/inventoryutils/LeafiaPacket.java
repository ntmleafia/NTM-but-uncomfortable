package com.hbm.inventory.leafia.inventoryutils;

import com.hbm.packet.PacketDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class LeafiaPacket implements IMessage {

	int x, y, z, identifier;
	int dimension;
	public final Map<Byte,Object> signal = new HashMap<>();

	public LeafiaPacket() {
	}
	public LeafiaPacket(BlockPos pos,String identifier) {
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
		this.identifier = identifier.hashCode();
	}
	private static byte getType(byte entry) {
		BitSet bit = BitSet.valueOf(new byte[]{entry}).get(5,8);
		return (bit.cardinality() == 0) ? 0 : bit.toByteArray()[0];
	}
	private static byte getKey(byte entry) {
		BitSet bit = BitSet.valueOf(new byte[]{entry}).get(0,5);
		return (bit.cardinality() == 0) ? 0 : bit.toByteArray()[0];
	}
	public LeafiaPacket __write(byte key,Object value) {
		BitSet bit = BitSet.valueOf(new byte[]{key});
		if (bit.get(5,8).cardinality() != 0)
			throw new RuntimeException("Leafia: Entry ID given for LeafiaPacket wasn't in the range of 0 ~ 31");
		if (value instanceof Boolean) {
		} else if (value instanceof Byte) {
			bit.set(5);
		} else if (value instanceof Short) {
			bit.set(6);
		} else if (value instanceof Integer) {
			bit.set(6);
			bit.set(5);
		} else if (value instanceof Long) {
			bit.set(7);
		} else if (value instanceof Float) {
			bit.set(7);
			bit.set(5);
		} else if (value instanceof Double) {
			bit.set(7);
			bit.set(6);
		} else if (value instanceof NBTTagCompound) {
			bit.set(7);
			bit.set(6);
			bit.set(5);
		} else
			throw new RuntimeException("Leafia: Given type not supported for LeafiaPacket...");
		this.signal.put((bit.cardinality() == 0) ? 0 : bit.toByteArray()[0],value);
		return this;
	}
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(identifier);
		signal.forEach((Byte entry,Object value) -> {
			buf.writeByte(entry);
			switch(getType(entry)) {
				case 0: buf.writeBoolean((boolean)value); break;
				case 1: buf.writeByte((byte)value); break;
				case 2: buf.writeShort((short)value); break;
				case 3: buf.writeInt((int)value); break;
				case 4: buf.writeLong((long)value); break;
				case 5: buf.writeFloat((float)value); break;
				case 6: buf.writeDouble((double)value); break;
				case 7: ByteBufUtils.writeTag(buf,(NBTTagCompound)value); break;
			}
		});
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		signal.clear();
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		identifier = buf.readInt();
		while (buf.readableBytes() >= 2) {
			byte entry = buf.readByte();
			Object value;
			switch(getType(entry)) {
				case 0: value = buf.readBoolean(); break;
				case 1: value = buf.readByte(); break;
				case 2: value = buf.readShort(); break;
				case 3: value = buf.readInt(); break;
				case 4: value = buf.readLong(); break;
				case 5: value = buf.readFloat(); break;
				case 6: value = buf.readDouble(); break;
				case 7: value = ByteBufUtils.readTag(buf); break;
				default: throw new RuntimeException("Leafia: Malformed packet!");
			}
			signal.put(entry,value);
		}
	}

	public static LeafiaPacket _start(TileEntity entity) {
		LeafiaPacket packet = new LeafiaPacket(entity.getPos(),((LeafiaPacketReceiver)entity).getPacketIdentifier());
		packet.dimension = entity.getWorld().provider.getDimension();
		return packet;
	}
	public void __sendToServer() {
		PacketDispatcher.wrapper.sendToServer(this);
	}
	public void __sendToClients(double range) {
		PacketDispatcher.wrapper.sendToAllAround(this,new NetworkRegistry.TargetPoint(dimension,x,y,z,range));
	}
	
	public static class Handler implements IMessageHandler<LeafiaPacket,IMessage> {
		@SideOnly(Side.CLIENT)
		public void handleLocal(LeafiaPacket m,MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				World world = Minecraft.getMinecraft().world;
				TileEntity LocalEntity = world.getTileEntity(new BlockPos(m.x, m.y, m.z));
				if (LocalEntity instanceof LeafiaPacketReceiver) {
					LeafiaPacketReceiver receiver = (LeafiaPacketReceiver)LocalEntity;
					if (receiver.getPacketIdentifier().hashCode() == m.identifier) {
						m.signal.forEach((Byte entry,Object value) -> {
							receiver.onReceivePacketLocal(getKey(entry),value);
						});
					}
				}
			});
		}
		@Override
		public IMessage onMessage(LeafiaPacket m,MessageContext ctx) {
			if (ctx.side.isClient()) {
				handleLocal(m,ctx);
			} else {
				ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
					EntityPlayer p = ctx.getServerHandler().player;
					BlockPos pos = new BlockPos(m.x,m.y,m.z);
					if(!p.world.isBlockLoaded(pos))
						return;
					TileEntity ServerEntity = p.world.getTileEntity(pos);
					if (ServerEntity instanceof LeafiaPacketReceiver) {
						LeafiaPacketReceiver receiver = (LeafiaPacketReceiver)ServerEntity;
						if (receiver.getPacketIdentifier().hashCode() == m.identifier) {
							m.signal.forEach((Byte entry,Object value) -> {
								receiver.onReceivePacketServer(getKey(entry),value,p);
							});
						}
					}
				});
			}
			return null;
		}
	}
}
