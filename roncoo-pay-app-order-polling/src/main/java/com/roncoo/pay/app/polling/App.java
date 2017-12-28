/*
 * Copyright 2015-2102 RonCoo(http://www.roncoo.com) Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.roncoo.pay.app.polling;


import com.roncoo.pay.app.polling.core.PollingPersist;
import com.roncoo.pay.app.polling.core.PollingTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.DelayQueue;

/**
 * <b>功能说明:消息APP启动类
 * 
 * 在PollingMessageListener中
 * 1.启动后，在PollingMessageListener中会从ActiveMQ的OrderQueue中获取消息，即bankOrderNo
 * 2.封装订单结果查询实体（RpOrderResultQueryVo）,主要用于MQ查询上游订单结果时,查询规则及查询结果
 * 3.自动注入PollingQueue，将2中订单结果查询实体，添加入通知任务延时队列
 * 		pollingQueue.addToNotifyTaskDelayQueue(rpOrderResultQueryVo); // 添加到通知队列(第一次通知)
 * 
 * 在PollingQueue中
 * 1.将传过来的对象进行通知次数判断，决定是否放在任务队列中
 * 	App.tasks.put(new PollingTask(rpOrderResultQueryVo));
 * 
 * 在App中
 * 1.生成线程池threadPool
 * 2.生成轮询业务类pollingPersist
 * 3.启动线程
 * 4.轮询取出延时队列中的PollingTask执行，通知时间到之后，执行通知处理
 * 5.执行PollingTask中run方法
 * 		pollingPersist.getOrderResult(rpOrderResultQueryVo);
 * 	
 * 
 * 在PollingPersist中
 * 1.boolean processingResult = rpTradePaymentManagerService.processingTradeRecord(rpOrderResultQueryVo.getBankOrderNo());
 * 2. 如果不成功，且未超过最大通知次数，再次添加进延时通知队列
 * 
 * 在processingTradeRecord(rpOrderResultQueryVo.getBankOrderNo())中
 * 1.主动根据bankOrderNo去查询订单状态
 * 
 * </b>
 * @author  Peter
 * <a href="http://www.roncoo.com">龙果学院(www.roncoo.com)</a>
 */
public class App 
{
    private static final Log LOG = LogFactory.getLog(App.class);

    //通知任务队列，
    public static DelayQueue<PollingTask> tasks = new DelayQueue<PollingTask>();

    private static ClassPathXmlApplicationContext context;

    private static ThreadPoolTaskExecutor threadPool;

    public static PollingPersist pollingPersist;

    public static void main(String[] args) {
        try {
            context = new ClassPathXmlApplicationContext(new String[] { "spring/spring-context.xml" });
            context.start();
            threadPool = (ThreadPoolTaskExecutor) context.getBean("threadPool");
            pollingPersist = (PollingPersist) context.getBean("pollingPersist");

            startThread(); // 启动任务处理线程

            LOG.info("== context start");
        } catch (Exception e) {
            LOG.error("== application start error:", e);
            return;
        }
        synchronized (App.class) {
            while (true) {
                try {
                    App.class.wait();
                } catch (InterruptedException e) {
                    LOG.error("== synchronized error:", e);
                }
            }
        }
    }

    private static void startThread() {
        LOG.info("==>startThread");

        threadPool.execute(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(100);
                        LOG.info("==>threadPool.getActiveCount():" + threadPool.getActiveCount());
                        LOG.info("==>threadPool.getMaxPoolSize():" + threadPool.getMaxPoolSize());
                        // 如果当前活动线程等于最大线程，那么不执行
                        if (threadPool.getActiveCount() < threadPool.getMaxPoolSize()) {
                            LOG.info("==>tasks.size():" + tasks.size());
                            final PollingTask task = tasks.take(); //使用take方法获取过期任务,如果获取不到,就一直等待,知道获取到数据
                            if (task != null) {
                                threadPool.execute(new Runnable() {
                                    public void run() {
                                        tasks.remove(task);
                                        task.run(); // 执行通知处理
                                        LOG.info("==>tasks.size():" + tasks.size());
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("系统异常;", e);
                }
            }
        });
    }

}
