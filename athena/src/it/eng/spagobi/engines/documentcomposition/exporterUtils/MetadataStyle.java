/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.documentcomposition.exporterUtils;

import java.util.Map;

import it.eng.spagobi.engines.documentcomposition.configuration.DocumentCompositionConfiguration;
import it.eng.spagobi.engines.exporters.DocumentCompositionExporter;

import org.apache.log4j.Logger;
import org.apache.lucene.search.FieldComparator.DocComparator;

/**
SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.

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




public class MetadataStyle {

	private int x;
	private int y;
	private int width=-1;
	private int height=-1;

	private static transient Logger logger=Logger.getLogger(MetadataStyle.class);


	/** If fails to read style return NULL
	 * 
	 * @param style
	 */


	public static MetadataStyle getMetadataStyle(String docLabel, String style, DocumentCompositionConfiguration docCompConf) {
		logger.debug("IN");
		int xT=-1;
		int yT=-1;
		int widthT=-1;
		int heightT=-1;

		try{
			int indexTop=style.indexOf("top:");

			int indexOfPXAfterTop=style.indexOf("px;", indexTop);
			String topValue=style.substring(indexTop+4, indexOfPXAfterTop);

			String leftValue="";
			int indexLeft=style.indexOf("left:");
			if(indexLeft==-1){
				indexLeft=style.indexOf("margin:");
				int indexOfPXAfterLeft=style.indexOf("px;", indexLeft);
				leftValue=style.substring(indexLeft+7, indexOfPXAfterLeft);

			}
			else{
				int indexOfPXAfterLeft=style.indexOf("px;", indexLeft);
				leftValue=style.substring(indexLeft+5, indexOfPXAfterLeft);
			}
			xT=Integer.valueOf(leftValue).intValue();
			yT=Integer.valueOf(topValue).intValue();

			// first tries to read configuration s from object DocCOmp, if fails read from style String

			Map<String, String> listPanelStyle=docCompConf.getLstPanelStyle();

			// ties to get information, form is like WIDTH_500|HEIGHT_308
			String styleSheet=listPanelStyle.get("STYLE__"+docLabel);
			if(styleSheet!=null){
				try{
					int indexOf = styleSheet.indexOf('|');
					String widthS=styleSheet.substring(6,indexOf);			
					String heightS=styleSheet.substring(indexOf+8);
					//System.out.println("width "+width+" height "+height);
					widthT=Integer.valueOf(widthS).intValue();	
					heightT=Integer.valueOf(heightS).intValue();
				}
				catch (Exception e) {
					logger.warn("COuld not retrieve width and height from document composition configuration objects, get from style string");		

				}
			}

			if(widthT==-1 || heightT==-1){

				int indexWidth=style.indexOf("width:");
				int indexOfPXAfterWidth=style.indexOf("px;", indexWidth);
				String widthValue=style.substring(indexWidth+6, indexOfPXAfterWidth);


				int indexHeight=style.indexOf("height:");
				int indexOfPXAfterHeight=style.indexOf("px;", indexHeight);
				String heightValue=style.substring(indexHeight+7, indexOfPXAfterHeight);


				widthT=Integer.valueOf(widthValue).intValue();
				heightT=Integer.valueOf(heightValue).intValue();

			}

		}
		catch (Exception e) {
			logger.error("Error in reading  metadata style for document "+docLabel);
			return null;
		}

		MetadataStyle metadataStyle=new MetadataStyle();
		metadataStyle.x=xT;
		metadataStyle.y=yT;
		metadataStyle.width=widthT;
		metadataStyle.height=heightT;
		logger.debug("OUT");
		return metadataStyle;
	}

	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int _width) {
		this.width = _width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int heightPercentage) {
		this.height = height;
	}



}
