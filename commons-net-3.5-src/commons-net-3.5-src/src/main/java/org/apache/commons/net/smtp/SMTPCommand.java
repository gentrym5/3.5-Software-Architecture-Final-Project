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

package org.apache.commons.net.smtp;

/**
 * SMTPCommand stores SMTP command codes as a type-safe enum.
 * Each constant carries the exact protocol wire string used in RFC 821 / RFC 5321.
 *
 * @since 3.5 (converted from static int constants to enum)
 */
public enum SMTPCommand
{
    HELO,
    MAIL("MAIL FROM:"),
    RCPT("RCPT TO:"),
    DATA,
    SEND("SEND FROM:"),
    SOML("SOML FROM:"),
    SAML("SAML FROM:"),
    RSET,
    VRFY,
    EXPN,
    HELP,
    NOOP,
    TURN,
    QUIT,
    AUTH,
    EHLO,
    ;

    // Aliases
    public static final SMTPCommand HELLO              = HELO;
    public static final SMTPCommand LOGIN              = HELO;
    public static final SMTPCommand MAIL_FROM          = MAIL;
    public static final SMTPCommand RECIPIENT          = RCPT;
    public static final SMTPCommand SEND_MESSAGE_DATA  = DATA;
    public static final SMTPCommand SEND_FROM          = SEND;
    public static final SMTPCommand SEND_OR_MAIL_FROM  = SOML;
    public static final SMTPCommand SEND_AND_MAIL_FROM = SAML;
    public static final SMTPCommand RESET              = RSET;
    public static final SMTPCommand VERIFY             = VRFY;
    public static final SMTPCommand EXPAND             = EXPN;
    public static final SMTPCommand LOGOUT             = QUIT;

    private final String command;

    SMTPCommand() {
        this.command = this.name();
    }

    SMTPCommand(String command) {
        this.command = command;
    }

    /**
     * Retrieve the SMTP protocol wire string for this command.
     *
     * @return The SMTP protocol command string (e.g. {@code "HELO"}, {@code "MAIL FROM:"}).
     */
    public final String getCommand()
    {
        return command;
    }
}
