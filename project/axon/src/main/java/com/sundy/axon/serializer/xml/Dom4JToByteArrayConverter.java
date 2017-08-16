/*
 * Copyright (c) 2010-2014. Axon Framework
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

package com.sundy.axon.serializer.xml;

import org.dom4j.Document;

import com.sundy.axon.common.io.IOUtils;
import com.sundy.axon.serializer.AbstractContentTypeConverter;

/**
 * Converter that converts Dom4j Document instances to a byte array. The Document is written as XML string, and
 * converted to bytes using the UTF-8 character set.
 *
 * @author Allard Buijze
 * @since 2.0
 */
public class Dom4JToByteArrayConverter extends AbstractContentTypeConverter<Document, byte[]> {

    @Override
    public Class<Document> expectedSourceType() {
        return Document.class;
    }

    @Override
    public Class<byte[]> targetType() {
        return byte[].class;
    }

    @Override
    public byte[] convert(Document original) {
        return original.asXML().getBytes(IOUtils.UTF8);
    }
}
