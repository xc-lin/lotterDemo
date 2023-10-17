package com.frank.lotterdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.frank.lotterdemo.domain.AwardBatch;
import com.frank.lotterdemo.service.AwardBatchService;
import com.frank.lotterdemo.mapper.AwardBatchMapper;
import org.springframework.stereotype.Service;

/**
* @author linxianchao3
* @description 针对表【award_batch】的数据库操作Service实现
* @createDate 2023-10-16 18:14:19
*/
@Service
public class AwardBatchServiceImpl extends ServiceImpl<AwardBatchMapper, AwardBatch>
implements AwardBatchService{

}
