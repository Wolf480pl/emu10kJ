package com.github.wolf480pl.emu10kj;

public class AccuTest {

    public static void main(String[] args) {
        Accumulator acc = new Accumulator();

        acc.write(0xffffffffL);
        printAcc(acc);

        testAllWrites();
    }

    public static void printAcc(Accumulator acc) {
        System.out.println("read: " + acc.read());
        System.out.println("lowWrap: " + acc.readLowWrap());
        System.out.println("lowSat: " + acc.readLowSat());
        System.out.println("hiWrap: " + acc.readHiWrap());
        System.out.println("hiSat: " + acc.readHiSat());
    }

    public static void testAllWrites() {
        Accumulator acc = new Accumulator();
        for (long l = 0; l >= 0;) {
            if ((l & 0xffffffffL) == 0) {
                System.out.println();
                System.out.printf("%016x", l);
            }
            System.out.print('.');
            // System.out.printf("%016x\n", l);
            for (int i = 0; i < 0x1000000; ++i) {
                testWrite(acc, l);
                testWrite(acc, ~l);
                ++l;
            }
        }
    }

    public static void testWrite(Accumulator acc, long l) {
        acc.write(l);
        long r = acc.read();
        if (r != l) {
            System.err.println("testWrite fail: written " + l + " got " + r);
        }

        final int i = (int) l;
        acc.writeLow(i);
        int ir = acc.readLowWrap();
        if (ir != i) {
            System.err.println("testWrite low fail: written " + l + " got " + r);
        }

        acc.writeHi(i);
        ir = acc.readHiWrap();
        if (ir != i) {
            System.err.println("testWrite hi fail: written " + l + " got " + r);
        }
    }
}
