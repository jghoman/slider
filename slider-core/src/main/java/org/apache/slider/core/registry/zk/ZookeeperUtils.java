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

package org.apache.slider.core.registry.zk;

import com.google.common.net.HostAndPort;
import org.apache.hadoop.util.StringUtils;
import org.apache.hoya.api.OptionKeys;
import org.apache.hoya.exceptions.BadConfigException;
import org.apache.hoya.tools.HoyaUtils;

import java.util.ArrayList;
import java.util.List;

public class ZookeeperUtils {

  public static String buildConnectionString(String zkHosts, int port) {
    String zkPort = Integer.toString(port);
    //parse the hosts
    String[] hostlist = zkHosts.split(",", 0);
    String quorum = HoyaUtils.join(hostlist, ":" + zkPort + ",");
    //this quorum has a trailing comma
    quorum = quorum.substring(0, quorum.length() - 1);
    return quorum;
  }

  /**
   * Take a quorum list and split it to (trimmed) pairs
   * @param hostPortQuorumList list of form h1:port, h2:port2,...
   * @return a possibly empty list of values between commas. They may not be
   * valid hostname:port pairs
   */
  public static List<String> splitToPairs(String hostPortQuorumList) {
    // split an address hot
    String[] strings = StringUtils.getStrings(hostPortQuorumList);
    List<String> tuples = new ArrayList<String>(strings.length);
    for (String s : strings) {
      tuples.add(s.trim());
    }
    return tuples;
  }

  /**
   * Split a quorum list into a list of hostnames and ports
   * @param hostPortQuorumList split to a list of hosts and ports
   * @return a list of values
   */
  public static List<HostAndPort> splitToHostsAndPorts(String hostPortQuorumList) {
    // split an address hot
    String[] strings = StringUtils.getStrings(hostPortQuorumList);
    List<HostAndPort> list = new ArrayList<HostAndPort>(strings.length);
    for (String s : strings) {
      list.add(HostAndPort.fromString(s.trim()));
    }
    return list;
  }

  /**
   * Build up to a hosts only list
   * @param hostAndPorts
   * @return a list of the hosts only
   */
  public static String buildHostsOnlyList(List<HostAndPort> hostAndPorts) {
    StringBuilder sb = new StringBuilder();
    for (HostAndPort hostAndPort : hostAndPorts) {
      sb.append(hostAndPort.getHostText()).append(",");
    }
    if (sb.length() > 0) {
      sb.delete(sb.length() - 1, sb.length());
    }
    return sb.toString();
  }

  public static String buildQuorumEntry(HostAndPort hostAndPort,
    int defaultPort) {
    String s = hostAndPort.toString();
    if (hostAndPort.hasPort()) {
      return s;
    } else {
      return s + ":" + defaultPort;
    }
  }

  /**
   * Build a quorum list, injecting a ":defaultPort" ref if needed on
   * any entry without one
   * @param hostAndPorts
   * @param defaultPort
   * @return
   */
  public static String buildQuorum(List<HostAndPort> hostAndPorts, int defaultPort) {
    List<String> entries = new ArrayList<String>(hostAndPorts.size());
    for (HostAndPort hostAndPort : hostAndPorts) {
      entries.add(buildQuorumEntry(hostAndPort, defaultPort));
    }
    return HoyaUtils.join(entries, ",", false);
  }
  
  public static String convertToHostsOnlyList(String quorum) throws
      BadConfigException {
    List<HostAndPort> hostAndPorts = splitToHostsAndPortsStrictly(quorum);
    return ZookeeperUtils.buildHostsOnlyList(hostAndPorts);
  }

  public static List<HostAndPort> splitToHostsAndPortsStrictly(String quorum) throws
      BadConfigException {
    List<HostAndPort> hostAndPorts =
        ZookeeperUtils.splitToHostsAndPorts(quorum);
    if (hostAndPorts.isEmpty()) {
      throw new BadConfigException("empty zookeeper quorum");
    }
    return hostAndPorts;
  }
  
  public static int getFirstPort(String quorum, int defVal) throws
      BadConfigException {
    List<HostAndPort> hostAndPorts = splitToHostsAndPortsStrictly(quorum);
    int port = hostAndPorts.get(0).getPortOrDefault(defVal);
    return port;

  }
}
