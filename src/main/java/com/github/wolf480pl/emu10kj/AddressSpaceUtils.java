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

import com.github.wolf480pl.emu10kj.TramSpace.OffsetReg;

public class AddressSpaceUtils {

    private AddressSpaceUtils() {
    }

    public static AddressSpace arr(int[] backend) {
        return new ArrayAddressSpace(backend);
    }

    public static AddressSpace arr(int size) {
        return new ArrayAddressSpace(size);
    }

    public static AddressSpace empty() {
        return arr(0); // ArrayAddressSpace defaults to zero if out of bounds
    }

    public static AddressSpace split(int width, int bits, AddressSpace... spaces) {
        return new SplitAddressSpace(32 - width, bits, spaces);
    }

    public static AddressSpace tram(AddressSpace backend, OffsetReg offset, int[] addrRegs) {
        return new TramSpace(backend, offset, addrRegs);
    }
}
