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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * 
 * Class created to remove dependency from BIObjectDAOHibImpl and 
 * it.eng.spagobi.engines.documentcomposition.configuration.DocumentConfigurationConfiguration
 *
 */
public class DocumentCompositionUtil {
	
	private Map documentsMap;
	private String templateFile;
	
	public static final String TEMPLATE_VALUE = "template_value";
	public static final String DOCUMENTS_CONFIGURATION = "DOCUMENTS_CONFIGURATION";

	
	private static transient Logger logger=Logger.getLogger(DocumentCompositionUtil.class);


	/**
	 * Instantiates a new document composition configuration.
	 * 
	 * @param DocumentCompositionConfigurationSB the document composition configuration sb
	 */
	public DocumentCompositionUtil (SourceBean DocumentCompositionConfigurationSB){
		documentsMap = new LinkedHashMap();

		templateFile = (String)DocumentCompositionConfigurationSB.getAttribute(TEMPLATE_VALUE);


		SourceBean documentsConfigurationSB = (SourceBean)DocumentCompositionConfigurationSB.getAttribute(DOCUMENTS_CONFIGURATION);

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
	
	//-----------------------------------------------------------------------------
}
