/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.analiticalmodel.document.util;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.documentcomposition.configuration.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Marco Cortella (marco.cortella@eng.it) Class created to remove dependency from BIObjectDAOHibImpl and
 *         it.eng.spagobi.engines.documentcomposition.configuration.DocumentConfigurationConfiguration
 */
public class DocumentCompositionUtil {

	private Map documentsMap;
	private final String templateFile;

	public static final String TEMPLATE_VALUE = "template_value";
	public static final String DOCUMENTS_CONFIGURATION = "DOCUMENTS_CONFIGURATION";

	private static transient Logger logger = Logger.getLogger(DocumentCompositionUtil.class);

	private static Integer DEFAULT_WIDTH = new Integer("1024");
	private static Integer DEFAULT_HEIGHT = new Integer("768");
	private Integer videoHeight = null;
	private Integer videoWidth = null;

	/**
	 * Instantiates a new document composition configuration.
	 * 
	 * @param DocumentCompositionConfigurationSB
	 *            the document composition configuration sb
	 */
	public DocumentCompositionUtil(SourceBean DocumentCompositionConfigurationSB) {
		documentsMap = new LinkedHashMap();

		templateFile = (String) DocumentCompositionConfigurationSB.getAttribute(TEMPLATE_VALUE);

		SourceBean documentsConfigurationSB = (SourceBean) DocumentCompositionConfigurationSB.getAttribute(DOCUMENTS_CONFIGURATION);
		initDocuments(documentsConfigurationSB);
	}

	private void initDocuments(SourceBean documentsConfigurationSB) {
		logger.debug("IN");
		Document document;
		String attributeValue;

		List documentList;
		List refreshDocList;
		List paramList;
		List styleList;
		SourceBean styleSB;
		SourceBean documentSB;
		SourceBean refreshSB;
		SourceBean dimensionSB;
		SourceBean parametersSB;
		SourceBean paramSB;
		SourceBean refreshDocLinkedSB;
		try {

			documentList = documentsConfigurationSB.getAttributeAsList(Constants.DOCUMENT);
			// create dimensions Map
			String videoWidthS = (documentsConfigurationSB.getAttribute(Constants.VIDEO_WIGTH) != null) ? documentsConfigurationSB.getAttribute(
					Constants.VIDEO_WIGTH).toString() : null;
			String videoHeightS = (documentsConfigurationSB.getAttribute(Constants.VIDEO_HEIGHT) != null) ? documentsConfigurationSB.getAttribute(
					Constants.VIDEO_HEIGHT).toString() : null;
			if (videoWidthS != null & videoHeightS != null) {
				videoWidth = Integer.valueOf(videoWidthS);
				videoHeight = Integer.valueOf(videoHeightS);
			} else {
				videoWidth = DEFAULT_WIDTH;
				videoHeight = DEFAULT_HEIGHT;

			}

			// loop on documents
			for (int i = 0; i < documentList.size(); i++) {

				documentSB = (SourceBean) documentList.get(i);
				document = new Document();

				// set the number that identify the document within of hash table
				document.setNumOrder(i);
				attributeValue = (String) documentSB.getAttribute(Constants.SBI_OBJ_LABEL);
				document.setSbiObjLabel(attributeValue);

				String snap = (String) documentSB.getAttribute(Constants.SNAPSHOT);
				document.setSnapshot(Boolean.valueOf(snap));

				BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(attributeValue);
				if (obj == null) {
					logger.error("Document with label " + attributeValue + " doesn't exist in SpagoBI. Check the label!");
					continue;
				}
				String typeCD = obj.getBiObjectTypeCode();
				document.setType(typeCD);

				attributeValue = (documentSB.getAttribute(Constants.TITLE) == null) ? "" : (String) documentSB.getAttribute(Constants.TITLE);
				document.setTitle(attributeValue);
				attributeValue = (documentSB.getAttribute(Constants.EXPORT) == null) ? null : (String) documentSB.getAttribute(Constants.EXPORT);
				// for retrocompatibility (when the attribute for the export is called exportDS
				if (attributeValue == null) {
					attributeValue = (documentSB.getAttribute(Constants.EXPORT_DS) == null) ? "false" : (String) documentSB.getAttribute(Constants.EXPORT_DS);
				}
				document.setActiveExport(attributeValue);

				// Does not neeed video dimensions in utils
				// Integer width = (documentsConfigurationSB.getAttribute(Constants.VIDEO_WIGTH) == null) ? DEFAULT_WIDTH : Integer
				// .valueOf((String) documentsConfigurationSB.getAttribute(Constants.VIDEO_WIGTH));
				// Integer height = (documentsConfigurationSB.getAttribute(Constants.VIDEO_HEIGHT) == null) ? DEFAULT_HEIGHT : Integer
				// .valueOf((String) documentsConfigurationSB.getAttribute(Constants.VIDEO_HEIGHT));
				//
				// document.setVideoWidth(getVideoDimensions("width", width));
				// document.setVideoHeight(getVideoDimensions("height", height));

				dimensionSB = (SourceBean) documentSB.getAttribute(Constants.STYLE);
				attributeValue = (String) dimensionSB.getAttribute(Constants.DIMENSION_STYLE);
				// attributeValue = (String)dimensionSB.getAttribute("class");
				document.setStyle(attributeValue);
				parametersSB = (SourceBean) documentSB.getAttribute(Constants.PARAMETERS);
				if (parametersSB != null) {
					paramList = parametersSB.getAttributeAsList(Constants.PARAMETER);
					Properties param = new Properties();
					// loop on parameters of single document
					for (int j = 0; j < paramList.size(); j++) {
						paramSB = (SourceBean) paramList.get(j);
						String sbiParLabel = (paramSB.getAttribute(Constants.SBI_PAR_LABEL) == null) ? "" : (String) paramSB
								.getAttribute(Constants.SBI_PAR_LABEL);
						param.setProperty("sbi_par_label_param_" + i + "_" + j, sbiParLabel);
						String typePar = (paramSB.getAttribute(Constants.TYPE) == null) ? "" : (String) paramSB.getAttribute(Constants.TYPE);
						param.setProperty("type_par_" + i + "_" + j, typePar);
						String defaultValuePar = (paramSB.getAttribute(Constants.DEFAULT_VALUE) == null) ? "" : (String) paramSB
								.getAttribute(Constants.DEFAULT_VALUE);
						param.setProperty("default_value_param_" + i + "_" + j, defaultValuePar);

						refreshSB = (SourceBean) paramSB.getAttribute(Constants.REFRESH);
						if (refreshSB != null) {
							refreshDocList = refreshSB.getAttributeAsList(Constants.REFRESH_DOC_LINKED);
							if (refreshDocList != null) {
								Properties paramRefreshLinked = new Properties();
								// loop on document linked to single parameter
								int k = 0;
								for (k = 0; k < refreshDocList.size(); k++) {
									refreshDocLinkedSB = (SourceBean) refreshDocList.get(k);
									String labelDoc = (refreshDocLinkedSB.getAttribute(Constants.LABEL_DOC) == null) ? "" : (String) refreshDocLinkedSB
											.getAttribute(Constants.LABEL_DOC);
									paramRefreshLinked.setProperty("refresh_doc_linked", labelDoc);
									String labelPar = (refreshDocLinkedSB.getAttribute(Constants.LABEL_PARAM) == null) ? "" : (String) refreshDocLinkedSB
											.getAttribute(Constants.LABEL_PARAM);
									paramRefreshLinked.setProperty("refresh_par_linked", labelPar);
									String defaultValueLinked = (paramSB.getAttribute(Constants.DEFAULT_VALUE) == null) ? "" : (String) paramSB
											.getAttribute(Constants.DEFAULT_VALUE);
									paramRefreshLinked.setProperty("default_value_linked", defaultValueLinked);
									String typeCrossPar = (refreshDocLinkedSB.getAttribute(Constants.TYPE_CROSS) == null) ? Constants.CROSS_INTERNAL
											: (String) refreshDocLinkedSB.getAttribute(Constants.TYPE_CROSS);
									paramRefreshLinked.setProperty("type_cross_linked", typeCrossPar);
									param.setProperty("param_linked_" + i + "_" + j + "_" + k, paramRefreshLinked.toString());
								}
								param.setProperty("num_doc_linked_param_" + i + "_" + j, new Integer(k).toString());
							}
						}
					}
					document.setParams(param);
				}
				addDocument(document);
			}
		} catch (Exception e) {
			logger.error("Error while initializing the document. ", e);
		}
		logger.debug("OUT");
	}

	/**
	 * Adds the document.
	 * 
	 * @param document
	 *            the document
	 */
	public void addDocument(Document document) {
		if (documentsMap == null)
			documentsMap = new LinkedHashMap();
		documentsMap.put(document.getSbiObjLabel(), document);
	}

	/**
	 * Gets the sbi obj labels array.
	 * 
	 * @return the sbi obj labels array
	 */
	public List getSbiObjLabelsArray() {
		logger.debug("IN");

		Collection collLabels = documentsMap.values();
		List retLabels = new ArrayList();
		Object[] arrDocs = collLabels.toArray();
		try {
			for (int i = 0; i < arrDocs.length; i++) {
				Document tmpDoc = (Document) arrDocs[i];
				retLabels.add(tmpDoc.getSbiObjLabel());
			}
		} catch (Exception e) {
			logger.error("Error while getting documents label.", e);
		}
		logger.debug("OUT");
		return retLabels;

	}

	/*
	 * Internal Class
	 */
	public static class Document {
		int numOrder;
		Integer videoWidth[];
		Integer videoHeight[];
		String title;
		String sbiObjLabel;
		String style;
		String namePar;
		String sbiParName;
		String type;
		String defaultValue;
		String typeCross;
		String activeExport;
		Boolean snapshot;
		Properties params;

		public Boolean getSnapshot() {
			return snapshot;
		}

		public void setSnapshot(Boolean snapshot) {
			this.snapshot = snapshot;
		}

		/**
		 * Gets the sbi obj label.
		 * 
		 * @return the sbi obj label
		 */
		public String getSbiObjLabel() {
			return sbiObjLabel;
		}

		/**
		 * Sets the sbi obj label.
		 * 
		 * @param sbiObjLabel
		 *            the new sbi obj label
		 */
		public void setSbiObjLabel(String sbiObjLabel) {
			this.sbiObjLabel = sbiObjLabel;
		}

		/**
		 * Gets the name par.
		 * 
		 * @return the name par
		 */
		public String getNamePar() {
			return namePar;
		}

		/**
		 * Sets the name par.
		 * 
		 * @param namePar
		 *            the new name par
		 */
		public void setNamePar(String namePar) {
			this.namePar = namePar;
		}

		/**
		 * Gets the sbi par name.
		 * 
		 * @return the sbi par name
		 */
		public String getSbiParName() {
			return sbiParName;
		}

		/**
		 * Sets the sbi par name.
		 * 
		 * @param sbiParName
		 *            the new sbi par name
		 */
		public void setSbiParName(String sbiParName) {
			this.sbiParName = sbiParName;
		}

		/**
		 * Gets the type.
		 * 
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * Sets the type.
		 * 
		 * @param type
		 *            the new type
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * Gets the default value.
		 * 
		 * @return the default value
		 */
		public String getDefaultValue() {
			return defaultValue;
		}

		/**
		 * Sets the default value.
		 * 
		 * @param defaultValue
		 *            the new default value
		 */
		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}

		/**
		 * Gets the params.
		 * 
		 * @return the params
		 */
		public Properties getParams() {
			return params;
		}

		/**
		 * Sets the params.
		 * 
		 * @param params
		 *            the new params
		 */
		public void setParams(Properties params) {
			this.params = params;
		}

		/**
		 * Gets the style.
		 * 
		 * @return the style
		 */
		public String getStyle() {
			return style;
		}

		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * @param title
		 *            the title to set
		 */
		public void setTitle(String title) {
			this.title = title;
		}

		/**
		 * /**
		 * 
		 * @return the activeExport
		 */
		public String getActiveExport() {
			return (activeExport == null) ? "false" : activeExport;
		}

		/**
		 * @param activeExport
		 *            the activeExport to set
		 */
		public void setActiveExport(String activeExport) {
			this.activeExport = activeExport;
		}

		/**
		 * Sets the style.
		 * 
		 * @param style
		 *            the new style
		 */
		public void setStyle(String style) {
			this.style = style;
		}

		/**
		 * Gets the num order.
		 * 
		 * @return the num order
		 */
		public int getNumOrder() {
			return numOrder;
		}

		/**
		 * Sets the num order.
		 * 
		 * @param numOrder
		 *            the new num order
		 */
		public void setNumOrder(int numOrder) {
			this.numOrder = numOrder;
		}

		/**
		 * Gets the video width.
		 * 
		 * @return the video width
		 */
		public Integer[] getVideoWidth() {
			return videoWidth;
		}

		/**
		 * Sets the video width.
		 * 
		 * @param videoWidth
		 *            the new video width
		 */
		public void setVideoWidth(Integer[] videoWidth) {
			this.videoWidth = videoWidth;
		}

		/**
		 * Gets the video height.
		 * 
		 * @return the video height
		 */
		public Integer[] getVideoHeight() {
			return videoHeight;
		}

		/**
		 * Sets the video height.
		 * 
		 * @param videoHeight
		 *            the new video height
		 */
		public void setVideoHeight(Integer[] videoHeight) {
			this.videoHeight = videoHeight;
		}

		/**
		 * @return the typeCross
		 */
		public String getTypeCross() {
			return typeCross;
		}

		/**
		 * @param typeCross
		 *            the typeCross to set
		 */
		public void setTypeCross(String typeCross) {
			this.typeCross = typeCross;
		}
	}

	// -----------------------------------------------------------------------------
}
