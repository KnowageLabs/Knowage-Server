/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.documentcomposition.configuration;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */

/*
 * This class create a configuration object (alias an object with all informations about the configuration template)
 * and create an object in session.
 */
public class DocumentCompositionConfiguration {

	private static transient Logger logger=Logger.getLogger(DocumentCompositionConfiguration.class);

	private String templateFile;
	private Map documentsMap;
	private Map lstDimensions = new LinkedHashMap();
	//list used by final jsp
	private Map lstUrl = new LinkedHashMap();
	private Map lstDivStyle = new LinkedHashMap();
	private Map lstUrlParams  = new LinkedHashMap();
	private Map lstDocLinked = new LinkedHashMap();
	private Map lstFieldLinked = new LinkedHashMap();
	private Map lstPanelStyle = new LinkedHashMap();
	private Map lstDocTitles = new LinkedHashMap();
	private Map lstCrossLinked = new LinkedHashMap();
	private Map lstDocExport = new LinkedHashMap();
	

	private Integer videoHeight=null;
	private Integer videoWidth=null;


	//constants for convert panel dimensions from percentage into pixel values
	Integer[] percentageValues = {new Integer("100"), new Integer("75"), new Integer("50"),new Integer("35"),  new Integer("25"), new Integer("15")};
	Integer[] widthPxValues800= {new Integer("800"), new Integer("600"), new Integer("400"), new Integer("280"), new Integer("200"), new Integer("120")};
	Integer[] widthPxValues1024 = {new Integer("1000"), new Integer("750"), new Integer("500"), new Integer("350"), new Integer("250"), new Integer("150")};
	Integer[] widthPxValues1280 = {new Integer("1200"), new Integer("900"), new Integer("600"),  new Integer("420"),new Integer("300"), new Integer("180")};
	Integer[] widthPxValues1400 = {new Integer("1400"), new Integer("1050"), new Integer("700"), new Integer("490"), new Integer("350"), new Integer("210")};
	Integer[] widthPxValues1680 = {new Integer("1600"), new Integer("1200"), new Integer("800"), new Integer("560"), new Integer("400"), new Integer("240")};
	Integer[] heightPxValues600 = {new Integer("600"), new Integer("450"), new Integer("300"), new Integer("210"), new Integer("150"), new Integer("90")};
	Integer[] heightPxValues768 = {new Integer("700"), new Integer("525"), new Integer("350"), new Integer("245"), new Integer("175"), new Integer("105")};
	Integer[] heightPxValues1024= {new Integer("1000"), new Integer("750"), new Integer("500"), new Integer("350"), new Integer("250"), new Integer("150")};
	Integer[] heightPxValues1050 = {new Integer("1050"), new Integer("787"), new Integer("525"), new Integer("370"), new Integer("262"), new Integer("160")};
	Integer[] generalWidthDimensions = {new Integer("1680"), new Integer("1400"), new Integer("1280"),
			new Integer("1024"), new Integer("800")};
	Integer[] generalHeightDimensions = {new Integer("1050"), new Integer("1024"),  new Integer("768"), new Integer("600")};
	private static Integer DEFAULT_WIDTH = new Integer("1024");
	private static Integer DEFAULT_HEIGHT = new Integer("768");

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
		 * @param sbiObjLabel the new sbi obj label
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
		 * @param namePar the new name par
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
		 * @param sbiParName the new sbi par name
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
		 * @param type the new type
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
		 * @param defaultValue the new default value
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
		 * @param params the new params
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
		 * @param title the title to set
		 */
		public void setTitle(String title) {
			this.title = title;
		}

		/**
		/**
		 * @return the activeExport
		 */
		public String getActiveExport() {
			return (activeExport == null)?"false":activeExport;
		}

		/**
		 * @param activeExport the activeExport to set
		 */
		public void setActiveExport(String activeExport) {
			this.activeExport = activeExport;
		}

		/**
		 * Sets the style.
		 * 
		 * @param style the new style
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
		 * @param numOrder the new num order
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
		 * @param videoWidth the new video width
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
		 * @param videoHeight the new video height
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
		 * @param typeCross the typeCross to set
		 */
		public void setTypeCross(String typeCross) {
			this.typeCross = typeCross;
		}
	}

	/**
	 * Instantiates a new document composition configuration.
	 */
	public DocumentCompositionConfiguration () {
		init();
	}

	/**
	 * Instantiates a new document composition configuration.
	 * 
	 * @param DocumentCompositionConfigurationSB the document composition configuration sb
	 */
	public DocumentCompositionConfiguration (SourceBean DocumentCompositionConfigurationSB){
		init();
		SourceBean documentsConfigurationSB;
		templateFile = (String)DocumentCompositionConfigurationSB.getAttribute(Constants.TEMPLATE_VALUE);


		documentsConfigurationSB = (SourceBean)DocumentCompositionConfigurationSB.getAttribute(Constants.DOCUMENTS_CONFIGURATION);

		initDocuments(documentsConfigurationSB);
	}

	/**
	 * initialize general objects
	 */
	private void init(){
		documentsMap = new LinkedHashMap();
		lstDimensions.put("widthPxValues800", widthPxValues800);
		lstDimensions.put("widthPxValues1024", widthPxValues1024);
		lstDimensions.put("widthPxValues1280", widthPxValues1280);
		lstDimensions.put("widthPxValues1400", widthPxValues1400);
		lstDimensions.put("widthPxValues1680", widthPxValues1680);
		lstDimensions.put("heightPxValues600", heightPxValues600);
		lstDimensions.put("heightPxValues768", heightPxValues768);
		lstDimensions.put("heightPxValues1024", heightPxValues1024);
		lstDimensions.put("heightPxValues1050", heightPxValues1050);
	}

	/**
	 * Adds the document.
	 * 
	 * @param document the document
	 */
	public void addDocument(Document document) {
		if(documentsMap == null) documentsMap = new LinkedHashMap();
		documentsMap.put(document.getSbiObjLabel(), document);
	}

	/**
	 * Reset documents.
	 */
	public void resetDocuments() {		
		documentsMap = new LinkedHashMap();
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
		try{

			documentList = documentsConfigurationSB.getAttributeAsList(Constants.DOCUMENT);
			//create dimensions Map
			String videoWidthS=(documentsConfigurationSB.getAttribute(Constants.VIDEO_WIGTH)!=null) ? documentsConfigurationSB.getAttribute(Constants.VIDEO_WIGTH).toString() : null;
			String videoHeightS=(documentsConfigurationSB.getAttribute(Constants.VIDEO_HEIGHT) != null) ? documentsConfigurationSB.getAttribute(Constants.VIDEO_HEIGHT).toString() : null;
			if(videoWidthS!=null & videoHeightS!=null){
				videoWidth=Integer.valueOf(videoWidthS);
				videoHeight=Integer.valueOf(videoHeightS);
			}
			else{
				videoWidth=DEFAULT_WIDTH;
				videoHeight=DEFAULT_HEIGHT;
				
			}

			//loop on documents
			for(int i = 0; i < documentList.size(); i++) {

				documentSB = (SourceBean)documentList.get(i);
				document = new Document();

				//set the number that identify the document within of hash table
				document.setNumOrder(i);	
				attributeValue = (String)documentSB.getAttribute(Constants.SBI_OBJ_LABEL);
				document.setSbiObjLabel(attributeValue);
				
				String snap= (String)documentSB.getAttribute(Constants.SNAPSHOT);
				document.setSnapshot(Boolean.valueOf(snap));
				
				BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(attributeValue);
				if (obj == null){
					logger.error ("Document with label " + attributeValue +  " doesn't exist in SpagoBI. Check the label!");
					continue;
				}
				String typeCD = obj.getBiObjectTypeCode();
				document.setType(typeCD);
				
				attributeValue = (documentSB.getAttribute(Constants.TITLE)==null)?"":(String)documentSB.getAttribute(Constants.TITLE);
				document.setTitle(attributeValue);
				attributeValue = (documentSB.getAttribute(Constants.EXPORT)==null)?null:(String)documentSB.getAttribute(Constants.EXPORT);
				//for retrocompatibility (when the attribute for the export is called exportDS
				if (attributeValue == null){
					attributeValue = (documentSB.getAttribute(Constants.EXPORT_DS)==null)?"false":(String)documentSB.getAttribute(Constants.EXPORT_DS);
				}
				document.setActiveExport(attributeValue);

				Integer width = (documentsConfigurationSB.getAttribute(Constants.VIDEO_WIGTH)==null)?DEFAULT_WIDTH:Integer.valueOf((String)documentsConfigurationSB.getAttribute(Constants.VIDEO_WIGTH));
				Integer height = (documentsConfigurationSB.getAttribute(Constants.VIDEO_HEIGHT)==null)?DEFAULT_HEIGHT:Integer.valueOf((String)documentsConfigurationSB.getAttribute(Constants.VIDEO_HEIGHT));


				document.setVideoWidth(getVideoDimensions("width", width));
				document.setVideoHeight(getVideoDimensions("height", height));

				dimensionSB = (SourceBean)documentSB.getAttribute(Constants.STYLE);			
				attributeValue = (String)dimensionSB.getAttribute(Constants.DIMENSION_STYLE);
				//attributeValue = (String)dimensionSB.getAttribute("class");
				document.setStyle(attributeValue);			
				parametersSB = (SourceBean)documentSB.getAttribute(Constants.PARAMETERS);	
				if(parametersSB!=null){
					paramList = parametersSB.getAttributeAsList(Constants.PARAMETER);
					Properties param = new Properties();
					//loop on parameters of single document
					for(int j = 0; j < paramList.size(); j++) {
						paramSB = (SourceBean)paramList.get(j);
						String sbiParLabel = (paramSB.getAttribute(Constants.SBI_PAR_LABEL)==null)?"":(String)paramSB.getAttribute(Constants.SBI_PAR_LABEL);
						param.setProperty("sbi_par_label_param_"+i+"_"+j, sbiParLabel);
						String typePar = (paramSB.getAttribute(Constants.TYPE)==null)?"":(String)paramSB.getAttribute(Constants.TYPE);
						param.setProperty("type_par_"+i+"_"+j, typePar);
						String defaultValuePar = (paramSB.getAttribute(Constants.DEFAULT_VALUE)==null)?"":(String)paramSB.getAttribute(Constants.DEFAULT_VALUE);
						param.setProperty("default_value_param_"+i+"_"+j, defaultValuePar);

						refreshSB = (SourceBean)paramSB.getAttribute(Constants.REFRESH);				
						if(refreshSB!=null){
							refreshDocList = refreshSB.getAttributeAsList(Constants.REFRESH_DOC_LINKED);
							if(refreshDocList!=null){
								Properties paramRefreshLinked = new Properties();
								//loop on document linked to single parameter
								int k = 0;
								for(k = 0; k < refreshDocList.size(); k++) {
									refreshDocLinkedSB = (SourceBean)refreshDocList.get(k);
									String labelDoc = (refreshDocLinkedSB.getAttribute(Constants.LABEL_DOC)==null)?"":(String)refreshDocLinkedSB.getAttribute(Constants.LABEL_DOC);
									paramRefreshLinked.setProperty("refresh_doc_linked", labelDoc);
									String labelPar = (refreshDocLinkedSB.getAttribute(Constants.LABEL_PARAM)==null)?"":(String)refreshDocLinkedSB.getAttribute(Constants.LABEL_PARAM);
									paramRefreshLinked.setProperty("refresh_par_linked", labelPar);
									String defaultValueLinked = (paramSB.getAttribute(Constants.DEFAULT_VALUE)==null)?"":(String)paramSB.getAttribute(Constants.DEFAULT_VALUE);
									paramRefreshLinked.setProperty("default_value_linked", defaultValueLinked);
									String typeCrossPar = (refreshDocLinkedSB.getAttribute(Constants.TYPE_CROSS)==null)?Constants.CROSS_INTERNAL:(String)refreshDocLinkedSB.getAttribute(Constants.TYPE_CROSS);
									paramRefreshLinked.setProperty("type_cross_linked", typeCrossPar);
									param.setProperty("param_linked_"+i+"_"+j+"_"+k, paramRefreshLinked.toString());
								}
								param.setProperty("num_doc_linked_param_"+i+"_"+j, new Integer(k).toString());
							}
						}
					}
					document.setParams(param);
				}
				addDocument(document);
			}		
		}catch (Exception e){
			logger.error ("Error while initializing the document. " , e);
		}
		logger.debug("OUT");
	}


	/**
	 * Gets the document.
	 * 
	 * @param documentName the document name
	 * 
	 * @return the document
	 */
	public Document getDocument(String documentName) {
		return (Document)documentsMap.get(documentName);
	}


	/**
	 * Gets the label.
	 * 
	 * @param documentLabel the document label
	 * 
	 * @return the label
	 */
	public String getLabel(String documentLabel) {
		Document document = getDocument(documentLabel);
		if(document != null) return document.getSbiObjLabel();

		return null;
	}


	/**
	 * Gets the labels array.
	 * 
	 * @return the labels array
	 */
	public List getLabelsArray() {
		logger.debug("IN");
		Collection collLabels = documentsMap.values();
		List retLabels = new ArrayList();
		Object[] arrDocs = (Object[])collLabels.toArray();
		try{
			int numDocAdded = 0;
			while (numDocAdded < arrDocs.length){
				for(int i=0; i < arrDocs.length; i++){
					Document tmpDoc =(Document) arrDocs[i];
					if (tmpDoc.getNumOrder() == numDocAdded){
						retLabels.add(tmpDoc.getSbiObjLabel());
						numDocAdded ++;
					}
				}
			}
		}catch(Exception e){
			logger.error("Error while getting documents label ", e);
			//System.out.println(e);
		}
		logger.debug("OUT");
		return retLabels;

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
		Object[] arrDocs = (Object[])collLabels.toArray();
		try{
			for(int i=0; i < arrDocs.length; i++){
				Document tmpDoc =(Document) arrDocs[i];
				retLabels.add(tmpDoc.getSbiObjLabel());
			}
		}catch(Exception e){
			logger.error("Error while getting documents label.", e);
		}
		logger.debug("OUT");
		return retLabels;

	}

	/**
	 * Gets the template file.
	 * 
	 * @return the template file
	 */
	public String getTemplateFile() {
		return templateFile;
	}

	/**
	 * Sets the template file.
	 * 
	 * @param templateFile the new template file
	 */
	public void setTemplateFile(String templateFile) {
		this.templateFile = templateFile;
	}

	/**
	 * Gets the parameters array.
	 * 
	 * @return the parameters array
	 */
	public List getParametersArray() {
		logger.debug("IN");
		Collection collDocs = documentsMap.values();
		List retParams = new ArrayList();
		Object[] arrPars = (Object[])collDocs.toArray();
		try{
			for(int i=0; i < arrPars.length; i++){
				Document tmpDoc =(Document) arrPars[i];
				retParams.add(tmpDoc.getParams());
			}
		}catch(Exception e){
			logger.error("Error while getting parameters.", e);
		}
		logger.debug("OUT");
		return retParams;

	}
	/*
	public List getParametersForDocument(String docLabel) {
		Collection collDocs = documentsMap.values();
		List retParams = new ArrayList();
		Object[] arrPars = (Object[])collDocs.toArray();
		try{
			for(int i=0; i < arrPars.length; i++){
				Document tmpDoc =(Document) arrPars[i];
				if (tmpDoc.getLabel().equalsIgnoreCase(docLabel))
					retParams.add(tmpDoc.getParams());
			}
		}catch(Exception e){
			System.out.println(e);
		}
		return retParams;

	}
	 */

	/**
	 * Gets the parameters for document.
	 * 
	 * @param docLabel the doc label
	 * 
	 * @return the parameters for document
	 */
	public HashMap getParametersForDocument(String docLabel) {
		logger.debug("IN");
		Collection collDocs = documentsMap.values();
		HashMap retParams = new LinkedHashMap();
		Object[] arrDocs = (Object[])collDocs.toArray();

		try{
			int cont = 0;
			while (cont < arrDocs.length){
				//loop on documents
				for(int i=0; i < arrDocs.length; i++){
					Document tmpDoc =(Document) arrDocs[i];
					if (tmpDoc.getNumOrder() == cont){
						if (tmpDoc.getSbiObjLabel().equalsIgnoreCase(docLabel)){
							Object paramsO=tmpDoc.getParams();
							if(paramsO!=null){
								Properties prop = (Properties)paramsO;
								Enumeration e =  prop.keys();
								int totParsLinked = 0;
								//loop on parameters of single document
								while (e.hasMoreElements() ){
									String key = (String)e.nextElement();
									retParams.put(key, (String)prop.get(key));
									if (key.startsWith("param_linked_"+(tmpDoc.getNumOrder())))
										totParsLinked ++;
								}
								retParams.put("num_doc_linked_"+(tmpDoc.getNumOrder()), Integer.valueOf(totParsLinked));
							}
						}
						cont ++;
					}
				}
			}
		}catch(Exception e){
			logger.error("Error while reading parameters.", e);


		}
		logger.debug("OUT");
		return retParams;

	}

	/**
	 * Reads and defines all maps with all information about configuration for refresh.
	 * 
	 * @param docLabel the logical label of document presents into document composition
	 */
	public void getInfoDocumentLinked(String docLabel) {
		logger.debug("IN");
		Document tmpDoc = getDocument(docLabel);
		int numDoc = 0;
		if (tmpDoc != null){
			numDoc = tmpDoc.getNumOrder();
			//set syle for div
			lstDivStyle.put("STYLE_DOC__"+numDoc, tmpDoc.getStyle());
			lstDocLinked.put("MAIN_DOC_LABEL__"+numDoc, tmpDoc.getSbiObjLabel());
			lstDocTitles.put("TITLE_DOC__"+ docLabel, tmpDoc.getTitle());
			lstDocExport.put("EXPORT_DOC__"+ docLabel, tmpDoc.getActiveExport());
			//gets layout informations (width and height) for next settings of ext-panels
			String docStyles = tmpDoc.getStyle();
			String tmpStyle = "";
			Integer[] widthPxValues = tmpDoc.getVideoWidth();
			Integer[] heightPxValues = tmpDoc.getVideoHeight();

			if (docStyles != null){
				String[] propValues = docStyles.split(";");
				for (int i=0; i<propValues.length; i++){
					String key = propValues[i].substring(0, propValues[i].indexOf(":"));
					String value = propValues[i].substring(propValues[i].indexOf(":")+1);
					if (key.equalsIgnoreCase("WIDTH") || key.equalsIgnoreCase("HEIGHT")){
						if (value.endsWith("%")){
							//if the value is defined in percentage, converts it in pixel value
							for (int j=0; j<percentageValues.length; j++){
								int diff = Integer.valueOf(value.substring(0, value.length()-1)).compareTo(percentageValues[j]);
								if (diff == 0){
									if (key.equalsIgnoreCase("WIDTH"))
										value = widthPxValues[j].toString();
									else if (key.equalsIgnoreCase("HEIGHT"))
										value = heightPxValues[j].toString();
									break;
								}
								else if (diff > 0 && j > 0){
									if (key.equalsIgnoreCase("WIDTH"))
										value = widthPxValues[j-1].toString();
									else if (key.equalsIgnoreCase("HEIGHT"))
										value = heightPxValues[j-1].toString();
									break;
								}
							}
						}
						else if (value.endsWith("px"))
							value = value.substring(0, value.length()-2);

						tmpStyle += key.toUpperCase() + "_" + value + "|";
					}
				}
				tmpStyle = tmpStyle.substring(0, tmpStyle.length()-1);
				lstPanelStyle.put("STYLE__"  + docLabel , tmpStyle);
			}

			try{
				HashMap paramsDoc = getParametersForDocument(docLabel);
				//loop on parameters of document
				int contOutPar = 0;

				for (int i=0; i< paramsDoc.size(); i++){
					int numParAdd = 0;
					String typePar = (paramsDoc.get("type_par_"+(numDoc)+"_"+i)==null)?"":(String)paramsDoc.get("type_par_"+(numDoc)+"_"+i);
					//if (typePar != null && typePar.equalsIgnoreCase("OUT")){

					if (typePar != null && typePar.indexOf("OUT")>= 0){
						lstFieldLinked.put("SBI_LABEL_PAR_MASTER__"+(numDoc)+"__"+contOutPar, (String)paramsDoc.get("sbi_par_label_param_"+(numDoc)+"_"+i));
						Integer totalNumDocLinked = (paramsDoc.get("num_doc_linked_"+(numDoc))==null)?new Integer(0):(Integer)paramsDoc.get("num_doc_linked_"+(numDoc));
						Integer numDocLinked = (paramsDoc.get("num_doc_linked_param_"+(numDoc)+"_"+i)==null)?new Integer(0):Integer.valueOf((String)paramsDoc.get("num_doc_linked_param_"+(numDoc)+"_"+i));
						lstFieldLinked.put("NUM_DOC_FIELD_LINKED__"+(numDoc)+"__"+contOutPar, numDocLinked);
						lstFieldLinked.put("TOT_NUM_DOC_FIELD_LINKED__"+(numDoc)+"__"+contOutPar, totalNumDocLinked);
						//loop on document linked to parameter
						for (int j=0; j<numDocLinked.intValue(); j++){

							String paramLinked = (paramsDoc.get("param_linked_"+(numDoc)+"_"+i+"_"+j)==null)?"":(String)paramsDoc.get("param_linked_"+(numDoc)+"_"+i+"_"+j);
							String typeCross = getCrossType(paramLinked);
							String[] params = paramLinked.split(",");
							Document linkedDoc = null;

							//loop on parameters of document linked
							for (int k=0; k<params.length; k++) {
								String labelDocLinked = params[k];
								labelDocLinked = labelDocLinked.replace("{", "");
								labelDocLinked = labelDocLinked.replace("}", "");
								if (labelDocLinked.trim().startsWith("refresh_doc_linked")){
									//get document linked 
									linkedDoc = getDocument(labelDocLinked.substring(labelDocLinked.indexOf("=")+1));
									if (linkedDoc == null && typeCross.equalsIgnoreCase(Constants.CROSS_EXTERNAL)){
										//defines a new doclinked only for the external cross
										linkedDoc = createExternalDocument(paramLinked);										
									}

								}/*else if (labelDocLinked.trim().startsWith("type_cross_linked")){
									//get type cross (External or internal) for document linked 
									typeCross = labelDocLinked.substring(labelDocLinked.indexOf("=")+1);										
								}*/
								else if (labelDocLinked.trim().startsWith("refresh_par_linked")){
									String tmpLabelLinked = labelDocLinked.substring(labelDocLinked.indexOf("=")+1);
									HashMap paramsDocLinked = getParametersForDocument(linkedDoc.getSbiObjLabel());
									int numLinked = linkedDoc.getNumOrder();
									for (int x=0; x< paramsDocLinked.size(); x++){
										String sbiLabelPar = (paramsDocLinked.get("sbi_par_label_param_"+numLinked+"_"+x)==null)?"":(String)paramsDocLinked.get("sbi_par_label_param_"+(numLinked)+"_"+x);
										//String labelPar = (paramsDocLinked.get("label_param_"+numLinked+"_"+x)==null)?"":(String)paramsDocLinked.get("label_param_"+(numLinked)+"_"+x);
										/*if ((sbiLabelPar != null && !sbiLabelPar.equals("") && sbiLabelPar.equalsIgnoreCase(tmpLabelLinked)) ||
												typeCross != null && typeCross.equalsIgnoreCase(Constants.CROSS_EXTERNAL)){*/
										if ((sbiLabelPar != null && !sbiLabelPar.equals("") && sbiLabelPar.equalsIgnoreCase(tmpLabelLinked))){
											lstDocLinked.put("DOC_LABEL_LINKED__"+numDoc+"__"+contOutPar+"__"+numParAdd, linkedDoc.getSbiObjLabel());
											lstFieldLinked.put("DOC_FIELD_LINKED__"+numDoc+"__"+contOutPar+"__"+numParAdd, linkedDoc.getSbiObjLabel()+"__"+sbiLabelPar );
											lstCrossLinked.put("DOC_CROSS_LINKED__"+numDoc+"__"+contOutPar+"__"+numParAdd, typeCross );
											numParAdd ++;
											break;
										}
										else if (sbiLabelPar == null || sbiLabelPar.equals(""))
											break;
									}								
								}
							}
						}
						contOutPar ++;
					} 
				}

			}catch(Exception e){
				logger.error("Error while getting informations about documents ", e);
			}
			logger.debug("OUT");
		}
	}

	/**
	 * Gets the documents array.
	 * 
	 * @return the documents array
	 */
	public List getDocumentsArray() {
		Collection collDocs = documentsMap.values();
		List retDocs = new ArrayList();
		Object[] arrDocs = (Object[])collDocs.toArray();
		try{
			int numDocAdded = 0;
			while (numDocAdded < arrDocs.length){
				for(int i=0; i < arrDocs.length; i++){
					Document tmpDoc =(Document) arrDocs[i];
					if (tmpDoc.getNumOrder() == numDocAdded){
						retDocs.add(tmpDoc);
						numDocAdded ++;
					}
				}
			}
		}catch(Exception e){
			System.out.println(e);
		}
		return retDocs;

	}

	private HashMap getMapFromString(String strToConvert){
		HashMap retHash = new LinkedHashMap();
		String[] tmpStr = strToConvert.split(",");
		for (int i=0; i < tmpStr.length; i++){
			String key = tmpStr[i].substring(0, tmpStr[i].indexOf("="));
			String value = tmpStr[i].substring(tmpStr[i].indexOf("=")+1);
			retHash.put(key, value);
		}
		return retHash;
	}

	private Integer[] getVideoDimensions(String type, Integer value){
		Integer[] realDimensions = null;
		int pos = 0;
		if (type.equalsIgnoreCase("width")){
			for (int i=0; i<generalWidthDimensions.length; i++){
				if (value.compareTo(generalWidthDimensions[i])==0){
					String labelDim = "widthPxValues"+generalWidthDimensions[i].toString();
					realDimensions = (Integer[])lstDimensions.get(labelDim);
					break;
				}
				else if (value.compareTo(generalWidthDimensions[i])>0){
					if (i>0)
						pos = i-1;
					String labelDim = "widthPxValues"+generalWidthDimensions[pos].toString();
					realDimensions = (Integer[])lstDimensions.get(labelDim);
					break;
				}
			}
		}
		else if (type.equalsIgnoreCase("height")){
			for (int i=0; i<generalHeightDimensions.length; i++){
				if (value.compareTo(generalHeightDimensions[i])==0){
					String labelDim = "heightPxValues"+generalHeightDimensions[i].toString();
					realDimensions = (Integer[])lstDimensions.get(labelDim);
					break;
				}
				else if (value.compareTo(generalHeightDimensions[i])>0){
					if (i>0)
						pos = i-1;
					String labelDim = "heightPxValues"+generalHeightDimensions[pos].toString();
					realDimensions = (Integer[])lstDimensions.get(labelDim);
					break;
				}
			}
		}

		return realDimensions;
	}

	/**
	 * Gets the lst url.
	 * 
	 * @return the lst url
	 */
	public Map getLstUrl() {
		return lstUrl;
	}

	/**
	 * Sets the lst url.
	 * 
	 * @param lstUrl the new lst url
	 */
	public void setLstUrl(Map lstUrl) {
		this.lstUrl = lstUrl;
	}

	/**
	 * Gets the lst div style.
	 * 
	 * @return the lst div style
	 */
	public Map getLstDivStyle() {
		return lstDivStyle;
	}

	/**
	 * Sets the lst style.
	 * 
	 * @param lstDivStyle the new lst style
	 */
	public void setLstStyle(Map lstDivStyle) {
		this.lstDivStyle = lstDivStyle;
	}

	/**
	 * Gets the lst url params.
	 * 
	 * @return the lst url params
	 */
	public Map getLstUrlParams() {
		return lstUrlParams;
	}

	/**
	 * Sets the lst url params.
	 * 
	 * @param lstUrlParams the new lst url params
	 */
	public void setLstUrlParams(Map lstUrlParams) {
		this.lstUrlParams = lstUrlParams;
	}

	/**
	 * Gets the lst doc linked.
	 * 
	 * @return the lst doc linked
	 */
	public Map getLstDocLinked() {
		return lstDocLinked;
	}

	/**
	 * Sets the lst doc linked.
	 * 
	 * @param lstDocLinked the new lst doc linked
	 */
	public void setLstDocLinked(Map lstDocLinked) {
		this.lstDocLinked = lstDocLinked;
	}

	/**
	 * Gets the lst field linked.
	 * 
	 * @return the lst field linked
	 */
	public Map getLstFieldLinked() {
		return lstFieldLinked;
	}

	/**
	 * Sets the lst field linked.
	 * 
	 * @param lstFieldLinked the new lst field linked
	 */
	public void setLstFieldLinked(Map lstFieldLinked) {
		this.lstFieldLinked = lstFieldLinked;
	}

	/**
	 * Gets the lst panel style.
	 * 
	 * @return the lst panel style
	 */
	public Map getLstPanelStyle() {
		return lstPanelStyle;
	}

	/**
	 * @return the lstDocTitles
	 */
	public Map getLstDocTitles() {
		return lstDocTitles;
	}

	/**
	 * @param lstDocTitles the lstDocTitles to set
	 */
	public void setLstDocTitles(Map lstDocTitles) {
		this.lstDocTitles = lstDocTitles;
	}

	/**
	 * @return the lstDocExport
	 */
	public Map getLstDocExport() {
		return lstDocExport;
	}

	/**
	 * @param lstDocExport the lstDocExport to set
	 */
	public void setLstDocExport(Map lstDocExport) {
		this.lstDocExport = lstDocExport;
	}

	/**
	 * Sets the lst panel style.
	 * 
	 * @param lstPanelStyle the new lst panel style
	 */
	public void setLstPanelStyle(Map lstPanelStyle) {
		this.lstPanelStyle = lstPanelStyle;
	}

	/**
	 * @return the lstDocCross
	 */
	public Map getLstCrossLinked() {
		return lstCrossLinked;
	}

	/**
	 * @param lstDocCross the lstDocCross to set
	 */
	public void setLstCrossLinked(Map lstCrossLinked) {
		this.lstCrossLinked = lstCrossLinked;
	}

	public Map getDocumentsMap() {
		return documentsMap;
	}

	public void setDocumentsMap(Map documentsMap) {
		this.documentsMap = documentsMap;
	}

	public Integer getVideoHeight() {
		return videoHeight;
	}

	public void setVideoHeight(Integer videoHeight) {
		this.videoHeight = videoHeight;
	}

	public Integer getVideoWidth() {
		return videoWidth;
	}

	public void setVideoWidth(Integer videoWidth) {
		this.videoWidth = videoWidth;
	}
	
	private Document createExternalDocument (String params){
		//the input parameter params contains only the configuration of the refreshed parameters, others params are added post
		Document toReturn = new Document();	
		String objLabel = "";
		int idxPar = 0; 
		String[] attributes = params.split(",");
		//set the number that identify the document within of hash table
		int idxDoc = documentsMap.size();
		toReturn.setNumOrder(idxDoc);	
		Properties param = new Properties();
		//loop on attributes for create a document for external url
		for (int j=0; j<attributes.length; j++) {
			String label = attributes[j];
			label = label.replace("{", "");
			label = label.replace("}", "");
			if (label.trim().startsWith("refresh_doc_linked")){
				objLabel = label.substring(label.indexOf("=")+1);
				toReturn.setSbiObjLabel(objLabel);				
			}
			if (label.trim().startsWith("refresh_par_linked")){
				param.setProperty("sbi_par_label_param_"+idxDoc+"_"+idxPar, label.substring(label.indexOf("=")+1));
			}
			if (label.trim().startsWith("default_value_linked")){
				param.setProperty("default_value_param_"+idxDoc+"_"+idxPar, label.substring(label.indexOf("=")+1));
			}
			param.setProperty("type_par_"+idxDoc+"_"+idxPar, "IN");
			
		}
		idxPar++;
		//add other input parameter of the crossed doc not refreshed (will get values by the request)
		try{
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(objLabel);
			List objParams = obj.getBiObjectParameters();
			for (int j=0; j<objParams.size(); j++) {
				BIObjectParameter par = (BIObjectParameter)objParams.get(j);
				if (!param.containsValue(par.getParameterUrlName())) {
					param.setProperty("sbi_par_label_param_"+idxDoc+"_"+idxPar, par.getParameterUrlName());
					param.setProperty("default_value_param_"+idxDoc+"_"+idxPar, "");
					param.setProperty("type_par_"+idxDoc+"_"+idxPar, "IN");
					idxPar++;
				}
			}
		}catch(Exception ex){
			logger.error("Error while create external document: " + ex);
			return null;
		}

		toReturn.setTypeCross(Constants.CROSS_EXTERNAL);
		toReturn.setParams(param);
		addDocument(toReturn);
		return toReturn;
	}

   private String getCrossType(String paramsAttr){
	   String[] params = paramsAttr.split(",");
	   String toReturn  = Constants.CROSS_INTERNAL;

		//loop on parameters
		for (int k=0; k<params.length; k++) {
			String label = params[k];
			label = label.replace("{", "");
			label = label.replace("}", "");
			if (label.trim().startsWith("type_cross_linked")){
				toReturn = label.substring(label.indexOf("=")+1);
				break;
			}
		}
		return toReturn;
   }
}