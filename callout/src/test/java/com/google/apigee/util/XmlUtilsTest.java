// Copyright 2020-2021 Google LLC.
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

package com.google.apigee.util;

import java.util.HashMap;
import java.util.Map;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

public class XmlUtilsTest {

  @Test
  public void testMultipleNamespaces() throws Exception {
    String xmlFragment =
        ""
            + "  <soap:Header>\n"
            + "    <wsse:Security soap:mustUnderstand='1'>\n"
            + "       <wsse:UsernameToken wsu:Id='UsernameToken-459'>\n"
            + "          <wsse:Username>username</wsse:Username>\n"
            + "          <wsse:Password Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText'>password</wsse:Password>\n"
            + "       </wsse:UsernameToken>\n"
            + "    </wsse:Security>\n"
            + " </soap:Header>";

    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
    namespaces.put(
        "wsse",
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
    namespaces.put(
        "wsu",
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

    Document doc = XmlUtils.parseXml(xmlFragment, namespaces);
    String s = XmlUtils.toString(doc, true);
    // System.out.printf("xformed: %s\n", s);
  }

  @Test
  public void testSingleNamespace() throws Exception {
    String xmlFragment =
        ""
            + "  <soap:Header>\n"
            + "    <wsse:Security soap:mustUnderstand='1' xmlns:wsse='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd'>\n"
            + "       <wsse:UsernameToken wsu:Id='UsernameToken-459' xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'>\n"
            + "          <wsse:Username>username</wsse:Username>\n"
            + "          <wsse:Password Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText'>password</wsse:Password>\n"
            + "       </wsse:UsernameToken>\n"
            + "    </wsse:Security>\n"
            + " </soap:Header>";

    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");

    Document doc = XmlUtils.parseXml(xmlFragment, namespaces);
    String s = XmlUtils.toString(doc, true);
    // System.out.printf("xformed: %s\n", s);
  }
}
