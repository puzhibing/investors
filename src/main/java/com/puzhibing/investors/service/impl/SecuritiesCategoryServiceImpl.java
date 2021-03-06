package com.puzhibing.investors.service.impl;

import com.puzhibing.investors.dao.SecuritiesCategoryMapper;
import com.puzhibing.investors.pojo.SecuritiesCategory;
import com.puzhibing.investors.service.ISecuritiesCategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class SecuritiesCategoryServiceImpl implements ISecuritiesCategoryService {

    @Resource
    private SecuritiesCategoryMapper securitiesCategoryMapper;


    /**
     * 根据编号获取证券类型
     * @param code
     * @return
     * @throws Exception
     */
    @Override
    public SecuritiesCategory queryByCode(String code) throws Exception {
        return securitiesCategoryMapper.queryByCode(code);
    }

    /**
     * 根据id查询数据
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public SecuritiesCategory selectById(Integer id) throws Exception {
        return securitiesCategoryMapper.selectById(id);
    }

    /**
     * 获取列表数据
     * @return
     * @throws Exception
     */
    @Override
    public List<SecuritiesCategory> selectList() throws Exception {
        return securitiesCategoryMapper.selectList();
    }
}
