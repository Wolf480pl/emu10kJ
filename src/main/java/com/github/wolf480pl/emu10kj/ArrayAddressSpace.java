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

public class ArrayAddressSpace implements AddressSpace {
    private final int[] data;

    public ArrayAddressSpace(int[] backend) {
        this.data = backend;
    }
    
    public ArrayAddressSpace(int size) {
        this.data = new int[size]; 
    }
    
    @Override
    public int read(int addr) {
        if (addr >= data.length) {
            return 0;
        }
        return data[addr];
    }

    @Override
    public void write(int addr, int value) {
        if (addr >= data.length) {
            return;
        }
        data[addr] = value;
    }

}
