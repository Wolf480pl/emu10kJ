package com.github.wolf480pl.emu10kj;

import static com.github.wolf480pl.emu10kj.AddressSpaceUtils.empty;
import static com.github.wolf480pl.emu10kj.Instruction.instr;
import static com.github.wolf480pl.emu10kj.Opcodes.MACS;

import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class ScMain {
    public static final int SAMPLES_PER_TICK = 500;
    public static final int TPS = SoundcardIO.SAMPLING_RATE / SAMPLES_PER_TICK;
    public static final long TICK_MS = TimeUnit.SECONDS.toMillis(1) / TPS;

    public static void main(String[] args) throws LineUnavailableException {
        SourceDataLine out = AudioSystem.getSourceDataLine(SoundcardIO.FORMAT);
        TargetDataLine in = AudioSystem.getTargetDataLine(SoundcardIO.FORMAT);
        SoundcardIO sio = new SoundcardIO(out, in);
        sio.init();

        DSP dsp = new Emu10k1(sio, new CompositeIO(empty(), empty()), empty());

        dsp.loadProgram(testProgram());

        dsp.dspAddressSpace().write(VOL0_ADDR, 0x7fffffff);
        dsp.dspAddressSpace().write(VOL1_ADDR, 0x3fffffff);

        while (true) {
            sio.tick();
            dsp.tick();

        }

    }

    public static short VOL0_ADDR = 0x100; // GPR 0
    public static short VOL1_ADDR = 0x101; // GPR 1

    public static Instruction[] code0 = new Instruction[]{
            // -------- FX2(0) const0 FX(0)
            instr(MACS, 0x030, 0x040, 0x000, VOL0_ADDR),
            // -------- FX2(0) FX2(0) FX(1)
            instr(MACS, 0x030, 0x030, 0x001, VOL1_ADDR)
    };

    public static Program testProgram() {
        return new InterpretedProgram(code0, 2, 0, 0);
    }
}
