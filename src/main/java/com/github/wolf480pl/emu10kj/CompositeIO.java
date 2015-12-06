package com.github.wolf480pl.emu10kj;

public class CompositeIO implements IO {
    private final AddressSpace in, out;

    public CompositeIO(AddressSpace in, AddressSpace out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public int readIn(int addr) {
        return in.read(addr);
    }

    @Override
    public void writeIn(int addr, int value) {
        in.write(addr, value);
    }

    @Override
    public AddressSpace inputSpace() {
        return in;
    }

    @Override
    public int readOut(int addr) {
        return out.read(addr);
    }

    @Override
    public void writeOut(int addr, int value) {
        out.write(addr, value);
    }

    @Override
    public AddressSpace outputSpace() {
        return out;
    }

}
