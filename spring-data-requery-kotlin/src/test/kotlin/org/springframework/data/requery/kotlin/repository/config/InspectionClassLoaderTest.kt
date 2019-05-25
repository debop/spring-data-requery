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

package org.springframework.data.requery.kotlin.repository.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class InspectionClassLoaderTest {

    @Test
    fun `should load external class`() {

        val classLoader = InspectionClassLoader(this.javaClass.classLoader)

        val isolated = classLoader.loadClass("org.h2.Driver")
        val included = javaClass.classLoader.loadClass("org.h2.Driver")

        assertThat(isolated.classLoader)
            .isSameAs(classLoader)
            .isNotSameAs(javaClass.classLoader)

        assertThat(isolated).isNotEqualTo(included)

        assertEquals(classLoader, isolated.classLoader)
        assertNotEquals(javaClass.classLoader, isolated.classLoader)

        assertEquals(javaClass.classLoader, included.classLoader)
        assertNotEquals(included, isolated)
    }
}