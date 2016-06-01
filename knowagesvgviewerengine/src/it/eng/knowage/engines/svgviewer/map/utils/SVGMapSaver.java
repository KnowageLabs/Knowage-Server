package it.eng.knowage.engines.svgviewer.map.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.svg.SVGDocument;

// TODO: Auto-generated Javadoc
/**
 * The Class SVGMapSaver.
 *
 * @author Andrea Gioia
 */
public class SVGMapSaver {

	/** The transformer factory. */
	private static TransformerFactory transformerFactory;

	static {
		transformerFactory = TransformerFactory.newInstance();
	}

	/**
	 * Save map.
	 *
	 * @param doc
	 *            the doc
	 * @param ouputFile
	 *            the ouput file
	 *
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws TransformerException
	 *             the transformer exception
	 */
	public static void saveMap(SVGDocument doc, File ouputFile) throws FileNotFoundException, TransformerException {
		saveMap(doc, new FileOutputStream(ouputFile));
	}

	/**
	 * Save map.
	 *
	 * @param doc
	 *            the doc
	 * @param outputStream
	 *            the output stream
	 *
	 * @throws TransformerException
	 *             the transformer exception
	 */
	public static void saveMap(SVGDocument doc, OutputStream outputStream) throws TransformerException {
		Transformer transformer;
		DOMSource source;
		StreamResult streamResult;

		transformer = transformerFactory.newTransformer();
		source = new DOMSource(doc);
		streamResult = new StreamResult(outputStream);
		transformer.transform(source, streamResult);
	}
}
