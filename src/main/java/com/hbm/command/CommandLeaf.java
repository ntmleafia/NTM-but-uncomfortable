package com.hbm.command;

import com.hbm.inventory.leafia.inventoryutils.LeafiaPacket;
import com.hbm.main.leafia.LeafiaEase;
import com.hbm.main.leafia.LeafiaShakecam;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandLeaf extends CommandBase {
    @Override
    public String getName() {
        return "hbmleaf";
    }
    @Override
    public String getUsage(ICommandSender sender) {
        return "myaaaaa";
    }
    @Override
    public int getRequiredPermissionLevel() {
        //Level 2 ops can do commands like setblock, gamemode, and give. They can't kick/ban or stop the server.
        return 2;
    }
    String[] shiftArgs(String[] args,int n) {
        if (n > args.length) return new String[0];
        String[] argsOut = new String[args.length-n];
        for (int i = 0; i < args.length-n; i++)
            argsOut[i] = args[i+n];
        return argsOut;
    }
    boolean darkRow = false;
    ITextComponent genSuggestion(String c) {
        TextComponentString compo = new TextComponentString("  "+c);
        Style style = new Style()
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new TextComponentString("Click to try out")))
                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,c))
                .setColor(darkRow ? TextFormatting.DARK_GRAY : TextFormatting.GRAY);
        darkRow = !darkRow;
        return compo.setStyle(style);
    }
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        if (args.length <= 0)
            return Collections.emptyList();
        else {
            String lastArg = args[args.length-1];
            List<String> list = new ArrayList<String>();
            boolean nosort = false;
            if (args.length-1 >= 1) { // we're currently at arg 2 (1 in index)
                switch (args[0]) { // so we look into what the player put for arg 1 (0 in index)
                    case "shake":
                        args = shiftArgs(args,1);
                        if (args.length-1 <= 0) {
                            list.add("?");
                            list.addAll(Arrays.asList(server.getOnlinePlayerNames()));
                        } else {
                            nosort = true;
                            args = shiftArgs(args,1);
                            boolean showCoords = false;
                            boolean showParams = true;
                            if (args.length-1 > 0) {
                                if ((!args[0].matches(".*\\D.*") || args[0].equals("~")) && args.length < 3) {
                                    showCoords = true;
                                    showParams = false;
                                }
                            } else showCoords = true;
                            if (showCoords) {
                                switch (args.length-1) {
                                    case 0: list.add(String.valueOf(sender.getPosition().getX())); break;
                                    case 1: list.add(String.valueOf(sender.getPosition().getY())); break;
                                    case 2: list.add(String.valueOf(sender.getPosition().getZ())); break;
                                }
                            }
                            if (showParams) {
                                list.add("type=simple");
                                list.add("type=smooth");
                                for (LeafiaShakecam.Preset preset : LeafiaShakecam.Preset.values()) {
                                    list.add("preset="+preset.name());
                                }
                                for (String s : new String[]{"range","intensity","curve","speed","duration"}) {
                                    list.add(s+"=");
                                    list.add(s+"+");
                                    list.add(s+"-");
                                    list.add(s+"*");
                                    list.add(s+"/");
                                }
                                for (String s : LeafiaEase.listEasesForCommands()) {
                                    list.add("ease="+s);
                                }
                                list.add("ease=none");
                            }
                        }
                        break;
                }
            } else {
                list.add("eases");
                list.add("shake");
            }
            if(list.size() > 1 && !nosort)
                list.sort((a,b) -> {
                    if(a == null || b == null) {
                        return -1;
                    }
                    return a.compareTo(b);
                });
            return list.stream().filter(s -> s.startsWith(lastArg)).collect(Collectors.toList());
        }
    }
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length <= 0) {
            throw new CommandException(getUsage(sender));
        } else {
            Style header = new Style().setColor(TextFormatting.LIGHT_PURPLE);
            darkRow = false;
            switch(args[0]) {
                case "eases":
                    sender.sendMessage(new TextComponentString("Available eases:").setStyle(header));
                    for (String s : LeafiaEase.listEasesForCommands()) {
                        sender.sendMessage(new TextComponentString("  "+s));
                    }
                    break;
                case "shake":
                    args = shiftArgs(args,1);
                    if (args.length < 1)
                        throw new WrongUsageException("/hbmleaf shake ? OR /hbmleaf shake <player> [<x> <y> <z>] [params...]", new Object[0]);
                    if (args[0].equals("?")) {
                        sender.sendMessage(new TextComponentString("Many examples:").setStyle(header));
                        sender.sendMessage(genSuggestion("/hbmleaf shake @a"));
                        sender.sendMessage(genSuggestion("/hbmleaf shake @a ~ ~ ~"));
                        sender.sendMessage(genSuggestion("/hbmleaf shake @a ~ ~ ~ duration*2"));
                        sender.sendMessage(genSuggestion("/hbmleaf shake @a intensity=9 range=30 duration=15"));
                        sender.sendMessage(genSuggestion("/hbmleaf shake @a type=smooth"));
                        sender.sendMessage(genSuggestion("/hbmleaf shake @a type=smooth preset=RUPTURE"));
                        sender.sendMessage(genSuggestion("/hbmleaf shake @a type=smooth preset=RUPTURE intensity*2"));
                        sender.sendMessage(genSuggestion("/hbmleaf shake @a ~ ~ ~ type=smooth curve=60 intensity=20 duration=30"));
                        sender.sendMessage(new TextComponentString(""));
                        sender.sendMessage(new TextComponentString("Available types:").setStyle(header));
                        sender.sendMessage(new TextComponentString("  simple | smooth"));
                        sender.sendMessage(new TextComponentString(""));
                        sender.sendMessage(new TextComponentString("Available presets:").setStyle(header));
                        for (LeafiaShakecam.Preset preset : LeafiaShakecam.Preset.values()) {
                            sender.sendMessage(new TextComponentString("  "+preset.name()).setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE).setItalic(true)).appendSibling(new TextComponentString(String.format(" (r=%1.1f i=%1.1f c=%1.1f s=%1.1f)",preset.range,preset.intensity,preset.curve,preset.speed))));
                        }
                        sender.sendMessage(new TextComponentString(""));
                        sender.sendMessage(new TextComponentString("Default parameters:").setStyle(header));
                        sender.sendMessage(new TextComponentString("  range=25 intensity=4 curve=2 speed=4 duration=5 ease=expoOut"));
                        sender.sendMessage(new TextComponentString(""));
                        sender.sendMessage(new TextComponentString("Use /hbmleaf eases to see all available eases.").setStyle(new Style().setColor(TextFormatting.GREEN)));
                        return;
                    }
                    List<EntityPlayerMP> players = getPlayers(server,sender,args[0]);
                    args = shiftArgs(args,1);
                    BlockPos pos = null;
                    if (args.length > 0) {
                        if (!args[0].matches(".*\\D.*") || args[0].equals("~")) {
                            pos = parseBlockPos(sender,args,0,false);
                            args = shiftArgs(args,3);
                        }
                    }
                    for (EntityPlayerMP player : players) {
                        ShakecamPacket packet = new ShakecamPacket(args);
                        packet.pos = pos;
                        LeafiaPacket._sendToClient(packet,player);
                    }
                    break;
            }
        }
    }
    public static class ShakecamPacket implements IMessage {
        public String[] params;
        public BlockPos pos = null;
        public ShakecamPacket() {
        }
        public ShakecamPacket(String[] args) {
            params = args;
        }
        @Override
        public void fromBytes(ByteBuf buf) {
            params = new String[buf.readByte()];
            for (int i = 0; i < params.length; i++) params[i] = ByteBufUtils.readUTF8String(buf);
            if (buf.readableBytes() >= 3)
                pos = new BlockPos(buf.readInt(),buf.readInt(),buf.readInt());
        }
        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeByte(params.length);
            for (String param : params) ByteBufUtils.writeUTF8String(buf, param);
            if (pos != null) {
                buf.writeInt(pos.getX());
                buf.writeInt(pos.getY());
                buf.writeInt(pos.getZ());
            }
        }
        public static class Handler implements IMessageHandler<ShakecamPacket, IMessage> {
            static final String[] numerics = new String[]{"range","intensity","curve","speed","duration"};
            @Override
            @SideOnly(Side.CLIENT)
            public IMessage onMessage(ShakecamPacket message, MessageContext ctx) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Float[] params = {null,null,null,null,null};
                    float[] adds = {0,0,0,0,0};
                    float[] multipliers = {1,1,1,1,1};
                    LeafiaEase.Ease ease = null;
                    LeafiaEase.Direction direction = null;
                    String type = "simple";
                    LeafiaShakecam.Preset preset = null;
                    boolean removeEase = false;
                    for (String arg : message.params) {
                        if (arg.startsWith("type="))
                            type = arg.substring(5);
                        for (int i = 0; i < numerics.length; i++) {
                            String s = numerics[i];
                            if (arg.startsWith(s)) {
                                arg = arg.substring(s.length());
                                if (arg.length() >= 2) {
                                    String op = arg.substring(0,1);
                                    arg = arg.substring(1);
                                    try {
                                        switch (op) {
                                            case "=":
                                                params[i] = (float)parseDouble(arg);
                                                break;
                                            case "+":
                                                adds[i] = (float)parseDouble(arg);
                                                break;
                                            case "-":
                                                adds[i] = -(float)parseDouble(arg);
                                                break;
                                            case "*":
                                                multipliers[i] = (float)parseDouble(arg);
                                                break;
                                            case "/":
                                                multipliers[i] = 1/(float)parseDouble(arg);
                                                break;
                                            default:
                                                Minecraft.getMinecraft().player.sendMessage(new TextComponentString("ERROR>> Invalid operator: "+op).setStyle(new Style().setColor(TextFormatting.RED)));
                                                break;
                                        }
                                    } catch (NumberInvalidException e) {
                                        Minecraft.getMinecraft().player.sendMessage(new TextComponentString("ERROR>> "+e.getMessage()).setStyle(new Style().setColor(TextFormatting.RED)));
                                    }
                                } else
                                    Minecraft.getMinecraft().player.sendMessage(new TextComponentString("ERROR>> Malformed numeric parameter!").setStyle(new Style().setColor(TextFormatting.RED)));
                            }
                        }
                        if (arg.startsWith("ease=")) {
                            arg = arg.substring(5);
                            if (arg.equals("none"))
                                removeEase = true;
                            else {
                                try {
                                    LeafiaEase easeInsta = LeafiaEase.parseEase(arg);
                                    ease = easeInsta.ease;
                                    direction = easeInsta.dir;
                                } catch (CommandException e) {
                                    Minecraft.getMinecraft().player.sendMessage(new TextComponentString("ERROR>> " + e.getMessage()).setStyle(new Style().setColor(TextFormatting.RED)));
                                }
                            }
                        }
                        if (arg.startsWith("preset=")) {
                            try {
                                preset = LeafiaShakecam.Preset.valueOf(arg.substring(7));
                            } catch (IllegalArgumentException e) {
                                Minecraft.getMinecraft().player.sendMessage(new TextComponentString("ERROR>> " + e.getMessage()).setStyle(new Style().setColor(TextFormatting.RED)));
                            }
                        }
                    }
                    LeafiaShakecam.shakeInstance shake;
                    switch(type) {
                        case "simple": shake = new LeafiaShakecam.shakeSimple(params[4],ease,direction); break;
                        case "smooth": shake = new LeafiaShakecam.shakeSmooth(params[4],ease,direction); break;
                        default:
                            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("ERROR>> Invalid type: "+type).setStyle(new Style().setColor(TextFormatting.RED)));
                            return;
                    }
                    if (preset != null)
                        shake.loadPreset(preset);
                    shake.configure(params[0],params[1],params[2],params[3]);
                    shake.range *= multipliers[0];
                    shake.intensity *= multipliers[1];
                    shake.curve *= multipliers[2];
                    shake.speed *= multipliers[3];
                    shake.duration *= multipliers[4];
                    shake.range += adds[0];
                    shake.intensity += adds[1];
                    shake.curve += adds[2];
                    shake.speed += adds[3];
                    shake.duration += adds[4];
                    if (removeEase)
                        shake.easeInstance = null;
                    LeafiaShakecam._addShake(message.pos,shake);
                });
                return null;
            }
        }
    }
}
