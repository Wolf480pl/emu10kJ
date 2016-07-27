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

public abstract class AbstractIO implements IO {
    private final AddressSpace inSpace = new InputSpace();
    private final AddressSpace outSpace = new OutputSpace();

    @Override
    public AddressSpace inputSpace() {
        return inSpace;
    }

    @Override
    public AddressSpace outputSpace() {
        return outSpace;
    }

    protected class OutputSpace implements AddressSpace {

        @Override
        public int read(int addr) {
            return readOut(addr);
        }

        @Override
        public void write(int addr, int value) {
            writeOut(addr, value);
        }
    }

    protected class InputSpace implements AddressSpace {

        @Override
        public int read(int addr) {
            return readIn(addr);
        }

        @Override
        public void write(int addr, int value) {
            writeIn(addr, value);
        }
    }
}
