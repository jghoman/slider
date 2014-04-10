/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.slider.server.services.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.slider.core.registry.ServiceInstanceData;

/**
 * This class creates a curator -but does not start or close it. That
 * is the responsbility of the owner
 */
public class CuratorHelper extends Configured {

  private final CuratorFramework curator;
  private final String connectionString;


  public CuratorHelper(Configuration conf, String connectionString) {
    super(conf);
    this.connectionString = connectionString;
    curator = createCurator(this.connectionString);
  }


  public CuratorFramework getCurator() {
    return curator;
  }

  public String getConnectionString() {
    return connectionString;
  }

  /**
   * Create a retry policy for this configuration
   * @param conf
   * @return
   */
  public RetryPolicy createRetryPolicy() {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    return retryPolicy;
  }
  
  private CuratorFramework createCurator(String connectionString) {
    CuratorFramework curator =
      CuratorFrameworkFactory.newClient(connectionString,
                                        createRetryPolicy());
    return curator;
  }

  /**
   * Create an (united) curator client service
   * @param connectionString ZK binding
   * @return the service
   */
  public CuratorService createCuratorClientService() {
    CuratorService curatorService =
      new CuratorService("Curator "  ,
                               curator);
    return curatorService;
  }

  public ServiceDiscoveryBuilder<ServiceInstanceData> createDiscoveryBuilder(String basePath) {
    ServiceDiscoveryBuilder<ServiceInstanceData> discoveryBuilder =
      ServiceDiscoveryBuilder.builder(ServiceInstanceData.class);
    discoveryBuilder.client(curator);
    discoveryBuilder.basePath(basePath);
    return discoveryBuilder;
  }

  /**
   * Create an instance
   * @param discoveryBuilder builder to create the discovery from
   */

  public RegistryBinderService<ServiceInstanceData> createRegistryBinderService(
    ServiceDiscoveryBuilder<ServiceInstanceData> discoveryBuilder) {
    return new RegistryBinderService<ServiceInstanceData>(curator, discoveryBuilder.build());
  }


 }
