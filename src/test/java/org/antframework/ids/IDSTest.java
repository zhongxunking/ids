/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2018-01-21 15:39 创建
 */
package org.antframework.ids;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
@Ignore
public class IDSTest {

    @Before
    public void init() {
        System.setProperty(IdsParams.APP_CODE_PROPERTY_NAME, "ids-test");
        System.setProperty(IdsParams.APP_PORT_PROPERTY_NAME, "8080");
        System.setProperty(IdsParams.SERVER_URL_PROPERTY_NAME, "http://localhost:6210");
        System.setProperty(IdsParams.ZK_URLS_PROPERTY_NAME, "localhost:2181");
        System.setProperty(IdsParams.HOME_PATH_PROPERTY_NAME, System.getProperty("user.home") + "/ids");
    }

    @Test
    public void testNewId() {
        long id1 = IDS.newId("aa");
        long id2 = IDS.newId("bb");
        long id3 = IDS.newId("cc");
        long id4 = IDS.newId("aa");
        long id5 = IDS.newId("bb");
    }
}
