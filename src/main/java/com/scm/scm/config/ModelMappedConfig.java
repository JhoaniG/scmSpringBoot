package com.scm.scm.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMappedConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
