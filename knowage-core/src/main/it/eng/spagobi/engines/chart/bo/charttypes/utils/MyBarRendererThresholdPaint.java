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

import it.eng.spagobi.engines.chart.bo.charttypes.targetcharts.WinLose;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * 
 * @author gavardi
 * Extension of barRenderer for targetCharts, regulates the colors
 *
 */
public class MyBarRendererThresholdPaint extends BarRenderer {

	boolean useTargets=true;
	HashMap<Double, TargetThreshold> thresholds=null;
	DefaultCategoryDataset dataset=null;
	TimeSeries timeSeries=null;
	Vector<String> nullValues=null;
	Color background=Color.WHITE;
	TargetThreshold bottomThreshold=null;
	private static transient Logger logger=Logger.getLogger(MyBarRendererThresholdPaint.class);


	public MyBarRendererThresholdPaint(boolean useTargets,
			HashMap<Double, TargetThreshold> thresholds,
			DefaultCategoryDataset dataset, 
			TimeSeries timeSeries,
			Vector<String> nullValues,
			TargetThreshold bottomThreshold,
			Color background) {
		super();
		this.useTargets = useTargets;
		this.thresholds = thresholds;
		this.dataset = dataset;
		this.timeSeries = timeSeries;
		this.nullValues=nullValues;
		this.background=background;
		this.bottomThreshold=bottomThreshold;
	}



	public Paint getItemPaint(int row, int column) {
		logger.debug("IN");
		String columnKey=(String)dataset.getColumnKey(column);
		int separator=columnKey.indexOf('-');
		String month=columnKey.substring(0,separator);
		String year=columnKey.substring(separator+1);
		Number value=dataset.getValue(row, column); 		// value put in dataset (- 0.5 or 0.5)

		Month currentMonth=new Month(Integer.valueOf(month),Integer.valueOf(year));   // value for that month
		TimeSeriesDataItem item = timeSeries.getDataItem(currentMonth);

		if(nullValues.contains(columnKey)){
			return background;
		}
		// If no item is retrieved means that no value was specified for that month in that year
		if(item==null || item.getValue()==null){
			return background;
		}

		Double currentValue=(Double)item.getValue();

		TreeSet<Double> orderedThresholds=new TreeSet<Double>(thresholds.keySet());

		Double thresholdGiveColor=null;		
		// if dealing with targets, begin from first target and go to on till the current value is major
		if(useTargets){
			boolean stop=false;
			for (Iterator iterator = orderedThresholds.iterator(); iterator.hasNext() && stop==false;) {
				Double currentThres = (Double) iterator.next();
				if(currentValue>=currentThres){
					thresholdGiveColor=currentThres;
				}
				else{
					stop=true;
				}
			}
			//previous threshold is the right threshold that has been passed, if it is null means that we are in the bottom case
		}
		else if(!useTargets){ 
			// if dealing with baseline, begin from first baseline and go to the last; 
			// opposite case than targets, it gets the next baseline
			boolean stop=false;
			for (Iterator iterator = orderedThresholds.iterator(); iterator.hasNext() && stop==false;) {
				Double currentThres = (Double) iterator.next();
				if(currentValue>currentThres){
				}
				else{
					stop=true;
					thresholdGiveColor=currentThres;
				}
			}
			if(stop==false) { // means that current value was > than last baselines, so we are in the bottom case
				thresholdGiveColor=null;
			}
		}


		// ******* Get the color *************
		Color colorToReturn=null;
		if(thresholdGiveColor==null){ //bottom case
			if(bottomThreshold!=null && bottomThreshold.getColor()!=null){
				colorToReturn=bottomThreshold.getColor();				
			}
			if(colorToReturn==null){
				colorToReturn=Color.BLACK;
			}
		}
		else{
			if(thresholds.get(thresholdGiveColor)!=null && thresholds.get(thresholdGiveColor).getColor()!=null)
			colorToReturn=thresholds.get(thresholdGiveColor).getColor();
			if(colorToReturn==null){
				colorToReturn=Color.BLACK;
			}

		}
		logger.debug("OUT");
		return colorToReturn;
	}




}