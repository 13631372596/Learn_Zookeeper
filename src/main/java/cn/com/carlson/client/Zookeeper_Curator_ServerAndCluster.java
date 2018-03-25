package cn.com.carlson.client;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.TestingServer;
import org.apache.curator.test.TestingZooKeeperServer;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.CreateMode;

import java.io.File;

/**
 *
 */
public class Zookeeper_Curator_ServerAndCluster {

    static String path = "/zk-test-curator-serverAndCluster";

    public static void main(String[] args) throws Exception{
        //自定义zk服务器对外服务的端口和dataDir,不指定datair默认在系统目录中创建
        /*TestingServer server = new TestingServer(2182, new File("/home/demo"));
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("localhost:2182")
                .sessionTimeoutMs(5000).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        client.start();
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,"data".getBytes());
        System.out.println(client.getChildren().forPath(path));
        server.close();*/
        //模拟3台机器组成的zk集群，并模拟将leader服务器kill掉
        TestingCluster cluster = new TestingCluster(3);
        cluster.start();
        Thread.sleep(2000);
        TestingZooKeeperServer leader = null;
        for (TestingZooKeeperServer zs : cluster.getServers()) {
            System.out.println(zs.getInstanceSpec().getServerId() + " - ");
            System.out.println(zs.getQuorumPeer().getServerState() + " - ");
            System.out.println(zs.getInstanceSpec().getDataDirectory().getAbsolutePath());
            if (zs.getQuorumPeer().getServerState().equals("leading")) {
                leader = zs;
            }
        }
        leader.kill();
        System.out.println("kill leader");
        for (TestingZooKeeperServer zs : cluster.getServers()) {
            System.out.println(zs.getInstanceSpec().getServerId() + " - ");
            System.out.println(zs.getQuorumPeer().getServerState() + " - ");
            System.out.println(zs.getInstanceSpec().getDataDirectory().getAbsolutePath());
        }
        cluster.stop();
    }
}
