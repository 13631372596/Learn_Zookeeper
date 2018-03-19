package cn.com.carlson.base;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 使用同步（sync）接口创建节点
 */
public class Zookeeper_Create_API_Sync_Usage implements Watcher{
    private static CountDownLatch cdl = new CountDownLatch(1);

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
            cdl.countDown();
        }
    }

    /**
     * 无论同步还是异步,zk都不支持递归,无法在父节点不存在时创建子节点。
     * 另外,创建已存在的持久节点会抛出NodeExistException
     */
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 5000, new Zookeeper_Create_API_Sync_Usage());
        cdl.await();
        String path1 = zooKeeper.create("/zk-test-ephemeral-", // 需要创建的数据节点的节点路径
                "".getBytes(),// 节点创建后的初始内容(只支持字节数组,需开发者将内容进行序列化或反序列化)
                ZooDefs.Ids.OPEN_ACL_UNSAFE, // 节点策略，OPEN_ACL_UNSAFE为任何操作都不受权限控制
                CreateMode.EPHEMERAL// 节点类型：持久PERSISTENT、持久顺序PERSISTENT_SEQUENTIAL、临时EPHEMERAL、临时顺序EPHEMERAL_SEQUENTIAL
                );
        System.out.println("Success create znode: " + path1);
        String path2 = zooKeeper.create("/zk-test-ephemeral-", "".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);//顺序节点自动在节点后缀加上数字
        System.out.println("Success create znode: " + path2);
    }
}
