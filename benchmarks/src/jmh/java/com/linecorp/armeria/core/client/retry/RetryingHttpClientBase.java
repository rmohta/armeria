/*
 * Copyright 2018 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.core.client.retry;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import com.linecorp.armeria.client.HttpClient;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerPort;

@State(Scope.Benchmark)
public abstract class RetryingHttpClientBase {

    private Server server;
    private HttpClient client;

    @Setup
    public void start() {
        server = Server.builder()
                       .http(0)
                       .service("/empty", (ctx, req) -> HttpResponse.of("\"\""))
                       .build();
        server.start().join();
        client = newClient();
    }

    @TearDown
    public void stop() {
        server.stop().join();
    }

    protected abstract HttpClient newClient();

    protected String baseUrl() {
        final ServerPort httpPort = server.activePorts().values().stream()
                                          .filter(ServerPort::hasHttp).findAny()
                                          .get();
        return "h2c://127.0.0.1:" + httpPort.localAddress().getPort();
    }

    @Benchmark
    public void empty() {
        client.get("/empty").aggregate().join();
    }
}
