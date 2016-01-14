package com.gradleware.tooling.toolingmodel.substitution.internal;

import org.gradle.api.Transformer;
import org.gradle.api.specs.Spec;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.util.CollectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModuleToProjectSubstitutor {
    public Set<EclipseProject> substitute(Set<EclipseProject> openProjects) {
        final Set<EclipseProject> allProjects = new HashSet<EclipseProject>();
        final Map<String, EclipseProject> projectNames = mapProjectsToNames(openProjects);

        for (EclipseProject project : openProjects) {
            Set<ExternalDependency> matchingExternalDependencies = findMatchingExternalDependencies(projectNames, project.getClasspath());

            if (!matchingExternalDependencies.isEmpty()) {
                Set<EclipseProjectDependency> substitutedProjectDependencies = createSubstitutedProjectDependencies(projectNames, matchingExternalDependencies);
                allProjects.add(new SubstitutedEclipseProject(project, matchingExternalDependencies, substitutedProjectDependencies));
            } else {
                allProjects.add(project);
            }
        }

        return allProjects;
    }

    private Map<String, EclipseProject> mapProjectsToNames(Set<EclipseProject> openProjects) {
        return CollectionUtils.collectMap(openProjects, new Transformer<String, EclipseProject>() {
            @Override
            public String transform(EclipseProject eclipseProject) {
                return eclipseProject.getName();
            }
        });
    }

    private Set<ExternalDependency> findMatchingExternalDependencies(final Map<String, EclipseProject> projectNames, DomainObjectSet<? extends ExternalDependency> classpath) {
        return CollectionUtils.filter(classpath, new Spec<ExternalDependency>() {
            @Override
            public boolean isSatisfiedBy(ExternalDependency element) {
                return projectNames.containsKey(element.getGradleModuleVersion().getName());
            }
        });
    }

    private Set<EclipseProjectDependency> createSubstitutedProjectDependencies(final Map<String, EclipseProject> projectNames, Set<ExternalDependency> matchingExternalDependencies) {
        return CollectionUtils.collect(matchingExternalDependencies, new Transformer<EclipseProjectDependency, ExternalDependency>() {
            @Override
            public EclipseProjectDependency transform(ExternalDependency externalDependency) {
                EclipseProject matchingEclipseProject = projectNames.get(externalDependency.getGradleModuleVersion().getName());
                String projectPath = String.format(":%s", matchingEclipseProject.getName());
                return new DefaultEclipseProjectDependency(projectPath, false, matchingEclipseProject);
            }
        });
    }
}
