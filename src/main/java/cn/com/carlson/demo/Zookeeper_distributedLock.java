package cn.com.carlson.demo;

import org.apache.zookeeper.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用zk简单实现分布式锁,使用场景如下单减库存
 */
public class Zookeeper_distributedLock implements Watcher{
    //zk客户端
    private ZooKeeper zk;
    //此次任务创建锁的子节点路径
    private String childPath;
    //此次任务创建锁的父节点路径
    private String parentPath;
    //计数器,初始值为线程的数量。每当一个线程完成后,计数器值减1,值为0表示所有线程已完成
    private static CountDownLatch cdl;

    @Override
    public void process(WatchedEvent event) {
        System.out.println(Thread.currentThread().getName() + " : " + event.getState() + " , " + event.getType());
    }

    private void dealWithLockPath(String parentPath) throws Exception {
        zk = new ZooKeeper("localhost:2181", 5000, new Zookeeper_distributedLock());
        this.parentPath = parentPath;
        //创建锁的子节点
        createLockPath();
        //获取锁
        getLock();
    }

    //检查节点是否存在
    private void createLockPath() throws Exception {
        if (zk.exists(parentPath, true) == null) {
            zk.create(parentPath,"".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        childPath = zk.create(parentPath+"/lock", "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(Thread.currentThread().getName() + " 创建子节点: " + childPath);
    }

    //获取锁
    private void getLock() throws Exception {
        List<String> childPathList = zk.getChildren(parentPath, false);
        if(isNotBlank(childPath) && childPathList != null){
            Collections.sort(childPathList);
            System.out.println(childPathList);
            //判断子节点的位置
            int index = childPathList.indexOf(childPath.substring(parentPath.length()+1));
            switch (index) {
                case -1:{
                    System.out.println(Thread.currentThread().getName() + " 节点不存在");
                    break;
                }
                case 0:{
                    getLockSuccess();
                    break;
                }
                default:{
                    //监听比自己序号小且最接近自己的子节点
                    String watchNode = parentPath + "/" + childPathList.get(index - 1);
                    System.out.println(Thread.currentThread().getName() + " 监听子节点: " + watchNode);
                    if(zk.exists(watchNode, new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (Event.EventType.NodeDeleted == event.getType()) {
                                System.out.println(watchNode + " 子节点被删除 ");
                                try {
                                    getLock();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }) == null){
                        //若监听子节点则尝试获取锁
                        getLock();
                    }
                    break;
                }
            }
        }
    }

    //成功获取锁
    public void getLockSuccess() throws Exception {
        System.out.println(Thread.currentThread().getName() + " 节点最小,获取锁并开始处理业务");
        /**
         * 1.思路-分布式下单减库存：通过产品ID向数据库查询最新库存,大于0时下单成功,然后马上更新库存
         * 2.思路-多台应用同时通过调度任务处理集合：为调度任务在zk中设置指定/path,处理对象时带上ID创建临时子节点/path/path-ID
         *      成功创建相当于获取锁,重新查询数据库处理状态,没有处理则进行处理,处理结束后更改状态
         *      创建失败则直接放弃对象的处理,获取下一个对象
         */
        Thread.sleep(2000);
        System.out.println(Thread.currentThread().getName() + " 处理业务结束，释放锁");
        zk.delete(childPath,-1);
        cdl.countDown();
    }

    //非空判断
    private boolean isNotBlank(String str) {
        if(str != null && !"".equals(str.trim())){
            return true;
        }else {
            return false;
        }
    }

    public static void main(String[] args) throws Exception{
        //newFixedThreadPool定长线程池,控制线程最大并发数,超出的线程会在队列中等待.最好根据系统资源进行设置,如Runtime.getRuntime().availableProcessors()
        ExecutorService service = Executors.newFixedThreadPool(10);
        int cdlNumber = 5;
        cdl = new CountDownLatch(cdlNumber);
        for (int i = 0; i < cdlNumber; i++) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    Zookeeper_distributedLock dLock = new Zookeeper_distributedLock();
                    try {
                        dLock.dealWithLockPath("/zk_demo_distributedLock");
                    } catch (Exception e) {
                        e.printStackTrace();
                        cdl.countDown();
                    }
                }
            });
        }
        //启动其他线程后调用await(),主线程阻塞
        cdl.await();
        service.shutdown();
    }
}
