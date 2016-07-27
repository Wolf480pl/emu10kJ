package com.github.wolf480pl.emu10kj;

public class AccuSpeedTest {

    public static void main(String[] args) {
        final Thread main = Thread.currentThread();
        Thread sampler = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    main.interrupt();
                }
            }
        };
        sampler.setDaemon(true);
        sampler.start();

        testSpeed();
    }

    public static void testSpeed() {
        Accumulator acc = new Accumulator();
        acc.write(0xffffffffL);
        int cnt = 0;
        while (true) {
            for (int i = 0; i < 48000; ++i) {
                testRMW(acc);
                testRMA(acc);
            }
            System.out.print('.');
            ++cnt;
            if (Thread.interrupted()) {
                System.out.println();
                System.out.println(cnt);
                System.out.println(acc.read());
                cnt = 0;
            }
        }
    }

    public static void testRMW(Accumulator acc) {
        acc.write(acc.read() * 5);
    }

    public static void testRMA(Accumulator acc) {
        acc.add(acc.read() * 5);
    }

}
