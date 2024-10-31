package com.hbm.core.leafia;

import com.hbm.main.leafia.WorldServerLeafia;
import com.hbm.main.leafia.leafiashader.LeafiaGls;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

import static com.hbm.core.leafia.TransformerCoreLeafia.classesBeingTransformed;
import static org.objectweb.asm.Opcodes.*;

public class LeafiaGlsTransformer implements IClassTransformer {
	static final int index = 0;
	@Override
	public byte[] transform(String name, String transformedName, byte[] classBeingTransformed) {
		//System.out.println("#Leaf: Transform Input: " + name + " : " + transformedName);
		boolean isObfuscated = !name.equals(transformedName);
		return (classesBeingTransformed[index].equals(transformedName))
				? TransformerCoreLeafia.transform(index, classBeingTransformed, isObfuscated)
				: classBeingTransformed;
	}
}
