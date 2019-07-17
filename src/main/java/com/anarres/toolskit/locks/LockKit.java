package com.anarres.toolskit.locks;

import org.apache.http.util.Asserts;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Created by ath on 2017/5/1.
 */
public class LockKit {

    private static final Logger LOG = LoggerFactory.getLogger(LockKit.class);
    private static int SESSION_TIMEOUT = 30000;
    private static byte[] DEF_DIR_BYTES = new byte[0];// 路径(文件夹)的默认data

    private final ZooKeeper zk;
    private final String lockGroupRoot, lockName;

    private CountDownLatch latch = new CountDownLatch(1);

    // 锁使用的zookeeper 是否已经关闭 (已经解锁)
    private volatile boolean closed = false;
    // 排在前面的锁地址
    private volatile String waitPath;
    // 是否已经拿到锁并可以执行。
    private volatile boolean isRun = false;
    // 获取锁有执行的监听器。
    private volatile Consumer<SynchronizedEvent> callback;
    // 本次加锁使用的地址
    private String selfPath;


    public LockKit(String zkCfg, int sessionTimeout, String lockGroupRoot, String lockName) throws IOException, InterruptedException {
        Asserts.notNull(zkCfg, "Zookeeper 不能为空");
        Asserts.notNull(lockGroupRoot, "lock group root path 不能为空");
        Asserts.notNull(lockName, "lock name 不能为空");

        // 安全创建连接
        this.zk = new ZooKeeper(zkCfg, sessionTimeout, this::onZkEvent);
        latch.await();

        this.lockGroupRoot = lockGroupRoot;
        this.lockName = lockName;
    }


    private void onZkEvent(WatchedEvent event) {
        switch (event.getState()) {
            case SyncConnected:
                latch.countDown();
                break;
            case Disconnected:
                this.closed = true;
                break;
        }

        // 如果发现 waitPath 被删除，表示自己前面的人已经解除锁占用
        if(event.getType() == Watcher.Event.EventType.NodeDeleted &&
                event.getPath().equals(this.waitPath)) {
            LOG.debug(this.selfPath + "-> 我前面的" + waitPath + "已经被删除，所占用解除，现在轮到我执行了");
            fireRun();
        }

    }

    private void initSelfPath() throws KeeperException, InterruptedException {
        // 安全创建锁root路径，路径为持久化
        String root = createPathSafe(zk, lockGroupRoot, CreateMode.PERSISTENT);
        LOG.debug("创建锁根路径:" + root);
        // 创建临时性锁目录 (创建递增路径，并获取该路径)
        this.selfPath = zk.create(root + "/" + lockName, DEF_DIR_BYTES, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        LOG.debug("创建锁:" + selfPath);
    }


    private synchronized void unlock() {
        try {
            if(null == zk.exists(selfPath, false)) {
                LOG.error(selfPath + "准备解锁时 发现节点已经不存在..");
                return;
            }
            zk.delete(this.selfPath, -1);
            zk.close();
        } catch (InterruptedException | KeeperException e) {
            LOG.error(selfPath + "解锁时发生异常", e);
        }
    }

    /**
     * 异步锁，调用该方法不会导致线程阻塞。而是在竞争到锁时自动回调。
     *
     * @param callback
     */
    public void synchronize(Consumer<SynchronizedEvent> callback) {
        this.setCallback(callback);
        if(null == zk) {
            callback.accept(new SynchronizedEvent(false, new RuntimeException("分布式锁依赖的 zookeeper 不能为空")));
            return;
        }
        if (closed) {
            callback.accept(new SynchronizedEvent(false, new RuntimeException("锁只能try lock一次。请重新创建锁后再进行try lock")));
            return;
        }
        try {
            initSelfPath();
            List<String> subNodes = zk.getChildren(lockGroupRoot, false);
            Collections.sort(subNodes);

            int index = subNodes.indexOf(selfPath.substring(lockGroupRoot.length() + 1));

            switch (index) {
                case -1:
                    LOG.error(selfPath + "节点已经不存在（已经被解锁,或因网络原因被迫解锁）");
                    callback.accept(new SynchronizedEvent(false, new RuntimeException("节点已经不存在（已经被解锁,或因网络原因被迫解锁）")));
                    break;
                case 0:
                    fireRun();
                    break;
                default:
                    this.waitPath = lockGroupRoot + "/" + subNodes.get(index - 1);
                    LOG.info(selfPath + "-> 在我前面的是:" + this.waitPath);
                    try {
                        zk.getData(this.waitPath, this::onZkEvent, new Stat());
                    } catch (KeeperException e) {
                        if(null == zk.exists(waitPath, false)) {
                            LOG.debug(selfPath + "-> 在我前面的人不见了，重新尝试锁住");
                            synchronize(callback);
                        } else {
                            callback.accept(new SynchronizedEvent(false, e));
                        }
                    }
            }
        } catch (KeeperException | InterruptedException e) {
            callback.accept(new SynchronizedEvent(false, e));
        }

    }

    protected void fireRun() {
        this.isRun = true;
        if(null != callback) {
            LOG.debug(selfPath + "-> 执行");
            callback.accept(new SynchronizedEvent(true, null));
            LOG.debug(selfPath + "-> 开始解锁");
            unlock();
            LOG.debug(selfPath + "-> 解锁结束");
        }
    }

    private void setCallback(Consumer<SynchronizedEvent> callback) {
        this.callback = callback;
        if(isRun) {
            callback.accept(new SynchronizedEvent(true, null));
            unlock();
        }
    }


    /**
     * 该方法将循环创建目录(仅创建目录)。
     * @param zk
     * @param path 路径
     * @param mode 目录内数据类型
     * @return
     */
    public static String createPathSafe(ZooKeeper zk, String path, CreateMode mode) throws KeeperException, InterruptedException {

        Stat stat = zk.exists(path, false);

        if(null != stat) {
            return path;
        }

        if(path.lastIndexOf('/') != 0) {
            String parent = path.substring(0, path.lastIndexOf('/'));
            createPathSafe(zk, parent, CreateMode.PERSISTENT);
        }

        return zk.create(path, DEF_DIR_BYTES, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
    }

    public static class SynchronizedEvent {
        private final boolean locked;
        private final Exception warn;

        public SynchronizedEvent(boolean locked, Exception warn) {
            this.locked = locked;
            this.warn = warn;
        }

        public boolean isLocked() {
            return locked;
        }

        public Exception getWarn() {
            return warn;
        }

    }
}
