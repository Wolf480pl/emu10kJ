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

public class Opcodes {
    public static final byte MACS = 0x0;
    public static final byte MACSN = 0x1;
    public static final byte MACW = 0x2;
    public static final byte MACWN = 0x3;
    public static final byte MACINTS = 0x4;
    public static final byte MACINTW = 0x5;
    public static final byte ACC3 = 0x6;
    public static final byte MACMV = 0x7;
    public static final byte ANDXOR = 0x8;
    public static final byte TESTNEG = 0x9;
    public static final byte LIMIT = 0xa;
    public static final byte LIMITL = 0xb;
    public static final byte LOG = 0xc;
    public static final byte EXP = 0xd;
    public static final byte INTERP = 0xe;
    public static final byte SKIP = 0xf;

    private Opcodes() {
    }

}
