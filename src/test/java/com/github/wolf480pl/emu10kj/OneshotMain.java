package com.github.wolf480pl.emu10kj;

import static com.github.wolf480pl.emu10kj.AddressSpaceUtils.empty;

public class OneshotMain {
    private AddressSpace in, out;
    private DSP dsp;

    public static void main(String[] args) {
        new OneshotMain().run();
    }

    public OneshotMain() {
        this.in = new TRAM(16);
        this.out = new TRAM(16);
        this.dsp = new Emu10k1(new CompositeIO(in, out), new CompositeIO(empty(), empty()), empty());
        dsp.loadProgram(ScMain.testProgram());
        dsp.dspAddressSpace().write(ScMain.VOL0_ADDR, 0x7fffffff);
        dsp.dspAddressSpace().write(ScMain.VOL1_ADDR, 0x3fffffff);
    }

    public void run() {
        in.write(0, 0x3fffffff + 2);
        in.write(1, 0x7fffffff);
        dsp.tick();
        System.out.printf("%08x\n", out.read(0));
    }
}
