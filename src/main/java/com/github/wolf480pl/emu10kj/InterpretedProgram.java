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
    public final int MAX_VALUE = 0x7FFFFFFF;
    public final int MIN_VALUE = 0x80000000;

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
    public long run(long acc, int[] gpr, int[] itram, int[] xtram) {
        for (Instruction instr : code) {
            byte opcode = instr.getOpcode();
            // TODO: implement TRAM
            switch (opcode) {
                case Opcodes.MACS:
                case Opcodes.MACSN:
                case Opcodes.MACW:
                case Opcodes.MACWN: {
                    acc += gprOrAcc(instr.getRegA(), acc, gpr);
                    long x = gpr[instr.getRegX()];
                    long y = gpr[instr.getRegY()];
                    acc = mac(acc, gpr, instr.getRegR(), x, y, 31, (opcode & (byte) 0x1) != 0, (opcode & (byte) 0x2) == 0);
                }
                break;
                case Opcodes.MACINTS:
                case Opcodes.MACINTW: {
                    acc += gprOrAcc(instr.getRegA(), acc, gpr);
                    long x = gpr[instr.getRegX()];
                    long y = gpr[instr.getRegY()];
                    /*
                     * TODO: With MACINTW, the result is wrapped around but the
                     * sign bit (bit 31) is zeroed. Essentially the wrap around
                     * occurs around bit 30 instead of bit 31 (I have no idea
                     * why this would be useful).
                     */
                    acc = mac(acc, gpr, instr.getRegR(), x, y, 0, false, (opcode & (byte) 0x1) == 0);
                }
                break;
                case Opcodes.ACC3: {
                    acc += gprOrAcc(instr.getRegA(), acc, gpr);
                    long x = gpr[instr.getRegX()];
                    long y = gpr[instr.getRegY()];
                    acc += x + y;
                    gpr[instr.getRegR()] = (int) clamp(acc);
                }
                break;
                case Opcodes.MACMV: {
                    gpr[instr.getRegR()] = (int) gprOrAcc(instr.getRegA(), acc, gpr);
                    long x = gpr[instr.getRegX()];
                    long y = gpr[instr.getRegY()];
                    // TODO: You sure there's no shift here?
                    acc += x * y;
                }
                break;
                case Opcodes.ANDXOR:
                    acc = gpr[instr.getRegR()] = (gpr[instr.getRegA()] & gpr[instr.getRegX()]) ^ gpr[instr.getRegY()];
                    break;
                case Opcodes.TESTNEG: {
                    int x = gpr[instr.getRegX()];
                    acc = gpr[instr.getRegR()] = (gpr[instr.getRegA()] >= gpr[instr.getRegY()]) ? x : -x;
                }
                break;
                case Opcodes.LIMIT:
                case Opcodes.LIMITL: {
                    int x = gpr[instr.getRegX()];
                    int y = gpr[instr.getRegY()];
                    boolean neg = (opcode & (byte) 0x1) > 0;
                    acc = gpr[instr.getRegR()] = ((gpr[instr.getRegA()] >= y) != neg) ? x : y;
                }
                break;

            }
        }
        return acc;
    }

    private long mac(long acc, int[] gpr, int regR, long x, long y, int shift, boolean neg, boolean sat) {
        if (neg) {
            x = -x;
        }
        acc += (x * y) >> shift;
        gpr[regR] = (int) (sat ? clamp(acc) : acc);
        return acc;
    }

    private long clamp(long x) {
        if (x > MAX_VALUE) {
            return MAX_VALUE;
        } else if (x < MIN_VALUE) {
            return MIN_VALUE;
        }
        return x;
    }

    private static long gprOrAcc(int idx, long acc, int[] gpr) {
        if (idx == -1) {
            return acc;
        }
        return gpr[idx];
    }

}
