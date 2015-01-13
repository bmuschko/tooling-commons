package com.gradleware.tooling.domain;

import com.gradleware.tooling.domain.generic.HierarchicalModel;

/**
 * Provides detailed information about the Gradle build.
 *
 * @see org.gradle.tooling.model.GradleProject
 */
public interface OmniGradleBuild {

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    OmniGradleProject getRootProject();

    /**
     * Returns the root project of the build.
     *
     * @return the root project
     */
    HierarchicalModel<GradleProjectFields> getRootProjectModel();

}
