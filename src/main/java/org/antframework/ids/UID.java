/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2018-01-20 16:25 创建
 */
package org.antframework.ids;

import org.antframework.common.util.id.IdGenerator;
import org.antframework.common.util.id.PeriodType;
import org.antframework.idcenter.client.Id;
import org.antframework.idcenter.client.IdContext;

/**
 * 唯一id（unique id）
 */
public class UID {
    // 机房id
    private static final String ROOM_ID = IdsParams.getRoomId();
    // id长度
    private static final int ID_LENGTH = ROOM_ID.length() + 20;
    // id上下文
    private static final IdContext ID_CONTEXT = IdsParams.createIdContext("common-uid");
    // id生成器
    private static final IdGenerator ID_GENERATOR = IdsParams.createIdGenerator("common-uid", PeriodType.DAY, 10000000L);
    // 机房id与workerId的组合
    private static String ROOM_ID_WORKER_ID = null;

    /**
     * 创建新的id
     */
    public static String newId() {
        String id = fromServer();
        if (id == null) {
            id = fromLocal();
        }
        return id;
    }

    // 从服务端获取id（格式：yyyyMMddHH+机房id+10位数的id）
    private static String fromServer() {
        Id id = ID_CONTEXT.getAcquirer().getId();
        if (id == null) {
            return null;
        }
        // 构建id
        StringBuilder builder = new StringBuilder(ID_LENGTH);
        builder.append(id.getPeriod().toString());
        builder.append(ROOM_ID);
        finishId(builder, id.getId());
        return builder.toString();
    }

    // 从本地获取id（格式：yyyyMMdd+机房id和5位数的workerId的组合+7位数的id）
    private static String fromLocal() {
        org.antframework.common.util.id.Id id = ID_GENERATOR.getId();
        // 构建id
        StringBuilder builder = new StringBuilder(ID_LENGTH);
        builder.append(id.getPeriod().toString());
        builder.append(getRoomIdWorkerId());
        finishId(builder, id.getId());
        return builder.toString();
    }

    // 获取机房id与workerId的组合
    private static String getRoomIdWorkerId() {
        if (ROOM_ID_WORKER_ID == null) {
            synchronized (UID.class) {
                if (ROOM_ID_WORKER_ID == null) {
                    int workerId = IdsParams.getWorkerId(75000);
                    String temp = Integer.toString(25000 + workerId);
                    ROOM_ID_WORKER_ID = temp.substring(0, 2) + ROOM_ID + temp.substring(2);
                }
            }
        }
        return ROOM_ID_WORKER_ID;
    }

    // 完成id
    private static void finishId(StringBuilder builder, long id) {
        String idStr = Long.toString(id);
        int length = ID_LENGTH - builder.length() - idStr.length();
        for (int i = 0; i < length; i++) {
            builder.append('0');
        }
        builder.append(idStr);
    }
}
