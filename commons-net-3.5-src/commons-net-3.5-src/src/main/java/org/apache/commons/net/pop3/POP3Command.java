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

package org.apache.commons.net.pop3;

/**
 * POP3Command stores POP3 command codes as a type-safe enum.
 *
 * @since 3.5 (converted from static int constants to enum)
 */
public enum POP3Command
{
    /** Send user name. */
    USER,
    /** Send password. */
    PASS,
    /** Quit session. */
    QUIT,
    /** Get status. */
    STAT,
    /** List message(s). */
    LIST,
    /** Retrieve message(s). */
    RETR,
    /** Delete message(s). */
    DELE,
    /** No operation — session keepalive. */
    NOOP,
    /** Reset session. */
    RSET,
    /** Authorization via APOP digest. */
    APOP,
    /** Retrieve top N lines from a message. */
    TOP,
    /** List unique message identifier(s). */
    UIDL,
    /** Query server capabilities. */
    CAPA,
    /** Authentication. */
    AUTH,
    ;

    /**
     * Get the POP3 protocol wire string for this command.
     *
     * @return The POP3 protocol command string (e.g. {@code "USER"}, {@code "RETR"}).
     */
    public final String getCommand()
    {
        return this.name();
    }
}
