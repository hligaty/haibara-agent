/*
 * Copyright 2023 hligaty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hligaty.haibaraag.spi;

import java.util.Map;

/**
 * Provider for generating generic table field descriptions.
 * For example, every table has a primary key,
 * which is usually a Long type field called id,
 * which should be displayed as "${tableName}${descriptionSuffix}" for documents
 *
 * @author hligaty
 * @see DefaultCommonTableFieldDescriptionProvider
 */
public interface CommonTableFieldDescriptionProvider extends Order {

    /**
     * Key: common table field definition, value: descriptionSuffix.
     *
     * @return common table field definition collection.
     */
    Map<CommonTableFieldDefinition, String> get();

}
