package com.andyadc.zookeeper.study.basic.configmanger;

/**
 * @author andaicheng
 * @version 2016/10/22
 */
public class ConfigTest {

    public static void main(String[] args) throws InterruptedException {
        ConfigManager manager = new ConfigManager();
        ClientApp clientApp = new ClientApp();

        //模拟【配置管理中心】初始化时，从db加载配置初始参数
        manager.loadConfigFromDB();
        //然后将配置同步到ZK
        manager.syncFtpConfigToZk();

        //模拟客户端程序运行
        clientApp.run();

        //模拟配置修改
        manager.updateFtpConfigToDB(23, "10.6.12.34", "newUser", "newPwd");
        manager.syncFtpConfigToZk();

        //模拟客户端自动感知配置变化
        clientApp.run();
    }
}
