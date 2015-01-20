package com.gradleware.tooling.toolingmodel.repository.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.gradleware.tooling.toolingmodel.BuildInvocationFields;
import com.gradleware.tooling.toolingmodel.OmniProjectTask;
import com.gradleware.tooling.toolingmodel.ProjectTaskFields;
import com.gradleware.tooling.toolingmodel.TaskSelectorsFields;
import com.gradleware.tooling.toolingmodel.generic.DefaultModel;
import com.gradleware.tooling.toolingmodel.generic.Model;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;
import org.gradle.tooling.model.GradleTask;
import org.gradle.tooling.model.gradle.BuildInvocations;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Holds the {@code Model&lt;BuildInvocationFields&gt;} for a given set of projects. Each project is identified by its unique full path.
 *
 * The primary advantage of this container is that it allows to work with a generics-free type compared to <code>Map&lt;String, Model&lt;BuildInvocationFields&gt;&gt;</code>.
 */
public final class BuildInvocationsContainer {

    @SuppressWarnings("RedundantStringConstructorCall")
    private static final String NULL_STRING = new String(); // ensure unique instance to use it as a null-string placeholder

    private final ImmutableSortedMap<String, Model<BuildInvocationFields>> buildInvocationsPerProject;

    private BuildInvocationsContainer(SortedMap<String, Model<BuildInvocationFields>> buildInvocationsPerProject) {
        this.buildInvocationsPerProject = ImmutableSortedMap.copyOfSorted(buildInvocationsPerProject);
    }

    /**
     * A {@code Map} of {@code Model&lt;BuildInvocationFields&gt;} per project, where each project is identified by its unique full path.
     *
     * @return the mapping of projects to build invocations
     */
    public ImmutableSortedMap<String, Model<BuildInvocationFields>> asMap() {
        return this.buildInvocationsPerProject;
    }

    /**
     * Converts a {@code Map} of {@link BuildInvocations} to a {@link BuildInvocationsContainer}.
     *
     * @param buildInvocations the build invocations to convert
     * @return the build invocations container
     */
    public static BuildInvocationsContainer from(Map<String, BuildInvocations> buildInvocations) {
        ImmutableSortedMap.Builder<String, Model<BuildInvocationFields>> buildInvocationsMap = ImmutableSortedMap.orderedBy(PathComparator.INSTANCE);
        for (String projectPath : buildInvocations.keySet()) {
            buildInvocationsMap.put(projectPath, DefaultOmniBuildInvocations.from(buildInvocations.get(projectPath), projectPath));
        }
        return new BuildInvocationsContainer(buildInvocationsMap.build());
    }

    /**
     * Converts a {@link GradleProject} to a {@link BuildInvocationsContainer}.
     *
     * @param project the Gradle project to convert
     * @param enforceAllTasksPublic if set to true {@code true}, all tasks should be made public
     * @return the build invocations container
     */
    public static BuildInvocationsContainer from(GradleProject project, boolean enforceAllTasksPublic) {
        ImmutableMultimap<String, Model<ProjectTaskFields>> tasks = buildProjectTasksRecursively(project, ArrayListMultimap.<String, Model<ProjectTaskFields>>create(), enforceAllTasksPublic);
        ImmutableMultimap<String, Model<TaskSelectorsFields>> taskSelectors = buildTaskSelectorsRecursively(project, ArrayListMultimap.<String, Model<TaskSelectorsFields>>create(), enforceAllTasksPublic);
        ImmutableSortedMap<String, Model<BuildInvocationFields>> buildInvocationsMap = buildBuildInvocationsMappingRecursively(tasks, taskSelectors);
        return new BuildInvocationsContainer(buildInvocationsMap);
    }

    private static ImmutableSortedMap<String, Model<BuildInvocationFields>> buildBuildInvocationsMappingRecursively(Multimap<String, Model<ProjectTaskFields>> projectTasks,
                                                                                                                    Multimap<String, Model<TaskSelectorsFields>> taskSelectors) {
        Preconditions.checkState(projectTasks.keySet().size() == taskSelectors.keySet().size());
        Preconditions.checkState(projectTasks.keySet().containsAll(taskSelectors.keySet()) && taskSelectors.keySet().containsAll(projectTasks.keySet()));

        ImmutableSortedMap.Builder<String, Model<BuildInvocationFields>> mapping = ImmutableSortedMap.orderedBy(PathComparator.INSTANCE);
        for (String projectPath : projectTasks.keySet()) {
            ImmutableList<Model<ProjectTaskFields>> projectTasksOfProject = ImmutableSortedSet.orderedBy(TaskComparator.INSTANCE).addAll(projectTasks.get(projectPath)).build().asList();
            ImmutableList<Model<TaskSelectorsFields>> taskSelectorsOfProject = ImmutableSortedSet.orderedBy(TaskSelectorComparator.INSTANCE).addAll(taskSelectors.get(projectPath)).build().asList();
            mapping.put(projectPath, DefaultOmniBuildInvocations.from(projectTasksOfProject, taskSelectorsOfProject));
        }
        return mapping.build();
    }

    private static ImmutableMultimap<String, Model<ProjectTaskFields>> buildProjectTasksRecursively(GradleProject project, Multimap<String, Model<ProjectTaskFields>> tasksPerProject, boolean enforceAllTasksPublic) {
        // add tasks of the current project
        for (GradleTask task : project.getTasks()) {
            tasksPerProject.put(project.getPath(), DefaultOmniProjectTask.from(task, enforceAllTasksPublic));
        }

        // recurse into child projects and add their tasks
        for (GradleProject childProject : project.getChildren()) {
            buildProjectTasksRecursively(childProject, tasksPerProject, enforceAllTasksPublic);
        }

        // return the tasks grouped by project path
        return ImmutableMultimap.copyOf(tasksPerProject);
    }

    @SuppressWarnings("StringEquality")
    private static ImmutableMultimap<String, Model<TaskSelectorsFields>> buildTaskSelectorsRecursively(GradleProject project, Multimap<String, Model<TaskSelectorsFields>> taskSelectorsPerProject, boolean enforceAllTasksPublic) {
        // add task selectors of the current project
        TreeBasedTable<String, String, String> aggregatedTasksWithDescription = TreeBasedTable.create(Ordering.usingToString(), PathComparator.INSTANCE);
        Set<String> publicTasks = Sets.newLinkedHashSet();
        collectAllTasksRecursively(project, aggregatedTasksWithDescription, publicTasks, enforceAllTasksPublic);
        for (String selectorName : aggregatedTasksWithDescription.rowKeySet()) {
            SortedMap<String, String> pathsAndDescriptions = aggregatedTasksWithDescription.row(selectorName);
            String description = pathsAndDescriptions.get(pathsAndDescriptions.firstKey()); // description from project task with smallest path
            SortedSet<String> fqnTaskNames = ImmutableSortedSet.orderedBy(PathComparator.INSTANCE).addAll(pathsAndDescriptions.keySet()).build();

            DefaultModel<TaskSelectorsFields> taskSelector = DefaultOmniTaskSelector.from(
                    selectorName,
                    description != NULL_STRING ? description : null,
                    project.getPath(),
                    publicTasks.contains(selectorName),
                    fqnTaskNames);

            taskSelectorsPerProject.put(project.getPath(), taskSelector);
        }

        // recurse into child projects and add their task selectors
        DomainObjectSet<? extends GradleProject> childProjects = project.getChildren();
        for (GradleProject childProject : childProjects) {
            buildTaskSelectorsRecursively(childProject, taskSelectorsPerProject, enforceAllTasksPublic);
        }

        // return the task selectors grouped by project path
        return ImmutableMultimap.copyOf(taskSelectorsPerProject);
    }

    private static void collectAllTasksRecursively(GradleProject project, Table<String, String, String> tasksWithDescription, Collection<String> publicTasks, boolean enforceAllTasksPublic) {
        for (GradleTask task : project.getTasks()) {
            // convert to OmniProjectTask to have the version-specific logic and default-values applied
            OmniProjectTask projectTask = DefaultOmniProjectTask.from(DefaultOmniProjectTask.from(task, enforceAllTasksPublic));

            // 1) store the path since the task selectors keep all the paths of the tasks they select
            // 2) store the description first by task name and then by path
            //      this allows to later fish out the description of the task whose name matches the selector name and
            //      whose path is the smallest for the given task name (the first entry of the table column)
            //      store null description as empty string to avoid that Guava chokes
            tasksWithDescription.put(projectTask.getName(), projectTask.getPath(), projectTask.getDescription() != null ? projectTask.getDescription() : NULL_STRING);

            // visible tasks are specified by Gradle as those that have a non-empty group
            if (projectTask.isPublic()) {
                publicTasks.add(task.getName());
            }
        }

        for (GradleProject childProject : project.getChildren()) {
            collectAllTasksRecursively(childProject, tasksWithDescription, publicTasks, enforceAllTasksPublic);
        }
    }

    private static final class TaskComparator implements Comparator<Model<ProjectTaskFields>> {

        private static final TaskComparator INSTANCE = new TaskComparator();

        @Override
        public int compare(Model<ProjectTaskFields> o1, Model<ProjectTaskFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(ProjectTaskFields.PATH), o2.get(ProjectTaskFields.PATH));
        }

    }

    private static final class TaskSelectorComparator implements Comparator<Model<TaskSelectorsFields>> {

        private static final TaskSelectorComparator INSTANCE = new TaskSelectorComparator();

        @Override
        public int compare(Model<TaskSelectorsFields> o1, Model<TaskSelectorsFields> o2) {
            return PathComparator.INSTANCE.compare(o1.get(TaskSelectorsFields.NAME), o2.get(TaskSelectorsFields.NAME));
        }

    }
}
