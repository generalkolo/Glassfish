/*
 * Copyright  2000,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.tools.ant.util;

/**
 * Implementation of FileNameMapper that always returns the source
 * file name without any leading directory information.
 *
 * <p>This is the default FileNameMapper for the copy and move
 * tasks if the flatten attribute has been set.</p>
 *
 */
public class FlatFileNameMapper implements FileNameMapper {

    /**
     * Ignored.
     */
    public void setFrom(String from) {
    }

    /**
     * Ignored.
     */
    public void setTo(String to) {
    }

    /**
     * Returns an one-element array containing the source file name
     * without any leading directory information.
     */
    public String[] mapFileName(String sourceFileName) {
        return new String[] {new java.io.File(sourceFileName).getName()};
    }
}
