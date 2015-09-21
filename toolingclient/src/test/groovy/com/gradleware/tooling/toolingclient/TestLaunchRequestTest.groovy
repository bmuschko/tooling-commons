/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.toolingclient

import com.gradleware.tooling.junit.TestDirectoryProvider
import com.gradleware.tooling.spock.ToolingClientSpecification
import org.gradle.tooling.events.ProgressEvent
import org.gradle.tooling.events.ProgressListener
import org.gradle.tooling.events.test.TestFinishEvent
import org.junit.Rule

class TestLaunchRequestTest extends ToolingClientSpecification {

  @Rule
  TestDirectoryProvider directoryProvider = new TestDirectoryProvider();

  def setup() {
    directoryProvider.createFile('settings.gradle');
    directoryProvider.createFile('build.gradle') <<
    '''apply plugin: 'java'
         repositories { mavenCentral() }
         dependencies { testCompile "junit:junit:4.12" }
    '''
    directoryProvider.createDir('src', 'test', 'java')
    directoryProvider.createFile('src', 'test', 'java', 'TestA.java') <<
    '''import static org.junit.Assert.assertTrue;
         import org.junit.Test;
         public class TestA {
             public @Test void testA1() { assertTrue(true); }
             public @Test void testA2() { assertTrue(true); }
         }
      '''
    directoryProvider.createFile('src', 'test', 'java', 'TestB.java') <<
    '''import static org.junit.Assert.assertTrue;
         import org.junit.Test;
         public class TestB {
             public @Test void testB1() { assertTrue(true); }
             public @Test void testB2() { assertTrue(true); }
         }
    '''
  }

  def "forJvmTestClasses"() {
    setup:
    TestConfig tests = TestConfig.forJvmTestClasses("TestA")
    TestLaunchRequest testLaunchRequest = toolingClient.newTestLaunchRequest(tests)
    testLaunchRequest.projectDir(directoryProvider.testDirectory)
    List finishedTests = []
    testLaunchRequest.typedProgressListeners({ ProgressEvent event ->
      if (event instanceof TestFinishEvent) {
        finishedTests << event.descriptor.name
      }
    } as ProgressListener)

    when:
    testLaunchRequest.executeAndWait()

    then:
    finishedTests.contains("testA1")
    !finishedTests.contains("testB1")
  }

  def "forJvmTestMethods"() {
    setup:
    TestConfig tests = TestConfig.forJvmTestMethods("TestA", "testA1")
    TestLaunchRequest testLaunchRequest = toolingClient.newTestLaunchRequest(tests)
    testLaunchRequest.projectDir(directoryProvider.testDirectory)
    List finishedTests = []
      testLaunchRequest.typedProgressListeners({ ProgressEvent event ->
      if (event instanceof TestFinishEvent) {
        finishedTests << event.descriptor.name
      }
    } as ProgressListener)

    when:
    testLaunchRequest.executeAndWait()

    then:
    finishedTests.contains("testA1")
    !finishedTests.contains("testA2")
    !finishedTests.contains("testB1")
    !finishedTests.contains("testB2")
  }

}
