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

public class Instruction {
    private final byte opcode;
    private final short regR, regA, regX, regY;

    public Instruction(byte opcode, short regR, short regA, short regX, short regY) {
        this.opcode = opcode;
        this.regR = regR;
        this.regA = regA;
        this.regX = regX;
        this.regY = regY;
    }

    public byte getOpcode() {
        return opcode;
    }

    public short getRegR() {
        return regR;
    }

    public short getRegA() {
        return regA;
    }

    public short getRegX() {
        return regX;
    }

    public short getRegY() {
        return regY;
    }

}
