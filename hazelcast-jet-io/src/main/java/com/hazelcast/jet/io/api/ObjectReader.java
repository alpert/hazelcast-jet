/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.io.api;

import com.hazelcast.nio.ObjectDataInput;

import java.io.IOException;

/**
 * Represents abstract object reader from binary structure to Java.
 *
 * @param <T> - type of the object;
 */
public interface ObjectReader<T> {
    /**
     * Read object from the binary source represented by objectDataInput;
     *
     * @param objectDataInput     - binary source;
     * @param objectReaderFactory - factory to construct object readers;
     * @return - Java representation of object
     * @throws IOException if error reading from input
     */
    T read(ObjectDataInput objectDataInput,
           ObjectReaderFactory objectReaderFactory) throws IOException;
}