package cn.com.carlson.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用Barrier控制多线程同步
 */
public class Zookeeper_Curator_CyclicBarrier {

    static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181")
            .sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000,3)).build();

    static CyclicBarrier cb = new CyclicBarrier(3);

    static DistributedBarrier barrier;

    static DistributedDoubleBarrier doubleBarrier;

    static class Runner implements Runnable{
        private String name;

        Runner(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                System.out.println(name + " is ready");
                Zookeeper_Curator_CyclicBarrier.cb.await();
                System.out.println(name + " finish");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception{

        //1.同一个JVM,CyclicBarrier可解决多线程同步
        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.submit(new Thread(new Runner("first")));
        executor.submit(new Thread(new Runner("third")));
        executor.submit(new Thread(new Runner("three")));
        executor.shutdown();

        client.start();
        //2.分布式环境使用Curator的DistributedBarrier（主动释放）
        String path01 = "/zk_test_curator_barrier";
        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier = new DistributedBarrier(client, path01);
                        System.out.println(Thread.currentThread().getName() + " is ready");
                        barrier.setBarrier();//完成Barrier设置
                        System.out.println(Thread.currentThread().getName() + " go");
                        barrier.waitOnBarrier();//等待Barrier释放
                        System.out.println(Thread.currentThread().getName() + " finish");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        Thread.sleep(2000);
        barrier.removeBarrier();//释放barrier

        //3.分布式环境使用Curator的DistributedDoubleBarrier(自动释放),类似JDK自带CyclicBarrier的实现(指定成员数阈值)
        /*String path02 = "/zk_test_curator_doublebarrier";
        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        doubleBarrier = new DistributedDoubleBarrier(client, path02,3);
                        System.out.println(Thread.currentThread().getName() + " is ready");
                        Thread.sleep(3000);
                        //每个Barrier会在enter方法后进行等待进入,当等待的成员数到3个时所有成员同时触发进入
                        doubleBarrier.enter();
                        System.out.println(Thread.currentThread().getName() + " go");
                        //同理,调用leave方法会再次等待退出,当等待的成员数达到3个时所有成员同时触发退出
                        doubleBarrier.leave();
                        System.out.println(Thread.currentThread().getName() + " finish");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }*/
    }
}
