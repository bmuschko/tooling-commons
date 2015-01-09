package com.gradleware.tooling.domain.model;

import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.domain.model.generic.DomainObjectField;

import java.io.File;

/**
 * Represents a Gradle script. A Gradle script may be a build script, settings script, or initialization script.
 *
 * @see org.gradle.tooling.model.gradle.GradleScript
 */
public final class GradleScriptFields {

    /**
     * The source file for this script, or {@code null} if this script has no associated source file. If the value is not null, the given source file will exist.
     */
    public static final DomainObjectField<File, GradleScriptFields> SOURCE_FILE =
            new DomainObjectField<File, GradleScriptFields>(TypeToken.of(File.class), TypeToken.of(GradleScriptFields.class));

    private GradleScriptFields() {
    }

}
