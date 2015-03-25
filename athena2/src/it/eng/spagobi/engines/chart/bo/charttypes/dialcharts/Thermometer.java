/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/** Configure and draw a thermomether chart
 *  * @author Giulio Gavardi
 * 
 */

package it.eng.spagobi.engines.chart.bo.charttypes.dialcharts;


import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.KpiInterval;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.RectangleInsets;

/**
 * 
 * @author Giulio Gavardi
 * 
 */


public class Thermometer extends DialCharts{


	private static transient Logger logger=Logger.getLogger(Thermometer.class);

	private	Vector intervals=null;
	private String units="";

	/** CONF PARAMETERS: */
	public static final String UNIT = "unit";

	/** POSSIBLE VALUES TO SET FOR UNIT PARAMETER: */
	public static final String FAHRENHEIT = "FAHRENHEIT";
	public static final String CELCIUS = "CELCIUS";
	public static final String KELVIN = "KELVIN";

	/** VALUES TO SET LABEL FOR SUBRANGES */
	public static final String NORMAL = "NORMAL";
	public static final String WARNING = "WARNING";
	public static final String CRITICAL = "CRITICAL";


	/**
	 * Instantiates a new thermometer.
	 */
	public Thermometer() {
		super();
		intervals=new Vector();
	}


	/**
	 * Creates a chart of type thermometer.
	 * 
	 * @param chartTitle  the chart title.
	 * @param dataset  the dataset.
	 * 
	 * @return A chart thermometer.
	 */


	public JFreeChart createChart(DatasetMap datasets) {
		logger.debug("IN");
		Dataset dataset=(Dataset)datasets.getDatasets().get("1");

		ThermometerPlot plot = new ThermometerPlot((ValueDataset)dataset);
		JFreeChart chart = new JFreeChart(name, JFreeChart.DEFAULT_TITLE_FONT,	plot, true);               
		chart.setBackgroundPaint(color);
	
		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

		
		plot.setInsets(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setPadding(new RectangleInsets(10.0, 10.0, 10.0, 10.0));
		plot.setThermometerStroke(new BasicStroke(2.0f));
		plot.setThermometerPaint(Color.lightGray);
		plot.setGap(3);
		plot.setValueLocation(3);
		plot.setValuePaint(labelsValueStyle.getColor());
		plot.setValueFont(new Font(labelsValueStyle.getFontName(), Font.PLAIN, labelsValueStyle.getSize()));

		plot.setRange(lower, upper);


		if(units.equalsIgnoreCase(FAHRENHEIT))plot.setUnits(ThermometerPlot.UNITS_FAHRENHEIT);	
		else if(units.equalsIgnoreCase(CELCIUS)) plot.setUnits(ThermometerPlot.UNITS_CELCIUS);	
		else if(units.equalsIgnoreCase(KELVIN)) plot.setUnits(ThermometerPlot.UNITS_KELVIN);	
		else plot.setUnits(ThermometerPlot.UNITS_NONE);


		// set subranges	
		for (Iterator iterator = intervals.iterator(); iterator.hasNext();){
			KpiInterval subrange = (KpiInterval) iterator.next();
			int range=0;
			if(subrange.getLabel().equalsIgnoreCase(NORMAL))range=(ThermometerPlot.NORMAL);
			else if(subrange.getLabel().equalsIgnoreCase(WARNING))range=(ThermometerPlot.WARNING);
			else if(subrange.getLabel().equalsIgnoreCase(CRITICAL))range=(ThermometerPlot.CRITICAL);

			plot.setSubrange(range, subrange.getMin(), subrange.getMax());
			if(subrange.getColor()!=null){
				plot.setSubrangePaint(range, subrange.getColor());
			}
			//plot.setDisplayRange(subrange.getRange(), subrange.getLower(), subrange.getUpper());	
		}
		//plot.setFollowDataInSubranges(true);
		logger.debug("OUT");

		return chart;       
	}




	/**
	 * set parameters for the creation of the chart getting them from template or from LOV.
	 * 
	 * @param content the content of the template.
	 * 
	 * @return A chart that displays a value as a dial.
	 */


	public void configureChart(SourceBean content) {
		logger.debug("IN");
		super.configureChart(content);

		if(!isLovConfDefined){

			if(confParameters.get(UNIT)!=null){	
				String unit=(String)confParameters.get(UNIT);
				setUnits(unit);
			}
			else
				setUnits("");


			//reading intervals information
			SourceBean subrangesSB = (SourceBean)content.getAttribute("INTERVALS");
			if(subrangesSB==null){
				subrangesSB = (SourceBean)content.getAttribute("CONF.INTERVALS");
			}
			List subrangesAttrsList=null;
			if(subrangesSB!=null){
				subrangesAttrsList = subrangesSB.getContainedSourceBeanAttributes();
			}

			if(subrangesAttrsList==null || subrangesAttrsList.isEmpty()){ // if subranges are not defined 
				logger.error("subranges not correctly defined");			}
			else{	

				Iterator subrangesAttrsIter = subrangesAttrsList.iterator();
				while(subrangesAttrsIter.hasNext()) {
					SourceBeanAttribute paramSBA = (SourceBeanAttribute)subrangesAttrsIter.next();
					SourceBean param = (SourceBean)paramSBA.getValue();
					String range= (String)param.getAttribute(LABEL_INTERVAL);
					String min= (String)param.getAttribute(MIN_INTERVAL);
					String max= (String)param.getAttribute(MAX_INTERVAL);
					String col= (String)param.getAttribute(COLOR_INTERVAL);

					KpiInterval subrange=new KpiInterval();

					subrange.setLabel(range);
					subrange.setMin(Double.valueOf(min).doubleValue());
					subrange.setMax(Double.valueOf(max).doubleValue());

					Color color=new Color(Integer.decode(col).intValue());
					if(color!=null){
						subrange.setColor(color);}
					else{
						subrange.setColor(Color.RED);
					}
					addIntervals(subrange);
				}
			}
		}
		else{

			String unit=(String)sbRow.getAttribute(UNIT);
			if(unit!=null)
				setUnits(unit);
			else
				setUnits("");

			String subranges=(String)sbRow.getAttribute("subranges");
			if(subranges!=null && subranges.equalsIgnoreCase("NO")){ // if intervals are not specified
				logger.warn("no subranges defined");
			}
			else{
				for(int i=1;i<=3;i++){
					KpiInterval subrange=new KpiInterval();
					String label=(String)sbRow.getAttribute("label"+(new Integer(i)).toString());
					String min=(String)sbRow.getAttribute("min"+(new Integer(i)).toString());
					String max=(String)sbRow.getAttribute("max"+(new Integer(i)).toString());
					String col=(String)sbRow.getAttribute("color"+(new Integer(i)).toString());

					subrange.setLabel(label);
					subrange.setMin(Double.valueOf(min).doubleValue());
					subrange.setMax(Double.valueOf(max).doubleValue());
					Color color=new Color(Integer.decode(col).intValue());
					subrange.setColor(color);
					addIntervals(subrange);

				}
			}
		}
		logger.debug("OUT");
	}




	/**
	 * Gets the intervals.
	 * 
	 * @return the intervals
	 */
	public Vector getIntervals() {
		return intervals;
	}




	/**
	 * Adds the intervals.
	 * 
	 * @param subrange the subrange
	 */
	public void addIntervals(KpiInterval subrange) {
		this.intervals.add(subrange);
	}




	/**
	 * Gets the units.
	 * 
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}




	/**
	 * Sets the units.
	 * 
	 * @param units the new units
	 */
	public void setUnits(String units) {
		this.units = units;
	}


}
