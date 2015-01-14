package com.gradleware.tooling.toolingclient;

import com.gradleware.tooling.toolingclient.internal.DefaultToolingClient;
import org.gradle.internal.Factory;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.GradleConnector;

/**
 * Entry class to interact with the Tooling API. All interactions happen by creating and invoking requests of type {@link ModelRequest}. The tooling client takes care of the
 * house-keeping of all issued requests and the long-living resources potentially associated with these requests. Once the interactions with Gradle are over, the tooling client
 * must be stopped to clean up all remaining resources.
 * <p>
 * A tooling client instance is thread-safe. Typically, a single tooling client instance is used for the entire life-time of the consumer interacting with the tooling client.
 *
 * @since 2.3
 */
public abstract class ToolingClient {

    /**
     * Creates a new instance. Typically, a single tooling client instance is used for the entire life-time of the consumer interacting with the tooling client.
     *
     * @return a new instance
     * @since 2.3
     */
    public static ToolingClient newClient() {
        return new DefaultToolingClient();
    }

    /**
     * Creates a new instance and uses the given factory whenever a new connector is required by the tooling client. Typically, a single tooling client instance is used for the
     * entire life-time of the consumer interacting with the tooling client.
     *
     * @param connectorFactory the connector factory
     * @return a new instance
     * @since 2.3
     */
    public static ToolingClient newClient(Factory<GradleConnector> connectorFactory) {
        return new DefaultToolingClient(connectorFactory);
    }

    /**
     * Creates a new model request. A model request is used to fetch a given model that is available through the Tooling API.
     *
     * @param modelType the type of the model to fetch through the Tooling API
     * @param <T> the type of the model to fetch
     * @return a new instance
     * @since 2.3
     */
    public abstract <T> ModelRequest<T> newModelRequest(Class<T> modelType);

    /**
     * Creates a new build action request. A build action request is used to run a given action in the build process. The build action and its result are serialized through the
     * Tooling API.
     *
     * @param buildAction the build action to run
     * @param <T> the result type of running the build action
     * @return a new instance
     * @since 2.3
     */
    public abstract <T> BuildActionRequest<T> newBuildActionRequest(BuildAction<T> buildAction);

    /**
     * Creates a new build launch request. A build launch request is used to execute a Gradle build. If an empty set of launchables is specified, the project's default tasks are
     * executed. The build is executed through the Tooling API.
     *
     * @param launchables the launchables to execute
     * @return a new instance
     * @since 2.3
     */
    public abstract BuildLaunchRequest newBuildLaunchRequest(LaunchableConfig launchables);

    /**
     * Stops the tooling client and applies the specified clean-up strategy to any associated resources and processes. May block or may not block, depending on the specified
     * cleanup strategy.
     *
     * @param strategy the clean-up strategy to apply
     * @since 2.3
     */
    public abstract void stop(CleanUpStrategy strategy);

    /**
     * Enumerates the different clean-up strategies.
     *
     * @since 2.3
     */
    public enum CleanUpStrategy {

        /**
         * Clean up all resources and forcefully shut down any associated running processes.
         *
         * @since 2.3
         */
        FORCEFULLY,

        /**
         * Clean up all resources and gracefully shut down any associated running processes.
         *
         * @since 2.3
         */
        GRACEFULLY

    }

}
