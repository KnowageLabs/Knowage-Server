/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engines.exporters;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ExecutionProxy;
import it.eng.spagobi.engines.chart.utils.ExportHighCharts;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration;
import it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration.Document;
import it.eng.spagobi.engines.documentcomposition.exporterUtils.CurrentConfigurationDocComp;
import it.eng.spagobi.engines.documentcomposition.exporterUtils.DocumentContainer;
import it.eng.spagobi.engines.documentcomposition.exporterUtils.PdfCreator;
import it.eng.spagobi.utilities.ParametersDecoder;

public class DocumentCompositionExporter {

	private static final Logger LOGGER = Logger.getLogger(DocumentCompositionExporter.class);

	private static final Random RANDOM = new SecureRandom();

	public File exportDocumentCompositionPDF(File tmpFile, DocumentCompositionConfiguration dcConf, BIObject document,
			IEngUserProfile profile, Map<String, CurrentConfigurationDocComp> currentConfs,
			Map<String, DocumentContainer> documentsMap, boolean defaultStyle) throws Exception {

		LOGGER.debug("IN");
		String output = null;
		InputStream svgInputStream = null;
		InputStream pngInputStream = null;
		OutputStream svgOutputStream = null;
		try {
			// Cycle on all documents in document composition
			Map docMap = dcConf.getDocumentsMap();
			for (Iterator iterator = docMap.keySet().iterator(); iterator.hasNext();) {
				Object key = iterator.next();
				Document doc = (Document) docMap.get(key);
				String label = doc.getSbiObjLabel();
				LOGGER.debug("Document " + label);

				// get document container information
				DocumentContainer documentContainer = documentsMap.get(label);

				if (documentContainer == null)
					continue;

				// Recover BIObject
				IBIObjectDAO dao = DAOFactory.getBIObjectDAO();
				BIObject objectID = dao.loadBIObjectByLabel(label);
				BIObject object = null;

				// get roles
				Collection roles = null;
				roles = ((UserProfile) profile).getRolesForUse();

				for (Iterator iterator2 = roles.iterator(); iterator2.hasNext();) {
					Object role = iterator2.next();
					try {
						object = dao.loadBIObjectForExecutionByIdAndRole(objectID.getId(), role.toString());
					} catch (Exception e) {
						LOGGER.error("error in recovering the role");
					}
					if (object != null)
						break;
				}

				// set parameters: from url retrieved by iframe, fill
				// BiObjectParameters with value
				LOGGER.debug("fill parameters from URL");
				fillBIObjectWithParameterValues(object, currentConfs.get(label));

				// only for HIGHCHARTS and EXT charts documents (SVG_label is
				// not null) isn't necessary
				// ri-execute the document but enough to pass the svg and
				// tranform it into png
				byte[] returnByteArray = null;
				if (currentConfs.get("SVG_" + label) != null) {

					Map tmpSvg = currentConfs.get("SVG_" + label).getParameters();
					String tmpContent = tmpSvg.get("SVG_" + label).toString();

					String svg = tmpContent;
					svgInputStream = new ByteArrayInputStream(svg.getBytes(UTF_8));
					File dir = new File(System.getProperty("java.io.tmpdir"));
					int randomInt = RANDOM.nextInt();
					File pdfFile = File.createTempFile(Integer.toString(randomInt), ".pdf", dir);
					svgOutputStream = new FileOutputStream(pdfFile);
					ExportHighCharts.transformSVGIntoPDF(svgInputStream, svgOutputStream);

					// read input from file
					pngInputStream = new FileInputStream(pdfFile);

					// Get the size of the file
					long length = pdfFile.length();

					if (length > Integer.MAX_VALUE) {
						LOGGER.error("file too large");
						return null;
					}

					// Create the byte array to hold the data
					returnByteArray = new byte[(int) length];

					// Read in the bytes
					int offset = 0;
					int numRead = 0;

					while (offset < returnByteArray.length && (numRead = pngInputStream.read(returnByteArray, offset,
							returnByteArray.length - offset)) >= 0) {
						offset += numRead;
					}

					// Ensure all the bytes have been read in
					if (offset < returnByteArray.length) {
						LOGGER.warn("Could not read all the file");
					}

				} else // if (!doc.getType().equals("CHART")) {
				if (true) {
					LOGGER.debug("call execution proxy");

					// Calling execution proxy
					ExecutionProxy proxy = new ExecutionProxy();

					proxy.setBiObject(object);

					// if engine is Birt, export in PDF, elsewhere in JPG
					Engine engine = object.getEngine();
					String driverName = engine.getDriverName();
					if (driverName != null && driverName.endsWith("BirtReportDriver")) {
						output = "PDF";
					} else {
						output = "JPG";
					}
					returnByteArray = proxy.exec(profile, ExecutionProxy.EXPORT_MODALITY, output);
				}
				// add content retrieved to Document Container
				LOGGER.debug("add content retrieved to Document Container");
				if (returnByteArray != null) {
					if (returnByteArray.length == 0)
						LOGGER.warn("empty byte array retrieved for document " + label);
					documentContainer.setContent(returnByteArray);
					documentContainer.setDocumentLabel(label);
					documentContainer.setDocumentType(object.getBiObjectTypeCode());
				}
			}

			try (FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
				PdfCreator pdfCreator = new PdfCreator();
				LOGGER.debug("Call PDF Creation");
				pdfCreator.setVideoHeight(dcConf.getVideoHeight());
				pdfCreator.setVideoWidth(dcConf.getVideoWidth());

				FileOutputStream pdfFile = pdfCreator.createPdfFile(fileOutputStream, documentsMap, defaultStyle);

				pdfFile.flush();
				pdfFile.close();
			}

			LOGGER.debug("OUT");

			return tmpFile;
		} catch (Exception e) {
			LOGGER.error(e);
			return null;
		} finally {
			// Close the input stream
			if (pngInputStream != null) {
				try {
					pngInputStream.close();
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
			if (svgInputStream != null) {
				try {
					svgInputStream.close();
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
			if (svgOutputStream != null) {
				try {
					svgOutputStream.close();
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
		}
	}

	/**
	 * function used to get attribute parsed from i frame URL and insert the values into BIObject
	 *
	 * @param object
	 * @param currentConf
	 */

	public void fillBIObjectWithParameterValues(BIObject object, CurrentConfigurationDocComp currentConf) {

		LOGGER.debug("IN");
		// For each parameter the object needs search for a value in currentConf
		if (currentConf == null)
			return;

		List<BIObjectParameter> parametersBO = object.getDrivers();
		Map<String, Object> currentParameters = currentConf.getParameters();
		if (currentParameters != null) {
			if (parametersBO != null) {
				for (Iterator<BIObjectParameter> iterator = parametersBO.iterator(); iterator.hasNext();) {
					BIObjectParameter parAss = iterator.next();
					String urlName = parAss.getParameterUrlName();
					// get the value if present, otherwise will keep the present
					// one

					Object valueObj = currentParameters.get(urlName);
					if (valueObj instanceof List) { // if it is a list put it
													 // into parAss
						List val = (List) valueObj;
						parAss.setParameterValues(val);
					} else { // else it is a String
						if (valueObj != null) {
							String valueString = valueObj.toString();
							List values = (new ParametersDecoder()).getOriginalValues(valueString);
							if (values != null) {
								LOGGER.debug("Put new values " + valueString + " to parameter " + urlName);
								parAss.setParameterValues(values);
								// remove value
								currentParameters.remove(urlName);
							}
						}
					}

				}
			}

			// Add the values that have not been removed (could be chart
			// particular parameters)

			if (parametersBO == null)
				parametersBO = new ArrayList<>();
			for (Iterator<String> iterator = currentParameters.keySet().iterator(); iterator.hasNext();) {
				String lab = iterator.next();
				BIObjectParameter biObjPar = new BIObjectParameter();
				biObjPar.setParameterUrlName(lab);

				Object valueObj = currentParameters.get(lab);
				if (valueObj instanceof List) { // if it is a list put it into
												 // parAss
					List val = (List) valueObj;
					biObjPar.setParameterValues(val);
				} else {
					if (valueObj != null) {
						String valueString = valueObj.toString();
						List values = (new ParametersDecoder()).getOriginalValues(valueString);
						biObjPar.setParameterValues(values);
					} else {
						continue;
					}
				}
				object.getDrivers().add(biObjPar);
			}

		}
		LOGGER.debug("OUT");
	}

}
