package com.gradleware.tooling.toolingmodel.substitution.internal;

import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.tooling.model.eclipse.HierarchicalEclipseProject;

public class DefaultEclipseProjectDependency implements EclipseProjectDependency {
    private final String path;
    private final boolean exported;
    private final EclipseProject target;

    public DefaultEclipseProjectDependency(String path, boolean exported, EclipseProject target) {
        this.path = path;
        this.exported = exported;
        this.target = target;
    }

    @Override
    public HierarchicalEclipseProject getTargetProject() {
        return target;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isExported() {
        return exported;
    }
}