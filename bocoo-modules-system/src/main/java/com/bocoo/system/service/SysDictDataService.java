package com.bocoo.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.constant.CacheNames;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.system.domain.bo.SysDictDataBo;
import com.bocoo.system.domain.entity.SysDictData;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.redis.utils.CacheUtils;
import com.bocoo.system.domain.vo.SysDictDataVo;
import com.bocoo.system.mapper.SysDictDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典 业务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysDictDataService {

    private final SysDictDataMapper dictDataMapper;
    private final SysI18nMessageService i18nMessageService;

    public TableDataInfo<SysDictDataVo> selectPageDictDataList(SysDictDataBo dictData, PageQuery pageQuery) {
        LambdaQueryWrapper<SysDictData> lqw = buildQueryWrapper(dictData);
        Page<SysDictDataVo> page = dictDataMapper.selectVoPage(pageQuery.build(), lqw);
        translateDictDataList(page.getRecords());
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询字典数据
     *
     * @param dictData 字典数据信息
     * @return 字典数据集合信息
     */
    public List<SysDictDataVo> selectDictDataList(SysDictDataBo dictData) {
        LambdaQueryWrapper<SysDictData> lqw = buildQueryWrapper(dictData);
        List<SysDictDataVo> dictDatas = dictDataMapper.selectVoList(lqw);
        translateDictDataList(dictDatas);
        return dictDatas;
    }

    private LambdaQueryWrapper<SysDictData> buildQueryWrapper(SysDictDataBo bo) {
        LambdaQueryWrapper<SysDictData> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getDictSort() != null, SysDictData::getDictSort, bo.getDictSort());
        lqw.like(StringUtils.isNotBlank(bo.getDictLabel()), SysDictData::getDictLabel, bo.getDictLabel());
        lqw.eq(StringUtils.isNotBlank(bo.getDictType()), SysDictData::getDictType, bo.getDictType());
        lqw.orderByAsc(SysDictData::getDictSort);
        return lqw;
    }

    /**
     * 根据字典数据ID查询信息
     *
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    public SysDictDataVo selectDictDataById(Long dictCode) {
        SysDictDataVo dictData = dictDataMapper.selectVoById(dictCode);
        translateDictData(dictData);
        return dictData;
    }

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     */
    public void deleteDictDataByIds(Long[] dictCodes) {
        for (Long dictCode : dictCodes) {
            SysDictData data = dictDataMapper.selectById(dictCode);
            dictDataMapper.deleteById(dictCode);
            CacheUtils.evict(CacheNames.SYS_DICT, data.getDictType());
        }
    }

    /**
     * 新增保存字典数据信息
     *
     * @param bo 字典数据信息
     * @return 结果
     */
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
    public List<SysDictDataVo> insertDictData(SysDictDataBo bo) {
        SysDictData data = MapstructUtils.convert(bo, SysDictData.class);
        int row = dictDataMapper.insert(data);
        if (row > 0) {
            return dictDataMapper.selectDictDataByType(data.getDictType());
        }
        throw ServiceException.ofMessageKey("dict.data.insert.failed");
    }

    /**
     * 修改保存字典数据信息
     *
     * @param bo 字典数据信息
     * @return 结果
     */
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#bo.dictType")
    public List<SysDictDataVo> updateDictData(SysDictDataBo bo) {
        SysDictData data = MapstructUtils.convert(bo, SysDictData.class);
        int row = dictDataMapper.updateById(data);
        if (row > 0) {
            return dictDataMapper.selectDictDataByType(data.getDictType());
        }
        throw ServiceException.ofMessageKey("dict.data.update.failed");
    }

    public boolean checkDictDataUnique(SysDictDataBo dict) {
        Long dictCode = ObjectUtil.isNull(dict.getDictCode()) ? -1L : dict.getDictCode();
        SysDictData entity = dictDataMapper.selectOne(new LambdaQueryWrapper<SysDictData>()
            .eq(SysDictData::getDictType, dict.getDictType()).eq(SysDictData::getDictValue, dict.getDictValue()));
        if (ObjectUtil.isNotNull(entity) && !dictCode.equals(entity.getDictCode())) {
            return false;
        }
        return true;
    }

    public void translateDictDataList(List<SysDictDataVo> dictDatas) {
        if (CollUtil.isEmpty(dictDatas)) {
            return;
        }
        dictDatas.forEach(this::translateDictData);
    }

    public void translateDictData(SysDictDataVo dictData) {
        if (dictData == null) {
            return;
        }
        dictData.setDictLabel(i18nMessageService.translate(dictData.getI18nKey(), dictData.getDictLabel()));
    }

}
