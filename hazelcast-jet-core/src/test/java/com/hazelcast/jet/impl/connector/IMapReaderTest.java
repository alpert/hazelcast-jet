/*
 * Copyright (c) 2008-2017, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.jet.impl.connector;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.DAG;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.JetTestInstanceFactory;
import com.hazelcast.jet.Vertex;
import com.hazelcast.nio.Address;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hazelcast.jet.Edge.between;
import static org.junit.Assert.assertEquals;

@Category(QuickTest.class)
@RunWith(HazelcastParallelClassRunner.class)
public class IMapReaderTest extends HazelcastTestSupport {

    private JetTestInstanceFactory factory;

    @Before
    public void setup() {
        factory = new JetTestInstanceFactory();
    }

    @After
    public void tearDown() throws Exception {
        Hazelcast.shutdownAll();
        factory.shutdownAll();
    }

    @Test
    public void when_readerConfiguredWithClientConfig_then_readFromRemoteCluster() throws Exception {
        JetInstance c1 = factory.newMember();
        factory.newMember();

        HazelcastInstance c2 = Hazelcast.newHazelcastInstance();
        Hazelcast.newHazelcastInstance();

        int messageCount = 20;
        Map<Integer, Integer> map = IntStream.range(0, messageCount).boxed().collect(Collectors.toMap(m -> m, m -> m));
        c2.getMap("producer").putAll(map);

        DAG dag = new DAG();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName("dev");
        clientConfig.getGroupConfig().setPassword("dev-pass");
        Address address = c2.getCluster().getLocalMember().getAddress();
        clientConfig.getNetworkConfig().addAddress(address.getHost() + ":" + address.getPort());

        Vertex producer = dag.newVertex("producer", IMapReader.supplier("producer", clientConfig)).localParallelism(4);
        Vertex consumer = dag.newVertex("consumer", IListWriter.supplier("consumer")).localParallelism(1);

        dag.edge(between(producer, consumer));

        Future<Void> execute = c1.newJob(dag).execute();
        assertCompletesEventually(execute);
        assertEquals(messageCount, c1.getList("consumer").size());
    }


}