package cn.com.carlson.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * 使用Curator实现分布式计时器,统计系统的在线人数
 */
public class Zookeeper_Curator_DistributedCount {

    static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181")
            .sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000,3)).build();

    public static void main(String[] args) throws Exception{
        String path = "/zk_test_curator_count";
        client.start();
        //分布式环境中使用的原子整型
        DistributedAtomicInteger count = new DistributedAtomicInteger(client, path, new RetryNTimes(3, 1000));
        AtomicValue<Integer> result = count.add(1);
        System.out.println(result.getStats() + " , " + result.postValue() + " , " +result.preValue());
        result = count.add(1);
        System.out.println(result.getStats() + " , " + result.postValue() + " , " +result.preValue());
    }
}
