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

package org.apache.commons.net;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

/**
 * Provides shared default socket factories used by socket clients.
 * This class centralizes the lifecycle of the default factories and exposes
 * them through a single controlled access point.
 */
public final class SocketFactoryProvider
{
    private static final SocketFactoryProvider INSTANCE = new SocketFactoryProvider();

    private final SocketFactory socketFactory;
    private final ServerSocketFactory serverSocketFactory;
    private final DatagramSocketFactory datagramSocketFactory;

    private SocketFactoryProvider()
    {
        socketFactory = SocketFactory.getDefault();
        serverSocketFactory = ServerSocketFactory.getDefault();
        datagramSocketFactory = new DefaultDatagramSocketFactory();
    }

    public static SocketFactoryProvider getInstance()
    {
        return INSTANCE;
    }

    public SocketFactory getSocketFactory()
    {
        return socketFactory;
    }

    public ServerSocketFactory getServerSocketFactory()
    {
        return serverSocketFactory;
    }

    public DatagramSocketFactory getDatagramSocketFactory()
    {
        return datagramSocketFactory;
    }
}