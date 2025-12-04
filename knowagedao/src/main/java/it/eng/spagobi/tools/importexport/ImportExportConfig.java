package it.eng.spagobi.tools.importexport;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ImportExportConfig {

	private static ImportExportConfig instance;
	private String exporterFolder;
	private String exporterClass;
	private String importerFolder;
	private String importerClass;
	private String importFileMaxSize;
	private String repoAss;
	private List<TransformerSpec> transformers;

	// Nome del file .properties (puoi cambiarlo)
	private static final String CONFIG_FILE = "conf/importexport.xml";

	// Costruttore privato per Singleton
	private ImportExportConfig() {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// Disable external entity references
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			// Disable DTDs entirely
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			// Enable secure processing
			factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
			// Prevent external entity resolution
			factory.setExpandEntityReferences(false);

			Document doc = factory.newDocumentBuilder().parse(input);
			doc.getDocumentElement().normalize();

			XPath xpath = XPathFactory.newInstance().newXPath();
			Element configElementExporter = (Element) xpath.evaluate("/IMPORTEXPORT/EXPORTER", doc, XPathConstants.NODE);
			exporterFolder = configElementExporter.getAttribute("exportFolder");
			exporterClass = configElementExporter.getAttribute("class");

			Element configElementImporter = (Element) xpath.evaluate("/IMPORTEXPORT/IMPORTER", doc, XPathConstants.NODE);
			importerFolder = configElementImporter.getAttribute("tmpFolder");
			importerClass = configElementImporter.getAttribute("class");

			importFileMaxSize = (String) xpath.evaluate("/IMPORTEXPORT/IMPORT_FILE_MAX_SIZE", doc, XPathConstants.STRING);

			Element configElementRepoAss = (Element) xpath.evaluate("/IMPORTEXPORT/ASSOCIATIONS_REPOSITORY", doc, XPathConstants.NODE);
			repoAss = configElementRepoAss.getAttribute("path");

			NodeList nodes = (NodeList) xpath.evaluate("/IMPORTEXPORT/TRANSFORMERS/TRANSFORM", doc, XPathConstants.NODESET);

			transformers = new ArrayList<>();

			for (int i = 0; i < nodes.getLength(); i++) {
				Element el = (Element) nodes.item(i);

				String from = el.getAttribute("from");
				String to = el.getAttribute("to");
				String cls = el.getAttribute("class");

				transformers.add(new TransformerSpec(from.trim(), to.trim(), cls == null ? "" : cls.trim()));
			}

		} catch (Exception e) {
			throw new RuntimeException("Errore nel caricamento del file di configurazione", e);
		}
	}

	// Metodo per ottenere l'istanza Singleton
	public static synchronized ImportExportConfig getInstance() {
		if (instance == null) {
			instance = new ImportExportConfig();
		}
		return instance;
	}

	public String getExporterFolder() {
		return exporterFolder;
	}

	public String getExporterClass() {
		return exporterClass;
	}

	public String getImporterFolder() {
		return importerFolder;
	}

	public String getImporterClass() {
		return importerClass;
	}

	public String getImportFileMaxSize() {
		return importFileMaxSize;
	}

	public String getRepoAss() {
		return repoAss;
	}

	public List<TransformerSpec> getTransformers() {
		return transformers;
	}

}
