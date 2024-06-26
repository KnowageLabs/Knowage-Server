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
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.ValueDataset;

/**
 * A class to generate Speedometer charts
 * @author Giulio Gavardi
 * 
 */


public class Meter extends DialCharts{

	private static transient Logger logger=Logger.getLogger(Meter.class);
	Vector intervals;


	/**
	 * Instantiates a new meter.
	 */
	public Meter() {
		super();
		intervals=new Vector();	
	}


	/**
	 * set parameters for the creation of the chart getting them from template or from LOV.
	 * 
	 * @param content the content of the template.
	 * 
	 * @return A chart that displays a value as a dial.
	 */

	public void configureChart(SourceBean content) {
		logger.info("IN");
		super.configureChart(content);

		if(!isLovConfDefined){
			logger.info("Configuration parameters set in template");
			//reading intervals information
			SourceBean intervalsSB = (SourceBean)content.getAttribute("INTERVALS");
			if(intervalsSB==null){
				intervalsSB = (SourceBean)content.getAttribute("CONF.INTERVALS");
			}
			List intervalsAttrsList=null;
			if(intervalsSB!=null){
				intervalsAttrsList = intervalsSB.getContainedSourceBeanAttributes();
			}

			if(intervalsAttrsList==null || intervalsAttrsList.isEmpty()){ // if intervals are not defined realize a single interval
				KpiInterval interval=new KpiInterval();
				interval.setLabel("");
				interval.setMin(getLower());
				interval.setMax(getUpper());
				interval.setColor(Color.WHITE);
				addInterval(interval);
			}
			else{	

				Iterator intervalsAttrsIter = intervalsAttrsList.iterator();
				while(intervalsAttrsIter.hasNext()) {
					SourceBeanAttribute paramSBA = (SourceBeanAttribute)intervalsAttrsIter.next();
					SourceBean param = (SourceBean)paramSBA.getValue();

					String label="";
					if(param.getAttribute("label")!=null)
						label=(String)param.getAttribute("label");
					String min= (String)param.getAttribute(MIN_INTERVAL);
					String max= (String)param.getAttribute(MAX_INTERVAL);
					String col= (String)param.getAttribute(COLOR_INTERVAL);

					KpiInterval interval=new KpiInterval();
					interval.setLabel(label);
					interval.setMin(Double.valueOf(min).doubleValue());
					interval.setMax(Double.valueOf(max).doubleValue());

					Color color=new Color(Integer.decode(col).intValue());
					if(color!=null){
						interval.setColor(color);}
					else{
						interval.setColor(Color.RED);
					}
					addInterval(interval);
				}
			}
		}
		else{
			logger.info("Configuration parameters set in LOV");
			String intervalsNumber=(String)sbRow.getAttribute(INTERVALS_NUMBER);
			if(intervalsNumber==null || intervalsNumber.equals("") || intervalsNumber.equals("0")){ // if intervals are not specified
				logger.warn("intervals not correctly defined, use default settings");
				KpiInterval interval=new KpiInterval();
				interval.setLabel("");
				interval.setMin(getLower());
				interval.setMax(getUpper());
				interval.setColor(Color.WHITE);
				addInterval(interval);
			}
			else{
				for(int i=1;i<=Integer.valueOf(intervalsNumber).intValue();i++){
					KpiInterval interval=new KpiInterval();
					String label="";
					if(sbRow.getAttribute("label"+(new Integer(i)).toString())!=null){
						label=(String)sbRow.getAttribute("label"+(new Integer(i)).toString());}
					String min=(String)sbRow.getAttribute("min"+(new Integer(i)).toString());
					String max=(String)sbRow.getAttribute("max"+(new Integer(i)).toString());
					String col=(String)sbRow.getAttribute("color"+(new Integer(i)).toString());
					interval.setLabel(label);
					interval.setMin(Double.valueOf(min).doubleValue());
					interval.setMax(Double.valueOf(max).doubleValue());
					Color color=new Color(Integer.decode(col).intValue());
					interval.setColor(color);
					addInterval(interval);

				}
			}
		}
		logger.debug("OUT");

	}

	/**
	 * Creates the chart .
	 * 
	 * @param chartTitle  the chart title.
	 * @param dataset  the dataset.
	 * 
	 * @return A chart .
	 */

	public JFreeChart createChart(DatasetMap datasets) {

		Dataset dataset=(Dataset)datasets.getDatasets().get("1");

		MeterPlot plot = new MeterPlot((ValueDataset)dataset);
		plot.setRange(new Range(lower, upper));


		for (Iterator iterator = intervals.iterator(); iterator.hasNext();) {
			KpiInterval interval = (KpiInterval) iterator.next();

			plot.addInterval(new MeterInterval(interval.getLabel(), new Range(interval.getMin(), interval.getMax()), 
					Color.lightGray, new BasicStroke(2.0f), 
					interval.getColor()));
		}

		plot.setNeedlePaint(Color.darkGray);
		plot.setDialBackgroundPaint(Color.white);
		plot.setDialOutlinePaint(Color.gray);
		plot.setDialShape(DialShape.CHORD);
		plot.setMeterAngle(260);
		plot.setTickLabelsVisible(true);
		//set tick label style
		Font tickLabelsFont = new Font(labelsTickStyle.getFontName(), Font.PLAIN, labelsTickStyle.getSize());
		plot.setTickLabelFont(tickLabelsFont);
		plot.setTickLabelPaint(labelsTickStyle.getColor());
		plot.setTickSize(5.0);
		plot.setTickPaint(Color.lightGray);
		if(units!=null){
			plot.setUnits(units);
		}

		plot.setValuePaint(labelsValueStyle.getColor());
		plot.setValueFont(new Font(labelsValueStyle.getFontName(), Font.PLAIN, labelsValueStyle.getSize()));

		JFreeChart chart = new JFreeChart(name, 
				JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
		chart.setBackgroundPaint(color);
		
		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

		return chart;
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
	 * Adds the interval.
	 * 
	 * @param interval the interval
	 */
	public void addInterval(KpiInterval interval) {
		intervals.add(interval);
	}




}







