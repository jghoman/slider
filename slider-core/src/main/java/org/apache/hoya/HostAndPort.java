/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hoya;

import java.util.Locale;

/**
 * Class that carries hostname and port
 */
public class HostAndPort {

  private final String hostname;
  private final int port;

  public HostAndPort(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;
  }
  
  public String getHostname() {
    return hostname;
  }

  public int getPort() {
    return port;
  }

  @Override
  public int hashCode() {
    int hc = hostname.hashCode();
    return hc * 31 + port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof HostAndPort)) return false;
    return this.compareTo((HostAndPort)o) == 0;
  }
  
  public int compareTo(HostAndPort other) {
    int compare = this.getHostname().toLowerCase(Locale.ENGLISH).
      compareTo(other.getHostname().toLowerCase(Locale.ENGLISH));
    if (compare != 0) return compare;
    compare = this.getPort() - other.getPort();
    return compare;
  }

  @Override
  public String toString() {
    return hostname + ":" + port;
  }
  
  /*
   * @param s String representation of HostAndPort
   * @return HostAndPort if s conforms to "host:port: format, otherwise null.
   */
  public static HostAndPort fromString(String s) {
    if (s == null || s.length() == 0) return null;
    int pos = s.indexOf(':');
    if (pos <= 0 || pos == s.length()-1) return null;
    int port = Integer.parseInt(s.substring(pos+1));
    return new HostAndPort(s.substring(0, pos), port);
  }
}
