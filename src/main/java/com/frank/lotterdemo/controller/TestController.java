package com.frank.lotterdemo.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.frank.lotterdemo.domain.AwardBatch;
import com.frank.lotterdemo.service.AwardBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Slf4j
public class TestController {

    @Autowired
    private AwardBatchService awardBatchService;

    private AtomicInteger concurrency = new AtomicInteger();

    ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);

    {
        ses.scheduleAtFixedRate(() -> {
            log.info("并发数: "+concurrency.get());
            concurrency.set(0);
        }, 0L, 1L, TimeUnit.SECONDS);
    }


    @GetMapping("/test")
    public String test() {
        concurrency.incrementAndGet();

        // 获取抽取的奖品
        AwardBatch currentAwardBatch = getAwardBatch();
        if (currentAwardBatch == null) {
            return "未中奖";
        }

        // 抽奖活动开始结束时间
        DateTime startTime = DateUtil.parse("2023-10-17 11:25:00");
        DateTime endTime = DateUtil.parse("2023-10-17 11:26:00");
        // 获取抽奖开始结束时间的间隔，
        long second = DateUtil.between(startTime, endTime, DateUnit.SECOND);
        // 根据间隔/ 当前奖品一共的数量，计算出每个奖品应该发放的间隔时间
        int segment = (int) (second / currentAwardBatch.getAmount());
        // startTime + 已发放奖品数量 * segment + segment随机数
        // random的种子数为当前奖品上次更新时间，只要这个奖品不被抽掉，那么这个随机数一定一样
        // 计算出每个奖品的释放时间
        DateTime releaseTime = DateUtil.offsetSecond(startTime,
                (currentAwardBatch.getAmount() - currentAwardBatch.getBalance()) * segment
                        + new Random(currentAwardBatch.getLastUpdateTime().getTime()).nextInt(segment));
        // 如果当前时间大于当前奖品的释放时间，则抽中
        if (new Date().after(releaseTime)) {
            Integer oldBalance = currentAwardBatch.getBalance();
            currentAwardBatch.setBalance(currentAwardBatch.getBalance() - 1);
            currentAwardBatch.setLastUpdateTime(new Date());
            boolean update = awardBatchService.update(currentAwardBatch, new LambdaUpdateWrapper<AwardBatch>().eq(AwardBatch::getBalance, oldBalance).eq(AwardBatch::getId, currentAwardBatch.getId()));
            // 乐观锁更新失败，那么为未中奖
            if (update) {
                return currentAwardBatch.getName();
            }
            return "未中奖";

        }
        return "未中奖";
    }

    /**
     * 获取当前抽取的奖品
     * @return
     */
    private AwardBatch getAwardBatch() {
        // 获取全部奖品
        List<AwardBatch> awardBatchList = awardBatchService.list();
        int weight = 0;
        AwardBatch currentAwardBatch = null;
        for (AwardBatch awardBatch : awardBatchList) {
            weight += awardBatch.getBalance();
        }
        // weight <= 0 说明抽完了
        if (weight <= 0) {
            return null;
        }
        // 随机取一个商品，商品库存越多，权重越大
        int randomWeight = new Random().nextInt(weight);
        for (AwardBatch awardBatch : awardBatchList) {
            randomWeight -= awardBatch.getBalance();
            if (randomWeight < 0) {
                currentAwardBatch = awardBatch;
                break;
            }
        }
        return currentAwardBatch;
    }

    @GetMapping("/yace")
    public void yace() throws InterruptedException {
        Map<String, AtomicInteger> result = new HashMap<>();
        result.put("prize1", new AtomicInteger());
        result.put("prize2", new AtomicInteger());
        result.put("prize3", new AtomicInteger());
        result.put("未中奖", new AtomicInteger());
        Date now = new Date();
        DateTime endTime = DateUtil.offsetMinute(now, 3);
        for (int i = 0; i < 20; i++) {
           new Thread(()->{
                   while (new Date().before(endTime)) {
                       String prize = test();
                       if (!prize.equals("未中奖")) {
                           log.info(prize);
                       }
                       result.get(prize).incrementAndGet();
                   }
           }).start();
        }
        while (true) {
            if (new Date().after(endTime)) {
                log.info(JSON.toJSONString(result));
            }
            Thread.sleep(10000);
        }
    }
}
