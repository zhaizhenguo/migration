package com.oscar.migration.config;

import org.dozer.DozerBeanMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author zzg
 * @description: JavaBean映射工具配置
 * @date 2021/1/19 14:17
 */
@Configuration
public class DozerConfig {

    @Bean
    public DozerBeanMapper mapper() {
        return new DozerBeanMapper();
    }
}
