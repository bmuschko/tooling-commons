/*
 * Copyright 2015 the original author or authors.
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

package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.*;
import com.gradleware.tooling.toolingmodel.util.Maybe;
import org.gradle.api.specs.Spec;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.eclipse.*;
import org.gradle.tooling.model.java.JavaSourceSettings;

import java.io.File;
import java.util.Comparator;
import java.util.List;

/**
 * Default implementation of the {@link OmniEclipseProject} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseProject implements OmniEclipseProject {

    private final HierarchyHelper<OmniEclipseProject> hierarchyHelper;
    private String name;
    private String description;
    private Path path;
    private File projectDirectory;
    private ImmutableList<OmniEclipseProjectDependency> projectDependencies;
    private ImmutableList<OmniExternalDependency> externalDependencies;
    private ImmutableList<OmniEclipseLinkedResource> linkedResources;
    private ImmutableList<OmniEclipseSourceDirectory> sourceDirectories;
    private Optional<List<OmniEclipseProjectNature>> projectNatures;
    private Optional<List<OmniEclipseBuildCommand>> buildCommands;
    private Maybe<OmniJavaSourceSettings> javaSourceSettings;

    private DefaultOmniEclipseProject(Comparator<? super OmniEclipseProject> comparator) {
        this.hierarchyHelper = new HierarchyHelper<OmniEclipseProject>(this, Preconditions.checkNotNull(comparator));
    }

    @Override
    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Path getPath() {
        return this.path;
    }

    private void setPath(Path path) {
        this.path = path;
    }

    @Override
    public File getProjectDirectory() {
        return this.projectDirectory;
    }

    private void setProjectDirectory(File projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    @Override
    public ImmutableList<OmniEclipseProjectDependency> getProjectDependencies() {
        return this.projectDependencies;
    }

    public void setProjectDependencies(List<OmniEclipseProjectDependency> projectDependencies) {
        this.projectDependencies = ImmutableList.copyOf(projectDependencies);
    }

    @Override
    public ImmutableList<OmniExternalDependency> getExternalDependencies() {
        return this.externalDependencies;
    }

    public void setExternalDependencies(List<OmniExternalDependency> externalDependencies) {
        this.externalDependencies = ImmutableList.copyOf(externalDependencies);
    }

    @Override
    public ImmutableList<OmniEclipseLinkedResource> getLinkedResources() {
        return this.linkedResources;
    }

    public void setLinkedResources(List<OmniEclipseLinkedResource> linkedResources) {
        this.linkedResources = ImmutableList.copyOf(linkedResources);
    }

    @Override
    public ImmutableList<OmniEclipseSourceDirectory> getSourceDirectories() {
        return this.sourceDirectories;
    }

    public void setSourceDirectories(List<OmniEclipseSourceDirectory> sourceDirectories) {
        this.sourceDirectories = ImmutableList.copyOf(sourceDirectories);
    }

    @Override
    public Optional<List<OmniEclipseProjectNature>> getProjectNatures() {
        return projectNatures;
    }

    public void setProjectNatures(Optional<List<OmniEclipseProjectNature>> projectNatures) {
        if (projectNatures.isPresent()) {
            this.projectNatures = Optional.<List<OmniEclipseProjectNature>>of(ImmutableList.<OmniEclipseProjectNature>copyOf(projectNatures.get()));
        } else {
            this.projectNatures = Optional.absent();
        }
    }

    @Override
    public Optional<List<OmniEclipseBuildCommand>> getBuildCommands() {
        return buildCommands;
    }

    public void setBuildCommands(Optional<List<OmniEclipseBuildCommand>> buildCommands) {
        if (buildCommands.isPresent()) {
            this.buildCommands = Optional.<List<OmniEclipseBuildCommand>>of(ImmutableList.copyOf(buildCommands.get()));
        } else {
            this.buildCommands = Optional.absent();
        }
    }

    @Override
    public Maybe<OmniJavaSourceSettings> getJavaSourceSettings() {
        return javaSourceSettings;
    }

    public void setJavaSourceSettings(Maybe<OmniJavaSourceSettings> javaSourceSettings) {
        this.javaSourceSettings = javaSourceSettings;
    }

    @Override
    public OmniEclipseProject getRoot() {
        return this.hierarchyHelper.getRoot();
    }

    @Override
    public OmniEclipseProject getParent() {
        return this.hierarchyHelper.getParent();
    }

    private void setParent(DefaultOmniEclipseProject parent) {
        this.hierarchyHelper.setParent(parent);
    }

    @Override
    public ImmutableList<OmniEclipseProject> getChildren() {
        return this.hierarchyHelper.getChildren();
    }

    private void addChild(DefaultOmniEclipseProject child) {
        child.setParent(this);
        this.hierarchyHelper.addChild(child);
    }

    @Override
    public ImmutableList<OmniEclipseProject> getAll() {
        return this.hierarchyHelper.getAll();
    }

    @Override
    public ImmutableList<OmniEclipseProject> filter(Spec<? super OmniEclipseProject> predicate) {
        return this.hierarchyHelper.filter(predicate);
    }

    @Override
    public Optional<OmniEclipseProject> tryFind(Spec<? super OmniEclipseProject> predicate) {
        return this.hierarchyHelper.tryFind(predicate);
    }

    public static DefaultOmniEclipseProject from(EclipseProject project, boolean buildCommandsAndNaturesAvailable, boolean sourceSettingsAvailable) {
        DefaultOmniEclipseProject eclipseProject = new DefaultOmniEclipseProject(OmniEclipseProjectComparator.INSTANCE);
        eclipseProject.setName(project.getName());
        eclipseProject.setDescription(project.getDescription());
        eclipseProject.setPath(Path.from(project.getGradleProject().getPath()));
        eclipseProject.setProjectDirectory(project.getProjectDirectory());
        eclipseProject.setProjectDependencies(toProjectDependencies(project.getProjectDependencies()));
        eclipseProject.setExternalDependencies(toExternalDependencies(project.getClasspath()));
        eclipseProject.setLinkedResources(toLinkedResources(project.getLinkedResources()));
        eclipseProject.setSourceDirectories(toSourceDirectories(project.getSourceDirectories()));
        if (buildCommandsAndNaturesAvailable) {
            eclipseProject.setProjectNatures(Optional.<List<OmniEclipseProjectNature>>of(toProjectNatures(project.getProjectNatures())));
            eclipseProject.setBuildCommands(Optional.<List<OmniEclipseBuildCommand>>of(toBuildCommands(project.getBuildCommands())));
        } else {
            eclipseProject.setProjectNatures(Optional.<List<OmniEclipseProjectNature>>absent());
            eclipseProject.setBuildCommands(Optional.<List<OmniEclipseBuildCommand>>absent());
        }
        if (sourceSettingsAvailable) {
            eclipseProject.setJavaSourceSettings(Maybe.of(toOmniJavaSourceSettings(project.getJavaSourceSettings())));
        } else {
            eclipseProject.setJavaSourceSettings(Maybe.<OmniJavaSourceSettings>absent());
        }

        for (EclipseProject child : project.getChildren()) {
            DefaultOmniEclipseProject eclipseChildProject = from(child, buildCommandsAndNaturesAvailable, sourceSettingsAvailable);
            eclipseProject.addChild(eclipseChildProject);
        }

        return eclipseProject;
    }

    private static ImmutableList<OmniEclipseProjectDependency> toProjectDependencies(DomainObjectSet<? extends EclipseProjectDependency> projectDependencies) {
        return FluentIterable.from(projectDependencies).transform(new Function<EclipseProjectDependency, OmniEclipseProjectDependency>() {
            @Override
            public OmniEclipseProjectDependency apply(EclipseProjectDependency input) {
                return DefaultOmniEclipseProjectDependency.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniExternalDependency> toExternalDependencies(DomainObjectSet<? extends ExternalDependency> externalDependencies) {
        // filter out invalid external dependencies
        // Gradle versions <= 1.10 return external dependencies from dependent projects that are not valid, i.e. all fields are null except the file with name 'unresolved dependency...'
        return FluentIterable.from(externalDependencies).filter(new Predicate<ExternalDependency>() {
            @Override
            public boolean apply(ExternalDependency input) {
                return input.getFile().exists();
            }
        }).transform(new Function<ExternalDependency, OmniExternalDependency>() {
            @Override
            public OmniExternalDependency apply(ExternalDependency input) {
                return DefaultOmniExternalDependency.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniEclipseLinkedResource> toLinkedResources(DomainObjectSet<? extends EclipseLinkedResource> linkedResources) {
        return FluentIterable.from(linkedResources).transform(new Function<EclipseLinkedResource, OmniEclipseLinkedResource>() {
            @Override
            public OmniEclipseLinkedResource apply(EclipseLinkedResource input) {
                return DefaultOmniEclipseLinkedResource.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniEclipseSourceDirectory> toSourceDirectories(DomainObjectSet<? extends EclipseSourceDirectory> sourceDirectories) {
        return FluentIterable.from(sourceDirectories).transform(new Function<EclipseSourceDirectory, OmniEclipseSourceDirectory>() {
            @Override
            public OmniEclipseSourceDirectory apply(EclipseSourceDirectory input) {
                return DefaultOmniEclipseSourceDirectory.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniEclipseProjectNature> toProjectNatures(DomainObjectSet<? extends EclipseProjectNature> projectNatures) {
        return FluentIterable.from(projectNatures).transform(new Function<EclipseProjectNature, OmniEclipseProjectNature>() {

            @Override
            public OmniEclipseProjectNature apply(EclipseProjectNature input) {
                return DefaultOmniEclipseProjectNature.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniEclipseBuildCommand> toBuildCommands(DomainObjectSet<? extends EclipseBuildCommand> buildCommands) {
        return FluentIterable.from(buildCommands).transform(new Function<EclipseBuildCommand, OmniEclipseBuildCommand>() {
            @Override
            public OmniEclipseBuildCommand apply(EclipseBuildCommand input) {
                return DefaultOmniEclipseBuildCommand.from(input);
            }
        }).toList();
    }

    private static OmniJavaSourceSettings toOmniJavaSourceSettings(final JavaSourceSettings javaSourceSettings) {
        if (javaSourceSettings != null) {
            String version = javaSourceSettings.getSourceLanguageLevel().getVersion().getName();
            DefaultOmniJavaVersion javaVersion = new DefaultOmniJavaVersion(version);
            DefaultOmniJavaLanguageLevel sourceLanguageLevel = new DefaultOmniJavaLanguageLevel(javaVersion);
            DefaultOmniJavaSourceSettings sourceSettings = new DefaultOmniJavaSourceSettings(sourceLanguageLevel);
            return sourceSettings;
        } else {
            return null;
        }
    }

    /**
     * Singleton comparator to compare {@code OmniEclipseProject} instances by their project path.
     */
    private enum OmniEclipseProjectComparator implements Comparator<OmniEclipseProject> {

        INSTANCE;

        @Override
        public int compare(OmniEclipseProject o1, OmniEclipseProject o2) {
            return o1.getPath().compareTo(o2.getPath());
        }

    }

}

