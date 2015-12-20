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

public class TramSpace implements AddressSpace {
    private final AddressSpace backend;
    private final int[] addrRegs;
    private final OffsetReg offset;
    
    public TramSpace(AddressSpace backend, OffsetReg offset, int[] addrRegs) {
        this.backend = backend;
        this.addrRegs = addrRegs;
        this.offset = offset;
    }

    @Override
    public int read(int addr) {
        if (addr >= addrRegs.length) {
            return 0;
        }
        return backend.read(offset.get() + addrRegs[addr]);
    }

    @Override
    public void write(int addr, int value) {
        if (addr >= addrRegs.length) {
            return;
        }
        backend.write(offset.get() + addrRegs[addr], value);
    }
    
    public interface OffsetReg {
        int get();
    }

}
