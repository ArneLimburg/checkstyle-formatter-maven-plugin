/*
 * Copyright 2025 Arne Limburg, Steffen Pieper.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.limburg.checkstyle;

import static dev.limburg.checkstyle.LineSeparator.fromString;
import static org.codehaus.plexus.util.FileUtils.resolveFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.checkstyle.exec.CheckstyleExecutor;
import org.apache.maven.plugins.checkstyle.exec.CheckstyleExecutorException;
import org.apache.maven.plugins.checkstyle.exec.CheckstyleExecutorRequest;
import org.apache.maven.plugins.checkstyle.exec.CheckstyleResults;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.FileUtils;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Violation;

import dev.limburg.checkstyle.formatter.FileFormatter;

@Mojo(name = "write")
public class CheckstyleFormatterMojo extends AbstractMojo {

    public static final String LINE_ENDING_PROPERTY_NAME = "lineEnding";
    private static final String JAVA_FILES = "**\\/*.java";
    private static final String DEFAULT_CONFIG_LOCATION = "sun_checks.xml";
    /**
     * Skip entire execution.
     *
     * @since 0.1.0
     */
    @Parameter(property = "checkstyle-formatter.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * Specifies the location of the resources to be used for Checkstyle.
     *
     * @since 0.1.0
     */
    @Parameter(defaultValue = "${project.resources}", readonly = true)
    protected List<Resource> resources;

    /**
     * Specifies the location of the test resources to be used for Checkstyle.
     *
     * @since 0.1.0
     */
    @Parameter(defaultValue = "${project.testResources}", readonly = true)
    protected List<Resource> testResources;

    /**
     * <p>
     * Specifies the location of the XML configuration to use.
     * <p>
     * Potential values are a filesystem path, a URL, or a classpath resource.
     * This parameter expects that the contents of the location conform to the
     * xml format (Checkstyle <a
     * href="https://checkstyle.org/config.html#Modules">Checker
     * module</a>) configuration of rulesets.
     * <p>
     * This parameter is resolved as resource, URL, then file. If successfully
     * resolved, the contents of the configuration is copied into the
     * <code>${project.build.directory}/checkstyle-configuration.xml</code>
     * file before being passed to Checkstyle as a configuration.
     * <p>
     * There are 2 predefined rulesets.
     * <ul>
     * <li><code>sun_checks.xml</code>: Sun Checks.</li>
     * <li><code>google_checks.xml</code>: Google Checks.</li>
     * </ul>
     *
     * @since 0.1.0
     */
    @Parameter(property = "checkstyle.config.location", defaultValue = DEFAULT_CONFIG_LOCATION)
    protected String configLocation;

    /**
     * <p>
     * Specifies the location of the properties file.
     * <p>
     * This parameter is resolved as URL, File then resource. If successfully
     * resolved, the contents of the properties location is copied into the
     * <code>${project.build.directory}/checkstyle-checker.properties</code>
     * file before being passed to Checkstyle for loading.
     * <p>
     * The contents of the <code>propertiesLocation</code> will be made
     * available to Checkstyle for specifying values for parameters within the
     * xml configuration (specified in the <code>configLocation</code>
     * parameter).
     *
     * @since 0.1.0
     */
    @Parameter(property = "checkstyle.properties.location")
    protected String propertiesLocation;

    /**
     * Allows for specifying raw property expansion information.
     */
    @Parameter
    protected String propertyExpansion;

    /**
     * <p>
     * Specifies the location of the License file (a.k.a. the header file) that
     * can be used by Checkstyle to verify that source code has the correct
     * license header.
     * <p>
     * You need to use <code>${checkstyle.header.file}</code> in your Checkstyle xml
     * configuration to reference the name of this header file.
     * <p>
     * For instance:
     * <pre>
     * &lt;module name="RegexpHeader"&gt;
     *   &lt;property name="headerFile" value="${checkstyle.header.file}"/&gt;
     * &lt;/module&gt;
     * </pre>
     *
     * @since 0.1.0
     */
    @Parameter(property = "checkstyle.header.file", defaultValue = "LICENSE.txt")
    protected String headerLocation;

    /**
     * Specifies the cache file used to speed up Checkstyle on successive runs.
     */
    @Parameter(defaultValue = "${project.build.directory}/checkstyle-cachefile")
    protected String cacheFile;

    /**
     * The key to be used in the properties for the suppressions file.
     *
     * @since 0.1.0
     */
    @Parameter(property = "checkstyle.suppression.expression", defaultValue = "checkstyle.suppressions.file")
    protected String suppressionsFileExpression;

    /**
     * <p>
     * Specifies the location of the suppressions XML file to use.
     * <p>
     * This parameter is resolved as resource, URL, then file. If successfully
     * resolved, the contents of the suppressions XML is copied into the
     * <code>${project.build.directory}/checkstyle-suppressions.xml</code> file
     * before being passed to Checkstyle for loading.
     * <p>
     * See <code>suppressionsFileExpression</code> for the property that will
     * be made available to your Checkstyle configuration.
     *
     * @since 0.1.0
     */
    @Parameter(property = "checkstyle.suppressions.location")
    protected String suppressionsLocation;

    /**
     * The file encoding to use when reading the source files. If the property <code>project.build.sourceEncoding</code>
     * is not set, the platform default encoding is used. <strong>Note:</strong> This parameter always overrides the
     * property <code>charset</code> from Checkstyle's <code>TreeWalker</code> module.
     *
     * @since 0.1.0
     */
    @Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}")
    protected String inputEncoding;
    /**
     * By using this property, you can specify the whole Checkstyle rules
     * inline directly inside this pom.
     *
     * <pre>
     * &lt;plugin&gt;
     *   ...
     *   &lt;configuration&gt;
     *     &lt;checkstyleRules&gt;
     *       &lt;module name="Checker"&gt;
     *         &lt;module name="FileTabCharacter"&gt;
     *           &lt;property name="eachLine" value="true" /&gt;
     *         &lt;/module&gt;
     *         &lt;module name="TreeWalker"&gt;
     *           &lt;module name="EmptyBlock"/&gt;
     *         &lt;/module&gt;
     *       &lt;/module&gt;
     *     &lt;/checkstyleRules&gt;
     *   &lt;/configuration&gt;
     *   ...
     * </pre>
     *
     * @since 0.1.0
     */
    @Parameter
    protected PlexusConfiguration checkstyleRules;

    /**
     * The header to use for the inline configuration.
     * Only used when you specify {@code checkstyleRules}.
     */
    @Parameter(
        defaultValue = "<?xml version=\"1.0\"?>\n"
        + "<!DOCTYPE module PUBLIC \"-//Checkstyle//DTD Checkstyle Configuration 1.3//EN\"\n"
        + "        \"https://checkstyle.org/dtds/configuration_1_3.dtd\">\n")
    protected String checkstyleRulesHeader;

    /**
     * Dump file for inlined Checkstyle rules.
     */
    @Parameter(
        property = "checkstyle.output.rules.file",
        defaultValue = "${project.build.directory}/checkstyle-rules.xml")
    protected File rulesFiles;

    /**
     * The Plugin Descriptor
     */
    @Parameter(defaultValue = "${plugin}", readonly = true, required = true)
    protected PluginDescriptor plugin;

    /**
     * Specifies the location of the test source directories to be used for Checkstyle.
     * Default value is <code>${project.testCompileSourceRoots}</code>.
     *
     * @since 2.13
     */
    // Compatibility with all Maven 3: default of 'project.testCompileSourceRoots' is done manually because of MNG-5440
    @Parameter
    protected List<String> testSourceDirectories;

    /**
     * Specifies whether generated source files should be excluded from Checkstyle.
     *
     * @since 3.3.1
     */
    @Parameter(property = "checkstyle.excludeGeneratedSources", defaultValue = "false")
    protected boolean excludeGeneratedSources;

    /**
     * Specifies the location of the source directories to be used for Checkstyle.
     * Default value is <code>${project.compileSourceRoots}</code>.
     *
     * @since 2.13
     */
    // Compatibility with all Maven 3: default of 'project.compileSourceRoots' is done manually because of MNG-5440
    @Parameter
    protected List<String> sourceDirectories;

    /**
     * Include or not the test source directory to be used for Checkstyle.
     *
     * @since 2.2
     */
    @Parameter(defaultValue = "false")
    protected boolean includeTestSourceDirectory;

    /**
     * Specifies the names filter of the source files to be excluded for
     * Checkstyle.
     */
    @Parameter(property = "checkstyle.excludes")
    protected String excludes;

    /**
     * Specifies the names filter of the source files to be used for Checkstyle.
     */
    @Parameter(property = "checkstyle.includes", defaultValue = JAVA_FILES, required = true)
    protected String includes;

    /**
     * Specifies the names filter of the files to be excluded for
     * Checkstyle when checking resources.
     *
     * @since 2.11
     */
    @Parameter(property = "checkstyle.resourceExcludes")
    protected String resourceExcludes;

    /**
     * Specifies the names filter of the files to be used for Checkstyle when checking resources.
     *
     * @since 2.11
     */
    @Parameter(property = "checkstyle.resourceIncludes", defaultValue = "**/*.properties", required = true)
    protected String resourceIncludes;

    /**
     * Whether to apply Checkstyle to resource directories.
     *
     * @since 2.11
     */
    @Parameter(property = "checkstyle.includeResources", defaultValue = "true", required = true)
    protected boolean includeResources = true;

    /**
     * Defines the line ending for all files.
     *
     * @since 0.1.0
     */
    @Parameter(property = "checkstyleFormatter.lineEnding")
    protected String resultingLineEnding;

    /**
     * Whether to apply Checkstyle to test resource directories.
     *
     * @since 2.11
     */
    @Parameter(property = "checkstyle.includeTestResources", defaultValue = "true", required = true)
    protected boolean includeTestResources = true;

    /**
     * The Maven Project Object.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * @since 2.5
     */
    protected final CheckstyleExecutor checkstyleExecutor;

    private FileFormatter formatter;

    @Inject
    public CheckstyleFormatterMojo(@Named("default") CheckstyleExecutor checkstyleExecutor, FileFormatter formatter) {
        this.checkstyleExecutor = checkstyleExecutor;
        this.formatter = formatter;
    }

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            return;
        }
        String effectiveConfigLocation = computeEffectiveConfigLocation();

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            CheckstyleExecutorRequest request = buildCheckstyleExecutorRequest(effectiveConfigLocation);
            CheckstyleResults results = checkstyleExecutor.executeCheckstyle(request);

            DefaultConfiguration lineEndingConfig = new DefaultConfiguration(LINE_ENDING_PROPERTY_NAME);
            lineEndingConfig.addProperty(LINE_ENDING_PROPERTY_NAME, fromString(resultingLineEnding).getSeparator());
            lineEndingConfig.addChild(results.getConfiguration());

            results.getFiles().entrySet()
                .forEach(entry -> formatter.formatEntry(entry, lineEndingConfig));

            getLog().warn(results.getFiles().values().stream()
                .flatMap(List::stream)
                .map(AuditEvent::getViolation)
                .map(Violation::getKey)
                .collect(Collectors.joining(", "))
            );
        } catch (CheckstyleException e) {
            throw new MojoExecutionException("Failed during checkstyle configuration", e);
        } catch (CheckstyleExecutorException e) {
            throw new MojoExecutionException("Failed during checkstyle execution", e);
        } finally {
            // be sure to restore original context classloader
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    private CheckstyleExecutorRequest buildCheckstyleExecutorRequest(String effectiveConfigLocation) {
        return new CheckstyleExecutorRequest().setExcludes(excludes)
            .setIncludes(includes)
            .setResourceIncludes(resourceIncludes)
            .setResourceExcludes(resourceExcludes)
            .setIncludeResources(includeResources)
            .setIncludeTestResources(includeTestResources)
            .setIncludeTestSourceDirectory(includeTestSourceDirectory)
            .setProject(project)
            .setSourceDirectories(getSourceDirectories())
            .setResources(resources)
            .setTestResources(testResources)
            .setSuppressionsLocation(suppressionsLocation)
            .setTestSourceDirectories(getTestSourceDirectories())
            .setConfigLocation(effectiveConfigLocation)
            .setConfigurationArtifacts(collectArtifacts("config"))
            .setPropertyExpansion(propertyExpansion)
            .setHeaderLocation(headerLocation)
            .setLicenseArtifacts(collectArtifacts("license"))
            .setCacheFile(cacheFile)
            .setSuppressionsFileExpression(suppressionsFileExpression)
            .setEncoding(inputEncoding)
            .setPropertiesLocation(propertiesLocation);
    }

    private String computeEffectiveConfigLocation() throws MojoExecutionException {
        String effectiveConfigLocation = configLocation;
        if (checkstyleRules != null) {
            if (!DEFAULT_CONFIG_LOCATION.equals(configLocation)) {
                throw new MojoExecutionException(
                    "If you use inline configuration for rules, don't specify " + "a configLocation");
            }
            if (checkstyleRules.getChildCount() > 1) {
                throw new MojoExecutionException("Currently only one root module is supported");
            }

            PlexusConfiguration checkerModule = checkstyleRules.getChild(0);

            try {
                FileUtils.forceMkdir(rulesFiles.getParentFile());
                FileUtils.fileWrite(rulesFiles, checkstyleRulesHeader + checkerModule.toString());
            } catch (final IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
            effectiveConfigLocation = rulesFiles.getAbsolutePath();
        }
        return effectiveConfigLocation;
    }

    private List<File> getSourceDirectories() {
        if (sourceDirectories == null) {
            sourceDirectories = filterBuildTarget(project.getCompileSourceRoots());
        }
        List<File> sourceDirs = new ArrayList<>(sourceDirectories.size());
        for (String sourceDir : sourceDirectories) {
            sourceDirs.add(resolveFile(project.getBasedir(), sourceDir));
        }
        return sourceDirs;
    }

    private List<Artifact> collectArtifacts(String hint) {
        List<Artifact> artifacts = new ArrayList<>();

        PluginManagement pluginManagement = project.getBuild().getPluginManagement();
        if (pluginManagement != null) {
            artifacts.addAll(getCheckstylePluginDependenciesAsArtifacts(pluginManagement.getPluginsAsMap(), hint));
        }

        artifacts.addAll(
            getCheckstylePluginDependenciesAsArtifacts(project.getBuild().getPluginsAsMap(), hint));

        return artifacts;
    }

    private List<Artifact> getCheckstylePluginDependenciesAsArtifacts(Map<String, Plugin> plugins, String hint) {
        List<Artifact> artifacts = new ArrayList<>();

        Plugin checkstylePlugin = plugins.get(plugin.getGroupId() + ":" + plugin.getArtifactId());
        if (checkstylePlugin != null) {
            for (Dependency dep : checkstylePlugin.getDependencies()) {
                // @todo if we can filter on hints, it should be done here...
                String depKey = dep.getGroupId() + ":" + dep.getArtifactId();
                artifacts.add(plugin.getArtifactMap().get(depKey));
            }
        }
        return artifacts;
    }

    private List<File> getTestSourceDirectories() {
        if (testSourceDirectories == null) {
            testSourceDirectories = filterBuildTarget(project.getTestCompileSourceRoots());
        }
        List<File> testSourceDirs = new ArrayList<>(testSourceDirectories.size());
        for (String testSourceDir : testSourceDirectories) {
            testSourceDirs.add(resolveFile(project.getBasedir(), testSourceDir));
        }
        return testSourceDirs;
    }

    private List<String> filterBuildTarget(List<String> allSourceDirectories) {
        if (!excludeGeneratedSources) {
            return allSourceDirectories;
        }

        List<String> filtered = new ArrayList<>(allSourceDirectories.size());
        Path buildTarget = resolveFile(project.getBasedir(), project.getBuild().getDirectory()).toPath();

        for (String sourceDir : allSourceDirectories) {
            Path src = resolveFile(project.getBasedir(), sourceDir).toPath();
            if (!src.startsWith(buildTarget)) {
                filtered.add(sourceDir);
            }
        }
        return filtered;
    }
}
