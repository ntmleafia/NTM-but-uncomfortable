package com.hbm.core.leafia;

import com.leafia.transformer.LeafiaOverlayDebug;
import com.leafia.transformer.WorldServerLeafia;
import com.leafia.transformer.LeafiaGls;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ALOAD;

public class TransformerCoreLeafia implements IClassTransformer {
	// fuck you in particular
	public static final String[] classesBeingTransformed = {
			"net.minecraft.client.renderer.GlStateManager",
			"net.minecraft.world.WorldServer",
			"net.minecraft.client.gui.GuiMainMenu"
	};
	@Override
	public byte[] transform(String name, String transformedName, byte[] classBeingTransformed) {
		//System.out.println("#Leaf: Transform Input: " + name + " : " + transformedName);
		boolean isObfuscated = !name.equals(transformedName);
		int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
		return index != -1 ? transform(index, classBeingTransformed, isObfuscated) : classBeingTransformed;
	}
	public static class LeafiaDevErrorGls extends RuntimeException {
		public LeafiaDevErrorGls(String s) {
			super(s);
		}
	}
	public static byte[] transform(int index,byte[] classBeingTransformed,boolean isObfuscated) {
		System.out.println("#Leaf: Transforming: " + classesBeingTransformed[index]);
		try {
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(classBeingTransformed);
			classReader.accept(classNode, 0);

			switch (index) {
				case 0:
					doTransform(classNode,isObfuscated,LeafiaGls.Handler.class,index);
					break;
				case 1:
					doTransform(classNode,isObfuscated,WorldServerLeafia.class,index);
					break;
				case 2:
					doTransform(classNode,isObfuscated,LeafiaOverlayDebug.class,index);// fuck you.period.
					break;
				default:
					throw new LeafiaDevErrorGls("#Leaf: Unexpected index "+index);
			}

			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(classWriter);
			System.out.println("#Leaf: Transform Complete: " + classesBeingTransformed[index] + " ("+classBeingTransformed.length+" -> "+classWriter.toByteArray().length+")");
			return classWriter.toByteArray();
		} catch (Exception e) {
			System.out.println("#Leaf ERROR: " + classesBeingTransformed[index]);
			e.printStackTrace();
			if (e instanceof LeafiaDevErrorGls)
				throw e;
		}
		System.out.println("#Leaf: Transform End: " + classesBeingTransformed[index]);
		return classBeingTransformed;
	}
	static final Map<String,String> furtherDeobf = new HashMap<>();
	static {
		//furtherDeobf.put();
		{
			furtherDeobf.put("func_179082_a","clearColor");
			furtherDeobf.put("func_179083_b","viewport");
			furtherDeobf.put("func_179084_k","disableBlend");
			furtherDeobf.put("func_179085_a","enableLight");
			furtherDeobf.put("func_179086_m","clear");
			furtherDeobf.put("func_179087_a","enableTexGenCoord");
			furtherDeobf.put("func_179088_q","enablePolygonOffset");
			furtherDeobf.put("func_179089_o","enableCull");
			furtherDeobf.put("func_179090_x","disableTexture2D");
			furtherDeobf.put("func_179091_B","enableRescaleNormal");
			furtherDeobf.put("func_179092_a","alphaFunc");
			furtherDeobf.put("func_179093_d","setFog");
			furtherDeobf.put("func_179094_E","pushMatrix");
			furtherDeobf.put("func_179095_a","setFogDensity");
			furtherDeobf.put("func_179096_D","loadIdentity");
			furtherDeobf.put("func_179097_i","disableDepth");
			furtherDeobf.put("func_179098_w","enableTexture2D");
			furtherDeobf.put("func_179099_b","popAttrib");
			furtherDeobf.put("func_179100_b","disableTexGenCoord");
			furtherDeobf.put("func_179101_C","disableRescaleNormal");
			furtherDeobf.put("func_179102_b","setFogStart");
			furtherDeobf.put("func_179103_j","shadeModel");
			furtherDeobf.put("func_179104_a","colorMaterial");
			furtherDeobf.put("func_179105_a","texGen");
			furtherDeobf.put("func_179106_n","disableFog");
			furtherDeobf.put("func_179107_e","cullFace");
			furtherDeobf.put("func_179108_z","enableNormalize");
			furtherDeobf.put("func_179109_b","translate");
			furtherDeobf.put("func_179110_a","multMatrix");
			furtherDeobf.put("func_179111_a","getFloat");
			//furtherDeobf.put("func_179112_b","blendFunc");
			furtherDeobf.put("func_179113_r","disablePolygonOffset");
			furtherDeobf.put("func_179114_b","rotate");
			furtherDeobf.put("func_179115_u","enableColorLogic");
			furtherDeobf.put("func_179116_f","colorLogicOp");
			furtherDeobf.put("func_179117_G","resetColor");
			furtherDeobf.put("func_179118_c","disableAlpha");
			furtherDeobf.put("func_179119_h","disableColorMaterial");
			//furtherDeobf.put("func_179120_a","tryBlendFuncSeparate");
			furtherDeobf.put("func_179121_F","popMatrix");
			furtherDeobf.put("func_179122_b","disableLight");
			furtherDeobf.put("func_179123_a","pushAttrib");
			furtherDeobf.put("func_179124_c","color");
			furtherDeobf.put("func_179125_c","texGenCoord");
			furtherDeobf.put("func_179126_j","enableDepth");
			furtherDeobf.put("func_179127_m","enableFog");
			furtherDeobf.put("func_179128_n","matrixMode");
			furtherDeobf.put("func_179129_p","disableCull");
			furtherDeobf.put("func_179130_a","ortho");
			furtherDeobf.put("func_179131_c","color");
			furtherDeobf.put("func_179132_a","depthMask");
			furtherDeobf.put("func_179133_A","disableNormalize");
			furtherDeobf.put("func_179134_v","disableColorLogic");
			furtherDeobf.put("func_179135_a","colorMask");
			furtherDeobf.put("func_179136_a","doPolygonOffset");
			furtherDeobf.put("func_179137_b","translate");
			furtherDeobf.put("func_179138_g","setActiveTexture");
			furtherDeobf.put("func_179139_a","scale");
			furtherDeobf.put("func_179140_f","disableLighting");
			furtherDeobf.put("func_179141_d","enableAlpha");
			furtherDeobf.put("func_179142_g","enableColorMaterial");
			furtherDeobf.put("func_179143_c","depthFunc");
			furtherDeobf.put("func_179144_i","bindTexture");
			furtherDeobf.put("func_179145_e","enableLighting");
			furtherDeobf.put("func_179146_y","generateTexture");
			furtherDeobf.put("func_179147_l","enableBlend");
			furtherDeobf.put("func_179148_o","callList");
			furtherDeobf.put("func_179149_a","texGen");
			furtherDeobf.put("func_179150_h","deleteTexture");
			furtherDeobf.put("func_179151_a","clearDepth");
			furtherDeobf.put("func_179152_a","scale");
			furtherDeobf.put("func_179153_c","setFogEnd");
			furtherDeobf.put("func_179198_a","setDisabled");
			furtherDeobf.put("func_179199_a","setState");
			furtherDeobf.put("func_179200_b","setEnabled");

			furtherDeobf.put("func_148821_a","blendFunc");
			furtherDeobf.put("func_179112_b","blendFunc");
			furtherDeobf.put("func_179120_a","tryBlendFuncSeparate");
			furtherDeobf.put("func_187401_a","blendFunc");
			furtherDeobf.put("func_187428_a","tryBlendFuncSeparate");

			furtherDeobf.put("func_180798_a","renderDebugInfoLeft");
		}
		furtherDeobf.put("func_73044_a","saveAllChunks");
	}
	public static class Helper {
		MethodNode method;
		public List<AbstractInsnNode> instructions;
		Class<?> listener;
		public Helper(MethodNode mthd, Class<?> listener) {
			method = mthd;
			instructions = new ArrayList<>();
			this.listener = listener;
		}
		public void stackManipulateStore(int op,int index) {
			instructions.add(new VarInsnNode(op,index));
		}
		public void stackPush(int op) {
			instructions.add(new InsnNode(op));
		}
		public void stackPushInt(int op,int value) {
			instructions.add(new IntInsnNode(op,value));
		}
		public void stackCall(String target,String desc) {
			//               V    Z    C    B    S     I   F     J    D      idk   L     (any)
			// descriptors: void bool char byte short int float long double array object method
			instructions.add(new MethodInsnNode(INVOKESTATIC,Type.getInternalName(listener),target,desc,false));
			System.out.println("#      Added method call "+target+" : "+desc);
		}
		public void bindDirectly(String name,String desc) {
			int index = 0;
			boolean referencing = false;
			int arrayCounter = 0;
			int ignoreCounter = 0;
			StringBuilder descArgs = new StringBuilder();
			for (String s : desc.split("")) {
				if (s.equals("(")) continue;
				if (s.equals(")")) break;
				if (referencing) {
					descArgs.append(s);
					if (s.equals("<"))
						ignoreCounter++;
					else if (s.equals(">"))
						ignoreCounter--;
					if (ignoreCounter <= 0) {
						if (s.equals(";")) {
							referencing = false;
							arrayCounter = 0;
						}
					}
				} else {
					if (!s.equals("x")) { // dummy for in case we wanted to skip some args
						if (s.equals("L")) {
							referencing = true;
							if (arrayCounter <= 0) {
								this.stackManipulateStore(ALOAD,index);
								index++;
							}
						} else if (s.equals("[")) {
							if (arrayCounter++ <= 0) {
								this.stackManipulateStore(ALOAD,index);
								index++;
							}
						} else if (arrayCounter <= 0) {
							switch (s) {
								case "Z": case "C": case "B": case "S":
								case "I": this.stackManipulateStore(ILOAD,index); break;
								case "F": this.stackManipulateStore(FLOAD,index); break;
								case "J": this.stackManipulateStore(LLOAD,index); break;
								case "D": this.stackManipulateStore(DLOAD,index); break;
							}
							index++;
						} else
							arrayCounter = 0;
						descArgs.append(s);
					} else index++;
				}
			}
			this.stackCall(name,"("+descArgs+")V");
		}
	}
	private static boolean tryBind(String name,String desc,Helper helper,int transformerIndex) {
		switch(transformerIndex) {
			case 0: {
				if (name.startsWith("enable") || name.startsWith("disable")) {
					for (String s : new String[]{
							"Blend","Alpha","Fog","Lighting","ColorMaterial","Texture2D", // shader.preInit
							"ColorLogic","Cull","Normalize","PolygonOffset","RescaleNormal","Depth" // randoms i found
					}) {
						if (name.endsWith(s)) {
							// thank https://godbolt.org/
							helper.stackPush(name.startsWith("enable") ? ICONST_1 : ICONST_0);
							helper.stackCall("rec" + s,"(Z)V");
							return true;
						}
					}
				}
				switch (name) {
					case "depthMask":
						helper.stackManipulateStore(ILOAD,0);
						helper.stackCall("depthMask","(Z)V");
						return true;
					case "depthFunc":
						helper.stackManipulateStore(ILOAD,0);
						helper.stackCall("depthFunc","(I)V");
						return true;
					case "blendFunc":
						if (desc.equals("(II)V")) {
							helper.stackManipulateStore(ILOAD,0);
							helper.stackManipulateStore(ILOAD,1);
							helper.stackCall("blendFunc","(II)V");
							return true;
						} else break;
					case "tryBlendFuncSeparate":
						if (desc.equals("(IIII)V")) {
							helper.stackManipulateStore(ILOAD,0);
							helper.stackManipulateStore(ILOAD,1);
							helper.stackManipulateStore(ILOAD,2);
							helper.stackManipulateStore(ILOAD,3);
							helper.stackCall("tryBlendFuncSeparate","(IIII)V");
							return true;
						} else break;
					case "alphaFunc":
					case "shadeModel":
						helper.bindDirectly(name,desc);
						return true;

					case "enableLight":
						helper.stackManipulateStore(ILOAD,0);
						helper.stackPush(ICONST_1);
						helper.stackCall("recLight","(IZ)V");
						return true;
					case "glEnableClientState":
						helper.stackManipulateStore(ILOAD,0);
						helper.stackPush(ICONST_1);
						helper.stackCall("recClientState","(IZ)V");
						return true;
					case "disableLight":
						helper.stackManipulateStore(ILOAD,0);
						helper.stackPush(ICONST_0);
						helper.stackCall("recLight","(IZ)V");
						return true;
					case "glDisableClientState":
						helper.stackManipulateStore(ILOAD,0);
						helper.stackPush(ICONST_0);
						helper.stackCall("recClientState","(IZ)V");
						return true;
				}
			} break;
			case 1:
				if (name.equals("saveAllChunks")) {
					helper.stackManipulateStore(ALOAD,0); // for instantized classes, var 0 will be "this" apparently (thanks https://godbolt.org/)
					helper.stackCall("saveAllChunks","(Lnet/minecraft/world/WorldServer;)V");
					return true;
				}
				break;
			case 2:
				for (AbstractInsnNode node : helper.method.instructions.toArray()) {
					// sneaky sneaky
					if (node instanceof LineNumberNode) {
						System.out.println("#    Line " + ((LineNumberNode) node).line);
						if (((LineNumberNode) node).line == 99) {
							helper.method.instructions.insert(
									node,
									new MethodInsnNode(
											INVOKESTATIC,Type.getInternalName(helper.listener),
											"injectWackySplashes","(Ljava/util/List;)V",false
									)
							);
							helper.method.instructions.insert(node,new VarInsnNode(ALOAD,2));
							System.out.println("#      Added method call injectWackySplashes : (Ljava/util/List;)V");
							return true;
						}
					}
					/*
					System.out.println("#      Node: "+Integer.toHexString(node.getOpcode()&0xFF)+" : "+node.getType()+" ["+node.getClass().getSimpleName()+"]");
					if (node instanceof MethodInsnNode)
						System.out.println("#      Method: "+((MethodInsnNode)node).owner+":"+((MethodInsnNode)node).name+"("+((MethodInsnNode)node).desc+")");
					else if (node instanceof VarInsnNode)
						System.out.println("#      Type: "+((VarInsnNode)node).getType()+", Var: "+((VarInsnNode)node).var);*/
				}
				/*
				if (name.equals("renderDebugInfoLeft")) {
					AbstractInsnNode lastLine = null;
					VarInsnNode lastALOAD = null;
					Integer listIndex = null;
					for (AbstractInsnNode node : helper.method.instructions.toArray()) {
						// sneaky sneaky
						//System.out.println("#      Node: "+Integer.toHexString(node.getOpcode()&0xFF)+" : "+node.getType()+" ["+node.getClass().getSimpleName()+"]");
						if (node instanceof LineNumberNode) {
							lastLine = node;
							lastALOAD = null;
						} else if (node instanceof VarInsnNode) {
							if (node.getOpcode() == ALOAD) {
								lastALOAD = (VarInsnNode)node;
							}
						} else if (node instanceof MethodInsnNode) {
							if (node.getOpcode() == INVOKEINTERFACE) {
								if (lastALOAD != null && listIndex == null)
									listIndex = lastALOAD.var;
							}
						} else if (node instanceof JumpInsnNode) { // we found a loop
							if (lastLine != null && listIndex != null) {
								helper.method.instructions.insertBefore(lastLine,new VarInsnNode(ALOAD,listIndex));
								helper.method.instructions.insertBefore(
										lastLine,
										new MethodInsnNode(
												INVOKESTATIC,Type.getInternalName(helper.listener),
												"injectDebugInfoLeft","(Ljava/util/List;)V",false
										)
								);
								System.out.println("#      Added method call injectDebugInfoLeft : (Ljava/util/List;)V");
								return true;
							}
						}
					}
				}*/
				// guess what's so sad, this whole thing was unnecessary
				// it was possible to modify using events from forge, in the most confusing name of just "Text" Bruh bro
				// Searchability -32768/10
				break;
		}
		return false;
	}
	private static void doTransform(ClassNode profilerClass,boolean isObfuscated,Class<?> listener,int transformIndex) {
		FMLDeobfuscatingRemapper pain = FMLDeobfuscatingRemapper.INSTANCE;
		System.out.println("#Leaf: Processing "+profilerClass.name);
		System.out.println("       srcFile: "+profilerClass.sourceFile);
		System.out.println("       outClass: "+profilerClass.outerClass);
		System.out.println("       outMthd: "+profilerClass.outerMethod);
		System.out.println("       outMthdDesc: "+profilerClass.outerMethodDesc);
		System.out.println("       signature: "+profilerClass.signature);
		System.out.println("       access: "+profilerClass.access);
		List<String> attempt = new ArrayList<>();
		for (MethodNode method : profilerClass.methods) {
			attempt.clear();
			Helper helper = new Helper(method,listener);
			System.out.println("#Leaf: Iterating "+method.name+" : "+ method.desc);
			String deobf = pain.mapMethodName(profilerClass.name,method.name,method.desc);
			if (deobf != null)
				attempt.add(deobf);
			System.out.println("#      De: "+deobf);
			if (furtherDeobf.containsKey(deobf)) {
				System.out.println("#      MCP deobf: " + furtherDeobf.get(deobf));
				attempt.add(furtherDeobf.get(deobf));
			} else
				System.out.println("#      MCP deobf was not able");

			for (int i = attempt.size()-1; i >= 0; i--) {
				String s = attempt.get(i);
				if (tryBind(s,method.desc,helper,transformIndex)) {
					if (transformIndex == 0)
						helper.stackCall("updateLastStack","()V");
					for (int o = helper.instructions.size()-1; o >= 0; o--)
						method.instructions.insert(helper.instructions.get(o));
					System.out.println("#      Patched!");
					break;
				} else if (helper.instructions.size() > 0)
					throw new LeafiaDevErrorGls("tryBind returned false despite modifying stack!");
				/*
				switch(s) {
					case "enableBlend":
						method.instructions.insert(new VarInsnNode(SIPUSH,3));
						// for the (S)V part, see {@link Type}
						method.instructions.insert(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(listener), "enableBlend", "(S)V", false));
						break;
					default: exit = false; break;
				}
				if (exit) {
					System.out.println("#      Patched!");
					break;
				}*/
			}
			System.out.println("#");
			/*
			if (method.name.equals("disableLighting") || method.name.equals("func_179140_f")) {
				System.out.println("#Leaf: Patching");
				method.instructions.insert(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(listener), "disableLighting", "()V", false));
			}
			if(method.name.equals("enableLighting") || method.name.equals("func_179145_e")){
				System.out.println("#Leaf: Patching");
				method.instructions.insert(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(listener), "enableLighting", "()V", false));
			}
			if(method.name.equals("translate") || method.name.equals("func_179137_b")){
				System.out.println("#Leaf: Patching");
				method.instructions.insert(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(listener), "translate", "()V", false));
			}
			if(method.name.equals("enableBlend") || method.name.equals("func_179147_l")){
				System.out.println("#Leaf: Patching");
				method.instructions.insert(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(listener), "enableBlend", "()V", false));
			}
			if(method.name.equals("disableBlend") || method.name.equals("func_179084_k")){
				System.out.println("#Leaf: Patching");
				method.instructions.insert(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(listener), "disableBlend", "()V", false));
			}*/
		}
	}
}
