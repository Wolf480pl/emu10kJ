package com.github.wolf480pl.emu10kj;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class SoundcardIO extends AbstractIO {
    public static final int SAMPLING_RATE = 48000;
    public static final AudioFormat FORMAT = new AudioFormat(SAMPLING_RATE, 16, 2, true, true);

    private short inL, inR, outL, outR;
    private final SourceDataLine out;
    private final TargetDataLine in;
    private final ByteBuffer buf = ByteBuffer.allocate(4);

    public SoundcardIO(SourceDataLine out, TargetDataLine in) {
        this.in = in;
        this.out = out;
    }

    public void init() throws LineUnavailableException {
        in.open(FORMAT);
        out.open(FORMAT);
        in.start();
        out.start();
    }

    public void tick() {
        buf.clear();
        buf.putShort(outL);
        buf.putShort(outR);
        out.write(buf.array(), 0, buf.position());

        buf.clear();
        int red = in.read(buf.array(), buf.position(), 4);
        inL = buf.getShort();
        inR = buf.getShort();
    }

    @Override
    public int readIn(int addr) {
        switch (addr) {
            case 0:
                return inL;
            case 1:
                return inR;
            default:
                return 0;
        }
    }

    @Override
    public void writeIn(int addr, int value) {
        switch (addr) {
            case 0:
                inL = (short) (value >> 32);
            case 1:
                inR = (short) (value >> 32);
            default:
        }
    }

    @Override
    public int readOut(int addr) {
        switch (addr) {
            case 0:
                return outL;
            case 1:
                return outR;
            default:
                return 0;
        }
    }

    @Override
    public void writeOut(int addr, int value) {
        switch (addr) {
            case 0:
                outL = (short) (value >> 32);
            case 1:
                outR = (short) (value >> 32);
            default:
        }
    }
}

