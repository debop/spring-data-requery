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

package org.springframework.boot.autoconfigure.data.requery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.requery.domain.City;
import org.springframework.boot.autoconfigure.data.requery.domain.CityRepository;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RequeryApplication
 *
 * @author debop
 * @since 18. 11. 1
 */
@Slf4j
@SpringBootApplication
@EnableRequeryRepositories(basePackageClasses = { CityRepository.class })
public class RequeryApplication {

    @Autowired
    CityRepository repository;

    @PostConstruct
    @Transactional
    public void transactionTest() {

        try {
            CityService service = new CityService(repository);
            service.saveCities(10);
        } catch (Exception e) {
            log.error("Fail to save cities");
        }

        assertThat(repository.count()).isEqualTo(0);
    }

    static class CityService {

        private final CityRepository repository;

        public CityService(@Nonnull final CityRepository repository) {
            this.repository = repository;
        }

        @Transactional
        public void saveCities(int count) {
            for (int i = 0; i < count; i++) {
                City city = new City();
                city.setName("Seoul" + i);
                city.setCountry("Korea" + i);

                repository.save(city);

                if (i == 5) {
                    repository.save(new City());
                }
            }
        }
    }


    public static void main(String[] args) {
        SpringApplication.run(RequeryApplication.class, args);
    }
}
