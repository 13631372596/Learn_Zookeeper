package cn.com.carlson.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * zk从集群中选举思路：多台机器同时向节点创建一个子节点，最终只有一台机器成功且成功的机器成为Master
 * Curator基于上述思路，并将节点创建、事件监听和自动选举过程进行分装，实现选举功能
 */
public class Zookeeper_Curator_MasterSelect {

    static CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2181")
            .sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000,3)).build();

    public static void main(String[] args) throws Exception{
        String path = "/zk_test_curator_master";
        client.start();
        //ls实例封装所有和master选举逻辑，包括和zk服务器的交互过程
        LeaderSelector ls = new LeaderSelector(client, path,//代表本次选举在该节点下进行
                new LeaderSelectorListenerAdapter() {//curator在成功获取master权力时回调该监听器
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                System.out.println("成为Master");
                Thread.sleep(2000);
                System.out.println("完成Master操作");
            }
        });
        ls.autoRequeue();
        ls.start();
        Thread.sleep(10000);
    }
}
