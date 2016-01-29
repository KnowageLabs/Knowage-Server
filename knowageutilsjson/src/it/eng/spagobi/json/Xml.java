package it.eng.spagobi.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Xml {

	public static String xml2json(String xml) throws TransformerFactoryConfigurationError, TransformerException {

		String json = "{}";

		byte[] bytes = xml.getBytes();
		InputStream inputStream = new ByteArrayInputStream(bytes);

		TransformerFactory factory = TransformerFactory.newInstance();

		InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("it/eng/spagobi/json/xml2Json.xslt");

		StreamSource source = new StreamSource(resourceAsStream);
		Templates template = factory.newTemplates(source);
		Transformer transformer = template.newTransformer();

		OutputStream os = new ByteArrayOutputStream();

		transformer.transform(new StreamSource(inputStream), new StreamResult(os));

		json = os.toString();

		return json;
	}

}
