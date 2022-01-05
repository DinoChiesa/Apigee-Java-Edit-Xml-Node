// EditXmlNode.java
//
// This is the source code for a Java callout for Apigee.
// This callout adds a node into a XML document, or edits a node that is
// already in a document.
//
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
// ------------------------------------------------------------------

package com.google.apigee.callouts;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.util.VariableRefResolver;
import com.google.apigee.util.XPathEvaluator;
import com.google.apigee.util.XmlUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EditXmlNode implements Execution {
  private static final String _varPrefix = "editxml_";
  private static final String DEFAULT_SOURCE_AND_DEST = "message.content";

  private enum EditAction {
    InsertBefore,
    Append,
    Replace,
    Remove
  }

  private Map properties; // read-only

  public EditXmlNode(Map properties) {
    this.properties = properties;
  }

  private static final String varName(String s) {
    return _varPrefix + s;
  }

  private String getSource(MessageContext msgCtxt) throws Exception {
    String source = getSimpleOptionalProperty("source", msgCtxt);
    return (source == null) ? DEFAULT_SOURCE_AND_DEST : source;
  }

  private Document getDocument(MessageContext msgCtxt) throws Exception {
    String source = getSource(msgCtxt);
    if (source == DEFAULT_SOURCE_AND_DEST) {
      return XmlUtils.parseXml(msgCtxt.getMessage().getContentAsStream());
    }
    String text = (String) msgCtxt.getVariable(source);
    if (text == null) {
      throw new IllegalStateException(
          String.format("source property (%s) resolves to null", source));
    }
    return XmlUtils.parseXml(text);
  }

  private String getOutputVar(MessageContext msgCtxt) throws Exception {
    String dest = getSimpleOptionalProperty("output-variable", msgCtxt);
    if (dest == null) {
      String source = getSource(msgCtxt);
      return (source == null) ? DEFAULT_SOURCE_AND_DEST : source;
    }
    return dest;
  }

  private String getXpath(MessageContext msgCtxt) throws Exception {
    return getSimpleRequiredProperty("xpath", msgCtxt);
  }

  private boolean getDebug() {
    String value = (String) this.properties.get("debug");
    if (value == null) return false;
    if (value.trim().toLowerCase().equals("true")) return true;
    return false;
  }

  private boolean getPretty(MessageContext msgCtxt) throws Exception {
    String pretty = getSimpleOptionalProperty("pretty", msgCtxt);
    if (pretty == null) return false;
    pretty = pretty.toLowerCase();
    return pretty.equals("true");
  }

  private String getNewNodeText(MessageContext msgCtxt) throws Exception {
    String n = getSimpleRequiredProperty("new-node-text", msgCtxt);
    return n;
  }

  private short getNewNodeType(MessageContext msgCtxt) throws Exception {
    String nodetype = getSimpleRequiredProperty("new-node-type", msgCtxt);
    nodetype = nodetype.toLowerCase();
    if (nodetype.equals("element")) return Node.ELEMENT_NODE;
    if (nodetype.equals("attribute")) return Node.ATTRIBUTE_NODE;
    if (nodetype.equals("text")) return Node.TEXT_NODE;
    throw new IllegalStateException("new-node-type value is unknown: (" + nodetype + ")");
  }

  private EditAction getAction(MessageContext msgCtxt) throws Exception {
    String action = getSimpleRequiredProperty("action", msgCtxt);
    action = action.toLowerCase();
    if (action.equals("insert-before")) return EditAction.InsertBefore;
    if (action.equals("append")) return EditAction.Append;
    if (action.equals("replace")) return EditAction.Replace;
    if (action.equals("remove")) return EditAction.Remove;
    throw new IllegalStateException("action value is unknown: (" + action + ")");
  }

  private String getSimpleRequiredProperty(String propName, MessageContext msgCtxt)
      throws Exception {
    String value = (String) this.properties.get(propName);
    if (value == null) {
      throw new IllegalStateException(propName + " resolves to an empty string.");
    }
    value = value.trim();
    if (value.equals("")) {
      throw new IllegalStateException(propName + " resolves to an empty string.");
    }
    value = VariableRefResolver.resolve(value, name -> (String) (msgCtxt.getVariable(name)));
    if (value == null || value.equals("")) {
      throw new IllegalStateException(propName + " resolves to an empty string.");
    }
    return value;
  }

  private String getSimpleOptionalProperty(String propName, MessageContext msgCtxt)
      throws Exception {
    String value = (String) this.properties.get(propName);
    if (value == null) {
      return null;
    }
    value = value.trim();
    if (value.equals("")) {
      return null;
    }
    value = VariableRefResolver.resolve(value, name -> (String) (msgCtxt.getVariable(name)));
    if (value == null || value.equals("")) {
      return null;
    }
    return value;
  }

  private Map<String, String> getNamespaces(MessageContext msgCtxt) throws Exception {
    Map<String, String> namespaces = new HashMap<String, String>();
    Function<String, String> r = name -> (String) (msgCtxt.getVariable(name));
    for (Object key : properties.keySet()) {
      String k = (String) key;
      if (k.startsWith("xmlns:")) {
        String[] parts = k.split(":", 2);
        namespaces.put(parts[1], VariableRefResolver.resolve((String) properties.get(k), r));
      }
    }
    return namespaces;
  }

  private void validate(NodeList nodes) throws IllegalStateException {
    int length = nodes.getLength();
    if (length != 1) {
      throw new IllegalStateException(
          "xpath does not resolve to one node. (length=" + length + ")");
    }
  }

  private void insertBefore(NodeList nodes, Node newNode, short newNodeType) {
    Node currentNode = nodes.item(0);
    switch (newNodeType) {
      case Node.ATTRIBUTE_NODE:
        Element parent = ((Attr) currentNode).getOwnerElement();
        parent.setAttributeNode((Attr) newNode);
        break;
      case Node.ELEMENT_NODE:
        currentNode.getParentNode().insertBefore(newNode, currentNode);
        break;
      case Node.TEXT_NODE:
        String v = currentNode.getNodeValue();
        currentNode.setNodeValue(newNode.getNodeValue() + v);
        break;
    }
  }

  private void append(NodeList nodes, Node newNode, short newNodeType) {
    Node currentNode = nodes.item(0);
    switch (newNodeType) {
      case Node.ATTRIBUTE_NODE:
        Element parent = ((Attr) currentNode).getOwnerElement();
        parent.setAttributeNode((Attr) newNode);
        break;
      case Node.ELEMENT_NODE:
        currentNode.appendChild(newNode);
        break;
      case Node.TEXT_NODE:
        if (currentNode.getNodeType() != Node.TEXT_NODE) {
          throw new IllegalStateException("wrong source node type.");
        }
        String v = currentNode.getNodeValue();
        currentNode.setNodeValue(v + newNode.getNodeValue());
        break;
    }
  }

  private void replace(NodeList nodes, Node newNode, short newNodeType) {
    Node currentNode = nodes.item(0);
    switch (newNodeType) {
      case Node.ATTRIBUTE_NODE:
        Element parent = ((Attr) currentNode).getOwnerElement();
        parent.removeAttributeNode((Attr) currentNode);
        parent.setAttributeNode((Attr) newNode);
        break;
      case Node.ELEMENT_NODE:
        currentNode.getParentNode().replaceChild(newNode, currentNode);
        break;
      case Node.TEXT_NODE:
        currentNode.setNodeValue(newNode.getNodeValue());
        break;
    }
  }

  private void remove(NodeList nodes) {
    Node currentNode = nodes.item(0);
    // delete adjacent empty text node if it is empty/whitespace
    Node prevSibling = currentNode.getPreviousSibling();
    if (prevSibling != null
        && prevSibling.getNodeType() == Node.TEXT_NODE
        && prevSibling.getNodeValue().trim().isEmpty()) {
      currentNode.getParentNode().removeChild(prevSibling);
    }
    currentNode.getParentNode().removeChild(currentNode);
  }

  private void execute0(Document document, MessageContext msgCtxt) throws Exception {
    String xpath = getXpath(msgCtxt);
    Map<String, String> namespaces = getNamespaces(msgCtxt);
    XPathEvaluator xpe = new XPathEvaluator(namespaces);
    NodeList nodes = (NodeList) xpe.evaluate(xpath, document, XPathConstants.NODESET);
    validate(nodes);
    EditAction action = getAction(msgCtxt);
    if (action == EditAction.Remove) {
      remove(nodes);
      return;
    }

    short newNodeType = getNewNodeType(msgCtxt);
    String text = getNewNodeText(msgCtxt);
    Node newNode = null;
    switch (newNodeType) {
      case Node.ELEMENT_NODE:
        // Create a duplicate node and transfer ownership of the
        // new node into the destination document.
        Document temp = XmlUtils.parseXml(text, namespaces);
        newNode = document.importNode(temp.getDocumentElement(), true);
        break;
      case Node.ATTRIBUTE_NODE:
        if (text.indexOf("=") < 1) {
          throw new IllegalStateException("attribute spec must be name=value");
        }
        String[] parts = text.split("=", 2);
        if (parts.length != 2) throw new IllegalStateException("attribute spec must be name=value");
        Attr attr = document.createAttribute(parts[0]);
        attr.setValue(parts[1]);
        newNode = attr;
        break;
      case Node.TEXT_NODE:
        newNode = document.createTextNode(text);
        break;
    }
    switch (action) {
      case InsertBefore:
        insertBefore(nodes, newNode, newNodeType);
        break;
      case Append:
        append(nodes, newNode, newNodeType);
        break;
      case Replace:
        replace(nodes, newNode, newNodeType);
        break;
    }
  }

  protected static String getStackTraceAsString(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

  public ExecutionResult execute(final MessageContext msgCtxt, final ExecutionContext execContext) {
    try {
      // Message msg = msgCtxt.getMessage();
      Document document = getDocument(msgCtxt);
      execute0(document, msgCtxt);
      String result = XmlUtils.toString(document, getPretty(msgCtxt));
      String outputVar = getOutputVar(msgCtxt);
      msgCtxt.setVariable(outputVar, result);
    } catch (Exception e) {
      if (getDebug()) {
        System.out.println(getStackTraceAsString(e));
      }
      String error = e.toString();
      msgCtxt.setVariable(varName("exception"), error);
      int ch = error.lastIndexOf(':');
      if (ch >= 0) {
        msgCtxt.setVariable(varName("error"), error.substring(ch + 2).trim());
      } else {
        msgCtxt.setVariable(varName("error"), error);
      }
      msgCtxt.setVariable(varName("stacktrace"), getStackTraceAsString(e));
      return ExecutionResult.ABORT;
    }

    return ExecutionResult.SUCCESS;
  }
}
