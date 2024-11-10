package com.hbm.tileentity.leafia.pwr;

import com.hbm.leafialib.math.MathLeafia;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class PWRDiagnosis {
    public static final Set<PWRDiagnosis> ongoing = new HashSet<>();
    public static void cleanup() {
        for (PWRDiagnosis task : ongoing) {
            if (task.checkTimeElapsed() >= 10_000)
                task.destroy();
        }
    }

    public int lastConfirmed;
    public final Set<BlockPos> activePos = new HashSet<>();
    public final Set<BlockPos> blockPos = new HashSet<>();
    public final Set<BlockPos> corePos = new HashSet<>();
    boolean closure = false;
    /*
    Creates PWRDiagnosis instance, and automatically adds to ongoing Set
     */
    public PWRDiagnosis() {
        confirmLife();
        ongoing.add(this);
    }
    public void addPosition(BlockPos pos) {
        if (closure) return;
        if (!blockPos.contains(pos)) {
            blockPos.add(pos);
            activePos.add(pos);
            confirmLife();
        }
    }
    public void removePosition(BlockPos pos) {
        if (closure) return;
        activePos.remove(pos);
        if (activePos.size() <= 0) {
            this.close();
        }
    }
    void close() {
        if (closure) return;
        closure = true;

    }
    public void confirmLife() {
        lastConfirmed = MathLeafia.getTime32s();
    }
    public int checkTimeElapsed() {
        return MathLeafia.getTimeDifference32s(MathLeafia.getTime32s(),lastConfirmed);
    }
    public void destroy() {
        if (ongoing.contains(this))
            ongoing.remove(this);
    }
}
