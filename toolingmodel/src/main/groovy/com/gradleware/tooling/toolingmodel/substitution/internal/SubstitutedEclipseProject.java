package com.gradleware.tooling.toolingmodel.substitution.internal;


import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.UnsupportedMethodException;
import org.gradle.tooling.model.eclipse.*;

import java.io.File;
import java.util.Set;

public class SubstitutedEclipseProject implements EclipseProject {

    private final EclipseProject delegate;
    private final Set<ExternalDependency> substitutedExternalDependencies;
    private final Set<EclipseProjectDependency> substitutedProjectDependencies;

    public SubstitutedEclipseProject(EclipseProject delegate, Set<ExternalDependency> substitutedExternalDependencies,
                                     Set<EclipseProjectDependency> substitutedProjectDependencies) {
        this.delegate = delegate;
        this.substitutedExternalDependencies = substitutedExternalDependencies;
        this.substitutedProjectDependencies = substitutedProjectDependencies;
    }

    @Override
    public EclipseProject getParent() {
        return delegate.getParent();
    }

    @Override
    public DomainObjectSet<? extends EclipseProject> getChildren() {
        return delegate.getChildren();
    }

    @Override
    public EclipseJavaSourceSettings getJavaSourceSettings() {
        return delegate.getJavaSourceSettings();
    }

    @Override
    public GradleProject getGradleProject() {
        return delegate.getGradleProject();
    }

    @Override
    public DomainObjectSet<? extends ExternalDependency> getClasspath() {
        return delegate.getClasspath();
    }

    @Override
    public DomainObjectSet<? extends EclipseProjectNature> getProjectNatures() throws UnsupportedMethodException {
        return delegate.getProjectNatures();
    }

    @Override
    public DomainObjectSet<? extends EclipseBuildCommand> getBuildCommands() throws UnsupportedMethodException {
        return null;
    }

    @Override
    public DomainObjectSet<? extends EclipseProjectDependency> getProjectDependencies() {
        return delegate.getProjectDependencies();
    }

    @Override
    public DomainObjectSet<? extends EclipseSourceDirectory> getSourceDirectories() {
        return delegate.getSourceDirectories();
    }

    @Override
    public DomainObjectSet<? extends EclipseLinkedResource> getLinkedResources() {
        return delegate.getLinkedResources();
    }

    @Override
    public File getProjectDirectory() throws UnsupportedMethodException {
        return delegate.getProjectDirectory();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    public Set<? extends ExternalDependency> getSubstitutedExternalDependencies() {
        return substitutedExternalDependencies;
    }

    public Set<? extends EclipseProjectDependency> getSubstitutedProjectDependencies() {
        return substitutedProjectDependencies;
    }
}
