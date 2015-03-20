/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi.bo;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

public interface IChart {
	
	/**
	 * Creates the chart.
	 * 
	 * @param chartTitle the chart title
	 * @param dataset the dataset
	 * 
	 * @return the j free chart
	 */
	public JFreeChart createChart(DatasetMap dataset);

	/**
	 * Configure chart.
	 * 
	 * @param content the content
	 */
	public void configureChart(SourceBean content);

	/**
	 * Calculate value.
	 * 
	 * @return A map of datasets (usually only one)
	 * 
	 * @throws Exception the exception
	 */
	public DatasetMap calculateValue() throws Exception;

	
	
	
	
	/**
	 * Filter dataset.
	 * 
	 * @param dataset the dataset
	 * @param categories the categories
	 * @param catSelected the cat selected
	 * @param numberCatsVisualization the number cats visualization
	 * 
	 * @return the dataset
	 */
	public Dataset filterDataset(Dataset dataset, HashMap categories, int catSelected, int numberCatsVisualization); 	
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name);
	
	/**
	 * Gets the width.
	 * 
	 * @return the width
	 */
	public int getWidth();
	
	/**
	 * Sets the width.
	 * 
	 * @param width the new width
	 */
	public void setWidth(int width);
	
	/**
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public int getHeight();
	
	/**
	 * Sets the height.
	 * 
	 * @param height the new height
	 */
	public void setHeight(int height);
	
	/**
	 * Gets the data.
	 * 
	 * @return the data
	 */
	public String getData();
	
	/**
	 * Sets the data.
	 * 
	 * @param data the new data
	 */
	public void setData(String data);
	
	/**
	 * Checks if is changeable view.
	 * 
	 * @return true, if is changeable view
	 */
	public boolean isChangeableView();
	
	/**
	 * Sets the change view checked.
	 * 
	 * @param b the new change view checked
	 */
	public void setChangeViewChecked(boolean b);
	
	/**
	 * Checks if is linkable.
	 * 
	 * @return true, if is linkable
	 */
	public boolean isLinkable();
	
	/**
	 * Gets the possible change pars.
	 * 
	 * @return the possible change pars
	 */
	public List getPossibleChangePars();
	
	/**
	 * Sets the change views parameter.
	 * 
	 * @param changePar the change par
	 * @param how the how
	 */
	public void setChangeViewsParameter(String changePar, boolean how);
	
	/**
	 * Gets the change view parameter.
	 * 
	 * @param changePar the change par
	 * 
	 * @return the change view parameter
	 */
	public boolean getChangeViewParameter(String changePar);
	
	/**
	 * Gets the change view parameter label.
	 * 
	 * @param changePar the change par
	 * @param i the i
	 * 
	 * @return the change view parameter label
	 */
	public String getChangeViewParameterLabel(String changePar, int i);	
	
	/**
	 * Checks if is legend.
	 * 
	 * @return true, if is legend
	 */
	public boolean isLegend();
	
	/**
	 * Sets the legend.
	 * 
	 * @param legend the new legend
	 */
	public void setLegend(boolean legend);
	
	/**
	 * Gets the parameters object.
	 * 
	 * @return the parameters object
	 */
	public Map getParametersObject();
	
	/**
	 * Sets the parameters object.
	 * 
	 * @param paramsObject the new parameters object
	 */
	public void setParametersObject(Map paramsObject);
	
	public void setTitleDimension(int d);
	
	public int getTitleDimension();

}
