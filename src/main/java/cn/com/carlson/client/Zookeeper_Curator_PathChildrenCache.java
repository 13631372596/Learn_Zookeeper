package cn.com.carlson.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Curator (Netflix公司开源的一套Zookeeper客户端框架):
 * 和其他客户端一样，无法对二级子节点(如“/zk_test_curator_pathChildren/c1/c2”)进行监听
 */
public class Zookeeper_Curator_PathChildrenCache {

    static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181")
            .sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000,3)).build();

    public static void main(String[] args) throws Exception{
        String path = "/zk_test_curator_pathChildren";
        client.start();
        PathChildrenCache cache = new PathChildrenCache(client, path, true);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        //对指定节点的子节点变更时间进行监听（新增/删除/数据变更）,对指定节点本身变更并没通知
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        System.out.println(1);
                        System.out.println(event.getData().getPath());
                        break;
                    case CHILD_UPDATED:
                        System.out.println(2);
                        System.out.println(event.getData().getPath());
                        break;
                    case CHILD_REMOVED:
                        System.out.println(3);
                        System.out.println(event.getData().getPath());
                        break;
                        default:break;
                }
            }
        });
        client.create().withMode(CreateMode.PERSISTENT).forPath(path);
        Thread.sleep(2000);
        client.create().withMode(CreateMode.PERSISTENT).forPath(path+"/c1");
        Thread.sleep(2000);
        client.delete().forPath(path+"/c1");
        Thread.sleep(2000);
        client.delete().forPath(path);
        Thread.sleep(2000);
        System.out.println("finish");
    }
}
