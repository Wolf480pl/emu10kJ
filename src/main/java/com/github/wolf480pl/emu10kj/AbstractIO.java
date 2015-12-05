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
