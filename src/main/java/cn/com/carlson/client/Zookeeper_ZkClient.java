package cn.com.carlson.client;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.List;

/**
 * Zookeeper API不足之处:
 * 1.Watcher一次性，需重才注册
 * 2.没重连机制
 * 3.没有对象级别的序列化
 * 4.无法级联创建和删除
 *
 * ZkClient(API基础上进行简单封装):
 * 1.没提供各种场景的实现；
 */
public class Zookeeper_ZkClient {
    public static void main(String[] args) {
        String path = "/zk-test-client";
        String childPath = "/zk-test-client/c1";
        ZkClient zkc = new ZkClient("localhost:2181", 5000);
        /**
         * listner非一次性,可对不存在的节点进行子节点变更的监听,当子节点列表创建、删除,服务端会把最新的子节点列表发送给客户端
         */
        zkc.subscribeChildChanges(path, new IZkChildListener() {
            public void handleChildChange(String path, List<String> currentChilds) throws Exception {
                System.out.println(path + " , " + currentChilds);
            }
        });
        zkc.createPersistent(childPath, true);//createParents表示是否需要递归创建父节点
        zkc.writeData(path,"test-data");
        //System.out.println((String) zkc.readData(path));
        //System.out.println(zkc.exists(path));
        zkc.deleteRecursive(path);//自动遍历删除节点
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("删除成功");
    }
}
