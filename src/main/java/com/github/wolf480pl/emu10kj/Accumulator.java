package com.github.wolf480pl.emu10kj;
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

/**
 * A 67-bit accumulator register
 *
 * NOTE: The implementation doesn't strictly adhere to as10k1 documentation
 *       but it does what makes sense to the author. If compatibility issues
 *       are discovered, this implementation might change.
 *
 *  6           6           3   2           0
 *  6 5 4 3 2 1 0           1 0 9         1 0
 * +-+-+-+-+-+-+-+- - - -+-+-+-+-+- - - -+-+-+
 * |S| Guard |   High Accu   |   Low Accu    |
 * +-+-+-+-+-+-+-+- - - -+-+-+-+-+- - - -+-+-+
 *
 * S - sign
 * Guard - 4 guard bits
 * High Accu - 31 high accumulator bits (retrived by MACS and ACC3, along with sign)
 * Low Accu - 31 low accumulator bits (retrived by MACINTS, along with sign)
 */
public class Accumulator {
    public static final int BITS = 67 - 1; // Subtract the sign bit
    public static final int INT_SIGN_MASK = 1 << 31;
    public static final long INT_SIGN_MASK_L = 1L << 31;

    /**
     * Little endian absolute value
     */
    private int[] value;
    private boolean negative;

    public Accumulator() {
        this.value = new int[3];
    }

    public long read() {
        /* Note: I know the highest bit of value[1] is being treated as a sign.
         *       This is the correct behavior. It may be useful for
         *       not-overflowing a sum of >2 numbers, where partial sums
         *       overflow, but the total doesn't.
         *
         *       If you ever doubt this works correctly, think of what happens
         *       to an 8-bit register when you do the following:
         *       127 + 1, -127 - 1, -127 - 2, and compare the results with what
         *       this method would do if the accumulator was 8-bit, with the
         *       sign bit as a separate boolean.
         */
        long x = ((long) value[1]) << 32 | (value[0] & 0xffffffffL);
        return negative ? -x : x;
    }

    public long readSat() {
        if (value[2] != 0 || (value[1] & INT_SIGN_MASK) != 0) {
            // Above long limit - saturate!
            return negative ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
        return read();
    }

    public int readLowWrap() {
        int x = value[0];
        // This is correct, see read()
        return negative ? -x : x;
    }

    public int readLowSat() {
        if (value[2] != 0 || value[1] != 0 || (value[0] & INT_SIGN_MASK) != 0) {
            // Overflow, so we saturate
            return negative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        return readLowWrap();
    }

    public int readHiWrap() {
        int x = (value[1] << 1) | (value[0] >>> 31);
        // This is correct, see read()
        return negative ? -x : x;
    }

    public int readHiSat() {
        if (value[2] != 0 || (value[1] & (INT_SIGN_MASK >> 1)) != 0) {
            // Overflow, so we saturate
            return negative ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        }
        return readHiWrap();
    }

    public void write(long val) {
        if (val < 0) {
            negative = true;
            val = -val;
        } else {
            negative = false;
        }
        value[0] = (int) val;
        value[1] = (int) (val >>> 32);
        value[2] = 0;
    }

    public void writeLow(int val) {
        write(val);
    }

    public void writeHi(int val) {
        write(((long) val) << 31);
    }

    public void add(long val) {
        if (val == 0) {
            return;
        }
        boolean valNeg = val < 0;
        if (valNeg) {
            val = -val;
        }
        long low = val & 0xffffffffL;
        long hi = val >>> 32;
        if (valNeg == negative) {
            add(hi, low);
            return;
        }
        int cmp = compareAbs(hi, low);
        if (cmp == 0) {
            value[0] = value[1] = value[2] = 0;
            negative = false;
            return;
        }
        if (cmp < 0) {
            // we have smaller magnitude than val, so we're at most 63 bits
            // swap the operands and change the resulting sign
            assert (value[2] == 0);
            long hi1 = value[1] & 0xffffffffL;
            value[1] = (int) hi;
            hi = hi1;
            long low1 = value[0] & 0xffffffffL;
            value[0] = (int) low;
            low = low1;
            negative = !negative;
        }
        subtract(hi, low);
    }

    protected void add(long hi, long low) {
        long v0 = low + (value[0] & 0xffffffffL);
        long v1 = hi + (value[1] & 0xffffffffL) + (v0 >>> 32);
        long v2 = value[2] + (v1 >>> 32);
        value[0] = (int) v0;
        value[1] = (int) v1;
        //TODO: implement accumulator signed overflow
        value[2] = (int) (v2 & ((1 << (BITS - 64)) - 1));
    }

    protected void subtract(long hi, long low) {
        long v0 = (value[0] & 0xffffffffL) - low;
        if (v0 < 0) {
            v0 += 1L << 32;
            hi += 1;
        }
        long v1 = (value[1] & 0xffffffffL) - hi;
        if (v1 < 0) {
            v1 += 1L << 32;
            value[2] -= 1;
        }
    }

    protected int compareAbs(long hi, long low) {
        if (value[2] != 0) {
            return 1;
        }
        long v1 = value[1] & 0xffffffffL;
        if (hi != v1) {
            return (v1 > hi) ? 1 : -1;
        }
        long v0 = value[0] & 0xffffffffL;
        if (low != v0) {
            return (v0 > low) ? 1 : -1;
        }
        return 0;
    }
}
