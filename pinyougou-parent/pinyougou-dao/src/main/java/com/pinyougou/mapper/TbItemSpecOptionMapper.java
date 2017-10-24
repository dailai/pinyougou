package com.pinyougou.mapper;

import com.pinyougou.pojo.TbItemSpecOption;
import com.pinyougou.pojo.TbItemSpecOptionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TbItemSpecOptionMapper {
    int countByExample(TbItemSpecOptionExample example);

    int deleteByExample(TbItemSpecOptionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TbItemSpecOption record);

    int insertSelective(TbItemSpecOption record);

    List<TbItemSpecOption> selectByExample(TbItemSpecOptionExample example);

    TbItemSpecOption selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TbItemSpecOption record, @Param("example") TbItemSpecOptionExample example);

    int updateByExample(@Param("record") TbItemSpecOption record, @Param("example") TbItemSpecOptionExample example);

    int updateByPrimaryKeySelective(TbItemSpecOption record);

    int updateByPrimaryKey(TbItemSpecOption record);
}