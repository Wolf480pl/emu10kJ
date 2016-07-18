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

public class InterpretedProgram implements Program {
    public static final int MAX_VALUE = Integer.MAX_VALUE;
    public static final int MIN_VALUE = Integer.MIN_VALUE;

    private final Instruction[] code;
    private final int gprs, itram, xtram;

    public InterpretedProgram(Instruction[] code, int gprs, int itram, int xtram) {
        this.code = code;
        this.gprs = gprs;
        this.itram = itram;
        this.xtram = xtram;
    }

    @Override
    public int getInstrCount() {
        return code.length;
    }

    @Override
    public int getGPRCount() {
        return gprs;
    }

    @Override
    public int getItramSize() {
        return itram;
    }

    @Override
    public int getXtramSize() {
        return xtram;
    }

    @Override
    public void run(DSP dsp) {
        for (Instruction instr : code) {
            long acc;
            int r;

            long la = gprOrAcc(instr.getRegA(), dsp);
            int a = (int) la;

            int x = dsp.readMemDsp(instr.getRegX());
            int y = dsp.readMemDsp(instr.getRegY());
            // to ensure long multiplication/comparison/addition is used
            long lx = x;
            long ly = y;

            byte opcode = instr.getOpcode();
            switch (opcode) {
                case Opcodes.MACS:
                case Opcodes.MACSN:
                case Opcodes.MACW:
                case Opcodes.MACWN:
                    loadA(dsp, instr.getRegA());
                    mac(dsp, lx, ly, (opcode & (byte) 0x1) != 0);
                    writeR(dsp, instr.getRegR(), (opcode & (byte) 0x2) == 0, true)
                    //mac(la, dsp, instr.getRegR(), lx, ly, 31, (opcode & (byte) 0x1) != 0, (opcode & (byte) 0x2) == 0);
                    break;
                case Opcodes.MACINTS:
                case Opcodes.MACINTW:
                    /*
                     * TODO: With MACINTW, the result is wrapped around but the
                     * sign bit (bit 31) is zeroed. Essentially the wrap around
                     * occurs around bit 30 instead of bit 31 (I have no idea
                     * why this would be useful).
                     */
                    loadA(dsp, instr.getRegA());
                    mac(dsp, lx, ly, false);
                    writeR(dsp, instr.getRegR(), (opcode & (byte) 0x1) == 0, false);
                    //mac(la, dsp, instr.getRegR(), lx, ly, 0, false, (opcode & (byte) 0x1) == 0);
                    break;
                case Opcodes.ACC3:
                    /*
                    acc = la + lx + ly;
                    dsp.writeMemDsp(instr.getRegR(), (int) clamp(acc));
                    dsp.writeAccu(acc);
                    */
                    loadA(dsp, instr.getRegA());
                    final Accumulator accu = dsp.getAccu();
                    accu.add(lx);
                    accu.add(ly);
                    dsp.writeR(dsp, instr.getRegR(), true, false);
                    break;
                case Opcodes.MACMV:
                    // Even if A is accu and R is accu, it will get overwritten
                    // later anyway, so don't bother with longs
                    // TODO: Does it wrap or saturate?
                    dsp.writeMemDsp(instr.getRegR(), a);
                    mac(dsp, lx, ly, false);
                    /*
                    acc = dsp.readAccu();
                    acc += lx * ly;
                    dsp.writeAccu(acc);
                    */
                    break;
                case Opcodes.ANDXOR:
                    // Even if A is accu, the higher bits will be zero after
                    // anding with X, so don't bother with longs
                    acc = (a & x) ^ y;
                    wrAccAndR(dsp, instr.getRegR(), acc);

                    break;
                case Opcodes.TESTNEG:
                    // Use long for comparing A and Y, cause A can be accu.
                    // Don't use long for result, cause it's either X or -X,
                    // which is always 32 bits
                    r = (la >= ly) ? x : -x;
                    wrAccAndR(dsp, instr.getRegR(), r);

                    break;
                case Opcodes.LIMIT:
                case Opcodes.LIMITL: {
                    boolean neg = (opcode & (byte) 0x1) > 0;
                    // Use long for comparing A and Y, cause A can be accu.
                    // Don't use long for result, cause it's either X or Y,
                    // which is always 32 bits
                    // We use != as boolean XOR
                    r = ((la >= ly) != neg) ? x : y;
                    wrAccAndR(dsp, instr.getRegR(), r);
                }
                break;
                case Opcodes.LOG: {
                    // A is linear input, X is max_exp, Y is sign_reg
                    boolean negative = la < 0;
                    int lin = negative ? -a : a;
                    int shifts = 0;
                    while (lin > 0 && shifts >= x) {
                        lin = lin << 1;
                        ++shifts;
                    }
                    int exp = x - shifts;
                    if (exp < 0) {
                        exp = 0;
                    } else {
                        exp += 1;
                        lin = lin << 1;
                    }
                    int expbits = expBits(x);
                    lin = lin >>> expbits + 1;
                    exp = exp << 31 - expbits;
                    r = exp | lin;
                    /*
                     * y - sign_reg
                     * 00b - r -> r
                     * 01b - r -> abs(r)
                     * 10b - r -> -abs(r)
                     * 11b - r -> -r
                     */
                    int sign = negative ? ~(y & 0x1) : (y & 0x2) >> 1;
                    r |= sign << 31;
                    wrAccAndR(dsp, instr.getRegR(), r);
                }
                case Opcodes.EXP: {
                    // A is logarithmic input, X is max_exp, Y is sign_reg
                    boolean sign = a < 0;
                    int log = a & 0x7fffffff;
                    /*
                     * Ok, we can't recover the sign, so we can't reverse
                     * the sign_reg transformation. Soo... let's do it forward again!
                     */
                    boolean negative = sign ? ((y & 0x1) == 0) : ((y & 0x2) == 2);
                    int expbits = expBits(x);
                    int linbits = 31 - expbits;
                    int exp = log >>> linbits;
                    int lin = log & ((1 << linbits) - 1);
                    lin = lin << expbits + 1;
                    if (exp == 0) {
                        exp += 1;
                    } else {
                        lin = lin >>> 1;
                        lin |= 0x80000000;
                    }
                    int shifts = x - exp;
                    lin = lin >>> shifts;
                    r = negative ? -lin : lin;
                    wrAccAndR(dsp, instr.getRegR(), r);
                }
                break;
                case Opcodes.INTERP:
                    acc = la + (lx * (ly - la) >> 31);
                    wrAccAndR(dsp, instr.getRegR(), acc);
                    break;
                case Opcodes.SKIP:
                    // TODO: This is complicated, but we should implement it one day.
                    break;
            }
        }
    }

    private static int expBits(int maxExp) {
        int expbits = 0;
        while (maxExp > 0) {
            maxExp = maxExp >>> 1;
            ++expbits;
        }
        return expbits;
    }

    private static void wrAccAndR(DSP dsp, short regR, long acc) {
        dsp.writeMemDsp(regR, (int) acc);
        dsp.writeAccu(acc);
    }

    private static void wrAccAndR(DSP dsp, short regR, int r) {
        dsp.writeMemDsp(regR, r);
        dsp.writeAccu(r);
    }

    private static void accAndWrR(DSP dsp, short regR, long val, boolean sat) {
        final Accumulator accu = dsp.getAccu();
        accu.add(val);
        if (!dsp.isAccuAddr(regR)) {
            long r = accu.read();
            if (sat) {
                r = clamp(r);
            }
            dsp.writeMemDsp(regR, (int) r);
        }
    }

    private static void writeR(DSP dsp, short regR, boolean sat, boolean hi) {
        final Accumulator accu = dsp.getAccu();
        if (!dsp.isAccuAddr(regR)) {
            final int r;
            if (hi) {
                r = sat ? accu.readHiSat() : accu.readHiWrap();
            } else {
                r = sat ? accu.readLowSat() : accu.readLowWrap();
            }
            dsp.writeMemDsp(regR, r);
        }
    }

    private static void loadA(DSP dsp, short regA) {
        if (!dsp.isAccuAddr(regA)) {
            dsp.getAccu().write(dsp.readMemDsp(regA));
        }
    }

    private static void mac(long acc, DSP dsp, short regR, long x, long y, int shift, boolean neg, boolean sat) {
        if (neg) {
            x = -x;
        }
        long val = (x * y) >> shift;
        dsp.writeMemDsp(regR, (int) (sat ? clamp(acc) : acc));
        dsp.writeAccu(acc);
    }

    private static void mac(DSP dsp, long x, long y, boolean neg) {
        final Accumulator accu = dsp.getAccu();
        final long xy = x * y;
        accu.add(neg ? -xy : xy);
    }

    private static long clamp(long x) {
        if (x > MAX_VALUE) {
            return MAX_VALUE;
        } else if (x < MIN_VALUE) {
            return MIN_VALUE;
        }
        return x;
    }

    private static long gprOrAcc(short addr, DSP dsp) {
        return dsp.readMemOrAccuDsp(addr);
    }

}
