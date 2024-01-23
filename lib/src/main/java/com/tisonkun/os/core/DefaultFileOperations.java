/*
 * Copyright 2024 tison <wander4096@gmail.com>
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

package com.tisonkun.os.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Default implementation for {@link FileOperationProvider} based on {@link FileInputStream}.
 */
public class DefaultFileOperations implements FileOperationProvider {
    @Override
    public InputStream readFile(String filePath) throws IOException {
        return new FileInputStream(filePath);
    }
}
