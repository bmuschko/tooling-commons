package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.EclipseProjectDependencyFields;
import com.gradleware.tooling.toolingmodel.EclipseProjectFields;
import com.gradleware.tooling.toolingmodel.EclipseSourceDirectoryFields;
import com.gradleware.tooling.toolingmodel.ExternalDependencyFields;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectDependency;
import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;
import com.gradleware.tooling.toolingmodel.generic.DefaultHierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.gradle.tooling.model.eclipse.EclipseProjectDependency;
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory;

import java.io.File;
import java.util.Comparator;
import java.util.List;

/**
 * Default implementation of the {@link OmniEclipseProject} interface.
 */
public final class DefaultOmniEclipseProject implements OmniEclipseProject {

    private final HierarchyHelper<OmniEclipseProject> hierarchyHelper;
    private String name;
    private String description;
    private String path;
    private File projectDirectory;
    private ImmutableList<OmniEclipseProjectDependency> projectDependencies;
    private ImmutableList<OmniExternalDependency> externalDependencies;
    private ImmutableList<OmniEclipseSourceDirectory> sourceDirectories;

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
    public String getPath() {
        return this.path;
    }

    private void setPath(String path) {
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
    public ImmutableList<OmniEclipseSourceDirectory> getSourceDirectories() {
        return this.sourceDirectories;
    }

    public void setSourceDirectories(List<OmniEclipseSourceDirectory> sourceDirectories) {
        this.sourceDirectories = ImmutableList.copyOf(sourceDirectories);
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
    public ImmutableList<OmniEclipseProject> filter(Predicate<? super OmniEclipseProject> predicate) {
        return this.hierarchyHelper.filter(predicate);
    }

    @Override
    public Optional<OmniEclipseProject> tryFind(Predicate<? super OmniEclipseProject> predicate) {
        return this.hierarchyHelper.tryFind(predicate);
    }

    public static DefaultOmniEclipseProject from(HierarchicalModel<EclipseProjectFields> project) {
        DefaultOmniEclipseProject eclipseProject = new DefaultOmniEclipseProject(OmniEclipseProjectComparator.INSTANCE);
        eclipseProject.setName(project.get(EclipseProjectFields.NAME));
        eclipseProject.setDescription(project.get(EclipseProjectFields.DESCRIPTION));
        eclipseProject.setPath(project.get(EclipseProjectFields.PATH));
        eclipseProject.setProjectDirectory(project.get(EclipseProjectFields.PROJECT_DIRECTORY));
        eclipseProject.setProjectDependencies(toProjectDependencies(project.get(EclipseProjectFields.PROJECT_DEPENDENCIES)));
        eclipseProject.setExternalDependencies(toExternalDependencies(project.get(EclipseProjectFields.EXTERNAL_DEPENDENCIES)));
        eclipseProject.setSourceDirectories(toSourceDirectories(project.get(EclipseProjectFields.SOURCE_DIRECTORIES)));

        for (HierarchicalModel<EclipseProjectFields> child : project.getChildren()) {
            DefaultOmniEclipseProject eclipseChildProject = from(child);
            eclipseProject.addChild(eclipseChildProject);
        }

        return eclipseProject;
    }

    private static ImmutableList<OmniEclipseProjectDependency> toProjectDependencies(List<Model<EclipseProjectDependencyFields>> projectDependencies) {
        return FluentIterable.from(projectDependencies).transform(new Function<Model<EclipseProjectDependencyFields>, OmniEclipseProjectDependency>() {
            @Override
            public OmniEclipseProjectDependency apply(Model<EclipseProjectDependencyFields> input) {
                return DefaultOmniEclipseProjectDependency.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniExternalDependency> toExternalDependencies(List<Model<ExternalDependencyFields>> externalDependencies) {
        return FluentIterable.from(externalDependencies).transform(new Function<Model<ExternalDependencyFields>, OmniExternalDependency>() {
            @Override
            public OmniExternalDependency apply(Model<ExternalDependencyFields> input) {
                return DefaultOmniExternalDependency.from(input);
            }
        }).toList();
    }

    private static ImmutableList<OmniEclipseSourceDirectory> toSourceDirectories(List<Model<EclipseSourceDirectoryFields>> sourceDirectories) {
        return FluentIterable.from(sourceDirectories).transform(new Function<Model<EclipseSourceDirectoryFields>, OmniEclipseSourceDirectory>() {
            @Override
            public OmniEclipseSourceDirectory apply(Model<EclipseSourceDirectoryFields> input) {
                return DefaultOmniEclipseSourceDirectory.from(input);
            }
        }).toList();
    }

    public static DefaultHierarchicalModel<EclipseProjectFields> from(EclipseProject project) {
        DefaultHierarchicalModel<EclipseProjectFields> eclipseProject = new DefaultHierarchicalModel<EclipseProjectFields>(EclipseProjectComparator.INSTANCE);
        eclipseProject.put(EclipseProjectFields.NAME, project.getName());
        eclipseProject.put(EclipseProjectFields.DESCRIPTION, project.getDescription());
        eclipseProject.put(EclipseProjectFields.PATH, project.getGradleProject().getPath());
        eclipseProject.put(EclipseProjectFields.PROJECT_DIRECTORY, project.getProjectDirectory());
        eclipseProject.put(EclipseProjectFields.PROJECT_DEPENDENCIES, toProjectDependencies(project.getProjectDependencies()));
        eclipseProject.put(EclipseProjectFields.EXTERNAL_DEPENDENCIES, toExternalDependencies(project.getClasspath()));
        eclipseProject.put(EclipseProjectFields.SOURCE_DIRECTORIES, toSourceDirectories(project.getSourceDirectories()));

        for (EclipseProject child : project.getChildren()) {
            DefaultHierarchicalModel<EclipseProjectFields> eclipseChildProject = from(child);
            eclipseProject.addChild(eclipseChildProject);
        }

        return eclipseProject;
    }

    private static ImmutableList<Model<EclipseProjectDependencyFields>> toProjectDependencies(DomainObjectSet<? extends EclipseProjectDependency> projectDependencies) {
        return FluentIterable.from(projectDependencies).transform(new Function<EclipseProjectDependency, Model<EclipseProjectDependencyFields>>() {
            @Override
            public Model<EclipseProjectDependencyFields> apply(EclipseProjectDependency input) {
                return DefaultOmniEclipseProjectDependency.from(input);
            }
        }).toList();
    }

    private static ImmutableList<Model<ExternalDependencyFields>> toExternalDependencies(DomainObjectSet<? extends ExternalDependency> externalDependencies) {
        return FluentIterable.from(externalDependencies).transform(new Function<ExternalDependency, Model<ExternalDependencyFields>>() {
            @Override
            public Model<ExternalDependencyFields> apply(ExternalDependency input) {
                return DefaultOmniExternalDependency.from(input);
            }
        }).toList();
    }

    private static ImmutableList<Model<EclipseSourceDirectoryFields>> toSourceDirectories(DomainObjectSet<? extends EclipseSourceDirectory> sourceDirectories) {
        return FluentIterable.from(sourceDirectories).transform(new Function<EclipseSourceDirectory, Model<EclipseSourceDirectoryFields>>() {
            @Override
            public Model<EclipseSourceDirectoryFields> apply(EclipseSourceDirectory input) {
                return DefaultOmniEclipseSourceDirectory.from(input);
            }
        }).toList();
    }

    /**
     * Compares OmniEclipseProjects by their project path.
     */
    private static final class OmniEclipseProjectComparator implements Comparator<OmniEclipseProject> {

        public static final OmniEclipseProjectComparator INSTANCE = new OmniEclipseProjectComparator();

        @Override
        public int compare(OmniEclipseProject o1, OmniEclipseProject o2) {
            return PathComparator.INSTANCE.compare(o1.getPath(), o2.getPath());
        }

    }

    /**
     * Compares EclipseProjects by their project path.
     */
    private static final class EclipseProjectComparator implements Comparator<Model<EclipseProjectFields>> {

        public static final EclipseProjectComparator INSTANCE = new EclipseProjectComparator();

        @Override
        public int compare(Model<EclipseProjectFields> o1, Model<EclipseProjectFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(EclipseProjectFields.PATH), o2.get(EclipseProjectFields.PATH));
        }

    }

}

