package cn.com.carlson.base;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 复用sessionId和sessionPasswd的zk对象实例
 */
public class Zookeeper_Contructor_Usage_With_SID_PASSWD implements Watcher{
    private static CountDownLatch cdl = new CountDownLatch(1);

    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("Receive watched event: " + watchedEvent);
        if(Event.KeeperState.SyncConnected == watchedEvent.getState()){
            cdl.countDown();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 5000, new Zookeeper_Contructor_Usage_With_SID_PASSWD());
        cdl.await();
        long sessionId = zooKeeper.getSessionId();//当前会话的ID
        byte[] passwd = zooKeeper.getSessionPasswd();//当前会话的密钥
        zooKeeper = new ZooKeeper("localhost:2181",5000,
                new Zookeeper_Contructor_Usage_With_SID_PASSWD(),sessionId,passwd);
        Thread.sleep(Integer.MAX_VALUE);
    }


}
