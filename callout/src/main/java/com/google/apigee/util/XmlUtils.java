// Copyright 2017-2021 Google LLC.
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtils {

  private static DocumentBuilder getBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

    DocumentBuilder builder = factory.newDocumentBuilder();
    // prevent DTD entities from being resolved.
    builder.setEntityResolver(
        new EntityResolver() {
          @Override
          public InputSource resolveEntity(String publicId, String systemId)
              throws SAXException, IOException {
            return new InputSource(new StringReader(""));
          }
        });

    return builder;
  }

  public static Document parseXml(InputStream in)
      throws IOException, SAXException, ParserConfigurationException {
    DocumentBuilder builder = getBuilder();
    InputStream bin = new BufferedInputStream(in);
    Document ret = builder.parse(new InputSource(bin));
    return ret;
  }

  public static Document parseXml(String s)
      throws IOException, SAXException, ParserConfigurationException {
    DocumentBuilder builder = getBuilder();
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(s));
    Document ret = builder.parse(is);
    return ret;
  }

  public static Document parseXml(String fragment, Map<String, String> namespaces)
      throws IOException, SAXException, ParserConfigurationException {
    DocumentBuilder builder = getBuilder();
    InputSource is = new InputSource();
    if (!namespaces.isEmpty()) {
      // prepend the namespace decls to the toplevel element
      Pattern firstWhitespacePattern = Pattern.compile("^(\\s*)(<[\\w:_0-9]+)(\\s|>)");
      Matcher matcher = firstWhitespacePattern.matcher(fragment);
      if (matcher.find()) {
        StringBuffer sb = new StringBuffer();
        matcher.appendReplacement(sb, "");
        sb.append(matcher.group(1));
        sb.append(matcher.group(2));
        for (Map.Entry<String, String> entry : namespaces.entrySet()) {
          String key = entry.getKey();
          String value = entry.getValue();
          sb.append(" xmlns:").append(key).append("='").append(value).append("' ");
        }
        sb.append(matcher.group(3));
        matcher.appendTail(sb);
        fragment = sb.toString();
      }
    }
    is.setCharacterStream(new StringReader(fragment));
    Document ret = builder.parse(is);
    return ret;
  }

  public static String toString(Document doc) throws TransformerException {
    return XmlUtils.toString(doc, false);
  }

  public static String toString(Document doc, boolean pretty) throws TransformerException {
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    if (pretty) transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(domSource, result);
    return writer.toString();
  }
}
