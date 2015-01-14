package com.gradleware.tooling.toolingapi;

import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.model.build.BuildEnvironment;

import java.io.File;
import java.net.URISyntaxException;

import static com.gradleware.tooling.toolingapi.ToolingClient.CleanUpStrategy;

public final class ToolingClientSample {

    public static void main(String[] args) throws URISyntaxException {
        final File pathToProjectDir = new File(args[0]);

        ToolingClient client = ToolingClient.newClient();

        try {
            CancellationTokenSource tokenSource = GradleConnector.newCancellationTokenSource();

            ModelRequest<BuildEnvironment> modelRequest = client.newModelRequest(BuildEnvironment.class).
                    projectDir(pathToProjectDir).
                    gradleUserHomeDir(new File("~/.gradle")).
                    gradleDistribution(GradleDistribution.fromBuild()).
                    colorOutput(true).
                    standardOutput(System.out).
                    standardError(System.err).
                    standardInput(System.in).
                    javaHomeDir(new File(".")).
                    jvmArguments("-Xmx64M").
                    tasks(new String[0]).
                    arguments("-q").
                    cancellationToken(tokenSource.token()).
                    progressListeners(new ProgressListener() {
                        @Override
                        public void statusChanged(ProgressEvent event) {
                            System.out.println(event.getDescription());
                        }
                    });
            LongRunningOperationPromise<BuildEnvironment> operation = modelRequest.execute();
            operation.onComplete(new Consumer<BuildEnvironment>() {
                @Override
                public void accept(BuildEnvironment input) {
                    System.out.println("input = " + input);
                }
            }).onFailure(new Consumer<GradleConnectionException>() {
                @Override
                public void accept(GradleConnectionException e) {
                    System.out.println("e = " + e);
                }
            });

            tokenSource.cancel();
        } finally {
            client.stop(CleanUpStrategy.GRACEFULLY);
        }
    }

}
