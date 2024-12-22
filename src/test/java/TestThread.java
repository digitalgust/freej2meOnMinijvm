public class TestThread {
    public static void main(String[] args) {
        ThreadGroup mainGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup systemGroup = mainGroup.getParent();
        System.out.println(mainGroup);
        System.out.println(systemGroup);
        Thread[] threads = new Thread[mainGroup.activeCount()];
        Thread.currentThread().getThreadGroup().enumerate(threads);
        for (Thread thread : threads) {
            System.out.println("main group:" + thread);
        }

        threads = new Thread[systemGroup.activeCount()];
        systemGroup.enumerate(threads);
        for (Thread thread : threads) {
            System.out.println("system group:" + thread);
        }

        CustomThreadGroup ctg = new CustomThreadGroup(systemGroup, "custom group");

        Thread t = new Thread(() -> {
            System.out.println("thread ");
        });
    }
}
