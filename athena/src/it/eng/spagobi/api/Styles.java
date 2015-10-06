package it.eng.spagobi.api;

import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

@Path("/Styles")
public class Styles {

	private static final String PATH_TO_STYLE = "/chart/style";

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public void metoda() throws Exception {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = dbf.newDocumentBuilder();

		File folder = new File(SpagoBIUtilities.getResourcePath() + PATH_TO_STYLE);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {

				String domstring = folder + "\\" + listOfFiles[i].getName();

				Document dom = db.parse(domstring);

			} else if (listOfFiles[i].isDirectory()) {

			}
		}

	}

}
