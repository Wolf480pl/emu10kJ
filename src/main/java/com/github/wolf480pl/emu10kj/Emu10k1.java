/*
 * Copyright (c) 2015 Wolf480pl <wolf480@interia.pl>
 * This program is licensed under the GNU Lesser General Public License.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.wolf480pl.emu10kj;

import java.util.Random;

import static com.github.wolf480pl.emu10kj.AddressSpaceUtils.*;

public class Emu10k1 implements DSP {
    public static final short GPR_COUNT = 256;
    public static final short ITRAM_REGS = 128;
    public static final short XTRAM_REGS = 32;
    public static final int ITRAM_SIZE = 8192;

    public static final short FX_START = 0x0;
    public static final short FX_END = 0xf;
    public static final short EXT_IN_START = 0x10;
    public static final short EXT_IN_END = 0x1f;
    public static final short EXT_OUT_START = 0x20;
    public static final short EXT_OUT_END = 0x2f;
    public static final short FX2_START = 0x30;
    public static final short FX2_END = 0x3f;
    public static final short CONST_START = 0x40;
    public static final short CONST_END = 0x55;
    public static final short ACCU = 0x56;
    public static final short CCR = 0x57;
    public static final short NOISE1 = 0x58;
    public static final short NOISE2 = 0x59;
    public static final short INTERRUPT = 0x5a;
    public static final short DBAC = 0x5b;
    public static final short GPR_START = 0x100;
    public static final short GPR_END = GPR_START + GPR_COUNT - 1;
    public static final short ITRAM_DATA_START = 0x200;
    public static final short ITRAM_DATA_END = ITRAM_DATA_START + ITRAM_REGS - 1;
    public static final short XTRAM_DATA_START = 0x280;
    public static final short XTRAM_DATA_END = XTRAM_DATA_START + XTRAM_REGS - 1;
    public static final short ITRAM_ADDR_START = 0x300;
    public static final short ITRAM_ADDR_END = ITRAM_ADDR_START + ITRAM_REGS - 1;
    public static final short XTRAM_ADDR_START = 0x380;
    public static final short XTRAM_ADDR_END = XTRAM_ADDR_START + XTRAM_REGS - 1;

    private final IO fxbus, extIO;
    private long accu;
    private int ccr;
    private Random rng1, rng2;
    private int noise1, noise2;
    private int dbac;
    private TramSpace.OffsetReg tramOffset = new TramOffset();
    private final int[] gpr = new int[GPR_COUNT];
    private final int[] itramAddr = new int[ITRAM_REGS];
    private final int[] xtramAddr = new int[XTRAM_REGS];
    private final TRAM iTram = new TRAM(ITRAM_SIZE);
    private final TRAM xTram;
    private final AddressSpace dspSpace;

    public Emu10k1(IO fxbus, IO extIO, TRAM xTram) {
        this.fxbus = fxbus;
        this.extIO = extIO;
        this.xTram = xTram;
        this.dspSpace = split(2, 
                split(2, 
                        split(4, fxbus.inputSpace(), extIO.inputSpace(), extIO.outputSpace(), fxbus.outputSpace()),
                        split(1, new SysSpace(), empty()),
                        empty(), empty()),
                arr(gpr),
                split(1, arr(itramAddr), arr(xtramAddr)),
                split(1, tram(iTram, tramOffset, itramAddr), tram(xTram, tramOffset, xtramAddr)));
    }

    @Override
    public int readMemDsp(short address) {
        if (address <= FX_END) {
            return fxbus.readIn(address - FX_START);
        } else if (address <= EXT_IN_END) {
            return extIO.readIn(address - EXT_IN_START);
        } else if (address <= EXT_OUT_END) {
            return extIO.readOut(address - EXT_OUT_START);
        } else if (address <= FX2_END) {
            return fxbus.readOut(address - FX2_START);
        } else if (address <= CONST_END) {
            return CONSTS[address - CONST_START];
        } else if (address == ACCU) {
            /*
             * From DSP perspective, ACCU can be only read with
             * readMemOrAccuDsp() (i.e. when used as A) or with
             * readAccu() (in case of MACMV).
             * Otherwise, it's just zero.
             */
            return 0;
        } else if (address == CCR) {
            return ccr;
        } else if (address == NOISE1) {
            return noise1;
        } else if (address == NOISE2) {
            return noise2;
        } else if (address == INTERRUPT) {
            return 0; // TODO: is it ok?
        } else if (address == DBAC) {
            return DBAC;
        } else if (address < GPR_START) {
            // Reserved / unknown
            return 0;
        } else if (address <= GPR_END) {
            return gpr[address - GPR_START];
        } else if (address <= ITRAM_DATA_END) {
            int idx = address - ITRAM_DATA_START;
            return iTram.read(tramAddr(itramAddr[idx]));
        } else if (address <= XTRAM_DATA_END) {
            int idx = address - XTRAM_DATA_START;
            return xTram.read(tramAddr(xtramAddr[idx]));
        } else if (address < ITRAM_ADDR_START) {
            // Reserved
            return 0;
        } else if (address <= ITRAM_ADDR_END) {
            int idx = address - ITRAM_DATA_START;
            return itramAddr[idx];
        } else if (address <= XTRAM_ADDR_END) {
            int idx = address - XTRAM_DATA_START;
            return xtramAddr[idx];
        } else {
            // Reserved
            return 0;
        }
    }

    private int tramAddr(int offset) {
        return 0; // TODO
    }

    @Override
    public void writeMemDsp(short address, int value) {
        // TODO Auto-generated method stub

    }

    @Override
    public long readAccu() {
        return accu;
    }

    @Override
    public void writeAccu(long value) {
        accu = value;
    }

    @Override
    public long readMemOrAccuDsp(short address) {
        if (address == ACCU) {
            return accu;
        }
        return readMemDsp(address);
    }

    private static final int[] CONSTS = new int[] { 0, 1, 2, 3, 4, 8, 0x10, 0x20, 0x100, 0x10000, 0x80000, 0x10000000,
            0x20000000, 0x40000000, 0x80000000, 0x7fffffff, 0xffffffff, 0xfffffffe, 0xc0000000, 0x4f1bbcdc, 0x5a7ef9db, 0x00100000 };
    public static final Constants CONSTANTS = new Constants(CONSTS);
    
    protected class SysSpace implements AddressSpace {
        @Override
        public int read(int addr) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void write(int addr, int value) {
            // TODO Auto-generated method stub
            
        }
    }
    
    protected class TramOffset implements TramSpace.OffsetReg {
        @Override
        public int get() {
            return dbac;
        }
    }

}
