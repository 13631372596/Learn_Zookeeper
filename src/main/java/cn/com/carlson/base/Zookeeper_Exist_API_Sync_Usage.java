package cn.com.carlson.base;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * 检测节点是否存在
 */
public class Zookeeper_Exist_API_Sync_Usage implements Watcher{
    private static CountDownLatch cdl = new CountDownLatch(1);
    private static ZooKeeper zk;

    @Override
    public void process(WatchedEvent event) {
        try {
            if (Event.KeeperState.SyncConnected == event.getState()) {
                if (Event.EventType.None == event.getType() && null == event.getPath()) {
                    cdl.countDown();
                } else {
                    System.out.println("Node(" + event.getPath() + ")"+event.getType());
                    zk.exists(event.getPath(), true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        String path = "/zk-book2";
        zk = new ZooKeeper("localhost:2181", 5000, new Zookeeper_Exist_API_Sync_Usage());
        cdl.countDown();
        //无论指定节点是否存在,都可通过exists注册Watcher监听创建、删除和更新事件,不包括指定节点的子节点变化
        zk.exists(path, true);
        zk.create(path, "111".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        zk.setData(path, "222".getBytes(), -1);
        zk.create(path + "/c1", "333".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        zk.delete(path + "/c1", -1);
        zk.delete(path, -1);
        Thread.sleep(Integer.MAX_VALUE);
    }
}
