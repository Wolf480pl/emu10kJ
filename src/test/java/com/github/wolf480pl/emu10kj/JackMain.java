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

import static com.github.wolf480pl.emu10kj.AddressSpaceUtils.empty;

import java.nio.FloatBuffer;

import org.jaudiolibs.jnajack.JackException;
import org.jaudiolibs.jnajack.util.SimpleAudioClient;

@SuppressWarnings("deprecation")
public class JackMain implements SimpleAudioClient.Processor {
    private static final int IOS = 16;

    private final AddressSpace in, out;
    private final DSP dsp;

    public JackMain() {
        this.in = new TRAM(16);
        this.out = new TRAM(16);
        this.dsp = new Emu10k1(new CompositeIO(in, out), new CompositeIO(empty(), empty()), empty());
        dsp.loadProgram(ScMain.testProgram());
        dsp.dspAddressSpace().write(ScMain.VOL0_ADDR, 0x7fffffff);
        dsp.dspAddressSpace().write(ScMain.VOL1_ADDR, 0x3fffffff);

    }

    public static void main(String[] args) throws JackException, InterruptedException {
        String[] inputs = new String[IOS];
        String[] outputs = new String[IOS];
        for (int i = 0; i < IOS; ++i) {
            inputs[i] = "in" + i;
            outputs[i] = "out" + i;
        }
        SimpleAudioClient client = SimpleAudioClient.create("emu10kJ", inputs, outputs, new JackMain());
        client.activate();
        while (true) {
            Thread.sleep(1000);
        }

    }

    @Override
    public void setup(float samplerate, int buffersize) {
        // TODO Auto-generated method stub

    }

    @Override
    public void process(FloatBuffer[] inputs, FloatBuffer[] outputs) {
        int size = inputs[0].capacity();
        for (int pos = 0; pos < size; ++pos) {

            for (int i = 0; i < IOS; ++i) {
                float val0 = inputs[i].get();
                float val1 = val0 * Integer.MAX_VALUE;
                in.write(i, (int) val1);
                if (i == 0) {
                    // System.out.printf("in %f %08x\n", val0, (int) val1);
                    //System.out.println("in " + val0 + " " + Integer.toHexString((int) val1));
                }
            }

            dsp.tick();

            for (int i = 0; i < IOS; ++i) {
                int val0 = out.read(i);
                float val1 = val0;
                float val2 = val1 / (Integer.MAX_VALUE);
                outputs[i].put(val2);
                if (i == 0) {
                    // System.out.printf("out %08x %f\n", val0, val2);
                    //System.out.println("out " + Integer.toHexString(val0) + " " + val2);
                }
            }
        }

    }

    @Override
    public void shutdown() {
        System.out.println("shutdown");

    }

}
