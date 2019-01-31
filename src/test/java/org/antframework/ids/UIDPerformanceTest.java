/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2018-02-05 19:32 创建
 */
package org.antframework.ids;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.*;

/**
 * UID性能测试
 */
@Ignore
public class UIDPerformanceTest {
    // 并发线程数
    private static final int THREAD_COUNT = 100;
    // 每个任务内循环次数
    private static final int COUNT_PER_TASK = 10000000;
    // 线程池
    private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            THREAD_COUNT,
            THREAD_COUNT,
            5,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(1),
            new ThreadPoolExecutor.DiscardPolicy());
    // 性能结果
    private Performance[] performances = new Performance[THREAD_COUNT];
    // 主线程等待队列
    private BlockingQueue blockingQueue = new LinkedBlockingQueue();

    @Before
    public void init() {
        System.setProperty(IdsParams.IDC_ID_KEY, "01");
        System.setProperty(IdsParams.IDCENTER_URL_KEY, "http://localhost:6210");
        System.setProperty(IdsParams.HOME_PATH_KEY, System.getProperty("user.home") + "/myids");
        System.setProperty(IdsParams.WORKER_KEY, "127.0.0.1:8080");
        System.setProperty(IdsParams.ZK_URLS_KEY, "localhost:2181");

        UID.newId();
    }

    @Test
    public void testPerformance() throws InterruptedException {
        for (int i = 0; i < THREAD_COUNT; i++) {
            threadPool.execute(new Task(i));
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            blockingQueue.take();
        }
        long tps = 0;
        for (int i = 0; i < THREAD_COUNT; i++) {
            System.out.println(performances[i]);
            tps += performances[i].getTps();
        }
        System.out.println("客户端总tps：" + tps);
    }

    // 任务
    private class Task implements Runnable {
        // 任务序号
        private int index;

        public Task(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            int nullCount = 0;
            for (int i = 0; i < COUNT_PER_TASK; i++) {
                String id = UID.newId();
                if (id == null) {
                    nullCount++;
                }
            }
            long endTime = System.currentTimeMillis();
            performances[index] = new Performance(index, startTime, endTime, COUNT_PER_TASK, nullCount);

            blockingQueue.offer(new Object());
        }
    }

    // 性能结果
    private static class Performance {
        // 任务序号
        private int index;
        // 开始时间
        private long startTime;
        // 结束时间
        private long endTime;
        // 循环次数
        private int count;
        // id出现null次数
        private int nullCount;

        public Performance(int index, long startTime, long endTime, int count, int nullCount) {
            this.index = index;
            this.startTime = startTime;
            this.endTime = endTime;
            this.count = count;
            this.nullCount = nullCount;
        }

        public int getIndex() {
            return index;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public int getCount() {
            return count;
        }

        public int getNullCount() {
            return nullCount;
        }

        public long getTimeCost() {
            return endTime - startTime;
        }

        public long getTps() {
            return (count - nullCount) * 1000L / getTimeCost();
        }

        @Override
        public String toString() {
            return String.format("任务序号：%d，开始时间：%s，结束时间：%s，循环次数：%d，id出现null次数：%d，总耗时：%d毫秒，tps：%d",
                    index,
                    DateFormatUtils.format(new Date(startTime), "yyyy-MM-dd HH:mm:ss.SSS"),
                    DateFormatUtils.format(new Date(endTime), "yyyy-MM-dd HH:mm:ss.SSS"),
                    count,
                    nullCount,
                    getTimeCost(),
                    getTps());
        }
    }
}
