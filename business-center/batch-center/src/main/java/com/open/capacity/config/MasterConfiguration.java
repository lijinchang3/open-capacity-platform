package com.open.capacity.config;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.open.capacity.dao.DeliverPostDao;
import com.open.capacity.support.BatchIncrementer;
import com.open.capacity.support.JobListener;

/**
 * @create Created by kl 2019年4月2日
 * Content :Master节点，派发分片任务到从节点
 */
@Configuration
public class MasterConfiguration {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;
    @Autowired
    DeliverPostDao deliverPostDao ;
 
    //作业
    @Bean
    public Job job(@Qualifier("masterStep") Step masterStep) {
    	
    	JobListener jobListener = new JobListener() ;
    	jobListener.setDeliverPostDao(deliverPostDao);
    	
        return jobBuilderFactory.get("endOfDayjob")
                .start(masterStep)
                .incrementer(new BatchIncrementer())
                .listener(jobListener)
                .build();
    }
    
    //作业步 ，job的一个执行环节
    @Bean("masterStep")
    public Step masterStep(@Qualifier("slaveStep") Step slaveStep,
                           PartitionHandler partitionHandler,
                           DataSource dataSource) {
        return stepBuilderFactory.get("masterStep")			
                .partitioner(slaveStep.getName(), new ColumnRangePartitioner(dataSource)) //设置分区
                .step(slaveStep)
                .partitionHandler(partitionHandler)
                .build();
    }
}
