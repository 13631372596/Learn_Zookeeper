package cn.com.carlson.base;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 使用异步API获取节点数据内容
 */
public class Zookeeper_GetData_API_ASync_Usage implements Watcher{
    private static CountDownLatch cdl = new CountDownLatch(1);
    private static ZooKeeper zk;

    @Override
    public void process(WatchedEvent event) {
        if (Event.KeeperState.SyncConnected == event.getState()) {
            if (Event.EventType.None == event.getType() && null == event.getPath()) {
                cdl.countDown();
                //节点的数据内容或数据版本变化都被看作是节点的变化
            } else if (event.getType() == Event.EventType.NodeDataChanged) {
                try {
                    zk.getData(event.getPath(),true,new IDataCallback(),null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class IDataCallback implements AsyncCallback.DataCallback{

        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            System.out.println(rc + ", " + path + ", " + new String(data));
            System.out.println(stat.getCzxid() + ", " + stat.getMzxid() + ", " + stat.getVersion());
        }
    }

    public static void main(String[] args) throws Exception {
        String path = "/zk-book";
        zk = new ZooKeeper("localhost:2181", 5000, new Zookeeper_GetData_API_ASync_Usage());
        cdl.countDown();
        zk.create(path, "123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        zk.getData(path, true, new IDataCallback(), null);
        /**
         * zk数据版本从0开始计数，-1仅是标识符表示基于最新版本进行更新
         * 针对数据版本进行更新，可有效避免一些分布式更新的并发问题
         */
        zk.setData(path, "234".getBytes(), -1);
        Thread.sleep(Integer.MAX_VALUE);
    }
}
