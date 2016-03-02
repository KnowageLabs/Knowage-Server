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
package it.eng.spagobi.engines.chart.bo.charttypes.utils;

import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.CombinedCategoryBar;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;

public class MyCategoryToolTipGenerator extends StandardCategoryToolTipGenerator {

	boolean enableFreeTip=false;
	HashMap<String, String> categoriesToolTips=null;
	HashMap<String, String> serieToolTips=null;
	HashMap<String, String> seriesCaption=null;
	
	private static transient Logger logger=Logger.getLogger(MyCategoryToolTipGenerator.class);
	
	


	public MyCategoryToolTipGenerator(boolean _enableFreeTip, HashMap<String, String> _serieToolTips, HashMap<String, String> _categoriesToolTips, HashMap<String, String> _seriesCaption) {
		logger.debug("IN");
		enableFreeTip=_enableFreeTip;
		serieToolTips=_serieToolTips;
		categoriesToolTips=_categoriesToolTips;
		seriesCaption=_seriesCaption;
		logger.debug("OUT");
	}


	public String generateToolTip(CategoryDataset dataset, int row, int column) {
		logger.debug("IN");
		//String tooltip=super.generateToolTip(dataset, row, column);
		String rowName="";
		String columnName="";
		try{
			Comparable rowNameC=(String)dataset.getRowKey(row);
			Comparable columnNameC=(String)dataset.getColumnKey(column);
			if(rowNameC!=null)rowName=rowNameC.toString();
			if(columnNameC!=null)columnName=columnNameC.toString();

		}
		catch (Exception e) {
			logger.error("error in recovering name of row and column");
			return "undef";
		}

		// check if there is a predefined FREETIP message
		if(enableFreeTip==true){
			if(categoriesToolTips.get("FREETIP_X_"+columnName)!=null){
				String freeName=categoriesToolTips.get("FREETIP_X_"+columnName);
				return freeName;
			}
		}

		String columnTipName=columnName;
		String rowTipName=rowName;
		// check if tip name are defined, else use standard
		if(categoriesToolTips.get("TIP_X_"+columnName)!=null){
			columnTipName=categoriesToolTips.get("TIP_X_"+columnName);
		}
		// search for series, if seriesCaption has a relative value use it! 
		String serieNameToSearch=null;
		if(seriesCaption!=null){
			serieNameToSearch=seriesCaption.get(rowName);
		}
		if(serieNameToSearch==null)serieNameToSearch=rowName;
				
		if(serieToolTips.get("TIP_"+serieNameToSearch)!=null){
			rowTipName=serieToolTips.get("TIP_"+serieNameToSearch);
		}

		Number num=dataset.getValue(row, column);
		String numS=(num!=null)? " = "+num.toString() : "";
		String toReturn="("+columnTipName+", "+rowTipName+")"+numS;

		logger.debug("OUT");
		return toReturn;
		

	}



}
