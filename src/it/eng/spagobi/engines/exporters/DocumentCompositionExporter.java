/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.exporters;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

public class DocumentCompositionExporter {

	private static transient Logger logger = Logger.getLogger(DocumentCompositionExporter.class);

	public File exportDocumentCompositionPDF(File tmpFile, DocumentCompositionConfiguration dcConf, BIObject document, IEngUserProfile profile,
			Map<String, CurrentConfigurationDocComp> currentConfs, Map<String, DocumentContainer> documentsMap, boolean defaultStyle) throws Exception {

		logger.debug("IN");
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
				logger.debug("Document " + label);

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
						logger.error("error in recovering the role");
					}
					if (object != null)
						break;
				}

				// set parameters: from url retrieved by iframe, fill
				// BiObjectParameters with value
				logger.debug("fill parameters from URL");
				fillBIObjectWithParameterValues(object, currentConfs.get(label));

				// only for HIGHCHARTS and EXT charts documents (SVG_label is
				// not null) isn't necessary
				// ri-execute the document but enough to pass the svg and
				// tranform it into png
				byte[] returnByteArray = null;
				if (currentConfs.get("SVG_" + label) != null) {

					Map tmpSvg = currentConfs.get("SVG_" + label).getParameters();
					String tmpContent = tmpSvg.get("SVG_" + label).toString();

					/*
					 * //create the png file using the svg String svg = tmpContent; svgInputStream = new ByteArrayInputStream(svg.getBytes("UTF-8")); File dir =
					 * new File(System.getProperty("java.io.tmpdir")); Random generator = new Random(); int randomInt = generator.nextInt(); File imgFile =
					 * File.createTempFile(Integer .valueOf(randomInt).toString(), ".png", dir); svgOutputStream = new FileOutputStream(imgFile);
					 * ExportHighCharts.transformSVGIntoPNG(svgInputStream, svgOutputStream); // read input from file pngInputStream = new
					 * FileInputStream(imgFile);
					 */
					String svg = tmpContent;
					svgInputStream = new ByteArrayInputStream(svg.getBytes("UTF-8"));
					File dir = new File(System.getProperty("java.io.tmpdir"));
					Random generator = new Random();
					int randomInt = generator.nextInt();
					File pdfFile = File.createTempFile(Integer.valueOf(randomInt).toString(), ".pdf", dir);
					svgOutputStream = new FileOutputStream(pdfFile);
					ExportHighCharts.transformSVGIntoPDF(svgInputStream, svgOutputStream);

					// read input from file
					pngInputStream = new FileInputStream(pdfFile);

					// Get the size of the file
					long length = pdfFile.length();

					if (length > Integer.MAX_VALUE) {
						logger.error("file too large");
						return null;
					}

					// Create the byte array to hold the data
					returnByteArray = new byte[(int) length];

					// Read in the bytes
					int offset = 0;
					int numRead = 0;

					while (offset < returnByteArray.length && (numRead = pngInputStream.read(returnByteArray, offset, returnByteArray.length - offset)) >= 0) {
						offset += numRead;
					}

					// Ensure all the bytes have been read in
					if (offset < returnByteArray.length) {
						logger.warn("Could not read all the file");
					}

				} else if (!doc.getType().equals("CHART")) {
					logger.debug("call execution proxy");

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
				logger.debug("add content retrieved to Document Container");
				if (returnByteArray != null) {
					if (returnByteArray.length == 0)
						logger.warn("empty byte array retrieved for document " + label);
					documentContainer.setContent(returnByteArray);
					documentContainer.setDocumentLabel(label);
					documentContainer.setDocumentType(object.getBiObjectTypeCode());
				}
			}

			FileOutputStream fileOutputStream = new FileOutputStream(tmpFile);
			PdfCreator pdfCreator = new PdfCreator();
			logger.debug("Call PDF Creation");
			pdfCreator.setVideoHeight(dcConf.getVideoHeight());
			pdfCreator.setVideoWidth(dcConf.getVideoWidth());

			FileOutputStream pdfFile = pdfCreator.createPdfFile(fileOutputStream, documentsMap, defaultStyle);

			pdfFile.flush();
			pdfFile.close();

			logger.debug("OUT");

			return tmpFile;
		} catch (Exception e) {
			logger.error(e);
			return null;
		} finally {
			// Close the input stream
			if (pngInputStream != null) {
				try {
					pngInputStream.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			if (svgInputStream != null) {
				try {
					svgInputStream.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			if (svgOutputStream != null) {
				try {
					svgOutputStream.close();
				} catch (IOException e) {
					logger.error(e);
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

		logger.debug("IN");
		// For each parameter the object needs search for a value in currentConf
		if (currentConf == null)
			return;

		List parametersBO = object.getBiObjectParameters();
		Map<String, Object> currentParameters = currentConf.getParameters();
		if (currentParameters != null) {
			if (parametersBO != null) {
				for (Iterator iterator = parametersBO.iterator(); iterator.hasNext();) {
					BIObjectParameter parAss = (BIObjectParameter) iterator.next();
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
								logger.debug("Put new values " + valueString + " to parameter " + urlName);
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
				parametersBO = new ArrayList<BIObjectParameter>();
			for (Iterator iterator = currentParameters.keySet().iterator(); iterator.hasNext();) {
				String lab = (String) iterator.next();
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
				object.getBiObjectParameters().add(biObjPar);
			}

		}
		logger.debug("OUT");
	}

}
