# ZooKeeper备忘

####	1.实用功能及场景
1.数据发布、订阅,集群管理

客户端对zk的一个节点注册监听，当节点内容或子节点列表发生变更，zk会订阅的客户端发送通知

2.负载均衡

3.分布式创建ID

根据顺序类型创建唯一ID

4.master选举

5.排它锁、共享锁

排它锁：其他任何事务都不能再对这个数据对象进行操作

共享锁：读请求要求比自己序号小的子节点都是读请求，写请求要求自己序号最小，否则等待

避免惊群：每个锁只需关注比自己序号小的是否存在

6.分布式队列

1.先入先出

类似共享锁，确定自己的节点序号是最小的，否则进行等待并向比自己序号小且最接近自己的节点注册Wathcher监听

2.同时触发

向节点注册对子节点列表变更的监听，创建子节点时统计个数，不足则重新注册变更监听，足够则进行业务处理

####	2.部分原理
1.事务ID

每个事务请求，zk都会分配一个全局唯一的事务ID，具有Atomic原子性、Consistency一致性、Isolation隔离性、Durability持久性

2.Stat属性说明

mzxid节点最后一次被更新时的事务ID、czxid被创建时的事务ID、pzxid子节点列表最后一次被修改时的事务ID、ehemeralOwner临时节点的会话sessionID（持久节点为0）、version节点数据的版本号（初始值为0、变更不管值是否变化依然+1）

3.Watcher

客户端：
在zk服务端注册watcher时首先会把watcher存储在DataWatchRegistration并封装到packet里(packet是zk最小的注册单元,createBB方法得知zk只会讲requestHeader和request两个属性序列化，watchRegistration并没有被序列化至底层字节数组)通过ClientCnxn发送。然后由ClientCnxn的readResponse方法接收服务端响应，最后在finishPacket中真正注册到watches中，watches为Map<String, Set<Watcher>>结构(节点路径和watcher对象一一映射)。

服务端：
接收请求后在FinalRequestProcessor中Opcode.getData判断当前请求是否注册watcher，注册则使用ZKDatabbase调用DataTree把ServerCnxn添加至WathcerManager的watchTable(路径和watcher集合一一映射)和watch2Paths(watcher和路径集合一一对应)中，转化为byte数组通过getData调用。serverCnxn是zk客户端和服务端之间的连接接口，默认实现是NIOServerCnxn，实现了process接口，可把ServerCnxn看作watcher对象。

触发：
FinalRequestProcessor接收请求后ZooKeeperServer的processTxn，底层ZKDatabase再到dataTree，最后在dataTree的setData调用dataWatches的triggerWatch触发。封装WatchedEvent(
KeeperState通知状态、EventType事件类型、path节点路径)，从watchTable中取出watcher并在watchTable和watch2paths中删除(一次性)，由于zk把ServerCnxn作为watcher存储则默认调用NIOServerCnxn的process方法。








