package cn.com.carlson.base;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 使用异步API获取子节点列表
 */
public class Zookeeper_GetChildren_API_ASync_Usage implements Watcher{
    private static CountDownLatch cdl = new CountDownLatch(1);
    private static ZooKeeper zk = null;

    @Override
    public void process(WatchedEvent event) {
        if(Event.KeeperState.SyncConnected == event.getState()){
            if(Event.EventType.None == event.getType() && null == event.getPath()){
                cdl.countDown();
            }else if(event.getType() == Event.EventType.NodeChildrenChanged){

            }
        }
    }

    static class IChildren2Callback implements AsyncCallback.Children2Callback{

        @Override
        public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
            System.out.println("Get children znode result: [response code: " + rc + ",param path: " + path
                + ",ctx: " + ctx + ",children list: "+ children + ",stat:" + stat);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        String path = "/zk-book3";
        zk = new ZooKeeper("localhost:2181",5000,new Zookeeper_GetChildren_API_ASync_Usage());
        cdl.await();
        zk.create(path,"".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(path+"/c1","".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        zk.getChildren(path, true, new IChildren2Callback(),null);//ctx不传参可得到childrenList
        Thread.sleep(Integer.MAX_VALUE);
    }
}
