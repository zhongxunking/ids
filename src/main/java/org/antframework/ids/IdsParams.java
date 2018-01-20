/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2018-01-20 21:04 创建
 */
package org.antframework.ids;

import org.antframework.common.util.id.IdGenerator;
import org.antframework.common.util.id.PeriodType;
import org.antframework.common.util.other.IPUtils;
import org.antframework.common.util.other.PropertyUtils;
import org.antframework.common.util.zookeeper.WorkerId;
import org.antframework.idcenter.client.IdContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * ids参数
 */
public class IdsParams {
    /**
     * 应用编码属性名
     */
    public static final String APP_CODE_PROPERTY_NAME = "app.code";
    /**
     * 应用端口属性名
     */
    public static final String APP_PORT_PROPERTY_NAME = "app.port";
    /**
     * id中心地址属性名
     */
    public static final String SERVER_URL_PROPERTY_NAME = "ids.server-url";
    /**
     * zookeeper地址属性名
     */
    public static final String ZK_URLS_PROPERTY_NAME = "ids.zk-urls";
    /**
     * ids的home文件路径属性名
     */
    public static final String HOME_PATH_PROPERTY_NAME = "ids.home-path";

    /**
     * 获取应用编码
     */
    static String getAppCode() {
        return PropertyUtils.getRequiredProperty(APP_CODE_PROPERTY_NAME);
    }

    /**
     * 获取workerId
     *
     * @param maxWorkerId 允许的最大workerId（不包含）
     */
    static int getWorkId(int maxWorkerId) {
        String worker = IPUtils.getIPV4() + ":" + PropertyUtils.getRequiredProperty(APP_PORT_PROPERTY_NAME);
        String[] zkUrls = StringUtils.split(PropertyUtils.getRequiredProperty(ZK_URLS_PROPERTY_NAME), ',');
        if (ArrayUtils.isEmpty(zkUrls)) {
            throw new IllegalArgumentException("必须配置zookeeper地址：" + ZK_URLS_PROPERTY_NAME);
        }
        String filePath = PropertyUtils.getRequiredProperty(HOME_PATH_PROPERTY_NAME) + "/workerId.properties";

        int workerId = WorkerId.getId(worker, zkUrls, "/ids/workerId", filePath);
        if (workerId >= maxWorkerId) {
            throw new IllegalStateException("worker数量超过最大值：" + maxWorkerId);
        }
        return workerId;
    }

    /**
     * 创建id上下文
     *
     * @param idCode id编码
     * @return id上下文
     */
    static IdContext createIdContext(String idCode) {
        IdContext.InitParams initParams = new IdContext.InitParams();
        initParams.setIdCode(idCode);
        initParams.setServerUrl(PropertyUtils.getRequiredProperty(SERVER_URL_PROPERTY_NAME));
        initParams.setInitAmount(1000);
        initParams.setMinTime(10 * 60 * 1000);
        initParams.setMaxTime(15 * 60 * 1000);

        return new IdContext(initParams);
    }

    /**
     * 创建id生成器
     *
     * @param idCode id编码
     * @return id生成器
     */
    static IdGenerator createIdGenerator(String idCode, PeriodType periodType, Long maxId) {
        String filePath = PropertyUtils.getRequiredProperty(HOME_PATH_PROPERTY_NAME) + "/" + idCode + "-idGenerator.properties";
        return new IdGenerator(periodType, 1000, maxId, filePath);
    }
}
