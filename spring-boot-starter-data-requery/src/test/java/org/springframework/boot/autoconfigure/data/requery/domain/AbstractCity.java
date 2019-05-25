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

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import lombok.Getter;
import org.springframework.data.requery.domain.ToStringBuilder;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Abstract class of {@link City} entity
 *
 * @author debop
 */
@Getter
@Entity
public class AbstractCity extends AbstractLifecycleEntity {

    private static final long serialVersionUID = 6441380830729259194L;

    @Key
    @Generated
    protected Long id;

    @Column(nullable = false)
    protected String name;

    protected String state;

    protected String country;

    protected String map;

    protected AbstractCity() {}

    protected AbstractCity(String name, String country) {
        this.name = name;
        this.country = country;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, state, country);
    }

    @Nonnull
    @Override
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("name", name)
            .add("state", state)
            .add("country", country);
    }
}
