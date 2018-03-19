package cn.com.carlson.base;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 使用异步（async）接口创建节点,仅需要实现AsyncCallback.StringCallback()接口即可
 */
public class Zookeeper_Create_API_ASync_Usage implements Watcher{
    private static CountDownLatch cdl = new CountDownLatch(1);

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
            cdl.countDown();
        }
    }

    static class IStringCallback implements AsyncCallback.StringCallback{
        /**
         * @param rc    服务端响应码: 0(OK)接口调用成功, -4(ConnectionLoss)客户端服务端连接已断, -110(NodeExists)节点已存在, -112(SessionExpired)会话已过期
         * @param path  接口调用时传入API的数据节点路径参数值
         * @param ctx
         * @param name  在服务端创建的完整节点名（路径）
         */
        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            System.out.println("Create path result:[" + rc + ", " + path + ", " + ctx + ", real path name: " + name);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 5000, new Zookeeper_Create_API_ASync_Usage());
        cdl.await();
        zooKeeper.create("/zk-test-ephemeral-", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                new IStringCallback(),//回调函数，节点创建后zk客户端自动调用
                "I am context."//用于传递一个对象，可在回调方法执行时使用
                );
        zooKeeper.create("/zk-test-ephemeral-", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,
                new IStringCallback(), "I am context.");
        zooKeeper.create("/zk-test-ephemeral-", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,
                new IStringCallback(), "I am context.");
        Thread.sleep(Integer.MAX_VALUE);
    }
}
