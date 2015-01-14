package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.GradleProjectFields;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleProject;
import com.gradleware.tooling.toolingmodel.generic.HierarchicalModel;
import org.gradle.tooling.model.GradleProject;

/**
 * Default implementation of the {@link OmniGradleBuild} interface.
 */
public final class DefaultOmniGradleBuild implements OmniGradleBuild {

    private final HierarchicalModel<GradleProjectFields> rootProjectModel;
    private final OmniGradleProject rootProject;

    private DefaultOmniGradleBuild(HierarchicalModel<GradleProjectFields> rootProjectModel, OmniGradleProject rootProject) {
        this.rootProjectModel = rootProjectModel;
        this.rootProject = rootProject;
    }

    @Override
    public OmniGradleProject getRootProject() {
        return this.rootProject;
    }

    @Override
    public HierarchicalModel<GradleProjectFields> getRootProjectModel() {
        return this.rootProjectModel;
    }

    public static DefaultOmniGradleBuild from(GradleProject gradleRootProject, boolean enforceAllTasksPublic) {
        HierarchicalModel<GradleProjectFields> rootProjectModel = DefaultOmniGradleProject.from(gradleRootProject, enforceAllTasksPublic);
        OmniGradleProject rootProject = DefaultOmniGradleProject.from(rootProjectModel);
        return new DefaultOmniGradleBuild(rootProjectModel, rootProject);
    }

}
