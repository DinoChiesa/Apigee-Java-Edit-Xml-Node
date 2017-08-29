# Java Edit Xml Node

This directory contains the Java source code and pom.xml file required to
compile a simple custom policy for Apigee Edge. The policy adds a node to an XML document, or removes a node from a document.

Suppose you have a document like this: 
```xml
 <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"> 
  <soap:Header/> 
  <soap:Body> 
    <act:test xmlns:act="http://yyyy.com"> 
      <abc> 
        <act:demo>fokyCS2jrkE5s+bC25L1Aax5sK....08GXIpwlq3QBJuG7a4Xgm4Vk</act:demo> 
      </abc> 
    </act:test> 
  </soap:Body> 
</soap:Envelope> 
```

And you'd like to replace the text node in the middle of that document with something else, maybe the decrypted version of that string. This policy lets you do that, allowing you to transform the above into this: 

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"> 
  <soap:Header/> 
  <soap:Body> 
    <act:test xmlns:act="http://yyyy.com"> 
      <abc> 
        <act:demo>decrypted-text-here</act:demo> 
      </abc> 
    </act:test> 
  </soap:Body> 
</soap:Envelope>
```

Today, in Apigee Edge, you could do this with an XSLT policy and an XSLT module.
But many people don't want to write or maintain XSLT. 
This policy allows you to accomplish the task with No coding required!

This policy also allows you to remove a single node from an XML document, or to insert a single node into an XML document. 



## Using this policy

You do not need to build the source code in order to use the policy in Apigee Edge. 
All you need is the built JAR, and the appropriate configuration for the policy. 
If you want to build it, feel free.  The instructions are at the bottom of this readme. 


1. copy the jar file, available in  target/edge-custom-edit-xml-node-1.0.4.jar , if you have built the jar, or in [the repo](bundle/apiproxy/resources/java/edge-custom-edit-xml-node-1.0.4.jar) if you have not, to your apiproxy/resources/java directory. You can do this offline, or using the graphical Proxy Editor in the Apigee Edge Admin Portal. 

2. include an XML file for the Java callout policy in your
   apiproxy/resources/policies directory. It should look
   like this:  
   ```xml
    <JavaCallout name='Java-EditXmlNode-1'>
        ...
      <ClassName>com.dinochiesa.edgecallouts.EditXmlNode</ClassName>
      <ResourceURL>java://edge-custom-edit-xml-node-1.0.4.jar</ResourceURL>
    </JavaCallout>
   ```  

3. use the Edge UI, or a command-line tool like [pushapi](https://github.com/carloseberhardt/apiploy) or [apigeetool](https://github.com/apigee/apigeetool-node) or similar to
   import the proxy into an Edge organization, and then deploy the proxy . 
   Eg,    
   ```./pushapi -v -d -o ORGNAME -e test -n edit-xml-node ```

4. Use a client to generate and send http requests to the proxy you just deployed . Eg,
   ```
   curl -i -X POST -H content-type:text/xml \
     'http://ORGNAME-test.apigee.net/edit-xml-node/t1?texttoadd=seven&xpath=/root/a/text()' \
     -d '<root><a>beta</a></root>'
   ```


## Notes on Usage

There is one callout class, com.dinochiesa.edgecallouts.EditXmlNode.

The policy is configured via properties set in the XML.  You can set these properties: 


| property name     | status    | description                               | 
| ----------------- |-----------|-------------------------------------------| 
| action            | Required  | append, insert-before, replace, or remove |
| xpath             | Required  | the xpath to resolve to a single node in the source document. |
| source            | Optional  | the source xml document. This should be the name of a context variable. If you omit this property, the policy will use "message.content" as the source. |
| new-node-type     | Required* | should be one of element, attribute, text. |
| new-node-text     | Required* | Depending on the value of new-node-type, this must take a value that corresponds to an element, attribute, or text node.  For an element, eg, `<foo>bar</foo>`.  For an attribute, do not use any quotes.  Eg, `attr1=value`  Or, for a Text node, any text string. |
| output-variable   | Optional  | the name of a variable to hold the result. If not present, the result is placed into "message.content". |


*The new-node-type and new-node-text are not required if removing a node. When you do use new-node-type, the type of the node to which the `xpath` resolves must match the `new-node-type` you specify in the configuration.  In other words, you can replace a text node with a text node (or append, or insert-before). Or, you can replace an element with an element (or append, or insert-before). You cannot use this policy to replace, for example, an element with a text node. Or to append a text node to an attribute. 

NB: There is no support for namespace-qualified attributes. 


## Example Policy Configurations

### Appending an Element

```xml
<JavaCallout name='Java-InsertXmlNode-1'>
  <Properties>
    <Property name='source'>request.content</Property>
    <Property name='new-node-type'>element</Property>
    <Property name='new-node-text'><Foo>text-value</Foo></Property>
    <Property name='xpath'>{request.queryparam.xpath}</Property>
    <Property name='action'>append</Property>
  </Properties>
  <ClassName>com.dinochiesa.edgecallouts.EditXmlNode</ClassName>
  <ResourceURL>java://edge-custom-edit-xml-node-1.0.4.jar</ResourceURL>
</JavaCallout>
```

### Replacing a text node

```xml
<JavaCallout name='Java-ReplaceXmlNode-1'>
  <Properties>
    <Property name='source'>request.content</Property>
    <Property name='new-node-type'>text</Property>
    <Property name='new-node-text'>{request.queryparam.texttoinsert}</Property>
    <Property name='xpath'>{request.queryparam.xpath}</Property>
    <Property name='action'>replace</Property>
    <Property name='output-variable'>my_variable</Property>
  </Properties>
  <ClassName>com.dinochiesa.edgecallouts.EditXmlNode</ClassName>
  <ResourceURL>java://edge-custom-edit-xml-node-1.0.4.jar</ResourceURL>
</JavaCallout>
```

### Replacing a text node using XML namespaces

Any property name that begins with `xmlns:` is treated as an xml prefix and namespace by the custom policy. The policy can use any of these namespaces for xpath resolution. You must specify a prefix to apply an xpath to a document that uses namespaces. Of course the prefix you use in your xpath need not match the prefix used in the document, according to XML and XPath processing rules. Only the namepace is required to be the same. 

```xml
<JavaCallout name='Java-ReplaceXmlNode-2'>
  <Properties>
    <Property name='xmlns:soap'>http://schemas.xmlsoap.org/soap/envelope/</Property>
    <Property name='xmlns:act'>http://yyyy.com</Property>
    <Property name='source'>request.content</Property>
    <Property name='new-node-type'>text</Property>
    <Property name='new-node-text'>{request.queryparam.texttoinsert}</Property>
    <Property name='xpath'>/soap:Envelope/soap:Body/act:test/abc/act:demo/text()</Property>
    <Property name='action'>replace</Property>
  </Properties>
  <ClassName>com.dinochiesa.edgecallouts.EditXmlNode</ClassName>
  <ResourceURL>java://edge-custom-edit-xml-node-1.0.4.jar</ResourceURL>
</JavaCallout>
```

Applied against a source a document like this: 
```xml
 <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"> 
  <soap:Header/> 
  <soap:Body> 
    <act:test xmlns:act="http://yyyy.com"> 
      <abc> 
        <act:demo>fokyCS2jrkE5s+bC25L1Aax5sK...J7nmd3OwHq/08GXIpwlq3QBJuG7a4Xgm4Vk</act:demo> 
      </abc> 
    </act:test> 
  </soap:Body> 
</soap:Envelope> 
```

The above policy configuration would produce this output

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"> 
  <soap:Header/> 
  <soap:Body> 
    <act:test xmlns:act="http://yyyy.com"> 
      <abc> 
        <act:demo>THE VALUE OF request.queryparam.texttoinsert APPEARS HERE</act:demo> 
      </abc> 
    </act:test> 
  </soap:Body> 
</soap:Envelope>
```


### Removing a SOAP Header

Using the "remove" action, you can also remove a node (which may have children) from an XML document. 

```xml
<JavaCallout name='Java-RemoveSoapHeader'>
  <Properties>
    <Property name='xmlns:soap'>http://schemas.xmlsoap.org/soap/envelope/</Property>
    <Property name='source'>request.content</Property>
    <Property name='xpath'>/soap:Envelope/soap:Header</Property>
    <Property name='action'>remove</Property>
  </Properties>
  <ClassName>com.dinochiesa.edgecallouts.EditXmlNode</ClassName>
  <ResourceURL>java://edge-custom-edit-xml-node-1.0.4.jar</ResourceURL>
</JavaCallout>
```

Applied against a source a document like this: 
```xml
 <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"> 
  <soap:Header>
     <Element1>....</Element1>
     <Element2>....</Element2>
  </soap:Header> 
  <soap:Body> 
    <act:test xmlns:act="http://yyyy.com"> 
      <abc> 
        <act:demo>fokyCS2jrkE5s+bC25L1Aax5sK...J7nmd3OwHq/08GXIpwlq3QBJuG7a4Xgm4Vk</act:demo> 
      </abc> 
    </act:test> 
  </soap:Body> 
</soap:Envelope> 
```

The above policy configuration would produce this output

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"> 
  <soap:Body> 
    <act:test xmlns:act="http://yyyy.com"> 
      <abc> 
        <act:demo>fokyCS2jrkE5s+bC25L1Aax5sK...J7nmd3OwHq/08GXIpwlq3QBJuG7a4Xgm4Vk</act:demo> 
      </abc> 
    </act:test> 
  </soap:Body> 
</soap:Envelope>
```


## Example API Proxy

You can find an example proxy bundle that uses the policy, [here in this repo](bundle/apiproxy).



## Building

Building from source requires Java 1.7, and Maven. 

1. unpack (if you can read this, you've already done that).

2. Before building _the first time_, configure the build on your machine by loading the Apigee jars into your local cache:
  ```
  ./buildsetup.sh
  ```

3. Build with maven.  
  ```
  mvn clean package
  ```
  This will build the jar and also run all the tests.



## Build Dependencies

- Apigee Edge expressions v1.0
- Apigee Edge message-flow v1.0
- codehaus jackson v1.9.7 (needed only for building+running tests)
- testng v6.8.7 (needed only for building+running tests)
- jmockit v1.7 (needed only for building+running tests)


These jars must be available on the classpath for the compile to
succeed. You do not need to worry about these jars if you are not building from source. The buildsetup.sh script will download the Apigee files for
you automatically, and will insert them into your maven cache. The pom file will take care of the other Jars. 

If you want to download them manually: 

The first two jars are
produced by Apigee; contact Apigee support to obtain these jars to allow
the compile, or get them here: 
https://github.com/apigee/api-platform-samples/tree/master/doc-samples/java-cookbook/lib

The others can be downloaded from Maven central. 


## License

This material is copyright 2015,2016 Apigee Corporation, 2017 Google Inc.
and is licensed under the [Apache 2.0 License](LICENSE). This includes the Java code as well as the API Proxy configuration. 

## Bugs

* If you try to insert an XML element that relies on an XML namespace, you must explicitly reference the namespace in the XML element. You cannot rely on the namespace prefix references. 
