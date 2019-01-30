/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2018-01-20 21:04 创建
 */
package org.antframework.ids;

import org.antframework.common.util.encryption.AdvancedCaesar;
import org.antframework.common.util.id.PeriodType;
import org.antframework.common.util.id.local.IdGenerator;
import org.antframework.common.util.other.PropertyUtils;
import org.antframework.common.util.zookeeper.WorkerId;
import org.antframework.idcenter.client.Ider;
import org.antframework.idcenter.client.IdersContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * ids参数
 */
public class IdsParams {
    /**
     * 数据中心id属性key（如果不存在多数据中心，则不用填）
     */
    public static final String IDC_ID_KEY = "ids.idc-id";
    /**
     * id中心地址属性key
     */
    public static final String SERVER_URL_KEY = "ids.server-url";
    /**
     * ids的home文件路径属性key
     */
    public static final String HOME_PATH_KEY = "ids.home-path";
    /**
     * worker编码属性key（每个应用实例的worker编码应该唯一）
     */
    public static final String WORKER_KEY = "ids.worker";
    /**
     * zookeeper地址属性key（存在多个zookeeper的话以“,”分隔（比如：192.168.0.1:2181,192.168.0.2:2181））
     */
    public static final String ZK_URLS_KEY = "ids.zk-urls";
    /**
     * 加密种子属性key（如果不需要对id进行加密，则不用填；否则填入整型数字，例如："123"）
     */
    public static final String ENCRYPTION_SEED_KEY = "ids.encryption-seed";
    // id提供者上下文
    private static volatile IdersContext IDERS_CONTEXT;

    /**
     * 获取数据中心id
     */
    static String getIdcId() {
        return PropertyUtils.getProperty(IDC_ID_KEY, "");
    }

    /**
     * 获取id提供者
     *
     * @param iderId id提供者的id（id编码）
     * @return id提供者
     */
    static Ider getIder(String iderId) {
        if (IDERS_CONTEXT == null) {
            synchronized (IdsParams.class) {
                if (IDERS_CONTEXT == null) {
                    IDERS_CONTEXT = new IdersContext(PropertyUtils.getRequiredProperty(SERVER_URL_KEY), 10 * 60 * 1000, 15 * 60 * 1000);
                }
            }
        }
        return IDERS_CONTEXT.getIder(iderId);
    }

    /**
     * 创建id生成器
     *
     * @param iderId     id提供者的id（id编码）
     * @param periodType 周期类型
     * @param maxId      id最大值（不包含）
     * @return id生成器
     */
    static IdGenerator createIdGenerator(String iderId, PeriodType periodType, Long maxId) {
        String filePath = PropertyUtils.getRequiredProperty(HOME_PATH_KEY) + File.separator + String.format("ids-idGenerator-%s.properties", iderId);
        return new IdGenerator(periodType, 1000, maxId, filePath);
    }

    /**
     * 获取workerId
     */
    static int getWorkerId() {
        String worker = PropertyUtils.getRequiredProperty(WORKER_KEY);
        String[] zkUrls = StringUtils.split(PropertyUtils.getRequiredProperty(ZK_URLS_KEY), ',');
        if (ArrayUtils.isEmpty(zkUrls)) {
            throw new IllegalArgumentException("必须配置zookeeper地址：" + ZK_URLS_KEY);
        }
        String filePath = PropertyUtils.getRequiredProperty(HOME_PATH_KEY) + File.separator + "ids-workerId.properties";

        return WorkerId.getId(worker, zkUrls, "/ids/workerId", filePath);
    }

    /**
     * 获取加密器
     */
    static AdvancedCaesar getEncryptor() {
        String seed = PropertyUtils.getProperty(ENCRYPTION_SEED_KEY);
        if (StringUtils.isBlank(seed)) {
            return null;
        }
        return new AdvancedCaesar("0123456789".toCharArray(), Long.parseLong(seed));
    }
}
