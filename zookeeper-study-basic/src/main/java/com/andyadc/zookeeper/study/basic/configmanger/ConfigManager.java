package com.andyadc.zookeeper.study.basic.configmanger;

import org.I0Itec.zkclient.ZkClient;

/**
 * @author andaicheng
 * @version 2016/10/22
 */
public class ConfigManager {

    private FtpConfig ftpConfig;

    /**
     * 模拟从db加载初始配置
     */
    public void loadConfigFromDB() {
        //query config from db
        //TODO...
        ftpConfig = new FtpConfig(21, "192.168.1.1", "test", "123456");
    }

    /**
     * 模拟更新DB中的配置
     */
    public void updateFtpConfigToDB(int port, String host, String user, String password) {
        if (ftpConfig == null) {
            ftpConfig = new FtpConfig();
        }
        ftpConfig.setPort(port);
        ftpConfig.setHost(host);
        ftpConfig.setUser(user);
        ftpConfig.setPassword(password);
        //write to db...
        //TODO...
    }

    public void syncFtpConfigToZk() {
        ZkClient client = ZKUtil.getZkclient();
        if (!client.exists(ZKUtil.CONFIG_NODE_NAME)) {
            client.createPersistent(ZKUtil.CONFIG_NODE_NAME, true);
        }
        client.writeData(ZKUtil.CONFIG_NODE_NAME, ftpConfig);
        client.close();
    }
}
