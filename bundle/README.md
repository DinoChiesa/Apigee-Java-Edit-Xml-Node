# API Proxy bundle to demonstrate Edit-Xml-Node

This Apigee API Proxy demonstrates the use of the custom Java
policy that Edits XML.  It can be used on private cloud or public
cloud instances of Apigee.  It relies on [the custom Java
policy](../callout) included here.


## Disclaimer

This example is not an official Google product, nor is it part of an
official Google product.


## Example usage

The examples that follow assume Apigee classic SaaS and that the shell variables have been set appropriately:

```
ORG=myorg
ENV=myenv
```

### Insert a node into an existing XML

```
curl -i -H 'content-type: application/xml' \
 -X POST \
 "https://$ORG-$ENV.apigee.net/edit-xml-node/t1-append-node?xpath=/Alpha/Bravo&texttoinsert=<Gamma/>&nodetype=element" \
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
 "https://$ORG-$ENV.apigee.net/edit-xml-node/t2-replace-xml-node?xpath=/Alpha/Bravo/Charlie/text()&texttoinsert=7&nodetype=text" \
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
 "https://$ORG-$ENV.apigee.net/edit-xml-node/t3-remove-xml-node?xpath=/Alpha/Bravo/Charlie" \
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
 "https://$ORG-$ENV.apigee.net/edit-xml-node/t4-remove-soap-header" \
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


### Insert a SOAP header

```
curl -i -H 'content-type: application/xml' \
 -X POST \
 "https://$ORG-$ENV.apigee.net/edit-xml-node/t5-insert-soap-header" \
 -d ' <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <act:test xmlns:act="http://yyyy.com">
      <abc>xyz</abc>
    </act:test>
  </soap:Body>
</soap:Envelope>'
```

Result:

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Header xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
      <wsse:Security soap:mustUnderstand="1">
         <wsse:UsernameToken wsu:Id="UsernameToken-459">
            <wsse:Username>person@example.com</wsse:Username>
            <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">VerySecret123</wsse:Password>
         </wsse:UsernameToken>
      </wsse:Security>
    </soap:Header><soap:Body>
    <act:test xmlns:act="http://yyyy.com">
      <abc>xyz</abc>
    </act:test>
  </soap:Body>
</soap:Envelope>
```

## Notes

Happy XML Editing!
