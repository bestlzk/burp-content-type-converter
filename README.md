Content-Type Converter
=========

Burp extension to convert Content-Type

Requirements: Java 8 (Due to issues with one of the libraries it only works on Java 8. I have not had any problems with Burp using Java 8.)

Right-click on a request in an editable message window such as Repeater, Intruder, and the Proxy interceptor

The following convertions are supported:

* Body Parameter to JSON
* JSON to Body Parameter
* Body Parameter to XML
* XML to Body Parameter
* JSON to XML
* XML to JSON
* GET Request Parameter to POST Request JSON
* GET Request Parameter to POST Request XML

### Body Parameter

```http request
POST /test HTTP/1.1
Host: www.example.com
Proxy-Connection: keep-alive
Content-Type: application/x-www-form-urlencoded;charset=UTF-8
Content-Length: 31

parameter1=111&parameters2=test
```

### JSON

```http request
POST /test HTTP/1.1
Host: www.example.com
Proxy-Connection: keep-alive
Content-Length: 41
Content-Type: application/json;charset=UTF-8

{"parameter1":"111","parameters2":"test"}
```

### XML
```http request
POST /test HTTP/1.1
Host: www.example.com
Proxy-Connection: keep-alive
Content-Length: 111
Content-Type: application/xml;charset=UTF-8

<?xml version="1.0" encoding="UTF-8" ?><root><parameter1>111</parameter1><parameters2>test</parameters2></root>
```