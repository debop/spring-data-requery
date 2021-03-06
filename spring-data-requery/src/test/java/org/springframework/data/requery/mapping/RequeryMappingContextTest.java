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

package org.springframework.data.requery.mapping;

import io.requery.Key;
import io.requery.Version;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * org.springframework.data.requery.mapping.RequeryMappingContextTest
 *
 * @author debop
 * @since 18. 6. 8
 */
public class RequeryMappingContextTest {

    @Test
    public void requeryPersistentEntityRejectsSpringDataAtVersionAnnotation() {

        RequeryMappingContext context = new RequeryMappingContext();
        RequeryPersistentEntity entity = context.getRequiredPersistentEntity(Sample.class);

        assertThat(entity).isNotNull();

        assertThat(entity.getRequiredPersistentProperty("id").isIdProperty()).isTrue();
        assertThat(entity.getRequiredPersistentProperty("springId").isIdProperty()).isFalse();

        assertThat(entity.getRequiredPersistentProperty("version").isVersionProperty()).isTrue();
        assertThat(entity.getRequiredPersistentProperty("springVersion").isVersionProperty()).isFalse();
    }


    static class Sample {

        @Key
        long id;

        @org.springframework.data.annotation.Id
        long springId;

        @Version
        long version;

        @org.springframework.data.annotation.Version
        long springVersion;
    }
}
