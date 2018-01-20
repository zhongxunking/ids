/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2018-01-21 00:53 创建
 */
package org.antframework.ids;

import org.junit.Ignore;
import org.junit.Test;

/**
 * UID单元测试
 */
@Ignore
public class UIDTest {

    @Test
    public void testNewId() throws InterruptedException {
        System.setProperty(IdsParams.APP_CODE_PROPERTY_NAME, "ids-test");
        System.setProperty(IdsParams.APP_PORT_PROPERTY_NAME, "8080");
        System.setProperty(IdsParams.SERVER_URL_PROPERTY_NAME, "http://localhost:6210");
        System.setProperty(IdsParams.ZK_URLS_PROPERTY_NAME, "localhost:2181");
        System.setProperty(IdsParams.HOME_PATH_PROPERTY_NAME, System.getProperty("user.home") + "/ids");

        String id0 = UID.newId();
        String id1 = UID.newId();
        String id2 = UID.newId();
        String id3 = UID.newId();
        String id4 = UID.newId();
    }
}
