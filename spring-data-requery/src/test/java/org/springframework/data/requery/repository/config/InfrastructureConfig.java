/*
 * Copyright 2018 Coupang Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.requery.repository.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.requery.meta.EntityModel;
import io.requery.sql.TableCreationMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.requery.configs.AbstractRequeryConfiguration;
import org.springframework.data.requery.domain.Models;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * InfrastructureConfig
 *
 * @author debop
 * @since 18. 6. 12
 */
@Slf4j
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class InfrastructureConfig extends AbstractRequeryConfiguration {

    @Override
    public EntityModel getEntityModel() {
        return Models.DEFAULT;
    }

    @Override
    public TableCreationMode getTableCreationMode() {
        return TableCreationMode.CREATE_NOT_EXISTS;
    }

    @Bean
    public DataSource dataSource() {

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:requery;DB_CLOSE_DELAY=-1;MODE=MySQL;");
        config.setAutoCommit(false);
        config.setUsername("sa");

        DataSource dataSource = new HikariDataSource(config);
        log.trace("DataSource={}", dataSource);
        return dataSource;

//        return new EmbeddedDatabaseBuilder()
//            .setName("data")
//            .setType(EmbeddedDatabaseType.H2)
//            .setScriptEncoding("UTF-8")
//            .ignoreFailedDrops(true)
//            .generateUniqueName(true)
//            .continueOnError(true)
//            .build();
    }
}
