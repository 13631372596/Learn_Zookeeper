package cn.com.carlson.client;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Curator (Netflix公司开源的一套Zookeeper客户端框架):
 */
public class Zookeeper_Curator {
    //重试策略
    static RetryPolicy rp = new ExponentialBackoffRetry(1000,//初始sleep时间
            3,//最大重试次数
            3000//最大sleep时间
    );

    //static CuratorFramework client = CuratorFrameworkFactory.newClient("locahost:2181",5000,3000,rp);
    static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181")
            .sessionTimeoutMs(5000).retryPolicy(rp)
            //.namespace("test-curator")//独立命名空间,客户端对节点的操作基于该相对目录
            .build();

    static CountDownLatch cdl = new CountDownLatch(2);

    static ExecutorService es = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws Exception{
        String path = "/test-curator";
        client.start();
        //创建临时节点，并自动递归创建父节点(持久)
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
            public void processResult(CuratorFramework cf, CuratorEvent event) throws Exception {
                //响应码用于标识事件的结果状态
                System.out.println(event.getResultCode() + " , " + event.getType());
                System.out.println(Thread.currentThread().getName());
                cdl.countDown();
            }
        },es//所有异步通知时间都由默认的EventThread处理(复杂处理单元消耗时间长)，可传入一个Executor实例处理较复杂的事件
        ).forPath(path,"init".getBytes());
        System.out.println("创建节点");
        Stat stat = new Stat();
        client.getData().storingStatIn(stat).forPath(path);
        //client.delete().guaranteed().forPath(path);//guaranteed()只要客户端会话有效，后台持续删直至删除成功
        client.delete().deletingChildrenIfNeeded().withVersion(stat.getVersion()).forPath(path);
        System.out.println("删除节点");
        cdl.await();
        es.shutdown();
    }
}
