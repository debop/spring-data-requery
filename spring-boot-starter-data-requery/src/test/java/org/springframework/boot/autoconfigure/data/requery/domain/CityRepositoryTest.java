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

package org.springframework.boot.autoconfigure.data.requery.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.requery.configs.TestRequeryConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.requery.repository.config.EnableRequeryRepositories;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CityRepositoryTest
 *
 * @author debop (Sunghyouk Bae)
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestRequeryConfiguration.class })
@EnableRequeryRepositories(basePackageClasses = { CityRepository.class })
@EnableTransactionManagement
@Transactional
public class CityRepositoryTest {

    @Autowired
    private CityRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    public void contextLoading() {
        assertThat(repository).isNotNull();
    }

    @Test
    public void testFindByIdAndDeletedFalse() {
        City city = new City("Seoul", "Korea");
        repository.save(city);

        assertThat(city.getId()).isNotNull();

        City active = repository.findByIdAndDeletedFalse(city.getId());
        assertThat(active).isNotNull();

        city.setDeleted(true);
        repository.save(city);

        City deleted = repository.findByIdAndDeletedFalse(city.getId());
        assertThat(deleted).isNull();
    }

    @Test
    public void testFindFirstByName() {

        City city = new City("Seoul", "Korea");
        repository.save(city);

        assertThat(city.getId()).isNotNull();

        City seoul = repository.findFirstByName("Seoul");
        assertThat(seoul).isNotNull();
        assertThat(seoul.getCountry()).isEqualTo(city.getCountry());
    }

    @Test(expected = Exception.class)
    @Transactional
    public void testTransaction() throws Exception {
        repository.deleteAll();

//        try {
        CityService service = new CityService(repository);
        service.saveCities(10);
//        } catch (Exception e) {
//            log.error("Fail to save cities", e);
//            if(repository.transaction().active()) {
//                repository.transaction().rollback();
//                repository.transaction().close();
//            }
//        }

        // assertThat(repository.count()).isEqualTo(0);
    }

    static class CityService {

        private final CityRepository repository;

        public CityService(@Nonnull final CityRepository repository) {
            this.repository = repository;
        }

        @Transactional(propagation = Propagation.REQUIRED)
        public void saveCities(int count) {
            for (int i = 0; i < count; i++) {
                City city = new City("Seoul " + i, "Korea " + i);

                repository.save(city);

                if (i == 5) {
                    repository.save(new City(null, null));
                }
            }
        }
    }
}
