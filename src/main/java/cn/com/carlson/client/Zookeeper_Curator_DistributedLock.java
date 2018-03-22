package cn.com.carlson.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * 使用Curator实现分布式锁
 */
public class Zookeeper_Curator_DistributedLock {

    static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181")
            .sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000,3)).build();

    public static void main(String[] args) throws Exception{
        String path = "/zk_test_curator_lock";
        client.start();
        InterProcessMutex ipm = new InterProcessMutex(client, path);
        CountDownLatch cdl = new CountDownLatch(1);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss|SSS");
        for (int i = 0; i < 10; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ipm.acquire();
                        cdl.await();
                        String orderNo = sdf.format(new Date());
                        System.out.println(Thread.currentThread().getName() + " , " + orderNo);
                        ipm.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        cdl.countDown();
    }
}
