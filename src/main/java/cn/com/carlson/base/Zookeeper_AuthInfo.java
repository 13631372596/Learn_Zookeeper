package cn.com.carlson.base;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class Zookeeper_AuthInfo {
    public static void main(String[] args) throws Exception {
        String path = "/zk_test_auth";
        String path2 = "/zk_test_auth/child";
        ZooKeeper zk = new ZooKeeper("localhost:2181",5000,null);
        zk.addAuthInfo("digest", "admin:test".getBytes());//admin:test相当于username:password
        zk.create(path, "data".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL,//赋予节点创建者所有的权限
                CreateMode.PERSISTENT);
        //正确权限获取
        ZooKeeper zk2 = new ZooKeeper("localhost:2181",5000,null);
        zk2.addAuthInfo("digest", "admin:test".getBytes());
        System.out.println(zk2.getData(path, false, null));
        //无权限获取
        ZooKeeper zk3 = new ZooKeeper("localhost:2181",5000,null);
        try {
            zk3.getData(path, false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //错误权限获取
        ZooKeeper zk4 = new ZooKeeper("localhost:2181",5000,null);
        zk4.addAuthInfo("digest", "admin:test2".getBytes());
        try {
            zk4.getData(path, false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //无删除子节点
        zk.create(path2, "data".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.EPHEMERAL);
        try {
            zk3.delete(path2, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //正确权限删除子节点
        zk2.delete(path2, -1);
        //无权限删除节点：节点的权限信息对于删除操作来说作用范围是其子节点，即原节点可自由删除
        zk3.delete(path,-1);
        System.out.println("成功删除" + path + " , " + path2);
    }

}
