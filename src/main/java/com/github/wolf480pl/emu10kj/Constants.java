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

import gnu.trove.map.TIntShortMap;
import gnu.trove.map.hash.TIntShortHashMap;

public class Constants {
    private final int[] map;
    private final TIntShortMap rev = new TIntShortHashMap();

    public Constants(int[] constants) {
        int size = Math.max(constants.length, Short.MAX_VALUE);
        map = Arrays.copyOf(constants, size);
        for (short i = 0; i < size; ++i) {
            rev.put(map[i], i);
        }
    }

    public int get(short idx) {
        return map[idx];
    }

    public short find(int constant) {
        return rev.get(constant);
    }

}
