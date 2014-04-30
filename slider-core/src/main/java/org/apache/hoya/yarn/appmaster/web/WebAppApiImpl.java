/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hoya.yarn.appmaster.web;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.hoya.SliderKeys;
import org.apache.hoya.api.HoyaClusterProtocol;
import org.apache.hoya.providers.ProviderRole;
import org.apache.hoya.providers.ProviderService;
import org.apache.hoya.yarn.appmaster.state.RoleStatus;
import org.apache.hoya.yarn.appmaster.state.StateAccessForProviders;
import org.apache.hoya.yarn.appmaster.web.rest.agent.AgentRestOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class WebAppApiImpl implements WebAppApi {
  private static final Logger log = LoggerFactory.getLogger(WebAppApiImpl.class);

  protected static final ProviderRole HOYA_AM_ROLE = new ProviderRole("Slider Application Master", SliderKeys.ROLE_HOYA_AM_PRIORITY_INDEX);

  protected final HoyaClusterProtocol clusterProto;
  protected final StateAccessForProviders appState;
  protected final ProviderService provider;
  
  public WebAppApiImpl(HoyaClusterProtocol clusterProto,
                       StateAccessForProviders appState, ProviderService provider) {
    checkNotNull(clusterProto);
    checkNotNull(appState);
    checkNotNull(provider);
    
    this.clusterProto = clusterProto;
    this.appState = appState;
    this.provider = provider;
  }

  /* (non-Javadoc)
   * @see org.apache.hoya.yarn.appmaster.web.WebAppApi#getAppState()
   */
  @Override
  public StateAccessForProviders getAppState() {
    return appState;
  }

  /* (non-Javadoc)
   * @see org.apache.hoya.yarn.appmaster.web.WebAppApi#getProviderService()
   */
  @Override
  public ProviderService getProviderService() {
    return provider;
  }

  /* (non-Javadoc)
   * @see org.apache.hoya.yarn.appmaster.web.WebAppApi#getClusterProtocol()
   */
  @Override
  public HoyaClusterProtocol getClusterProtocol() {
    return clusterProto;
  }
  
  /* (non-Javadoc)
   * @see org.apache.hoya.yarn.appmaster.web.WebAppApi#getRoleStatusByName()
   */
  @Override
  public TreeMap<String,RoleStatus> getRoleStatusByName() {
    Map<Integer,ProviderRole> rolesById = rolesById(provider.getRoles());
    Map<Integer,RoleStatus> status = appState.getRoleStatusMap();
    
    return getRoleStatusesByName(rolesById, status);
  }
  
  /**
   * Get the ProviderRoles by their index
   * @param roles
   * @return
   */
  private Map<Integer,ProviderRole> rolesById(List<ProviderRole> roles) {
    Map<Integer,ProviderRole> rolesById = new HashMap<Integer,ProviderRole>();
    rolesById.put(SliderKeys.ROLE_HOYA_AM_PRIORITY_INDEX, HOYA_AM_ROLE);

    for (ProviderRole role : roles) {
      rolesById.put(role.id, role);
    }

    return rolesById;
  }

  /**
   * Join the ProviderRole by their ID with the RoleStatus by their ID, to get the RoleStatus by role name.
   * @param rolesById
   * @param statusById
   * @return A Map of RoleStatus by the role name
   */
  private TreeMap<String,RoleStatus> getRoleStatusesByName(Map<Integer,ProviderRole> rolesById, Map<Integer,RoleStatus> statusById) {
    TreeMap<String,RoleStatus> statusByName = new TreeMap<String,RoleStatus>();
    for (Entry<Integer,ProviderRole> role : rolesById.entrySet()) {
      final RoleStatus status = statusById.get(role.getKey());

      if (null == status) {
        log.error("Found ID ({}) which has no known ProviderRole", role.getKey());
      } else {
        statusByName.put(role.getValue().name, status);
      }
    }

    return statusByName;
  }

  @Override
  public AgentRestOperations getAgentRestOperations() {
    return provider.getAgentRestOperations();
  }
}
