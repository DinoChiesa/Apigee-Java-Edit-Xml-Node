# API Proxy bundle to demonstrate Edit-Xml-Node

This Apigee Edge API Proxy demonstrates the use of the custom Java
policy that Edits XML.  It can be used on private cloud or public
cloud instances of Edge.  It relies on [the custom Java
policy](../callout) included here.


## Disclaimer

This example is not an official Google product, nor is it part of an
official Google product.


## Example usage

In all the examples that follow, you should replace the APIHOST with something like

* $ORG-$ENV.apigee.net, if running in the Apigee-managed public cloud
* VHOST_IP:VHOST_PORT, if running in a self-managed cloud


### Insert a node into an existing XML

```
curl -i -H 'content-type: application/xml' \
 -X POST \
 'https://APIHOST/edit-xml-node/t1-append-node?xpath=/Alpha/Bravo&texttoinsert=<Gamma/>&nodetype=element' \
 -d '<Alpha>
 <Bravo>
   <Charlie>123445.09</Charlie>
   <Delta/>
 </Bravo>
 <Hotel>
   <Foxtrot>1776</Foxtrot>
   <Charlie>17</Charlie>
 </Hotel>
</Alpha>'
```

Result:

```xml
<Alpha>
  <Bravo>
    <Charlie>123445.09</Charlie>
    <Delta/>
    <Gamma/>
  </Bravo>
  <Hotel>
    <Foxtrot>1776</Foxtrot>
    <Charlie>17</Charlie>
  </Hotel>
</Alpha>
```

### Replace a node in an existing XML Document

```
curl -i -H 'content-type: application/xml' \
 -X POST \
 'https://APIHOST/edit-xml-node/t2-replace-xml-node?xpath=/Alpha/Bravo/Charlie/text()&texttoinsert=7&nodetype=text' \
 -d '<Alpha>
 <Bravo>
   <Charlie>123445.09</Charlie>
   <Delta/>
 </Bravo>
 <Hotel>
   <Foxtrot>1776</Foxtrot>
   <Charlie>17</Charlie>
 </Hotel>
</Alpha>'
```

Result:

```xml
<Alpha>
  <Bravo>
    <Charlie>7</Charlie>
    <Delta/>
  </Bravo>
  <Hotel>
    <Foxtrot>1776</Foxtrot>
    <Charlie>17</Charlie>
  </Hotel>
</Alpha>
```

### Remove a node specified in Xpath

```
curl -i -H 'content-type: application/xml' \
 -X POST \
 'https://APIHOST/edit-xml-node/t3-remove-xml-node?xpath=/Alpha/Bravo/Charlie' \
 -d '<Alpha>
 <Bravo>
   <Charlie>123445.09</Charlie>
   <Delta/>
 </Bravo>
 <Hotel>
   <Foxtrot>1776</Foxtrot>
   <Charlie>17</Charlie>
 </Hotel>
</Alpha>'
```

Result:

```xml
<Alpha>
  <Bravo>
    <Delta/>
  </Bravo>
  <Hotel>
    <Foxtrot>1776</Foxtrot>
    <Charlie>17</Charlie>
  </Hotel>
</Alpha>
```

### Remove a SOAP header

```
curl -i -H 'content-type: application/xml' \
 -X POST \
 'https://APIHOST/edit-xml-node/t4-remove-soap-header' \
 -d ' <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Header>
    <Element1>abcdefg</Element1>
    <Element2>12989893903</Element2>
  </soap:Header>
  <soap:Body>
    <act:test xmlns:act="http://yyyy.com">
      <abc>
        <act:demo>fokyCS2jrkE5s+bC25L1Aax5sK....08GXIpwlq3QBJuG7a4Xgm4Vk</act:demo>
      </abc>
    </act:test>
  </soap:Body>
</soap:Envelope>'
```

Result:

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <act:test xmlns:act="http://yyyy.com">
      <abc>
        <act:demo>fokyCS2jrkE5s+bC25L1Aax5sK....08GXIpwlq3QBJuG7a4Xgm4Vk</act:demo>
      </abc>
    </act:test>
  </soap:Body>
</soap:Envelope>
```

## Notes

Happy XML Editing!
