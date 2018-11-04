/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2018-01-21 00:53 创建
 */
package org.antframework.ids;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * UID单元测试
 */
@Ignore
public class UIDTest {

    @Before
    public void init() {
        System.setProperty(IdsParams.IDC_ID_PROPERTY_NAME, "01");
        System.setProperty(IdsParams.SERVER_URL_PROPERTY_NAME, "http://localhost:6210");
        System.setProperty(IdsParams.HOME_PATH_PROPERTY_NAME, System.getProperty("user.home") + "/ids");
        System.setProperty(IdsParams.WORKER_PROPERTY_NAME, "127.0.0.1:8080");
        System.setProperty(IdsParams.ZK_URLS_PROPERTY_NAME, "localhost:2181");
        System.setProperty(IdsParams.ENCRYPTION_SEED, "123");
    }

    @Test
    public void testNewId() throws InterruptedException {
        String id0 = UID.newId();
        String id1 = UID.newId();
        String id2 = UID.newId();
        String id3 = UID.newId();
        String id4 = UID.newId();
    }

    @Test
    public void testNewIdPerformance() {
        UID.newId();
        long startTime = System.currentTimeMillis();
        int count = 10000000;
        int nullCount = 0;
        for (int i = 0; i < count; i++) {
            String id = UID.newId();
            if (id == null) {
                nullCount++;
            }
        }
        long timeCost = System.currentTimeMillis() - startTime;
        System.out.println(String.format("循环：%d次，id出现null：%d次，总耗时：%d毫秒，tps：%d", count, nullCount, timeCost, (count - nullCount) * 1000L / timeCost));
    }
}
