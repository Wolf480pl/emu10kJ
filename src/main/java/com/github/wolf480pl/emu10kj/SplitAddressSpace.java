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

import java.util.Arrays;

public class SplitAddressSpace implements AddressSpace {
    private final int bits;
    private final int size;
    private final AddressSpace[] subspaces;

    public SplitAddressSpace(int bits, AddressSpace... subspaces) {
        this.bits = bits;
        this.size = 1 << bits;
        this.subspaces = Arrays.copyOf(subspaces, size);
    }

    @Override
    public int read(int addr) {
        int high = addr >>> (32 - bits);
        int low = addr & (size - 1);
        return subspaces[high].read(low);
    }

    @Override
    public void write(int addr, int value) {
        int high = addr >>> (32 - bits);
        int low = addr & (size - 1);
        subspaces[high].write(low, value);
    }

}
