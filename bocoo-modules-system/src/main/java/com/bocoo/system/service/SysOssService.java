package com.bocoo.system.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.constant.CacheNames;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.core.service.OssService;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StreamUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.file.FileUtils;
import com.bocoo.common.core.utils.SpringUtils;
import com.bocoo.common.oss.core.OssClient;
import com.bocoo.common.oss.entity.UploadResult;
import com.bocoo.common.oss.enumd.AccessPolicyType;
import com.bocoo.common.oss.factory.OssFactory;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.system.domain.entity.SysOss;
import com.bocoo.system.domain.bo.SysOssBo;
import com.bocoo.system.domain.vo.SysOssVo;
import com.bocoo.system.mapper.SysOssMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 文件上传 服务层实现
 *
 * @author cmx
 */

/**
 * 系统OSS服务实现类，提供文件上传、下载、查询、删除等操作。
 * 实现了通用的OSS接口，并基于数据库和OSS客户端进行文件管理。
 */
@RequiredArgsConstructor
@Service
public class SysOssService implements OssService {

    private final SysOssMapper ossMapper;

    /**
     * 分页查询OSS文件列表
     *
     * @param bo        查询条件对象
     * @param pageQuery 分页参数对象
     * @return 返回分页数据信息，包含OSS文件VO列表
     */
    public TableDataInfo<SysOssVo> queryPageList(SysOssBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysOss> lqw = buildQueryWrapper(bo);
        pageQuery.setOrderByColumn("create_time");
        pageQuery.setIsAsc("desc");
        Page<SysOssVo> result = ossMapper.selectPageOssList(pageQuery.build(), lqw);
        List<SysOssVo> filterResult = StreamUtils.toList(result.getRecords(), this::matchingUrl);
        result.setRecords(filterResult);
        return TableDataInfo.build(result);
    }


    /**
     * 根据ID集合批量获取OSS文件信息
     *
     * @param ossIds OSS文件ID集合
     * @return 返回OSS文件VO列表
     */
    public List<SysOssVo> listByIds(Collection<Long> ossIds) {
        List<SysOssVo> list = new ArrayList<>();
        for (Long id : ossIds) {
            SysOssVo vo = SpringUtils.getAopProxy(this).getById(id);
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    list.add(this.matchingUrl(vo));
                } catch (Exception ignored) {
                    // 如果oss异常无法连接则将数据直接返回
                    list.add(vo);
                }
            }
        }
        return list;
    }

    /**
     * 根据ID字符串获取对应的文件URL并拼接返回
     *
     * @param ossIds 以逗号分隔的OSS文件ID字符串
     * @return 拼接后的URL字符串
     */
    public String selectUrlByIds(String ossIds) {
        List<String> list = new ArrayList<>();
        for (Long id : StringUtils.splitTo(ossIds, Convert::toLong)) {
            SysOssVo vo = SpringUtils.getAopProxy(this).getById(id);
            if (ObjectUtil.isNotNull(vo)) {
                try {
                    list.add(this.matchingUrl(vo).getUrl());
                } catch (Exception ignored) {
                    // 如果oss异常无法连接则将数据直接返回
                    list.add(vo.getUrl());
                }
            }
        }
        return String.join(StringUtils.SEPARATOR, list);
    }

    /**
     * 构建查询条件包装器
     *
     * @param bo 查询条件对象
     * @return LambdaQueryWrapper 查询条件构造器
     */
    private LambdaQueryWrapper<SysOss> buildQueryWrapper(SysOssBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysOss> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), SysOss::getFileName, bo.getFileName());
        lqw.like(StringUtils.isNotBlank(bo.getOriginalName()), SysOss::getOriginalName, bo.getOriginalName());
        lqw.eq(StringUtils.isNotBlank(bo.getFileSuffix()), SysOss::getFileSuffix, bo.getFileSuffix());
        lqw.eq(StringUtils.isNotBlank(bo.getUrl()), SysOss::getUrl, bo.getUrl());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
                SysOss::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        lqw.eq(StringUtils.isNotBlank(bo.getCreateBy()), SysOss::getCreateBy, bo.getCreateBy());
        lqw.eq(StringUtils.isNotBlank(bo.getService()), SysOss::getService, bo.getService());
        return lqw;
    }

    /**
     * 根据ID获取OSS文件详情（带缓存）
     *
     * @param ossId OSS文件ID
     * @return 返回OSS文件VO对象
     */
    @Cacheable(cacheNames = CacheNames.SYS_OSS, key = "#ossId")
    public SysOssVo getById(Long ossId) {
        return ossMapper.selectVoById(ossId);
    }

    /**
     * 下载指定ID的OSS文件
     *
     * @param ossId    OSS文件ID
     * @param response HTTP响应对象
     * @throws IOException IO异常
     */
    public void download(Long ossId, HttpServletResponse response) throws IOException {
        SysOssVo sysOss = SpringUtils.getAopProxy(this).getById(ossId);
        if (ObjectUtil.isNull(sysOss)) {
            throw new ServiceException("文件数据不存在!");
        }
        FileUtils.setAttachmentResponseHeader(response, sysOss.getOriginalName());
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE + "; charset=UTF-8");
        OssClient storage = OssFactory.instance(sysOss.getService());
        try (InputStream inputStream = storage.getObjectContent(sysOss.getUrl())) {
            int available = inputStream.available();
            IoUtil.copy(inputStream, response.getOutputStream(), available);
            response.setContentLength(available);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 上传MultipartFile类型的文件
     *
     * @param file MultipartFile文件对象
     * @return 返回上传后的OSS文件VO对象
     */
    public SysOssVo upload(MultipartFile file) {
        String originalfileName = file.getOriginalFilename();
        String suffix = null;

        if (originalfileName != null && originalfileName.lastIndexOf(".") > 0) {
            suffix = originalfileName.substring(originalfileName.lastIndexOf("."));
        }

        OssClient storage = OssFactory.instance();
        UploadResult uploadResult;
        try {
            uploadResult = storage.uploadSuffix(file.getBytes(), suffix, file.getContentType());
        } catch (IOException e) {
            throw new ServiceException("文件上传失败");
        }

        // 保存文件信息
        return buildResultEntity(originalfileName, suffix, storage.getConfigKey(), uploadResult);
    }


    /**
     * 上传本地File类型的文件
     *
     * @param file 本地文件对象
     * @return 返回上传后的OSS文件VO对象
     */
    public SysOssVo upload(File file) {
        String originalfileName = file.getName();
        String suffix = StringUtils.substring(originalfileName, originalfileName.lastIndexOf("."), originalfileName.length());
        OssClient storage = OssFactory.instance();
        UploadResult uploadResult = storage.uploadSuffix(file, suffix);
        // 保存文件信息
        return buildResultEntity(originalfileName, suffix, storage.getConfigKey(), uploadResult);
    }

    /**
     * 构建并保存上传结果实体
     *
     * @param originalfileName 原始文件名
     * @param suffix           文件后缀
     * @param configKey        配置键
     * @param uploadResult     上传结果对象
     * @return 返回OSS文件VO对象
     */
    private SysOssVo buildResultEntity(String originalfileName, String suffix, String configKey, UploadResult uploadResult) {
        SysOss oss = new SysOss();
        oss.setUrl(uploadResult.getUrl());
        oss.setFileSuffix(suffix);
        oss.setFileName(uploadResult.getFilename());
        oss.setOriginalName(originalfileName);
        oss.setService(configKey);
        oss.setDeptId(LoginHelper.getLoginUser().getDeptId());

        ossMapper.insert(oss);
        SysOssVo sysOssVo = MapstructUtils.convert(oss, SysOssVo.class);
        return this.matchingUrl(sysOssVo);
    }


    /**
     * 批量删除OSS文件及其数据库记录
     *
     * @param ids     要删除的OSS文件ID集合
     * @param isValid 是否需要做业务校验
     * @return 删除是否成功
     */
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 做一些业务上的校验,判断是否需要校验
        }
        List<SysOss> list = ossMapper.selectBatchIds(ids);
        for (SysOss sysOss : list) {
            OssClient storage = OssFactory.instance(sysOss.getService());
            storage.delete(sysOss.getUrl());
        }
        return ossMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 匹配Url
     *
     * @param oss OSS对象
     * @return oss 匹配Url的OSS对象
     */
    private SysOssVo matchingUrl(SysOssVo oss) {
        OssClient storage = OssFactory.instance(oss.getService());
        // 仅修改桶类型为 private 的URL，临时URL时长为120s
        if (AccessPolicyType.PRIVATE == storage.getAccessPolicy()) {
            oss.setUrl(storage.getPrivateUrl(oss.getFileName(), 120));
        }
        return oss;
    }
}
