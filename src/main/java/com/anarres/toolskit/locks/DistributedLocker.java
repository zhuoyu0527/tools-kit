package com.anarres.toolskit.locks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;

/**
 * Created by ath on 2017/5/2.
 */
public class DistributedLocker {
    private static final Logger LOG = LoggerFactory.getLogger(DistributedLocker.class);

    private final String zkCfg;
    private final int sessionTimeout;
    public DistributedLocker(String zkCfg, int sessionTimeout) {
        this.zkCfg = zkCfg;
        this.sessionTimeout = sessionTimeout;
    }

    public Optional<LockKit> createLock(String lockGroupRoot, String lockName) {
        // 安全创建ZooKeeper
        try {

            return Optional.of(new LockKit(zkCfg, sessionTimeout, lockGroupRoot, lockName));
        } catch (IOException | InterruptedException e) {
            LOG.error(MessageFormat.format("创建{0}下的{1}锁失败", lockGroupRoot, lockName), e);
        }

        return Optional.empty();
    }


}
