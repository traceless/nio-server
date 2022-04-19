package com.helloworld.springboot.threadtest;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.web.jsf.FacesContextUtils;

import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * @author doctor
 */
public class TestMaxThreadNum {

    private static final int TEST_NUMBER = 100000007;

    /**
     * 计算线程池最佳最大的线程数
     * 已知io时间和cpu时间，那么可以得到线程池最佳的线程数大小
     * （W+C）/C * CPU核心数
     * 实际应用场景中，其实cpu跟io时间占比可能是2:8、1:9
     * io密集型可能是1:20
     * 
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        // calculationCpuTime();
        // cpu时间ms，请先执行上面的代码 calculationCpuTime 得到计算单个质数消耗的cpu时间，然后填到下面去
        int cpuTime = 28;
        // IO时间ms，可以设置cpu的倍数，这样方便得到一个最大线程数的整数
        int sleepTime = cpuTime * 3;
        // cpu核心数 Processor
        int processorNum = Runtime.getRuntime().availableProcessors();
        // 最佳线程数量，也就是继续增加线程数，也无法让任务执行的更快了，但是你减少线程，那么性能就会立马下降。
        int maxThreadNum = (sleepTime + cpuTime) / cpuTime * processorNum;
        // 任务数量：最好可以整除 maxThreadNum
        int taskNum = 1020;
        // 每个线程应该处理的任务数量
        int threadTaskNum = taskNum / maxThreadNum;
        System.out.println("------maxThreadNum:  " + maxThreadNum + " threadTaskNum:" + threadTaskNum);

        ThreadPoolExecutor thread = new ThreadPoolExecutor(maxThreadNum, maxThreadNum, 100, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), new DefaultThreadFactory("task-io"));
        // 热身运动，让线程数先达到最大
        for (int i = 0; i < maxThreadNum; i++) {
            thread.execute(() -> {
                mockIoWait(50);
            });
        }
        Thread.sleep(3500);

        final CountDownLatch countDown = new CountDownLatch(maxThreadNum);

        System.out.println("-----线程热身完成，最大核心线程数量----" + thread.getPoolSize() + " start :" + System.currentTimeMillis());
        long start = System.currentTimeMillis();
        for (int i = 0; i < maxThreadNum; i++) {
            thread.execute(() -> {
                // 每个线程应该处理的任务数量
                for (int j = 0; j < threadTaskNum; j++) {
                    executeIoAndCpu(sleepTime, TEST_NUMBER);
                }
                countDown.countDown();
            });
        }
        countDown.await();
        System.out.println("io task end ms: " + (System.currentTimeMillis() - start));

    }

    /**
     * 此方法可以得到 单个任务消耗cpu的时间
     * 
     * @param args
     * @throws InterruptedException
     */
    public static void calculationCpuTime() throws InterruptedException {

        // 任务数量，请设置一定的数量，确保整方法执行的时间在30s左右，这样能得到一个相对准确的数值，请多跑几次
        int taskNum = 1000;
        // 线程数量，应该等于cpu数量，因为接下来的任务只有cpu计算型
        int maxThreadNum = Runtime.getRuntime().availableProcessors();
        // 每个线程应该处理的任务数量
        int threadTaskNum = taskNum / maxThreadNum;
        ThreadPoolExecutor thread = new ThreadPoolExecutor(maxThreadNum, maxThreadNum, 100, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), new DefaultThreadFactory("task-cpu"));
        // 热身运动，让线程数先达到最大
        for (int i = 0; i < maxThreadNum; i++) {
            thread.execute(() -> {
                mockIoWait(50);
            });
        }
        Thread.sleep(2500);

        final CountDownLatch countDown = new CountDownLatch(maxThreadNum);
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(maxThreadNum);

        System.out.println("-----线程热身完成，最大核心线程数量----" + thread.getPoolSize() + " start :" + System.currentTimeMillis());
        long start = System.currentTimeMillis();
        // 12345981
        for (int j = 0; j < maxThreadNum; j++) {
            // 每个线程应该处理的
            thread.execute(() -> {
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("-------开始计算---" + Thread.currentThread().getName() + " startTime:"
                        + System.currentTimeMillis());
                checkPrimeNum(TEST_NUMBER, threadTaskNum);
                System.out.println("-------计算完毕---" + Thread.currentThread().getName());
                countDown.countDown();
            });
        }
        countDown.await();
        long spendTime = (System.currentTimeMillis() - start);
        System.out.println("cpu task end ms: " + (System.currentTimeMillis() - start));
        System.out.println("单个质数计算花费的cpu时间约为: " + spendTime / taskNum + " ms ");
    }

    /**
     * 检查一个数字是否质数
     * 
     * @param primeNum   被检查的数字
     * @param checkTimes 应该检查多少次
     */
    public static void executeIoAndCpu(int ioTime, int primeNum) {
        mockIoWait(ioTime);
        calculationPrime(primeNum);
    }

    /**
     * 检查一个数字是否质数
     * 
     * @param primeNum   被检查的数字
     * @param checkTimes 应该检查多少次
     */
    public static void checkPrimeNum(int primeNum, int checkTimes) {
        for (int i = 0; i < checkTimes; i++) {
            calculationPrime(primeNum);
        }
    }

    /**
     * 模拟网络 io等待
     * 
     * @param sleepTime 模拟io时间
     */
    private static void mockIoWait(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算一个数是否为质数
     * 
     * @param number
     * @return
     */
    private static boolean calculationPrime(int n) {
        for (int i = n - 1; i > 1; i--) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}
