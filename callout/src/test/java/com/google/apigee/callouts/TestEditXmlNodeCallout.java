// Copyright © 2017-2024 Google LLC.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.google.apigee.callouts;

import com.apigee.flow.execution.ExecutionResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.apigee.fakes.FakeExecutionContext;
import com.google.apigee.fakes.FakeMessage;
import com.google.apigee.fakes.FakeMessageContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestEditXmlNodeCallout {
  private static final String testDataDir = "src/test/resources/test-data";

  FakeMessage message;
  FakeMessageContext msgCtxt;
  FakeExecutionContext exeCtxt;

  @BeforeMethod
  public void beforeMethod(Method method) throws Exception {
    String methodName = method.getName();
    String className = method.getDeclaringClass().getName();
    System.out.printf("\n\n==================================================================\n");
    System.out.printf("TEST %s.%s()\n", className, methodName);

    message = new FakeMessage();
    msgCtxt = new FakeMessageContext(message);
    exeCtxt = new FakeExecutionContext();
  }

  @DataProvider(name = "batch1")
  public static Object[][] getDataForBatch1() throws IOException, IllegalStateException {

    // @DataProvider requires the output to be a Object[][]. The inner
    // Object[] is the set of params that get passed to the test method.
    // So, if you want to pass just one param to the constructor, then
    // each inner Object[] must have length 1.

    ObjectMapper om = new ObjectMapper();
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Path currentRelativePath = Paths.get("");
    // String s = currentRelativePath.toAbsolutePath().toString();
    // System.out.println("Current relative path is: " + s);

    // read in all the *.json files in the test-data directory
    File testDir = new File(testDataDir);
    if (!testDir.exists()) {
      throw new IllegalStateException("no test directory.");
    }
    File[] files = testDir.listFiles();
    Arrays.sort(files);
    if (files.length == 0) {
      throw new IllegalStateException("no tests found.");
    }
    int c = 0;
    ArrayList<TestCase> list = new ArrayList<TestCase>();
    for (File file : files) {
      String name = file.getName();
      if (name.endsWith(".json")) {
        TestCase tc = om.readValue(file, TestCase.class);
        tc.setTestName(name.substring(0, name.length() - 5));
        list.add(tc);
      }
    }

    return list.stream().map(tc -> new TestCase[] {tc}).toArray(Object[][]::new);
  }

  @Test
  public void testDataProviders() throws IOException {
    Assert.assertTrue(getDataForBatch1().length > 0);
  }

  @Test(dataProvider = "batch1")
  public void test2_Configs(TestCase tc) {
    if (tc.getDescription() != null)
      System.out.printf("  %10s - %s\n", tc.getTestName(), tc.getDescription());
    else System.out.printf("  %10s\n", tc.getTestName());

    message.setContent(tc.getInput().get("message-content"));

    EditXmlNode callout = new EditXmlNode(tc.getInput()); // properties

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);
    String actualContent = msgCtxt.getVariable("message.content");

    String s = tc.getExpected().get("success");
    ExecutionResult expectedResult =
        (s != null && s.toLowerCase().equals("true"))
            ? ExecutionResult.SUCCESS
            : ExecutionResult.ABORT;
    // check result and output
    if (expectedResult == actualResult) {
      if (expectedResult == ExecutionResult.SUCCESS) {
        String expectedContent = tc.getExpected().get("message-content");
        expectedContent = expectedContent.replace('\'', '"');
        if (!actualContent.equals(expectedContent)) {
          // System.out.printf("  FAIL - content\n");
          System.err.printf("    got     : %s\n", actualContent);
          System.err.printf("    expected: %s\n", expectedContent);
        }
        Assert.assertEquals(actualContent, expectedContent, "result not as expected");
      } else {
        String expectedError = tc.getExpected().get("error");
        if (expectedError != null) {
          String actualError = msgCtxt.getVariable("editxml_error");
          Assert.assertEquals(actualError, expectedError, "error not as expected");
        }
      }
    } else {
      Assert.assertEquals(actualResult, expectedResult, "result not as expected");
    }
    System.out.println("=========================================================");
  }
}
