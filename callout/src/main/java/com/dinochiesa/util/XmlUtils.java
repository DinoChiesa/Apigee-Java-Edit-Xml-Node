package com.dinochiesa.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

public class XmlUtils {

    private static DocumentBuilder getBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder();
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
        if (pretty)
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(domSource, result);
        return writer.toString();
    }
}