package cn.com.carlson.base;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 最基本的会话创建：实现Watcher接口，重写process方法
 */
public class Zookeeper_Constructor_Usage_Simple implements Watcher{
    private static CountDownLatch cdl = new CountDownLatch(1);

    /**
     * 该方法负责处理来自Zookeeper服务端的Watcher通知，收到服务端发来的SyncConnected事件后，解除主程序在CountDownLatch的等待阻塞
     * @param watchedEvent
     */
    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println("receive watched event: " + watchedEvent);
        if(Event.KeeperState.SyncConnected == watchedEvent.getState()){
            cdl.countDown();
        }
    }

    public static void main(String[] args) throws IOException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181",
                5000, //会话超时(毫秒)
                new Zookeeper_Constructor_Usage_Simple());
        System.out.println(zooKeeper.getState());
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("zookeeper session established");
    }

}
