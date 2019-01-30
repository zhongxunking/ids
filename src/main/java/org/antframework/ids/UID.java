/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2018-01-20 16:25 创建
 */
package org.antframework.ids;

import org.antframework.common.util.encryption.AdvancedCaesar;
import org.antframework.common.util.id.Id;
import org.antframework.common.util.id.PeriodType;
import org.antframework.common.util.id.local.IdGenerator;
import org.antframework.idcenter.client.Ider;

/**
 * 唯一id（unique id）
 */
public class UID {
    // 数据中心id
    private static final String IDC_ID = IdsParams.getIdcId();
    // 数据中心id与workerId的组合
    private static String IDC_ID_WORKER_ID = getIdcIdWorkerId();
    // id长度
    private static final int ID_LENGTH = 20 + IDC_ID.length();
    // id提供者
    private static final Ider IDER = IdsParams.getIder("common-uid");
    // id生成器
    private static final IdGenerator ID_GENERATOR = IdsParams.createIdGenerator("common-uid", PeriodType.DAY, 10000000L);
    // 加密器
    private static final AdvancedCaesar ENCRYPTOR = IdsParams.getEncryptor();

    /**
     * 获取新id
     */
    public static String newId() {
        String id = fromServer();
        if (id == null) {
            id = fromLocal();
        }
        return id;
    }

    // 从服务端获取id（格式：yyyyMMddHH+数据中心id+10位数的id）
    private static String fromServer() {
        Id id = IDER.acquire();
        if (id == null) {
            return null;
        }
        return formatId(id, IDC_ID);
    }

    // 从本地获取id（格式：yyyyMMdd+数据中心id和5位数的workerId的组合+7位数的id）
    private static String fromLocal() {
        Id id = ID_GENERATOR.getId();
        return formatId(id, IDC_ID_WORKER_ID);
    }

    // 对id进行格式化
    private static String formatId(Id id, String addition) {
        StringBuilder builder = new StringBuilder(ID_LENGTH);
        builder.append(id.getPeriod());
        builder.append(addition);
        // 计算格式化后的id值（包括加密）
        String idStr = Long.toString(id.getId());
        int zeroLength = ID_LENGTH - builder.length() - idStr.length();
        StringBuilder idBuilder = new StringBuilder(zeroLength);
        for (int i = 0; i < zeroLength; i++) {
            idBuilder.append('0');
        }
        idBuilder.append(idStr);
        idStr = idBuilder.toString();
        if (ENCRYPTOR != null) {
            // 加密
            idStr = ENCRYPTOR.encode(idStr);
        }
        // 追加到末尾
        builder.append(idStr);

        return builder.toString();
    }

    // 获取数据中心id与workerId的组合
    private static String getIdcIdWorkerId() {
        int workerId = IdsParams.getWorkerId();
        if (workerId >= 75000) {
            throw new IllegalStateException("workerId必须小于75000");
        }
        String workerIdStr = Integer.toString(25000 + workerId);
        return workerIdStr.substring(0, 2) + IDC_ID + workerIdStr.substring(2);
    }
}
