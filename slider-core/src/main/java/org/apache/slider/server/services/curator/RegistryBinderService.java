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

import com.google.common.base.Preconditions;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.ServiceType;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.slider.server.services.ClosingService;

import java.io.IOException;
import java.net.URL;

/**
 * YARN service for Curator service discovery; the discovery instance's
 * start/close methods are tied to the lifecycle of this service
 * @param <Payload> the payload of the operation
 */
public class RegistryBinderService<Payload> extends ClosingService {

  private final ServiceDiscovery<Payload> discovery;

  /**
   * Create an instance
   * @param discovery discovery instance -not yet started
   */
  public RegistryBinderService(ServiceDiscovery<Payload> discovery) {
    super("RegistryBinderService", discovery);

    this.discovery =
      Preconditions.checkNotNull(discovery, "null discovery arg");
  }


  @Override
  protected void serviceStart() throws Exception {
    super.serviceStart();
    discovery.start();
  }

  /**
   * register an instance -only 
   * @param id
   * @param name
   * @param url
   * @param payload
   * @return the instance
   * @throws Exception
   */
  public ServiceInstance<Payload> register(String id,
                                           String name,
                                           URL url,
                                           Payload payload) throws Exception {
    Preconditions.checkState(isInState(STATE.STARTED), "Not started: " + this);
    int port = url.getPort();
    if (port == 0) {
      throw new IOException("Port undefined in " + url);
    }
    UriSpec uriSpec = new UriSpec(url.toString());
    ServiceInstance<Payload> instance = builder()
      .name(name)
      .id(id)
      .payload(
        payload)
      .port(port)
      .serviceType(
        ServiceType.DYNAMIC)
      .uriSpec(
        uriSpec)
      .build();
    discovery.registerService(instance);
    return instance;

  }

  /**
   * Create a builder. This is already pre-prepared with address, registration
   * time and a (random) UUID
   * @return a builder
   * @throws IOException IO problems, including enumerating network ports
   */
  public ServiceInstanceBuilder<Payload> builder() throws
                                                   IOException {
    try {
      return ServiceInstance.builder();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException(e);
    }
  }
}
