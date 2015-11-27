
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

import static com.github.wolf480pl.emu10kj.AddressSpaceUtils.arr;
import static com.github.wolf480pl.emu10kj.AddressSpaceUtils.empty;
import static com.github.wolf480pl.emu10kj.AddressSpaceUtils.split;
import static com.github.wolf480pl.emu10kj.AddressSpaceUtils.tram;

import java.util.Random;

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
    private int dbac = 0;
    private TramSpace.OffsetReg tramOffset = new TramOffset();
    private final int[] gpr = new int[GPR_COUNT];
    private final int[] itramAddr = new int[ITRAM_REGS];
    private final int[] xtramAddr = new int[XTRAM_REGS];
    private final TRAM iTram = new TRAM(ITRAM_SIZE);
    private final AddressSpace xTram;
    private final AddressSpace dspSpace;
    private Program program = null;

    public Emu10k1(IO fxbus, IO extIO, AddressSpace xTram) {
        this.fxbus = fxbus;
        this.extIO = extIO;
        this.xTram = xTram;
        this.dspSpace = split(10, 2,
                split(8, 2,
                        split(6, 2, fxbus.inputSpace(), extIO.inputSpace(), extIO.outputSpace(), fxbus.outputSpace()),
                        split(6, 1, new SysSpace(), empty()),
                        empty(), empty()),
                arr(gpr),
                split(8, 1, arr(itramAddr), arr(xtramAddr)),
                split(8, 1, tram(iTram, tramOffset, itramAddr), tram(xTram, tramOffset, xtramAddr)));

        this.rng1 = new Random();
        this.rng2 = new Random();
    }

    @Override
    public int readMemDsp(short address) {
        return dspSpace.read(address);
    }

    @Override
    public void writeMemDsp(short address, int value) {
        dspSpace.write(address, value);
    }

    @Override
    public AddressSpace dspAddressSpace() {
        return dspSpace;
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

    @Override
    public void loadProgram(Program program) {
        this.program = program;
    }

    @Override
    public void tick() {
        this.noise1 = rng1.nextInt();
        this.noise2 = rng2.nextInt();
        program.run(this);
        ++dbac;
    }

    private static final int[] CONSTS = new int[] { 0, 1, 2, 3, 4, 8, 0x10, 0x20, 0x100, 0x10000, 0x80000, 0x10000000,
            0x20000000, 0x40000000, 0x80000000, 0x7fffffff, 0xffffffff, 0xfffffffe, 0xc0000000, 0x4f1bbcdc, 0x5a7ef9db, 0x00100000 };
    public static final Constants CONSTANTS = new Constants(CONSTS);

    protected class SysSpace implements AddressSpace {
        public static final int L_ACCU = ACCU - CONST_START;
        public static final int L_CCR = CCR - CONST_START;
        public static final int L_NOISE1 = NOISE1 - CONST_START;
        public static final int L_NOISE2 = NOISE2 - CONST_START;
        public static final int L_INTERRUPT = INTERRUPT - CONST_START;
        public static final int L_DBAC = DBAC - CONST_START;

        @Override
        public int read(int addr) {
            if (addr < CONSTS.length) {
                return CONSTS[addr];
            }
            switch (addr) {
                case L_ACCU:
                    /*
                     * From DSP perspective, ACCU can be only read with
                     * readMemOrAccuDsp() (i.e. when used as A) or with
                     * readAccu() (in case of MACMV).
                     * Otherwise, it's just zero.
                     */
                    return 0;
                case L_CCR:
                    return ccr;
                case L_NOISE1:
                    return noise1;
                case L_NOISE2:
                    return noise2;
                case L_INTERRUPT:
                    return 0; // TODO: is it ok?
                case L_DBAC:
                    return dbac;
                default:
                    // Reserved / unknown
                    return 0;
            }
        }

        @Override
        public void write(int addr, int value) {
            if (addr < CONSTS.length) {
                return; // No writing to constants, baka!
            }
            switch (addr) {
                case L_ACCU:
                case L_CCR:
                    /*
                     * No reason to write to these, as they'll get
                     * immediately overwriten (during the same instruction)
                     */
                    // falls thru to NOISE
                case L_NOISE1:
                case L_NOISE2:
                    // read only
                    break;
                case L_INTERRUPT:
                    if (value < 0) {
                        //TODO: trigger interrupt
                    }
                    break;
                case L_DBAC:
                    // TODO should we allow this?
                    dbac = value;
                    break;
                default:
                    // Reserved / unknown, so NOP
                    break;
            }
        }
    }

    protected class TramOffset implements TramSpace.OffsetReg {
        @Override
        public int get() {
            return dbac;
        }
    }

}
