package cn.com.carlson.base;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 使用同步API获取子节点列表
 */
public class Zookeeper_GetChildren_API_Sync_Usage implements Watcher{
    private static CountDownLatch cdl = new CountDownLatch(1);
    private static ZooKeeper zk = null;

    @Override
    public void process(WatchedEvent event) {
        if(Event.KeeperState.SyncConnected == event.getState()){
            if(Event.EventType.None == event.getType() && null == event.getPath()){
                cdl.countDown();
            }else if(event.getType() == Event.EventType.NodeChildrenChanged){
                try{
                    System.out.println("ReGetChild:"+zk.getChildren(event.getPath(),true));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        String path = "/zk-book3";
        zk = new ZooKeeper("localhost:2181",5000,new Zookeeper_GetChildren_API_Sync_Usage());
        cdl.await();
        //不能在临时节点下面创建子节点
        zk.create(path,"".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zk.create(path+"/c1","".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        //注册Watcher,一旦有子节点被创建,服务端仅会向客户端发出NodeChildrenChanged通知,不会有节点变化情况。此外触发一次通知后该Watcher失效
        List<String> childrenList = zk.getChildren(path, true);
        System.out.println(childrenList);//数据节点的相对节点路径
        zk.create(path+"/c2","".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        Thread.sleep(Integer.MAX_VALUE);
    }
}
