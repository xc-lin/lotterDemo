package com.frank.lotterdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.frank.lotterdemo.domain.AwardBatch;
import org.apache.ibatis.annotations.Mapper;

/**
* @author linxianchao3
* @description 针对表【award_batch】的数据库操作Mapper
* @createDate 2023-10-16 18:14:19
* @Entity generator.domain.AwardBatch
*/
@Mapper
public interface AwardBatchMapper extends BaseMapper<AwardBatch> {


}
