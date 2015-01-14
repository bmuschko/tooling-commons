package com.gradleware.tooling.domain.repository;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.Request;

import java.io.File;
import java.util.List;

/**
 * Container to hold those attributes of a {@link com.gradleware.tooling.toolingclient.Request} that must not change between request invocations if the semantics of how the build is
 * executed must not changed.
 */
public final class FixedRequestAttributes {

    private final File projectDir;
    private final File gradleUserHomeDir;
    private final GradleDistribution gradleDistribution;
    private final File javaHome;
    private final ImmutableList<String> jvmArguments;
    private final ImmutableList<String> arguments;

    public FixedRequestAttributes(File projectDir, File gradleUserHomeDir, GradleDistribution gradleDistribution, File javaHome, List<String> jvmArguments, List<String> arguments) {
        this.projectDir = projectDir;
        this.gradleUserHomeDir = gradleUserHomeDir;
        this.gradleDistribution = gradleDistribution;
        this.javaHome = javaHome;
        this.jvmArguments = ImmutableList.copyOf(jvmArguments);
        this.arguments = ImmutableList.copyOf(arguments);
    }

    @SuppressWarnings("UnusedDeclaration")
    public File getProjectDir() {
        return this.projectDir;
    }

    @SuppressWarnings("UnusedDeclaration")
    public File getGradleUserHomeDir() {
        return this.gradleUserHomeDir;
    }

    @SuppressWarnings("UnusedDeclaration")
    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }

    @SuppressWarnings("UnusedDeclaration")
    public File getJavaHome() {
        return this.javaHome;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImmutableList<String> getJvmArguments() {
        return this.jvmArguments;
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImmutableList<String> getArguments() {
        return this.arguments;
    }

    public void apply(Request<?> request) {
        request.projectDir(this.projectDir);
        request.gradleUserHomeDir(this.gradleUserHomeDir);
        request.gradleDistribution(this.gradleDistribution);
        request.javaHomeDir(this.javaHome);
        request.jvmArguments(this.jvmArguments.toArray(new String[this.jvmArguments.size()]));
        request.arguments(this.arguments.toArray(new String[this.arguments.size()]));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        FixedRequestAttributes that = (FixedRequestAttributes) other;
        return Objects.equal(this.projectDir, that.projectDir) &&
                Objects.equal(this.gradleUserHomeDir, that.gradleUserHomeDir) &&
                Objects.equal(this.gradleDistribution, that.gradleDistribution) &&
                Objects.equal(this.javaHome, that.javaHome) &&
                Objects.equal(this.jvmArguments, that.jvmArguments) &&
                Objects.equal(this.arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                this.projectDir,
                this.gradleUserHomeDir,
                this.gradleDistribution,
                this.javaHome,
                this.jvmArguments,
                this.arguments);
    }

}
