package com.bocoo.common.core.service;

import java.util.Collection;

/**
 * 通用 OSS服务
 *
 * @author cmx
 */
public interface OssService {

    /**
     * 通过ossId查询对应的url
     *
     * @param ossIds ossId串逗号分隔
     * @return url串逗号分隔
     */
    String selectUrlByIds(String ossIds);

    /**
     * 删除OSS文件及其数据库记录
     *
     * @param ossIds OSS文件ID集合
     * @return 删除是否成功
     */
    Boolean deleteByIds(Collection<Long> ossIds);

}
