/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.boxcharts;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */




public class BoxCharts extends ChartImpl {

	Map confParameters;
	String categoryLabel="";
	String valueLabel="";
	double min;
	double max;

	Integer numberCatVisualization=null;

	//HashMap seriesNumber=null; //track serie name with number position (to preserve color)
	int categoriesNumber=0;
	HashMap categories;
	//int currentSerie=-1;
	//Vector currentSeries=null;

	private static transient Logger logger=Logger.getLogger(BoxCharts.class);


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
		categories=new HashMap();

		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

		SourceBean sbRows=SourceBean.fromXMLString(res);
		List listAtts=sbRows.getAttributeAsList("ROW");

		// run all categories (one for each row)
		categoriesNumber=0;

		boolean first=true;
		boolean first2=true;

		double temp;

		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBean category = (SourceBean) iterator.next();
			List atts=category.getContainedAttributes();

			// atts are all the serie, run through them and sets what you need
			List values=new ArrayList();
			String nameP="";
			String value="";
			String catValue="";

			if(first2){
				if (name.indexOf("$F{") >= 0){
					setTitleParameter(atts);
				}
				if (getSubName()!= null && getSubName().indexOf("$F") >= 0){
					setSubTitleParameter(atts);
				}
				first2=false;
			}
			
			for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();

				nameP=new String(object.getKey());
				value=new String((String)object.getValue());


				if(nameP.equalsIgnoreCase("x"))
				{
					catValue=value;
					categoriesNumber=categoriesNumber+1;
					categories.put(new Integer(categoriesNumber),value);
				}
				else {
					Double valore=Double.valueOf(value);

					// set minimum e maximus to avoid auto range of axis
					if(first){
						min=valore.doubleValue();
						max=valore.doubleValue();
						first=false;
					}
					
					if(min>valore.doubleValue())min=valore.doubleValue();
					if(max<valore.doubleValue())max=valore.doubleValue();
					
					values.add(valore);

				}
			}	
			dataset.add(values, "serie", catValue);
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

		if(confParameters.get("category_label")!=null){	
			categoryLabel=(String)confParameters.get("category_label");
		}
		else
		{
			categoryLabel="category";
		}

		if(confParameters.get("value_label")!=null){	
			valueLabel=(String)confParameters.get("value_label");
		}
		else
		{
			valueLabel="values";
		}

		if(confParameters.get("n_visualization")!=null){	
			String nu=(String)confParameters.get("n_visualization");
			numberCatVisualization=Integer.valueOf(nu);
		}
		else
		{
			numberCatVisualization=new Integer(1);
		}


		logger.debug("OUT");
	}


	/**
	 * Use for slider: limits the categories visualization from cat selected to cat selected+numberscatsVisualization.
	 * 
	 * @param dataset the dataset
	 * @param categories the categories
	 * @param catSelected the cat selected
	 * @param numberCatsVisualization the number cats visualization
	 * 
	 * @return the dataset
	 */

	public Dataset filterDataset(Dataset dataset, HashMap categories, int catSelected, int numberCatsVisualization) {
		logger.debug("IN");
		DefaultCategoryDataset catDataset=(DefaultCategoryDataset)dataset;

		int numCats=categories.size();
		Vector visCat=new Vector();
		// from the choice point to min(chose point+interval, end point)
		//int startPoint=((catSelected-1)*numberCatsVisualization)+1;
		int startPoint=catSelected;

		int endPoint;
		if((startPoint+numberCatsVisualization-1)<=(categories.size()))
			endPoint=startPoint+numberCatsVisualization-1;
		else
			endPoint=categories.size();

		for(int i=(startPoint);i<=endPoint;i++){
			String name=(String)categories.get(new Integer(i));
			visCat.add(name);
		}


		List columns=new Vector(catDataset.getColumnKeys());
		for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
			String col = (String) iterator.next();
			if(!(visCat.contains(col))){
				catDataset.removeColumn(col);
			}			
		}
		logger.debug("OUT");

		return catDataset;

	}

	/**
	 * Limits the dataset to a particular serie.
	 * 
	 * @param dataset the dataset
	 * @param serie the serie
	 * 
	 * @return the dataset
	 */

	/*public Dataset filterDatasetSeries(Dataset dataset, Vector series) {
		logger.debug("IN");
		DefaultCategoryDataset catDataset=(DefaultCategoryDataset)dataset;

		//keeps track of wich series has to be shown
		currentSeries=series;

		//List rowKeys=new Vector();

		List rowKeys=new Vector(catDataset.getRowKeys());

		for (Iterator iterator = rowKeys.iterator(); iterator.hasNext();) {
			String row = (String) iterator.next();
			if(!(series.contains(row))){
				catDataset.removeRow(row);			
			}			
		}

		logger.debug("OUT");
		return catDataset;

	}
	 */



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

	/**
	 * Gets the category label.
	 * 
	 * @return the category label
	 */
	public String getCategoryLabel() {
		return categoryLabel;
	}

	/**
	 * Sets the category label.
	 * 
	 * @param categoryLabel the new category label
	 */
	public void setCategoryLabel(String categoryLabel) {
		this.categoryLabel = categoryLabel;
	}

	/**
	 * Gets the value label.
	 * 
	 * @return the value label
	 */
	public String getValueLabel() {
		return valueLabel;
	}

	/**
	 * Sets the value label.
	 * 
	 * @param valueLabel the new value label
	 */
	public void setValueLabel(String valueLabel) {
		this.valueLabel = valueLabel;
	}

	/**
	 * Gets the categories number.
	 * 
	 * @return the categories number
	 */
	public int getCategoriesNumber() {
		return categoriesNumber;
	}

	/**
	 * Sets the categories number.
	 * 
	 * @param categoriesNumber the new categories number
	 */
	public void setCategoriesNumber(int categoriesNumber) {
		this.categoriesNumber = categoriesNumber;
	}

	/**
	 * Gets the categories.
	 * 
	 * @return the categories
	 */
	public Map getCategories() {
		return categories;
	}



	/**
	 * Gets the number cat visualization.
	 * 
	 * @return the number cat visualization
	 */
	public Integer getNumberCatVisualization() {
		return numberCatVisualization;
	}

	/**
	 * Sets the number cat visualization.
	 * 
	 * @param numberCatVisualization the new number cat visualization
	 */
	public void setNumberCatVisualization(Integer numberCatVisualization) {
		this.numberCatVisualization = numberCatVisualization;
	}







}
