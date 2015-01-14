package com.gradleware.tooling.toolingmodel.repository;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;

/**
 * Repository for Gradle build models. Listeners can be registered to get notified about model updates. It is left to the implementation through which channel the events are
 * broadcast.
 */
public interface NewModelRepository {

    /**
     * Registers the given {@code listener} to receive model change events.
     *
     * @param listener listener to subscribe to receiving events
     */
    void register(Object listener);

    /**
     * Unregisters the given {@code listener} from receiving model change events.
     *
     * @param listener listener to unsubscribe from receiving events
     */
    void unregister(Object listener);

    /**
     * Fetches the {@link OmniBuildEnvironment} synchronously and broadcasts it through a {@link NewBuildEnvironmentUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the build environment, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniBuildEnvironment fetchBuildEnvironmentAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniGradleBuildStructure} synchronously and broadcasts it through a {@link NewGradleBuildStructureUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle build, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniGradleBuildStructure fetchGradleBuildAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

    /**
     * Fetches the {@link OmniGradleBuild} synchronously and broadcasts it through a {@link NewGradleBuildUpdateEvent}.
     *
     * @param transientRequestAttributes the transient request attributes
     * @param fetchStrategy the fetch strategy
     * @return the gradle project, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
     */
    OmniGradleBuild fetchGradleProjectAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

//    /**
//     * Fetches the {@link org.gradle.tooling.model.eclipse.EclipseProject} synchronously and broadcasts it through a {@link com.gradleware.tooling.toolingmodel.repository.EclipseProjectUpdateEvent}.
//     *
//     * @param transientRequestAttributes the transient request attributes
//     * @param fetchStrategy the fetch strategy
//     * @return the eclipse project, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
//     */
//    EclipseProject fetchEclipseProjectAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);
//

//    /**
//     * Fetches the {@link org.gradle.tooling.model.gradle.BuildInvocations} synchronously and broadcasts them through a {@link com.gradleware.tooling.toolingmodel.repository.BuildInvocationsUpdateEvent}.
//     *
//     * @param transientRequestAttributes the transient request attributes
//     * @param fetchStrategy the fetch strategy
//     * @return the build invocations, never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
//     */
//    BuildInvocationsContainer fetchBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);
//
//    /**
//     * Fetches the {@link org.gradle.tooling.model.GradleProject} and {@link org.gradle.tooling.model.gradle.BuildInvocations} synchronously and broadcasts them through a {@link com.gradleware.tooling.toolingmodel.repository.GradleProjectUpdateEvent} and {@link
//     * com.gradleware.tooling.toolingmodel.repository.BuildInvocationsUpdateEvent}.
//     *
//     * @param transientRequestAttributes the transient request attributes
//     * @param fetchStrategy the fetch strategy
//     * @return the gradle project and build invocations, pair values never null unless strategy {@link FetchStrategy#FROM_CACHE_ONLY} is used and the value is not in the cache
//     */
//    Pair<GradleProject, BuildInvocationsContainer> fetchGradleProjectWithBuildInvocationsAndWait(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy);

}
