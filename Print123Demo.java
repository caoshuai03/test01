import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 多线程按顺序打印 1、2、3，共 50 次。
 */
public class Print123Demo {
    private static final int TOTAL_TIMES = 50;

    private final Lock lock = new ReentrantLock();
    private final Condition condition1 = lock.newCondition();
    private final Condition condition2 = lock.newCondition();
    private final Condition condition3 = lock.newCondition();
    private int turn = 1;

    public static void main(String[] args) throws InterruptedException {
        Print123Demo demo = new Print123Demo();
        demo.startPrinting();
    }

    private void startPrinting() throws InterruptedException {
        Thread t1 = new Thread(() -> printNumber(1, condition1, condition2), "Printer-1");
        Thread t2 = new Thread(() -> printNumber(2, condition2, condition3), "Printer-2");
        Thread t3 = new Thread(() -> printNumber(3, condition3, condition1), "Printer-3");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("\n打印完成，共 " + TOTAL_TIMES + " 轮。");
    }

    private void printNumber(int number, Condition current, Condition next) {
        for (int i = 0; i < TOTAL_TIMES; i++) {
            lock.lock();
            try {
                while (turn != number) {
                    current.await();
                }

                System.out.print(number);
                if (number == 3) {
                    System.out.print(' ');
                }

                turn = number % 3 + 1;
                next.signal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } finally {
                lock.unlock();
            }
        }
    }
}
