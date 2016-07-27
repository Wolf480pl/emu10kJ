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
        // System.out.println("out: " + outL + " " + outL);
        buf.clear();
        buf.putShort(outL);
        buf.putShort(outR);
        out.write(buf.array(), 0, buf.position());
        // out.drain();

        buf.clear();
        int red = in.read(buf.array(), buf.position(), 4);
        inL = buf.getShort();
        inR = buf.getShort();
        if (red < 4) {
            System.out.println(red);
        }
        // System.out.println("in: " + inL + " " + inL);
    }

    @Override
    public int readIn(int addr) {
        switch (addr) {
            case 0:
                // System.out.println("read L " + inL);
                return inL;
            case 1:
                // System.out.println("read R " + inR);
                return inR;
            default:
                return 0;
        }
    }

    @Override
    public void writeIn(int addr, int value) {
        switch (addr) {
            case 0:
                System.out.println("write inL " + inL);
                inL = (short) (value >> 32);
                break;
            case 1:
                System.out.println("write inR " + inR);
                inR = (short) (value >> 32);
                break;
            default:
                System.out.println("write inaddr " + addr);
                break;
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
                // System.out.println("write L " + outL);
                outL = (short) (value >> 32);
                break;
            case 1:
                // System.out.println("write R " + outR);
                outR = (short) (value >> 32);
                break;
            default:
                System.out.println("write addr " + addr);
        }
    }
}

