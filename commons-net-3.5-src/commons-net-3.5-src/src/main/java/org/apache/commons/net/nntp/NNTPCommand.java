/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.nntp;

/**
 * NNTPCommand stores NNTP command codes as a type-safe enum, per RFC 977.
 *
 * @since 3.5 (converted from static int constants to enum)
 */
public enum NNTPCommand
{
    ARTICLE,
    BODY,
    GROUP,
    HEAD,
    HELP,
    IHAVE,
    LAST,
    LIST,
    NEWGROUPS,
    NEWNEWS,
    NEXT,
    POST,
    QUIT,
    SLAVE,
    STAT,
    AUTHINFO,
    XOVER,
    XHDR,
    ;

    /**
     * Retrieve the NNTP protocol wire string for this command.
     *
     * @return The NNTP protocol command string (e.g. {@code "ARTICLE"}, {@code "AUTHINFO"}).
     */
    public final String getCommand()
    {
        return this.name();
    }
}
