/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.scattercharts;
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


import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.bo.ChartImpl;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;


/**   @author Antonella Giachino
 *     antonella.giachino@eng.it
 */




public class ScatterCharts extends ChartImpl {

	Map confParameters;
	Vector series;
	String xLabel="";
	String yLabel="";
	Vector currentSeries=null;
	String viewAnnotations="";
	HashMap colorMap=new HashMap();
	LinkedMap annotationMap = new LinkedMap();
	String defaultColor = "";

	double xMin;
	double xMax;
	double yMin;
	double yMax;


	private static transient Logger logger=Logger.getLogger(ScatterCharts.class);


	/**
	 * Inherited by IChart: calculates chart value.
	 * 
	 * @return the dataset
	 * 
	 * @throws Exception the exception
	 */

	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");
		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);

		DefaultXYDataset dataset = new DefaultXYDataset(); 

		SourceBean sbRows=SourceBean.fromXMLString(res);
		List listAtts=sbRows.getAttributeAsList("ROW");

		series=new Vector();

		boolean firstX=true;
		boolean firstY=true;
		double xTempMax=0.0;
		double xTempMin=0.0;
		double yTempMax=0.0;
		double yTempMin=0.0;
		boolean first=true;

		// In list atts there are all the series, let's run each
		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBean serie = (SourceBean) iterator.next();
			List atts=serie.getContainedAttributes();

			String catValue="";
			String serValue="";

			if(first){
				if (name.indexOf("$F{") >= 0){
					setTitleParameter(atts);
				}
				if (getSubName()!= null &&  getSubName().indexOf("$F") >= 0){
					setSubTitleParameter(atts);
				}
				first=false;
			}

			double[] x=new double[atts.size()];
			double[] y=new double[atts.size()];

			//List x=new ArrayList();
			//List y=new ArrayList();
			//ArrayList z=new ArrayList();

			String name="";
			String value="";

			//run all the attributes of the serie
			for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();

				name=new String(object.getKey());
				value=(((String)object.getValue()).equals("null"))?"0":new String((String)object.getValue());

				if(name.equalsIgnoreCase("x"))
				{
					catValue=value;


				}

				else if(String.valueOf(name.charAt(0)).equalsIgnoreCase("x") ||
						String.valueOf(name.charAt(0)).equalsIgnoreCase("y")) {
					String pos=String.valueOf(name.charAt(0));
					String numS=name.substring(1);
					int num=Integer.valueOf(numS).intValue();

					double valueD=0.0;
					try{
						valueD=(Double.valueOf(value)).doubleValue();
					}
					catch (NumberFormatException e) {
						Integer intero=Integer.valueOf(value);
						valueD=intero.doubleValue();

					}


					if(pos.equalsIgnoreCase("x")){
						x[num]=valueD;

						if(firstX){
							xTempMin=valueD;
							xTempMax=valueD;
							firstX=false;
						}
						if(valueD<xTempMin)xTempMin=valueD;
						if(valueD>xTempMax)xTempMax=valueD;


					}
					else if(pos.equalsIgnoreCase("y")){
						y[num]=valueD;

						if(firstY){
							yTempMin=valueD;
							yTempMax=valueD;
							firstY=false;
						}
							if(valueD<yTempMin)yTempMin=valueD;
							if(valueD>yTempMax)yTempMax=valueD;		


					}
				}

			}
			
			xMin=xTempMin;
			xMax=xTempMax;
			
			yMin=yTempMin;
			yMax=yTempMax;
			

			double[][] seriesT = new double[][] { y, x};

			dataset.addSeries(catValue, seriesT);
			series.add(catValue);
			//add annotations on the chart if requested
			if (viewAnnotations != null && viewAnnotations.equalsIgnoreCase("true")){
				annotationMap.put(catValue, seriesT);
			}

		}
		logger.debug("OUT");
		DatasetMap datasets=new DatasetMap();
		datasets.addDataset("1",dataset);
		return datasets;
	}


	/**
	 * Calculates chart value;
	 * 
	 * 
	 * public Dataset calculateValue(String cat, Map parameters) throws Exception {
	 * logger.debug("IN");
	 * String res=DataSetAccessFunctions.getDataSetResult(profile, getData(),parameters);
	 * 
	 * DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	 * 
	 * SourceBean sbRows=SourceBean.fromXMLString(res);
	 * List listAtts=sbRows.getAttributeAsList("ROW");
	 * 
	 * 
	 * // run all categories (one for each row)
	 * categoriesNumber=0;
	 * for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
	 * SourceBean category = (SourceBean) iterator.next();
	 * List atts=category.getContainedAttributes();
	 * 
	 * HashMap series=new HashMap();
	 * String catValue="";
	 * 
	 * String name="";
	 * String value="";
	 * 
	 * //run all the attributes, to define series!
	 * for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
	 * SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();
	 * 
	 * name=new String(object.getKey());
	 * value=new String((String)object.getValue());
	 * if(name.equalsIgnoreCase("x"))catValue=value;
	 * else series.put(name, value);
	 * }
	 * for (Iterator iterator3 = series.keySet().iterator(); iterator3.hasNext();) {
	 * String nameS = (String) iterator3.next();
	 * String valueS=(String)series.get(nameS);
	 * dataset.addValue(Double.valueOf(valueS).doubleValue(), nameS, catValue);
	 * categoriesNumber=categoriesNumber+1;
	 * }
	 * 
	 * }
	 * logger.debug("OUT");
	 * return dataset;
	 * }
	 * 
	 * @param content the content
	 */

	public void configureChart(SourceBean content) {
		logger.debug("IN");
		super.configureChart(content);
		confParameters = new HashMap();
		SourceBean confSB = (SourceBean)content.getAttribute("CONF");

		if(confSB==null) return;
		List confAttrsList = confSB.getAttributeAsList("PARAMETER");
        
		Iterator confAttrsIter = confAttrsList.iterator();
		while(confAttrsIter.hasNext()) {
			SourceBean param = (SourceBean)confAttrsIter.next();
			String nameParam = (String)param.getAttribute("name");
			String valueParam = (String)param.getAttribute("value");
			confParameters.put(nameParam, valueParam);
		}	

		if(confParameters.get("x_label")!=null){	
			xLabel=(String)confParameters.get("x_label");
		}
		else
		{
			xLabel="x";
		}

		if(confParameters.get("y_label")!=null){	
			yLabel=(String)confParameters.get("y_label");
		}
		else
		{
			yLabel="y";
		}
		
		//reading default color if present
		if(confParameters.get("default_color")!=null){	
			defaultColor=(String)confParameters.get("default_color");
		}
		else{
			defaultColor="";
		}
		
		//reading series colors if present
		SourceBean colors = (SourceBean)content.getAttribute("SERIES_COLORS");
		if(colors==null){
			colors = (SourceBean)content.getAttribute("CONF.SERIES_COLORS");
		}
		if(colors!=null){
			colorMap=new HashMap();
			List atts=colors.getContainedAttributes();
			String colorNum="";
			String colorSerie="";
			String num="";
			for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();

				String seriesName=new String(object.getKey());

				colorSerie=new String((String)object.getValue());
				Color col=new Color(Integer.decode(colorSerie).intValue());
				if(col!=null){
					colorMap.put(seriesName,col); 
				}
			}		

		}
		
		//reading annotations choice if present
		if(confParameters.get("view_annotation")!=null){	
			viewAnnotations=(String)confParameters.get("view_annotation");
		}
		else
		{
			viewAnnotations="";
		}


		logger.debug("OUT");
	}

	/**
	 * Gets the conf parameters.
	 * 
	 * @return the conf parameters
	 */
	public Map getConfParameters() {
		return confParameters;
	}

	/**
	 * Sets the conf parameters.
	 * 
	 * @param confParameters the new conf parameters
	 */
	public void setConfParameters(Map confParameters) {
		this.confParameters = confParameters;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#createChart(java.lang.String, org.jfree.data.general.Dataset)
	 */
	public JFreeChart createChart(DatasetMap dataset) {
		// TODO Auto-generated method stub
		return super.createChart(dataset);
	}




	public Vector getSeries() {
		return series;
	}




	public void setSeries(Vector series) {
		this.series = series;
	}




	public String getXLabel() {
		return xLabel;
	}




	public void setXLabel(String label) {
		xLabel = label;
	}




	public String getYLabel() {
		return yLabel;
	}




	public void setYLabel(String label) {
		yLabel = label;
	}




	public static Logger getLogger() {
		return logger;
	}




	public static void setLogger(Logger logger) {
		ScatterCharts.logger = logger;
	}




	public Vector getCurrentSeries() {
		return currentSeries;
	}




	public void setCurrentSeries(Vector currentSeries) {
		this.currentSeries = currentSeries;
	}
}
