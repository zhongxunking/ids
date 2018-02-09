/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2018-01-20 20:43 创建
 */
package org.antframework.ids;

import org.antframework.common.util.other.Cache;
import org.antframework.idcenter.client.Id;
import org.antframework.idcenter.client.IdAcquirer;
import org.antframework.idcenter.client.IdContext;
import org.antframework.idcenter.client.core.PeriodType;

/**
 * 应用内id生成器
 */
public class IDS {
    // id上下文缓存
    private static final Cache<String, IdContext> CACHE = new Cache<>(new Cache.Supplier<String, IdContext>() {
        @Override
        public IdContext get(String key) {
            return IdsParams.createIdContext(IdsParams.getAppCode() + "-" + key);
        }
    });

    /**
     * 创建新的id
     *
     * @param idCode id编码
     * @return 新的id
     * @throws IllegalStateException 如果获取id失败
     */
    public static long newId(String idCode) {
        IdAcquirer idAcquirer = CACHE.get(idCode).getAcquirer();
        Id id = idAcquirer.getId();
        if (id == null) {
            throw new IllegalStateException("从id中心获取id失败");
        }
        if (id.getPeriod().getType() != PeriodType.NONE) {
            throw new IllegalStateException("id周期类型必须是无");
        }
        return id.getId();
    }
}
