package cn.com.carlson.base;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 *  删除节点,只允许删除子节点
 */
public class Zookeeper_Delete_API_ASync_Usage implements Watcher {
    private static CountDownLatch cdl = new CountDownLatch(1);

    @Override
    public void process(WatchedEvent watchedEvent) {
       if (Watcher.Event.KeeperState.SyncConnected == watchedEvent.getState()) {
           cdl.countDown();
       }
    }

    static class IVoidCallback implements AsyncCallback.VoidCallback {
        @Override
        public void processResult(int i, String s, Object o) {
            System.out.println(i + " , " + s + " , " + o);
        }
    }

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 5000, new Zookeeper_Delete_API_ASync_Usage());
        System.out.println(zooKeeper.getState());
        String path = zooKeeper.create("/zk-test-ephemeral-", "1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zooKeeper.delete(path,-1, //节点的数据内容,-1表示任何版本
                new IVoidCallback(),"delete");
    }
}
