package com.leafia.contents.machines.elevators.car;

import com.hbm.lib.HBMSoundEvents;
import com.leafia.contents.machines.elevators.EvBuffer;
import com.leafia.contents.machines.elevators.EvPulleyTE;
import com.leafia.contents.machines.elevators.EvShaft;
import com.leafia.contents.machines.elevators.car.chips.EvChipBase;
import com.leafia.contents.machines.elevators.car.chips.EvChipS6;
import com.leafia.contents.machines.elevators.car.styles.EvWallBase;
import com.leafia.contents.machines.elevators.car.styles.panels.ElevatorPanelBase;
import com.leafia.contents.machines.elevators.car.styles.panels.S6Door;
import com.leafia.dev.custompacket.LeafiaCustomPacket;
import com.leafia.dev.custompacket.LeafiaCustomPacketEncoder;
import com.leafia.dev.optimization.bitbyte.LeafiaBuf;
import com.leafia.unsorted.IEntityCustomCollision;
import com.llib.group.LeafiaMap;
import com.llib.group.LeafiaSet;
import com.leafia.dev.math.FiaMatrix;
import com.llib.technical.FifthString;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ElevatorEntity extends Entity implements IEntityMultiPart, IEntityCustomCollision {
	public static final DataParameter<String> STYLE_FLOOR = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.STRING);
	public static final DataParameter<String> STYLE_CEILING = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.STRING);
	public static final DataParameter<String> STYLE_FRONT = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.STRING);
	public static final DataParameter<String> STYLE_LEFT = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.STRING);
	public static final DataParameter<String> STYLE_RIGHT = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.STRING);
	public static final DataParameter<String> STYLE_BACK = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.STRING);
	public static final DataParameter<String>[] styleParams = new DataParameter[]{STYLE_FLOOR,STYLE_CEILING,STYLE_FRONT,STYLE_LEFT,STYLE_BACK,STYLE_RIGHT};
	public static final DataParameter<String> FLOOR_DISPLAY = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.STRING);
	public static final String[] ALLOWED_DIGITS = new String[]{"0","1","2","3","4","5","6","7","8","9","-","L"};
	public static boolean isDigitAllowed(String s) {
		for (String allowedDigit : ALLOWED_DIGITS) {
			if (allowedDigit.equals(s)) return true;
		}
		return false;
	}
	public boolean doorOpen = false;
	public boolean down = false;
	public LeafiaSet<Integer> targetFloors = new LeafiaSet<>();
	public LeafiaSet<Integer> getTargetFloorsFromEnabledButtons() {
		LeafiaSet<Integer> floors = new LeafiaSet<>();
		for (String id : enabledButtons) {
			if (id.startsWith("floor")) {
				try {
					int floor = Integer.parseInt(id.substring(5));
					floors.add(floor);
				} catch (NumberFormatException ignored) {}
			}
		}
		return floors;
	}
	public boolean isOnTargetFloor() {
		int floor = getDataInteger(FLOOR);
		for (Integer targetFloor : targetFloors) {
			if (targetFloor == floor) return true;
		}
		return false;
	}
	public Integer getNextFloor() {
		int floor = getDataInteger(FLOOR);
		Integer nextFloor = null;
		for (Integer targetFloor : targetFloors) {
			if (!down) {
				if (targetFloor > floor) {
					if (nextFloor == null) nextFloor = targetFloor;
					else nextFloor = Math.min(nextFloor,targetFloor);
				}
			} else {
				if (targetFloor < floor) {
					if (nextFloor == null) nextFloor = targetFloor;
					else nextFloor = Math.max(nextFloor,targetFloor);
				}
			}
		}
		return nextFloor;
	}
	public static final DataParameter<Integer> FLOOR = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.VARINT);
	public static final DataParameter<Integer> ARROW = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.VARINT);
	public static final DataParameter<Float> DOOR_IN = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.FLOAT);
	public static final DataParameter<Float> DOOR_OUT = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.FLOAT);
	public static final DataParameter<Integer> PULLEY_X = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.VARINT);
	public static final DataParameter<Integer> PULLEY_Y = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.VARINT);
	public static final DataParameter<Integer> PULLEY_Z = EntityDataManager.createKey(ElevatorEntity.class,DataSerializers.VARINT);
	@Nullable public EvPulleyTE getPulley() {
		BlockPos pos = new BlockPos(getDataInteger(PULLEY_X),getDataInteger(PULLEY_Y),getDataInteger(PULLEY_Z));
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof EvPulleyTE)
			return (EvPulleyTE)te;
		return null;
	}
	EvPulleyTE pulley = null;
	public void findPulley() {
		for (int i = (int)posY; i < 255; i++) {
			TileEntity te = world.getTileEntity(new BlockPos(posX,posY+i,posZ));
			if (te instanceof EvPulleyTE) {
				pulley = (EvPulleyTE)te;
				BlockPos pos = pulley.getPos();
				dataManager.set(PULLEY_X,pos.getX());
				dataManager.set(PULLEY_Y,pos.getY());
				dataManager.set(PULLEY_Z,pos.getZ());
			}
		}
	}

	public LeafiaMap<String,List<HitSrf>> surfaces = new LeafiaMap<>();
	@Override
	protected void entityInit() {
		this.dataManager.register(STYLE_FLOOR,"s6floor");
		this.dataManager.register(STYLE_CEILING,"s6ceiling");
		this.dataManager.register(STYLE_FRONT,"s6door");
		this.dataManager.register(STYLE_LEFT,"s6window");
		this.dataManager.register(STYLE_BACK,"s6door");
		this.dataManager.register(STYLE_RIGHT,"s6window");
		this.dataManager.register(FLOOR,1);
		this.dataManager.register(FLOOR_DISPLAY,"1");
		this.dataManager.register(ARROW,1);
		this.dataManager.register(DOOR_IN,0f);
		this.dataManager.register(DOOR_OUT,0f);
		this.dataManager.register(PULLEY_X,1);
		this.dataManager.register(PULLEY_Y,1);
		this.dataManager.register(PULLEY_Z,1);
		width = 30/16f;
		height = 0.1f;
	}
	public String getDataString(DataParameter<String> param) {
		return this.dataManager.get(param);
	}
	public Integer getDataInteger(DataParameter<Integer> param) {
		return this.dataManager.get(param);
	}
	public Float getDataFloat(DataParameter<Float> param) {
		return this.dataManager.get(param);
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public boolean attackEntityFromPart(MultiPartEntityPart part,DamageSource source,float damage) {
		return false;
	}
	public List<ElevatorPanelBase> getPanels() {
		List<ElevatorPanelBase> list = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			ElevatorPanelBase panel = getPanel(i);
			if (panel != null)
				list.add(panel);
		}
		return list;
	}
	@Nullable
	public ElevatorPanelBase getPanel(int side) {
		String style = getDataString(styleParams[side+2]);
		switch(style) {
			case "s6door": return new S6Door(side);
		}
		return null;
	}
	public EvWallBase getWallInstance(int side) {
		ElevatorPanelBase panel = getPanel(side);
		if (panel != null) return panel;
		String style = getDataString(styleParams[side+2]);
		switch(style) {
		}
		return new EvWallBase(side);
	}
	@Override
	public Entity[] getParts() {
		if (!world.isRemote) return super.getParts();
		List<ElevatorPanelBase> panels = getPanels();
		Entity[] list = new Entity[buttons.size()*panels.size()];
		for (int i = 0; i < buttons.size(); i++) {
			for (int j = 0; j < panels.size(); j++)
				list[i*panels.size()+j] = buttons.get(i).hitboxes.get(j);
		}
		return list;
	}
	public LeafiaSet<String> enabledButtons = new LeafiaSet<>();
	public LeafiaMap<String,Integer> clickedButtons = new LeafiaMap<>(); // client only


	public static class ElevatorButton {
		int x;
		int y;
		final ElevatorEntity entity;
		final List<MultiPartEntityPart> hitboxes = new ArrayList<>();
		final List<ElevatorPanelBase> panels;
		final String id;
		// z = 0.9365;
		ElevatorButton(ElevatorEntity elevator,String id,int x,int y) {
			entity = elevator;
			panels = elevator.getPanels();
			this.id = id;
			if (elevator.world.isRemote) {
				for (ElevatorPanelBase panel : panels) {
					hitboxes.add(new MultiPartEntityPart(elevator,id,1 / 16f,1 / 16f) {
						@Override
						public boolean processInitialInteract(EntityPlayer player,EnumHand hand) {
							return onInteract(player,hand);
						}
					});
				}
			}
			this.x = x;
			this.y = y;
		}
		void onUpdate() {
			for (int i = 0; i < panels.size(); i++) {
				MultiPartEntityPart hitbox = hitboxes.get(i);
				ElevatorPanelBase panel = panels.get(i);
				FiaMatrix mat = new FiaMatrix(new Vec3d(entity.posX,entity.posY,entity.posZ)).rotateY(-entity.rotationYaw).rotateY(panel.rotation*90);
				double offset = 0.5;
				if (this instanceof FireButton) offset *= -1;
				Vec3d pos = mat.translate(panel.getStaticX()/16d+x/16d+offset/16d,y/16d,-panel.getStaticZ()).position;
				hitbox.setPosition(pos.x,pos.y,pos.z);
				hitbox.onUpdate();
			}
		}
		public boolean onInteract(EntityPlayer player,EnumHand hand) {
			return entity.onButtonPressed(id,player,hand);
		}
	}
	public static class FloorButton extends ElevatorButton {
		String label;
		int floor;
		public FloorButton(ElevatorEntity e,int floor,String label,int x,int y) {
			super(e,"floor"+floor,x,y);
			this.floor = floor;
			this.label = label;
		}
	}
	public static class OpenButton extends ElevatorButton {
		public OpenButton(ElevatorEntity e,int x,int y) { super(e,"open",x,y); }
	}
	public static class CloseButton extends ElevatorButton {
		public CloseButton(ElevatorEntity e,int x,int y) { super(e,"close",x,y); }
	}
	public static class BellButton extends ElevatorButton {
		public BellButton(ElevatorEntity e,int x,int y) { super(e,"bell",x,y); }
	}
	public static class FireButton extends ElevatorButton {
		public FireButton(ElevatorEntity e,int x,int y) { super(e,"fire",x,y); }
	}
	public boolean onButtonPressed(String id,EntityPlayer player,EnumHand hand) {
		if (world.isRemote) {
			player.swingArm(hand);
			LeafiaCustomPacket.__start(new EvButtonInteractPacket(this,id,hand)).__sendToServer();
		}

		/*if (!world.isRemote) {
			world.createExplosion(null,posX,posY,posZ,1,false);
			if (id.equals("fire")) {
				buttons.clear();
				dataManager.set(STYLE_BACK,"s6wall");
				buttons.add(new FloorButton(this,1,"1",1,14));
				buttons.add(new FloorButton(this,-1,"-1",-2,14));
				EvButtonSyncPacket packet = new EvButtonSyncPacket();
				packet.serverEntity = this;
				LeafiaCustomPacket.__start(packet).__sendToAll();
				//buttons.add(new OpenButton(this,1,9));
				//buttons.add(new CloseButton(this,-2,9));
			}
		}*/ // argh fuck it!!
		return true;
	}
	public static class EvButtonInteractPacket implements LeafiaCustomPacketEncoder {
		ElevatorEntity localEntity;
		String localId;
		EnumHand localHand;
		public EvButtonInteractPacket() {}
		public EvButtonInteractPacket(ElevatorEntity localEntity,String localId,EnumHand localHand) {
			this.localEntity = localEntity;
			this.localHand = localHand;
			this.localId = localId;
		}
		@Override
		public void encode(LeafiaBuf buf) {
			buf.writeInt(localEntity.getEntityId());
			buf.writeFifthString(new FifthString(localId));
			buf.writeByte(localHand.ordinal());
		}
		@Override
		public Consumer<MessageContext> decode(LeafiaBuf buf) {
			int id = buf.readInt();
			String pressedButton = buf.readFifthString().toString();
			EnumHand hand = EnumHand.values()[buf.readByte()];
			return (ctx)-> {
				EntityPlayerMP plr = ctx.getServerHandler().player;
				World world = plr.world;
				Entity get = world.getEntityByID(id);
				if (get != null && get instanceof ElevatorEntity) {
					ElevatorEntity entity = (ElevatorEntity)get;
					entity.onButtonServer(pressedButton,plr,hand);
				}
			};
		}
	}
	public static class EvButtonSyncPacket implements LeafiaCustomPacketEncoder {
		ElevatorEntity serverEntity;
		static int identifierBits = 3;
		@Override
		public void encode(LeafiaBuf buf) {
			buf.writeInt(serverEntity.getEntityId());
			buf.writeInt(serverEntity.buttons.size());
			for (ElevatorButton btn : serverEntity.buttons) {
				if (btn instanceof FloorButton) {
					buf.insert(0,identifierBits);
					buf.writeInt(((FloorButton)btn).floor);
					buf.writeFifthString(new FifthString(((FloorButton)btn).label));
				} else if (btn instanceof FireButton)
					buf.insert(1,identifierBits);
				else if (btn instanceof OpenButton)
					buf.insert(2,identifierBits);
				else if (btn instanceof CloseButton)
					buf.insert(3,identifierBits);
				else if (btn instanceof BellButton)
					buf.insert(4,identifierBits);
				buf.writeByte(btn.x);
				buf.writeByte(btn.y);
			}
		}
		@Override
		public Consumer<MessageContext> decode(LeafiaBuf buf) {
			int id = buf.readInt();
			int btnCount = buf.readInt();
			return (ctx)->{
				World world = Minecraft.getMinecraft().world;
				Entity get = world.getEntityByID(id);
				if (get != null && get instanceof ElevatorEntity) {
					ElevatorEntity entity = (ElevatorEntity)get;
					entity.buttons.clear();
					for (int i = 0; i < btnCount; i++) {
						int buttonType = buf.extract(identifierBits);
						switch(buttonType) {
							case 0: entity.buttons.add(new FloorButton(entity,buf.readInt(),buf.readFifthString().toString(),buf.readByte(),buf.readByte())); break;
							case 1: entity.buttons.add(new FireButton(entity,buf.readByte(),buf.readByte())); break;
							case 2: entity.buttons.add(new OpenButton(entity,buf.readByte(),buf.readByte())); break;
							case 3: entity.buttons.add(new CloseButton(entity,buf.readByte(),buf.readByte())); break;
							case 4: entity.buttons.add(new BellButton(entity,buf.readByte(),buf.readByte())); break;
						}
					}
				}
			};
		}
	}
	public static class EvButtonEnablePacket implements LeafiaCustomPacketEncoder {
		ElevatorEntity serverEntity;
		public EvButtonEnablePacket() {}
		public EvButtonEnablePacket(ElevatorEntity serverEntity) {
			this.serverEntity = serverEntity;
		}
		@Override
		public void encode(LeafiaBuf buf) {
			buf.writeInt(serverEntity.getEntityId());
			buf.writeByte(serverEntity.enabledButtons.size());
			for (String id : serverEntity.enabledButtons)
				buf.writeFifthString(new FifthString(id));
		}
		@Override
		public Consumer<MessageContext> decode(LeafiaBuf buf) {
			int id = buf.readInt();
			String[] enableds = new String[buf.readByte()];
			for (int i = 0; i < enableds.length; i++)
				enableds[i] = buf.readFifthString().toString();
			return (ctx)-> {
				World world = Minecraft.getMinecraft().world;
				Entity get = world.getEntityByID(id);
				if (get != null && get instanceof ElevatorEntity) {
					ElevatorEntity entity = (ElevatorEntity)get;
					entity.enabledButtons.clear();
					entity.enabledButtons.addAll(Arrays.asList(enableds));
				}
			};
		}
	}
	public static class EvButtonClickPacket implements LeafiaCustomPacketEncoder {
		ElevatorEntity serverEntity;
		String serverId;
		public EvButtonClickPacket() {}
		public EvButtonClickPacket(ElevatorEntity serverEntity,String serverId) {
			this.serverEntity = serverEntity;
			this.serverId = serverId;
		}
		@Override
		public void encode(LeafiaBuf buf) {
			buf.writeInt(serverEntity.getEntityId());
			buf.writeFifthString(new FifthString(serverId));
		}
		@Override
		public Consumer<MessageContext> decode(LeafiaBuf buf) {
			int id = buf.readInt();
			String pressedButton = buf.readFifthString().toString();
			return (ctx)-> {
				World world = Minecraft.getMinecraft().world;
				Entity get = world.getEntityByID(id);
				if (get != null && get instanceof ElevatorEntity) {
					ElevatorEntity entity = (ElevatorEntity)get;
					entity.clickedButtons.put(pressedButton,0);
				}
			};
		}
	}
	public EvChipBase controller = null;
	public List<ElevatorButton> buttons = new ArrayList<>();
	public ElevatorEntity(World worldIn) {
		super(worldIn);
		updateHitSurfaces();
		controller = new EvChipS6(this);
		/*
		surfaces.put("wallF",new HitSrf(new FiaMatrix().rotateY(0).translate(0,0,-19/16d),-15/16d,0,15/16d,36/16d,4d/16d).setType(0));
		surfaces.put("wallL",new HitSrf(new FiaMatrix().rotateY(90).translate(0,0,-19/16d),-15/16d,0,15/16d,36/16d,4d/16d).setType(0));
		surfaces.put("wallB",new HitSrf(new FiaMatrix().rotateY(180).translate(0,0,-19/16d),-15/16d,0,15/16d,36/16d,4d/16d).setType(0));
		surfaces.put("wallR",new HitSrf(new FiaMatrix().rotateY(-90).translate(0,0,-19/16d),-15/16d,0,15/16d,36/16d,4d/16d).setType(0));
		*/
		buttons.add(new FloorButton(this,1,"★L",1,14));
		buttons.add(new FloorButton(this,-1,"-1",-2,14));
		buttons.add(new OpenButton(this,1,9));
		buttons.add(new CloseButton(this,-2,9));
		buttons.add(new FireButton(this,-2,20));
	}
	void updateHitSurfaces() {
		surfaces.clear();
		HitSrf floorSrf = new HitSrf(new FiaMatrix().rotateX(90),-15/16d,-15/16d,15/16d,15/16d,3d/16d).setType(-1);
		HitSrf ceilingSrf = new HitSrf(new FiaMatrix().rotateX(-90).translateWorld(0,36/16d,0),-15/16d,-15/16d,15/16d,15/16d,3d).setType(1);
		surfaces.put("floor",Collections.singletonList(floorSrf));
		surfaces.put("ceiling",Collections.singletonList(ceilingSrf));
		for (int i = 0; i < 4; i++) {
			EvWallBase wall = getWallInstance(i);
			List<HitSrf> srfs = wall.getHitSurfaces();
			for (HitSrf srf : srfs)
				srf.mat = new FiaMatrix().rotateY(90*i).travel(srf.mat);
			surfaces.put("wall"+i,srfs);
		}
	}
	public static class HitSrf {
		public FiaMatrix mat;
		public double x0;
		public double x1;
		public double y0;
		public double y1;
		double depth;
		public boolean enabled = true;
		public int type = 0; // 0 = wall, -1 = floor, 1 = ceiling
		public HitSrf(FiaMatrix mat,double x0,double y0,double x1,double y1,double depth) {
			this.mat = mat;
			this.x0 = x0;
			this.x1 = x1;
			this.y0 = y0;
			this.y1 = y1;
			this.depth = depth;
		}
		public HitSrf setType(int t) { this.type = t; return this; }
	}
	public static Vec3d collideVector(Vec3d pt,Vec3d vel,double length,HitSrf srf,double x,double y,double z,double yaw) {
		FiaMatrix mat = new FiaMatrix(new Vec3d(x,y,z)).rotateY(-yaw).travel(srf.mat).translate(0,0,srf.depth/2);

		FiaMatrix relative = mat.toObjectSpace(new FiaMatrix(pt));
		if (relative.getX() < srf.x0) return null;
		if (relative.getX() > srf.x1) return null;
		if (relative.getY() < srf.y0) return null;
		if (relative.getY() > srf.y1) return null;
		//if (relative.getZ() < -srf.depth/2) return null;
		//if (relative.getZ() > srf.depth/2) return null;
		FiaMatrix velRelative = new FiaMatrix().rotateAlong(mat).toObjectSpace(new FiaMatrix(vel));
		FiaMatrix horizontalVel = velRelative.scale(1/velRelative.getZ());
		//LeafiaDebug.debugLog(Minecraft.getMinecraft().world,"velocity: "+velRelative.position);
		/*if (relative.getZ() < srf.depth/2 && relative.getZ() > -srf.depth/2-length) {//velRelative.getZ() > 0) {
			//LeafiaDebug.debugLog(Minecraft.getMinecraft().world,"relative: "+relative.getZ());
			double wedge = -srf.depth-relative.getZ()-length;
			//LeafiaDebug.debugLog(Minecraft.getMinecraft().world,"wedge: "+wedge);
			FiaMatrix intersection = relative.add(horizontalVel.scale(wedge).position);
			//LeafiaDebug.debugLog(Minecraft.getMinecraft().world,intersection.position);
			if (intersection.getX() < srf.x0) return null;
			if (intersection.getX() > srf.x1) return null;
			if (intersection.getY() < srf.y0) return null;
			if (intersection.getY() > srf.y1) return null;
			Vec3d vec = mat.toWorldSpace(new FiaMatrix(new Vec3d(relative.getX(),relative.getY(),-srf.depth/2-length))).position;
			//LeafiaDebug.debugPos(Minecraft.getMinecraft().world,new BlockPos(vec),1/20f,0xFF0000,"Move");
			return vec;
		}
		if (relative.getZ() > srf.depth/2 && relative.getZ() < srf.depth/2+length) {//velRelative.getZ() < 0) {
			double wedge = srf.depth+relative.getZ()+length;
			FiaMatrix intersection = relative.subtract(horizontalVel.scale(wedge).position);
			if (intersection.getX() < srf.x0) return null;
			if (intersection.getX() > srf.x1) return null;
			if (intersection.getY() < srf.y0) return null;
			if (intersection.getY() > srf.y1) return null;
			return mat.toWorldSpace(new FiaMatrix(new Vec3d(relative.getX(),relative.getY(),srf.depth/2+length))).position;
			//LeafiaDebug.debugPos(Minecraft.getMinecraft().world,new BlockPos(vec),1/20f,0xFF0000,"Move");
		}*/
		if (relative.getZ() < 0 && relative.getZ() > -srf.depth/2-length) {
			Vec3d vec = mat.toWorldSpace(new FiaMatrix(new Vec3d(relative.getX(),relative.getY(),-srf.depth/2-length))).position;
			//LeafiaDebug.debugPos(Minecraft.getMinecraft().world,new BlockPos(vec),1/20f,0xFF0000,"Move");
			return vec;
		}
		else if (relative.getZ() > 0 && relative.getZ() < srf.depth/2+length) {
			return mat.toWorldSpace(new FiaMatrix(new Vec3d(relative.getX(),relative.getY(),srf.depth/2+length))).position;
		}
		return null;
	}
	public static Vec3d killVelocity(Vec3d velocity,HitSrf srf,double yaw) {
		FiaMatrix mat = new FiaMatrix().rotateY(-yaw).rotateAlong(srf.mat);
		FiaMatrix relative = mat.toObjectSpace(new FiaMatrix(velocity));
		Vec3d vec = mat.toWorldSpace(new FiaMatrix(new Vec3d(relative.getX(),relative.getY(),0))).position;
		return vec;
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player,EnumHand hand) {
		if (!world.isRemote) {
			//world.createExplosion(null,posX,posY,posZ,1,false);
		}
		return true;
	}


	public void processEntity(Entity e) {
		if (getEntityBoundingBox().expand(0,-Math.min(0,motionY)*4-e.height,0).contains(new Vec3d(e.posX,e.posY,e.posZ))) {
			e.fallDistance = 0;
			e.setPosition(e.posX+motionX,e.posY+motionY,e.posZ+motionZ);
			if (e.posY < posY+motionY)
				e.setPosition(e.posX,posY+motionY,e.posZ); // anti-fallthrough
		}
		for (List<HitSrf> srfs : surfaces.values()) {
			for (HitSrf srf : srfs) {
				Vec3d pushed = null;
				if (srf.type == -1) {
					pushed = collideVector(new Vec3d(e.posX,e.posY,e.posZ),new Vec3d(e.motionX,e.motionY,e.motionZ),0,srf,posX,posY,posZ,rotationYaw);
				} else if (srf.type == 0) {
					AxisAlignedBB bounding = e.getEntityBoundingBox();
					double thickness = (bounding.maxX-bounding.minX)/4+(bounding.maxZ-bounding.minZ)/4;
					pushed = collideVector(new Vec3d(e.posX,e.posY+e.height/2,e.posZ),new Vec3d(e.motionX,e.motionY,e.motionZ),thickness,srf,posX,posY,posZ,rotationYaw);
					if (pushed != null) pushed = pushed.subtract(0,e.height/2,0);
				} else if (srf.type == 1) {
					pushed = collideVector(new Vec3d(e.posX,e.posY+e.height,e.posZ),new Vec3d(e.motionX,e.motionY,e.motionZ),0,srf,posX,posY,posZ,rotationYaw);
					if (pushed != null) pushed = pushed.subtract(0,e.height,0);
				}
				if (pushed != null) {
					e.setPosition(pushed.x,pushed.y,pushed.z);
					Vec3d vel = killVelocity(new Vec3d(e.motionX,e.motionY,e.motionZ),srf,rotationYaw);
					e.setVelocity(vel.x,vel.y,vel.z);
				}
			}
		}
	}
	@Override
	public void onUpdate() {
		super.onUpdate();
		//this.rotationYaw+=45/20f;
		if (world.isRemote) {
			for (ElevatorButton button : buttons) button.onUpdate();
			for (String btn : clickedButtons.keySet()) {
				clickedButtons.put(btn,clickedButtons.get(btn)+1);
				if (clickedButtons.get(btn) > 4)
					clickedButtons.remove(btn);
			}
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (!player.isSpectator())
				processEntity(player);
			setPosition(posX+motionX,posY+motionY,posZ+motionZ);
		} else {
			AxisAlignedBB area = new AxisAlignedBB(posX-1.5,posY-0.2,posZ-1.5,posX+1.5,posY+2.5,posZ+1.5);
			List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this,area);
			for (Entity entity : entities) {
				if (!(entity instanceof EntityPlayer))
					processEntity(entity);
				else
					entity.fallDistance = 0;
			}
			if (pulley == null) {
				findPulley();
				if (pulley == null)
					setVelocity(motionX/2,motionY-9.8/400,motionZ/2);
			}
			for (int i = 0; i < 2; i++) {
				EnumFacing face = EnumFacing.byHorizontalIndex(i);
				if (world.getBlockState(new BlockPos(posX+0.5,posY+0.5,posZ+0.5).add(face.getDirectionVec())).getBlock() instanceof EvShaft) {
					setVelocity(0,motionY,0);
					break;
				}
			}
			if (controller != null)
				controller.onUpdate();
			setPosition(posX+motionX,posY+motionY,posZ+motionZ);
			double prevMotionY = motionY;
			doBlockCollisions();
			boolean collided = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / (double)2.0F, this.posZ);
			if (collided && prevMotionY < -15/20d) {
				boolean foundBuffer = false;
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						if (world.getBlockState(new BlockPos(posX+0.5+i,posY-0.5,posZ+0.5+j)).getBlock() instanceof EvBuffer) {
							foundBuffer = true;
							break;
						}
					}
				}
				if (!foundBuffer)
					world.createExplosion(null,posX,posY,posZ,5,false);
			}
		}
	}
	public void onButtonServer(String id,EntityPlayer player,EnumHand hand) {
		if (id.equals("fire")) {
			buttons.clear();
			dataManager.set(STYLE_BACK,"s6wall");
			buttons.add(new FloorButton(this,1,"★L",-2,14));
			buttons.add(new FloorButton(this,2,"2",1,14));
			EvButtonSyncPacket packet = new EvButtonSyncPacket();
			packet.serverEntity = this;
			LeafiaCustomPacket.__start(packet).__sendToAll();
			buttons.add(new OpenButton(this,1,9));
			buttons.add(new CloseButton(this,-2,9));
		} else LeafiaCustomPacket.__start(new EvButtonClickPacket(this,id)).__sendToAll();
		LeafiaCustomPacket.__start(new EvButtonEnablePacket(this)).__sendToAll();
	}
	@Override
	public boolean canBeCollidedWith() {
		return !this.isDead;
	}
	@Nullable
	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		return null;//new AxisAlignedBB(posX-19/16d,posY-3/16d,posX-19/16d,posX+19/16d,posY,posZ+19/16d);
	}
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox()
	{
		return new AxisAlignedBB(posX-19/16d,posY-3/16d,posX-19/16d,posX+19/16d,posY,posZ+19/16d);
	}

	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		return new AxisAlignedBB(posX-20/16d,posY-4/16d,posX-20/16d,posX+20/16d,posY+40/16d,posZ+20/16d);
	}
	@Override
	public List<AxisAlignedBB> getCollisionBoxes(Entity other) {
		if (new Vec3d(posX,posY+39/32d,posZ).distanceTo(new Vec3d(other.posX,other.posY+other.height/2,other.posZ)) <  1.25) {
			List<AxisAlignedBB> list = new ArrayList<>();
			//list.add(new AxisAlignedBB(posX-19/16d,posY+36/16d,posX-19/16d,posX+19/16d,posY+39/16d,posZ+19/16d));
			return list;
		}
		return null;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		if (controller != null)
			controller.readEntityFromNBT(compound);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		if (controller != null)
			controller.writeEntityToNBT(compound);
	}
}
