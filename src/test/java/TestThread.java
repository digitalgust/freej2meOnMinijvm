public class TestThread {
    static class AThread extends Thread {
        long sum = 0;

        public void run() {
            System.out.println("start");
            while (true) {
                sum++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("interrupt");
                }
            }
        }
    }

    public static void main(String[] args) {
        AThread t = new AThread();
        t.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t.interrupt();


//        ThreadGroup mainGroup = Thread.currentThread().getThreadGroup();
//        ThreadGroup systemGroup = mainGroup.getParent();
//        System.out.println(mainGroup);
//        System.out.println(systemGroup);
//        Thread[] threads = new Thread[mainGroup.activeCount()];
//        Thread.currentThread().getThreadGroup().enumerate(threads);
//        for (Thread thread : threads) {
//            System.out.println("main group:" + thread);
//        }
//
//        threads = new Thread[systemGroup.activeCount()];
//        systemGroup.enumerate(threads);
//        for (Thread thread : threads) {
//            System.out.println("system group:" + thread);
//        }
//
//        CustomThreadGroup ctg = new CustomThreadGroup(systemGroup, "custom group");
//
//        Thread t = new Thread(() -> {
//            System.out.println("thread ");
//        });
    }
}
