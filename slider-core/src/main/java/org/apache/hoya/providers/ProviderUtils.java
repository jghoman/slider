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

package org.apache.hoya.providers;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hoya.SliderKeys;
import org.apache.hoya.api.ClusterDescription;
import org.apache.hoya.api.OptionKeys;
import org.apache.hoya.api.ResourceKeys;
import org.apache.hoya.api.RoleKeys;
import org.apache.hoya.core.conf.AggregateConf;
import org.apache.hoya.core.conf.ConfTreeOperations;
import org.apache.hoya.core.conf.MapOperations;
import org.apache.hoya.exceptions.BadCommandArgumentsException;
import org.apache.hoya.exceptions.BadConfigException;
import org.apache.hoya.exceptions.SliderException;
import org.apache.hoya.exceptions.SliderInternalStateException;
import org.apache.hoya.tools.HoyaFileSystem;
import org.apache.hoya.tools.SliderUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * this is a factoring out of methods handy for providers. It's bonded to a log at
 * construction time
 */
public class ProviderUtils implements RoleKeys {

  protected final Logger log;

  /**
   * Create an instace
   * @param log log directory to use -usually the provider
   */
  
  public ProviderUtils(Logger log) {
    this.log = log;
  }

  /**
   * Add oneself to the classpath. This does not work
   * on minicluster test runs where the JAR is not built up
   * @param providerResources map of provider resources to add these entries to
   * @param provider provider to add
   * @param jarName name of the jar to use
   * @param hoyaFileSystem target filesystem
   * @param tempPath path in the cluster FS for temp files
   * @param libdir relative directory to place resources
   * @param miniClusterTestRun
   * @return true if the class was found in a JAR
   * 
   * @throws FileNotFoundException if the JAR was not found and this is NOT
   * a mini cluster test run
   * @throws IOException IO problems
   * @throws SliderException any Slider problem
   */
  public static boolean addProviderJar(Map<String, LocalResource> providerResources,
      Object provider,
      String jarName,
      HoyaFileSystem hoyaFileSystem,
      Path tempPath,
      String libdir,
      boolean miniClusterTestRun) throws
      IOException,
      SliderException {
    try {
      SliderUtils.putJar(providerResources,
          hoyaFileSystem,
          provider.getClass(),
          tempPath,
          libdir,
          jarName);
      return true;
    } catch (FileNotFoundException e) {
      if (miniClusterTestRun) {
        return false;
      } else {
        throw e;
      }
    }
  }
  
  /**
   * Add a set of dependencies to the provider resources being built up,
   * by copying them from the local classpath to the remote one, then
   * registering them
   * @param providerResources map of provider resources to add these entries to
   * @param hoyaFileSystem target filesystem
   * @param tempPath path in the cluster FS for temp files
   * @param libdir relative directory to place resources
   * @param resources list of resource names (e.g. "hbase.jar"
   * @param classes list of classes where classes[i] refers to a class in
   * resources[i]
   * @throws IOException IO problems
   * @throws SliderException any Slider problem
   */
  public static void addDependencyJars(Map<String, LocalResource> providerResources,
                                       HoyaFileSystem hoyaFileSystem,
                                       Path tempPath,
                                       String libdir,
                                       String[] resources,
                                       Class[] classes
                                      ) throws
                                        IOException,
      SliderException {
    if (resources.length != classes.length) {
      throw new SliderInternalStateException(
        "mismatch in Jar names [%d] and classes [%d]",
        resources.length,
        classes.length);
    }
    int size = resources.length;
    for (int i = 0; i < size; i++) {
      String jarName = resources[i];
      Class clazz = classes[i];
      SliderUtils.putJar(providerResources,
          hoyaFileSystem,
          clazz,
          tempPath,
          libdir,
          jarName);
    }
    
  }

  /**
   * build the log directory
   * @return the log dir
   */
  public String getLogdir() throws IOException {
    String logdir = System.getenv("LOGDIR");
    if (logdir == null) {
      logdir =
        SliderKeys.TMP_LOGDIR_PREFIX + UserGroupInformation.getCurrentUser().getShortUserName();
    }
    return logdir;
  }


  public void validateNodeCount(AggregateConf instanceDescription,
                                String name, int min, int max) throws
                                                               BadCommandArgumentsException {
    MapOperations component =
      instanceDescription.getResourceOperations().getComponent(name);
    int count;
    if (component == null) {
      count = 0;
    } else {
      count = component.getOptionInt(ResourceKeys.COMPONENT_INSTANCES, 0);
    }
    validateNodeCount(name, count, min, max);
  }
  
  /**
   * Validate the node count and heap size values of a node class 
   *
   * @param name node class name
   * @param count requested node count
   * @param min requested heap size
   * @param max
   * @throws BadCommandArgumentsException if the values are out of range
   */
  public void validateNodeCount(String name,
                                int count,
                                int min,
                                int max) throws BadCommandArgumentsException {
    if (count < min) {
      throw new BadCommandArgumentsException(
        "requested no of %s nodes: %d is below the minimum of %d", name, count,
        min);
    }
    if (max > 0 && count > max) {
      throw new BadCommandArgumentsException(
        "requested no of %s nodes: %d is above the maximum of %d", name, count,
        max);
    }
  }

  /**
   * copy all options beginning site. into the site.xml
   * @param clusterSpec cluster specification
   * @param sitexml map for XML file to build up
   */
  public void propagateSiteOptions(ClusterDescription clusterSpec,
                                    Map<String, String> sitexml) {
    Map<String, String> options = clusterSpec.options;
    propagateSiteOptions(options, sitexml);
  }

  public void propagateSiteOptions(Map<String, String> options,
                                   Map<String, String> sitexml) {
    propagateSiteOptions(options, sitexml, "");
  }

  public void propagateSiteOptions(Map<String, String> options,
                                   Map<String, String> sitexml,
                                   String configName) {
    propagateSiteOptions(options, sitexml, configName, null);
  }

  public void propagateSiteOptions(Map<String, String> options,
                                   Map<String, String> sitexml,
                                   String configName,
                                   Map<String,String> tokenMap) {
    String prefix = OptionKeys.SITE_XML_PREFIX +
                    (configName.length() > 0 ? configName + "." : "");
    for (Map.Entry<String, String> entry : options.entrySet()) {
      String key = entry.getKey();
      if (key.startsWith(prefix)) {
        String envName = key.substring(prefix.length());
        if (!envName.isEmpty()) {
          String value = entry.getValue();
          if (tokenMap != null) {
            for (Map.Entry<String,String> token : tokenMap.entrySet()) {
              value = value.replaceAll(Pattern.quote(token.getKey()),
                                       token.getValue());
            }
          }
          sitexml.put(envName, value);
        }
      }
    }
  }

  /**
   * Propagate an option from the cluster specification option map
   * to the site XML map, using the site key for the name
   * @param clusterSpec cluster specification
   * @param optionKey key in the option map
   * @param sitexml  map for XML file to build up
   * @param siteKey key to assign the value to in the site XML
   * @throws BadConfigException if the option is missing from the cluster spec
   */
  public void propagateOption(MapOperations global,
                              String optionKey,
                              Map<String, String> sitexml,
                              String siteKey) throws BadConfigException {
    sitexml.put(siteKey, global.getMandatoryOption(optionKey));
  }


  /**
   * Build the image dir. This path is relative and only valid at the far end
   * @param clusterSpec cluster spec
   * @param bindir bin subdir
   * @param script script in bin subdir
   * @return the path to the script
   * @throws FileNotFoundException if a file is not found, or it is not a directory* 
   */
  public String buildPathToHomeDir(AggregateConf instanceDefinition
                                   ,
                                  String bindir,
                                  String script) throws
                                                 FileNotFoundException,
                                                 BadConfigException {
    MapOperations globalOptions =
      instanceDefinition.getInternalOperations().getGlobalOptions();
    String applicationHome =
      globalOptions.get(OptionKeys.INTERNAL_APPLICATION_HOME);
    String imagePath =
      globalOptions.get(OptionKeys.INTERNAL_APPLICATION_IMAGE_PATH);
    return buildPathToHomeDir(imagePath, applicationHome, bindir, script);
  }

  public String buildPathToHomeDir(String imagePath,
                                   String applicationHome,
                                   String bindir, String script) throws
                                                                 FileNotFoundException {
    String path;
    File scriptFile;
    if (imagePath!=null) {
      File tarball = new File(SliderKeys.LOCAL_TARBALL_INSTALL_SUBDIR);
      scriptFile = findBinScriptInExpandedArchive(tarball, bindir, script);
      // now work back from the script to build the relative path
      // to the binary which will be valid remote or local
      StringBuilder builder = new StringBuilder();
      builder.append(SliderKeys.LOCAL_TARBALL_INSTALL_SUBDIR);
      builder.append("/");
      //for the script, we want the name of ../..
      File archive = scriptFile.getParentFile().getParentFile();
      builder.append(archive.getName());
      path = builder.toString();

    } else {
      // using a home directory which is required to be present on 
      // the local system -so will be absolute and resolvable
      File homedir = new File(applicationHome);
      path = homedir.getAbsolutePath();

      //this is absolute, resolve its entire path
      SliderUtils.verifyIsDir(homedir, log);
      File bin = new File(homedir, bindir);
      SliderUtils.verifyIsDir(bin, log);
      scriptFile = new File(bin, script);
      SliderUtils.verifyFileExists(scriptFile, log);
    }
    return path;
  }

  
  /**
   * Build the image dir. This path is relative and only valid at the far end
   * @param internal internal options
   * @param bindir bin subdir
   * @param script script in bin subdir
   * @return the path to the script
   * @throws FileNotFoundException if a file is not found, or it is not a directory* 
   */
  public String buildPathToScript(AggregateConf instance,
                                String bindir,
                                String script) throws FileNotFoundException {
    return buildPathToScript(instance.getInternalOperations(), bindir, script);
  }
  /**
   * Build the image dir. This path is relative and only valid at the far end
   * @param internal internal options
   * @param bindir bin subdir
   * @param script script in bin subdir
   * @return the path to the script
   * @throws FileNotFoundException if a file is not found, or it is not a directory* 
   */
  public String buildPathToScript(ConfTreeOperations internal,
                                String bindir,
                                String script) throws FileNotFoundException {
    
    String homedir = buildPathToHomeDir(
      internal.get(OptionKeys.INTERNAL_APPLICATION_IMAGE_PATH),
      internal.get(OptionKeys.INTERNAL_APPLICATION_HOME),
      bindir,
      script);
    return buildScriptPath(bindir, script, homedir);
  }
  
  

  public String buildScriptPath(String bindir, String script, String homedir) {
    StringBuilder builder = new StringBuilder(homedir);
    builder.append("/");
    builder.append(bindir);
    builder.append("/");
    builder.append(script);
    return builder.toString();
  }


  public static String convertToAppRelativePath(File file) {
    return convertToAppRelativePath(file.getPath());
  }

  public static String convertToAppRelativePath(String path) {
    return ApplicationConstants.Environment.PWD.$() + "/" + path;
  }


  public static void validatePathReferencesLocalDir(String meaning, String path)
      throws BadConfigException {
    File file = new File(path);
    if (!file.exists()) {
      throw new BadConfigException("%s directory %s not found", meaning, file);
    }
    if (!file.isDirectory()) {
      throw new BadConfigException("%s is not a directory: %s", meaning, file);
    }
  }

  /**
   * get the user name
   * @return the user name
   */
  public String getUserName() throws IOException {
    return UserGroupInformation.getCurrentUser().getShortUserName();
  }

  /**
   * Find a script in an expanded archive
   * @param base base directory
   * @param bindir bin subdir
   * @param script script in bin subdir
   * @return the path to the script
   * @throws FileNotFoundException if a file is not found, or it is not a directory
   */
  public File findBinScriptInExpandedArchive(File base,
                                             String bindir,
                                             String script)
      throws FileNotFoundException {
    
    SliderUtils.verifyIsDir(base, log);
    File[] ls = base.listFiles();
    if (ls == null) {
      //here for the IDE to be happy, as the previous check will pick this case
      throw new FileNotFoundException("Failed to list directory " + base);
    }

    log.debug("Found {} entries in {}", ls.length, base);
    List<File> directories = new LinkedList<File>();
    StringBuilder dirs = new StringBuilder();
    for (File file : ls) {
      log.debug("{}", false);
      if (file.isDirectory()) {
        directories.add(file);
        dirs.append(file.getPath()).append(" ");
      }
    }
    if (directories.size() > 1) {
      throw new FileNotFoundException(
        "Too many directories in archive to identify binary: " + dirs);
    }
    if (directories.isEmpty()) {
      throw new FileNotFoundException(
        "No directory found in archive " + base);
    }
    File archive = directories.get(0);
    File bin = new File(archive, bindir);
    SliderUtils.verifyIsDir(bin, log);
    File scriptFile = new File(bin, script);
    SliderUtils.verifyFileExists(scriptFile, log);
    return scriptFile;
  }

  /**
   * Return any additional arguments (argv) to provide when starting this role
   * 
   * @param roleOptions
   *          The options for this role
   * @return A non-null String which contains command line arguments for this role, or the empty string.
   */
  public static String getAdditionalArgs(Map<String,String> roleOptions) {
    if (roleOptions.containsKey(RoleKeys.ROLE_ADDITIONAL_ARGS)) {
      String additionalArgs = roleOptions.get(RoleKeys.ROLE_ADDITIONAL_ARGS);
      if (null != additionalArgs) {
        return additionalArgs;
      }
    }

    return "";
  }
  
  public int getRoleResourceRequirement(String val,
                                        int defVal,
                                        int maxVal) {
    if (val==null) {
      val = Integer.toString(defVal);
    }
    Integer intVal;
    if (ResourceKeys.YARN_RESOURCE_MAX.equals(val)) {
      intVal = maxVal;
    } else {
      intVal = Integer.decode(val);
    }
    return intVal;
  }
}
