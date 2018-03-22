package cn.com.carlson.client;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Curator (Netflix公司开源的一套Zookeeper客户端框架):
 */
public class Zookeeper_Curator_NodeCache {

    static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181")
            .sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000,3)).build();

    public static void main(String[] args) throws Exception{
        String path = "/zk_test_curator_nodecache";
        final org.apache.curator.framework.recipes.cache.NodeCache cache = new NodeCache(client, path, false);
        client.start();
        //设置为true:NodeCacheListener创建或数据改变时触发,节点被删除不会触发
        cache.start(true);
        cache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                System.out.println(new String(cache.getCurrentData().getData()));
            }
        });
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,"data1".getBytes());
        client.setData().forPath(path,"data2".getBytes());
        Thread.sleep(2000);
        client.delete().deletingChildrenIfNeeded().forPath(path);
        Thread.sleep(2000);
        System.out.println("finish");
    }
}
