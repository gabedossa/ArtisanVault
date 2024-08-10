package com.dossa.ArtisanVault.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JdbcConfig {
    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcTemplate JdbcTemplate(){
        return new JdbcTemplate(dataSource);
    }
}
