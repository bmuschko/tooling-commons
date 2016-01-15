package com.gradleware.tooling.toolingmodel.substitution

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.toolingmodel.substitution.internal.SubstitutedEclipseProject
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.eclipse.EclipseProject
import org.junit.Rule
import spock.lang.Specification

import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class GradleCompositeBuilderIntegrationTest extends Specification {

    @Rule
    TestDirectoryProvider directoryProvider = new TestDirectoryProvider()

    def "cannot create composite with no participating projects"() {
        when:
        GradleCompositeBuilder.newComposite().build()

        then:
        Throwable t = thrown(IllegalStateException)
        t.message == "A composite build requires at least one participating project."
    }

    def "cannot request model that is not an interface"() {
        given:
        File project1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(project1, 'commons-lang:commons-lang:2.6')

        when:
        ProjectConnection project1Connection = createProjectConnection(project1)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        gradleCompositeBuild.getModel(String)

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "Cannot fetch a model of type 'java.lang.String' as this type is not an interface."

        cleanup:
        project1Connection?.close()
    }

    def "cannot request model for unknown model"() {
        given:
        File project1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(project1, 'commons-lang:commons-lang:2.6')

        when:
        ProjectConnection project1Connection = createProjectConnection(project1)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        gradleCompositeBuild.getModel(List)

        then:
        Throwable t = thrown(IllegalArgumentException)
        t.message == "The only supported model for a Gradle composite is EclipseWorkspace.class."

        cleanup:
        project1Connection?.close()
    }

    def "can create composite with single participating project"() {
        given:
        File project1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(project1, 'commons-lang:commons-lang:2.6')

        when:
        ProjectConnection project1Connection = createProjectConnection(project1)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 1
        EclipseProject eclipseProject1 = assertProjectInWorkspace(eclipseWorkspace, 'project-1')
        assertExternalDependencies(eclipseProject1, new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))
        assertNoProjectDependencies(eclipseProject1)

        cleanup:
        project1Connection?.close()
    }

    def "can create composite with multiple participating projects"() {
        given:
        File project1 = directoryProvider.createDir('project-1')
        createBuildFileWithDependency(project1, 'commons-lang:commons-lang:2.6')
        File project2 = directoryProvider.createDir('project-2')
        createBuildFileWithDependency(project2, 'log4j:log4j:1.2.17')

        when:
        ProjectConnection project1Connection = createProjectConnection(project1)
        ProjectConnection project2Connection = createProjectConnection(project2)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection, project2Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 2
        EclipseProject eclipseProject1 = assertProjectInWorkspace(eclipseWorkspace, 'project-1')
        assertExternalDependencies(eclipseProject1, new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))
        assertNoProjectDependencies(eclipseProject1)
        EclipseProject eclipseProject2 = assertProjectInWorkspace(eclipseWorkspace, 'project-2')
        assertExternalDependencies(eclipseProject2, new ExternalDependency(group: 'log4j', name: 'log4j', version: '1.2.17'))
        assertNoProjectDependencies(eclipseProject2)

        cleanup:
        project1Connection?.close()
        project2Connection?.close()
    }

    def "can create composite with single participating multi-project build"() {
        given:
        File rootProjectDir = directoryProvider.createDir('multi-project-1')
        createBuildFileWithDependency(new File(rootProjectDir, 'sub-1'), 'commons-lang:commons-lang:2.6')
        createBuildFileWithDependency(new File(rootProjectDir, 'sub-2'), 'log4j:log4j:1.2.17')
        createSettingsFile(rootProjectDir, ['sub-1', 'sub-2'])

        when:
        ProjectConnection project1Connection = createProjectConnection(rootProjectDir)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 3
        assertProjectInWorkspace(eclipseWorkspace, 'multi-project-1')
        EclipseProject eclipseProject1 = assertProjectInWorkspace(eclipseWorkspace, 'sub-1')
        assertExternalDependencies(eclipseProject1, new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))
        assertNoProjectDependencies(eclipseProject1)
        EclipseProject eclipseProject2 = assertProjectInWorkspace(eclipseWorkspace, 'sub-2')
        assertExternalDependencies(eclipseProject2, new ExternalDependency(group: 'log4j', name: 'log4j', version: '1.2.17'))
        assertNoProjectDependencies(eclipseProject2)

        cleanup:
        project1Connection?.close()
    }

    def "can create composite for sub-project in a single participating multi-project build"() {
        given:
        File rootProjectDir = directoryProvider.createDir('multi-project')
        File subProject = new File(rootProjectDir, 'sub')
        File subSubProject = new File(subProject, 'sub-sub')
        createBuildFileWithDependency(subProject, 'commons-lang:commons-lang:2.6')
        createBuildFileWithDependency(subSubProject, 'log4j:log4j:1.2.17')
        createSettingsFile(rootProjectDir, ['sub', 'sub:sub-sub'])

        when:
        ProjectConnection project1Connection = createProjectConnection(subSubProject)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 3
        assertProjectInWorkspace(eclipseWorkspace, 'multi-project')
        EclipseProject eclipseProject1 = assertProjectInWorkspace(eclipseWorkspace, 'sub')
        assertExternalDependencies(eclipseProject1, new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))
        assertNoProjectDependencies(eclipseProject1)
        EclipseProject eclipseProject2 = assertProjectInWorkspace(eclipseWorkspace, 'sub-sub')
        assertExternalDependencies(eclipseProject2, new ExternalDependency(group: 'log4j', name: 'log4j', version: '1.2.17'))
        assertNoProjectDependencies(eclipseProject2)

        cleanup:
        project1Connection?.close()
    }

    def "can create composite with single participating of a deeply nested multi-project build"() {
        given:
        File rootProjectDir = directoryProvider.createDir('multi-project-1')
        File subProject1 = new File(rootProjectDir, 'sub-1')
        File subProject2 = new File(rootProjectDir, 'sub-2')
        File subSubProject1 = new File(subProject1, 'sub-sub-1')
        File subSubProject2 = new File(subProject2, 'sub-sub-2')
        createBuildFileWithDependency(subProject1, 'commons-lang:commons-lang:2.6')
        createBuildFileWithDependency(subProject2, 'log4j:log4j:1.2.17')
        createBuildFileWithDependency(subSubProject1, 'commons-math:commons-math:1.2')
        createBuildFileWithDependency(subSubProject2, 'commons-codec:commons-codec:1.10')
        createSettingsFile(rootProjectDir, ['sub-1', 'sub-2', 'sub-1:sub-sub-1', 'sub-2:sub-sub-2'])

        when:
        ProjectConnection project1Connection = createProjectConnection(rootProjectDir)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 5
        assertProjectInWorkspace(eclipseWorkspace, 'multi-project-1')
        EclipseProject eclipseProject1 = assertProjectInWorkspace(eclipseWorkspace, 'sub-1')
        assertExternalDependencies(eclipseProject1, new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))
        assertNoProjectDependencies(eclipseProject1)
        EclipseProject eclipseProject2 = assertProjectInWorkspace(eclipseWorkspace, 'sub-2')
        assertExternalDependencies(eclipseProject2, new ExternalDependency(group: 'log4j', name: 'log4j', version: '1.2.17'))
        assertNoProjectDependencies(eclipseProject2)
        EclipseProject eclipseProject3 = assertProjectInWorkspace(eclipseWorkspace, 'sub-sub-1')
        assertExternalDependencies(eclipseProject3, new ExternalDependency(group: 'commons-math', name: 'commons-math', version: '1.2'))
        assertNoProjectDependencies(eclipseProject3)
        EclipseProject eclipseProject4 = assertProjectInWorkspace(eclipseWorkspace, 'sub-sub-2')
        assertExternalDependencies(eclipseProject4, new ExternalDependency(group: 'commons-codec', name: 'commons-codec', version: '1.10'))
        assertNoProjectDependencies(eclipseProject4)

        cleanup:
        project1Connection?.close()
    }

    def "can create composite with multiple participating multi-project builds"() {
        given:
        File rootProjectDir1 = directoryProvider.createDir('multi-project-1')
        createBuildFileWithDependency(new File(rootProjectDir1, 'sub-a'), 'commons-lang:commons-lang:2.6')
        createBuildFileWithDependency(new File(rootProjectDir1, 'sub-b'), 'log4j:log4j:1.2.17')
        createSettingsFile(rootProjectDir1, ['sub-a', 'sub-b'])

        File rootProjectDir2 = directoryProvider.createDir('multi-project-2')
        createBuildFileWithDependency(new File(rootProjectDir2, 'sub-1'), 'commons-math:commons-math:1.2')
        createBuildFileWithDependency(new File(rootProjectDir2, 'sub-2'), 'commons-codec:commons-codec:1.10')
        createSettingsFile(rootProjectDir2, ['sub-1', 'sub-2'])

        when:
        ProjectConnection project1Connection = createProjectConnection(rootProjectDir1)
        ProjectConnection project2Connection = createProjectConnection(rootProjectDir2)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection, project2Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 6
        assertProjectInWorkspace(eclipseWorkspace, 'multi-project-1')
        assertProjectInWorkspace(eclipseWorkspace, 'multi-project-2')
        EclipseProject eclipseProject1 = assertProjectInWorkspace(eclipseWorkspace, 'sub-a')
        assertExternalDependencies(eclipseProject1, new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))
        assertNoProjectDependencies(eclipseProject1)
        EclipseProject eclipseProject2 = assertProjectInWorkspace(eclipseWorkspace, 'sub-b')
        assertExternalDependencies(eclipseProject2, new ExternalDependency(group: 'log4j', name: 'log4j', version: '1.2.17'))
        assertNoProjectDependencies(eclipseProject2)
        EclipseProject eclipseProject3 = assertProjectInWorkspace(eclipseWorkspace, 'sub-1')
        assertExternalDependencies(eclipseProject3, new ExternalDependency(group: 'commons-math', name: 'commons-math', version: '1.2'))
        assertNoProjectDependencies(eclipseProject3)
        EclipseProject eclipseProject4 = assertProjectInWorkspace(eclipseWorkspace, 'sub-2')
        assertExternalDependencies(eclipseProject4, new ExternalDependency(group: 'commons-codec', name: 'commons-codec', version: '1.10'))
        assertNoProjectDependencies(eclipseProject4)

        cleanup:
        project1Connection?.close()
        project2Connection?.close()
    }

    def "can create composite with participating projects that have duplicate names"() {
        given:
        File project1 = directoryProvider.createDir('project-1-root/project')
        createBuildFileWithDependency(project1, 'commons-lang:commons-lang:2.6')
        File project2 = directoryProvider.createDir('project-2-root/project')
        createBuildFileWithDependency(project2, 'log4j:log4j:1.2.17')

        when:
        ProjectConnection project1Connection = createProjectConnection(project1)
        ProjectConnection project2Connection = createProjectConnection(project2)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection, project2Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)
        def projects = eclipseWorkspace.openProjects

        then:
        projects.size() == 2
        projects*.name as Set == ['project', 'project2'] as Set

        cleanup:
        project1Connection?.close()
        project2Connection?.close()
    }
    
    def "can create composite with participating projects that have duplicate names in project hierarchy"() {
        given:
        File rootProjectDir1 = directoryProvider.createDir('multi-project-1')
        createBuildFileWithDependency(new File(rootProjectDir1, 'sub-a'), 'commons-lang:commons-lang:2.6')
        createBuildFileWithDependency(new File(rootProjectDir1, 'sub-b'), 'log4j:log4j:1.2.17')
        createSettingsFile(rootProjectDir1, ['sub-a', 'sub-b'])

        File rootProjectDir2 = directoryProvider.createDir('multi-project-2')
        createBuildFileWithDependency(new File(rootProjectDir2, 'sub-a'), 'commons-lang:commons-lang:2.6')
        createBuildFileWithDependency(new File(rootProjectDir2, 'sub-b'), 'log4j:log4j:1.2.17')
        createSettingsFile(rootProjectDir2, ['sub-a', 'sub-b'])

        when:
        ProjectConnection project1Connection = createProjectConnection(rootProjectDir1)
        ProjectConnection project2Connection = createProjectConnection(rootProjectDir2)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection, project2Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)
        def projects = eclipseWorkspace.openProjects

        then:
        projects.size() == 6
        projects*.name as Set == ['multi-project-1', 'multi-project-1-sub-a', 'multi-project-1-sub-b', 'multi-project-2', 'multi-project-2-sub-a', 'multi-project-2-sub-b'] as Set

        cleanup:
        project1Connection?.close()
        project2Connection?.close()
    }

    def "can substitute external dependency with project dependency for composite with two single project builds"() {
        given:
        File repoDir = directoryProvider.createDir('repo')
        File localArtifactDir = directoryProvider.createDir('repo/org/gradle/project1/1.0')
        createJarFile(new File(localArtifactDir, 'project1-1.0.jar'))

        File projectDir1 = directoryProvider.createDir('project1')
        createBuildFileWithDependency(projectDir1, 'commons-lang:commons-lang:2.6')

        File projectDir2 = directoryProvider.createDir('project2')
        File buildFile2 = createBuildFile(projectDir2)
        buildFile2 << javaBuildScript()
        buildFile2 << """
            repositories {
                maven {
                    // Artifact needs to be resolvable
                    url 'file://$repoDir.absolutePath'
                }
            }

            dependencies {
                compile 'org.gradle:project1:1.0'
            }
        """

        when:
        ProjectConnection project1Connection = createProjectConnection(projectDir1)
        ProjectConnection project2Connection = createProjectConnection(projectDir2)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection, project2Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 2
        EclipseProject eclipseProject1 = assertProjectInWorkspace(eclipseWorkspace, 'project1')
        assertExternalDependencies(eclipseProject1, new ExternalDependency(group: 'commons-lang', name: 'commons-lang', version: '2.6'))
        assertNoProjectDependencies(eclipseProject1)
        SubstitutedEclipseProject eclipseProject2 = assertProjectInWorkspace(eclipseWorkspace, 'project2')
        assertExternalDependencies(eclipseProject2, new ExternalDependency(group: 'org.gradle', name: 'project1', version: '1.0'))
        assertNoProjectDependencies(eclipseProject2)
        eclipseProject2.substitutedExternalDependencies.size() == 1
        assert eclipseProject2.substitutedExternalDependencies.find {
            it.gradleModuleVersion.group == 'org.gradle' && it.gradleModuleVersion.name == 'project1' && it.gradleModuleVersion.version == '1.0'
        }
        eclipseProject2.substitutedProjectDependencies.size() == 1
        assert eclipseProject2.substitutedProjectDependencies.find {
            it.path == ':project1'
        }
    }

    def "can substitute external dependency with project dependency for composite with deeply nested multi-project build"() {
        given:
        File projectDir1 = directoryProvider.createDir('project1')
        File subProject1 = new File(projectDir1, 'sub1')
        File subProject2 = new File(projectDir1, 'sub2')
        File subSubProject1 = new File(subProject1, 'subsub1')
        File subSubProject2 = new File(subProject2, 'subsub2')
        createBuildFileWithDependency(subProject1, 'commons-lang:commons-lang:2.6')
        createBuildFileWithDependency(subProject2, 'log4j:log4j:1.2.17')
        createBuildFileWithDependency(subSubProject1, 'commons-math:commons-math:1.2')
        createBuildFileWithDependency(subSubProject2, 'commons-codec:commons-codec:1.10')
        createSettingsFile(projectDir1, ['sub1', 'sub2', 'sub1:subsub1', 'sub2:subsub2'])

        File repoDir = directoryProvider.createDir('repo')
        File localArtifactDir = directoryProvider.createDir('repo/org/gradle/subsub2/1.0')
        createJarFile(new File(localArtifactDir, 'subsub2-1.0.jar'))

        File projectDir2 = directoryProvider.createDir('project2')
        File buildFile2 = createBuildFile(projectDir2)
        buildFile2 << javaBuildScript()
        buildFile2 << """
            repositories {
                maven {
                    // Artifact needs to be resolvable
                    url 'file://$repoDir.absolutePath'
                }
            }

            dependencies {
                compile 'org.gradle:subsub2:1.0'
            }
        """

        when:
        ProjectConnection project1Connection = createProjectConnection(projectDir1)
        ProjectConnection project2Connection = createProjectConnection(projectDir2)
        GradleCompositeBuild gradleCompositeBuild = createCompositeBuild(project1Connection, project2Connection)
        EclipseWorkspace eclipseWorkspace = gradleCompositeBuild.getModel(EclipseWorkspace)

        then:
        eclipseWorkspace.openProjects.size() == 6
        EclipseProject eclipseProject1 = assertProjectInWorkspace(eclipseWorkspace, 'subsub2')
        assertExternalDependencies(eclipseProject1, new ExternalDependency(group: 'commons-codec', name: 'commons-codec', version: '1.10'))
        assertNoProjectDependencies(eclipseProject1)
        SubstitutedEclipseProject eclipseProject2 = assertProjectInWorkspace(eclipseWorkspace, 'project2')
        assertExternalDependencies(eclipseProject2, new ExternalDependency(group: 'org.gradle', name: 'subsub2', version: '1.0'))
        assertNoProjectDependencies(eclipseProject2)
        eclipseProject2.substitutedExternalDependencies.size() == 1
        assert eclipseProject2.substitutedExternalDependencies.find {
            it.gradleModuleVersion.group == 'org.gradle' && it.gradleModuleVersion.name == 'subsub2' && it.gradleModuleVersion.version == '1.0'
        }
        eclipseProject2.substitutedProjectDependencies.size() == 1
        assert eclipseProject2.substitutedProjectDependencies.find {
            it.path == ':subsub2'
        }
    }

    private ProjectConnection createProjectConnection(File projectDir) {
        GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
    }

    private GradleCompositeBuild createCompositeBuild(ProjectConnection... participants) {
        GradleCompositeBuilder compositeBuilder = GradleCompositeBuilder.newComposite()

        participants.each {
            compositeBuilder.withParticipant(it)
        }

        compositeBuilder.build()
    }

    private File createBuildFileWithDependency(File projectDir, String coordinates) {
        File buildFile = createBuildFile(projectDir)
        buildFile << javaBuildScript()
        buildFile << """
            dependencies {
                compile '$coordinates'
            }
        """
        buildFile
    }

    private File createBuildFile(File projectDir) {
        createDir(projectDir)
        File buildFile = new File(projectDir, 'build.gradle')
        createFile(buildFile)
        buildFile
    }

    private String javaBuildScript() {
        """
            apply plugin: 'java'

            repositories {
                mavenCentral()
            }
        """
    }

    private File createSettingsFile(File projectDir, List<String> projectPaths) {
        File settingsFile = new File(projectDir, 'settings.gradle')
        createFile(settingsFile)
        String includes = projectPaths.collect { "'$it'" }.join(', ')
        settingsFile << "include $includes"
        settingsFile
    }

    private void createDir(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Failed to create directory $dir")
        }
    }

    private void createFile(File file) {
        if (!file.exists() && !file.createNewFile()) {
            throw new IllegalStateException("Failed to create file $file")
        }
    }

    private EclipseProject assertProjectInWorkspace(EclipseWorkspace eclipseWorkspace, String projectName) {
        EclipseProject eclipseProject = eclipseWorkspace.openProjects.find { it.name == projectName }
        assert eclipseProject
        eclipseProject
    }

    private void assertExternalDependencies(EclipseProject eclipseProject, ExternalDependency... externalDependencies) {
        assert eclipseProject.classpath.size() == externalDependencies.size()

        externalDependencies.each { externalDependency ->
            assert eclipseProject.classpath.collect { it.gradleModuleVersion }.find {
                it.group == externalDependency.group && it.name == externalDependency.name && it.version == externalDependency.version
            }
        }
    }

    private void assertNoProjectDependencies(EclipseProject eclipseProject) {
        assert eclipseProject.projectDependencies.size() == 0
    }

    private void createJarFile(File jarFile) {
        Manifest manifest = new Manifest()
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, '1.0')
        JarOutputStream target = new JarOutputStream(new FileOutputStream(jarFile), manifest)
        target.close()
    }

    private static class ExternalDependency {
        String group
        String name
        String version
    }
}
