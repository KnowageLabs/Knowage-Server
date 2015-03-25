/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/** Configure and draw a dialChart
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
import java.awt.GradientPaint;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.ArcDialFrame;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.ValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;

/**
 * 
 * @author Giulio Gavardi
 * 
 */

public class SimpleDial extends DialCharts{

	private static transient Logger logger=Logger.getLogger(SimpleDial.class);

	double increment=0.0;
	int minorTickCount=0;
	Vector intervals;


	boolean horizontalView=false; //false is vertical, true is horizontal
	boolean horizontalViewConfigured=false;
	public static final String CHANGE_VIEW_HORIZONTAL="horizontal";

	public static final String CHANGE_VIEW_LABEL="Set View Orientation";
	public static final String CHANGE_VIEW_LABEL1="Set Vertical View";
	public static final String CHANGE_VIEW_LABEL2="Set Horizontal View";

	/** CONF PARAMETERS: */
	public static final String MINOR_TICK = "minor_tick";
	public static final String ORIENTATION = "orientation";
	public static final String INCREMENT = "increment";


	/**
	 * Instantiates a new simple dial.
	 */
	public SimpleDial() {
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
		logger.debug("IN");
		super.configureChart(content);


		if(!isLovConfDefined){
			if(confParameters.get(INCREMENT)!=null){	
				String increment=(String)confParameters.get(INCREMENT);
				setIncrement(Double.valueOf(increment).doubleValue());
			}
			else {
				logger.error("increment not defined");
				setIncrement(Double.valueOf(1.0).doubleValue());
			}
			if(confParameters.get(MINOR_TICK)!=null){	
				String minorTickCount=(String)confParameters.get(MINOR_TICK);
				setMinorTickCount(Integer.valueOf(minorTickCount).intValue());
			}
			else {
				setMinorTickCount(10);
			}

			if(confParameters.get(ORIENTATION)!=null){	
				String orientation=(String)confParameters.get(ORIENTATION);
				if(orientation.equalsIgnoreCase("vertical")){
					horizontalViewConfigured=true;
					horizontalView=false;
				}
				else if(orientation.equalsIgnoreCase("horizontal")){
					horizontalViewConfigured=true;
					horizontalView=true;
				}
			}


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
				logger.warn("intervals not defined; default settings");
				/*KpiInterval interval=new KpiInterval();
				interval.setMin(getLower());
				interval.setMax(getUpper());
				interval.setColor(Color.white);
				addInterval(interval);*/
			}
			else{	

				Iterator intervalsAttrsIter = intervalsAttrsList.iterator();
				while(intervalsAttrsIter.hasNext()) {
					SourceBeanAttribute paramSBA = (SourceBeanAttribute)intervalsAttrsIter.next();
					SourceBean param = (SourceBean)paramSBA.getValue();
					String min= (String)param.getAttribute(MIN_INTERVAL);
					String max= (String)param.getAttribute(MAX_INTERVAL);
					String col= (String)param.getAttribute(COLOR_INTERVAL);

					KpiInterval interval=new KpiInterval();
					interval.setMin(Double.valueOf(min).doubleValue());
					interval.setMax(Double.valueOf(max).doubleValue());

					Color color=new Color(Integer.decode(col).intValue());
					if(color!=null){
						interval.setColor(color);}
					else{
						// sets default color
						interval.setColor(Color.white);
					}
					addInterval(interval);
				}
			}
		}

		else{
			String increment=(String)sbRow.getAttribute(INCREMENT);
			String minorTickCount=(String)sbRow.getAttribute(MINOR_TICK);

			String orientation="";
			if(sbRow.getAttribute(ORIENTATION)!=null){
				orientation=(String)sbRow.getAttribute(ORIENTATION);
				if(orientation.equalsIgnoreCase("vertical")){
					horizontalView=false;
					horizontalViewConfigured=true;
				}
				else if (orientation.equalsIgnoreCase("horizontal")){
					horizontalView=true;
					horizontalViewConfigured=true;
				}
			}


			setIncrement(Double.valueOf(increment).doubleValue());
			setMinorTickCount(Integer.valueOf(minorTickCount).intValue());			



			String intervalsNumber=(String)sbRow.getAttribute(INTERVALS_NUMBER);
			if(intervalsNumber==null || intervalsNumber.equals("") || intervalsNumber.equals("0")){ // if intervals are not specified
				/*KpiInterval interval=new KpiInterval();
			interval.setMin(getLower());
			interval.setMax(getUpper());
			interval.setColor(Color.WHITE);
			addInterval(interval);*/
			}
			else{
				for(int i=1;i<=Integer.valueOf(intervalsNumber).intValue();i++){
					KpiInterval interval=new KpiInterval();
					String min=(String)sbRow.getAttribute("min"+(new Integer(i)).toString());
					String max=(String)sbRow.getAttribute("max"+(new Integer(i)).toString());
					String col=(String)sbRow.getAttribute("color"+(new Integer(i)).toString());
					interval.setMin(Double.valueOf(min).doubleValue());
					interval.setMax(Double.valueOf(max).doubleValue());
					Color color=new Color(Integer.decode(col).intValue());
					interval.setColor(color);
					addInterval(interval);

				}
			}

		}
		logger.debug("out");
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
		// get data for diagrams
		logger.debug("IN");
		Dataset dataset=(Dataset)datasets.getDatasets().get("1");


		DialPlot plot = new DialPlot();
		plot.setDataset((ValueDataset)dataset);

		ArcDialFrame dialFrame=null;
		if(!horizontalView){
			plot.setView(0.78, 0.37, 0.22, 0.26);     
			dialFrame = new ArcDialFrame(-10.0, 20.0); 
		}
		else{
			plot.setView(0.21, 0.0, 0.58, 0.30);
			dialFrame = new ArcDialFrame(60.0, 60.0);
		}

		dialFrame.setInnerRadius(0.65);
		dialFrame.setOuterRadius(0.90);
		dialFrame.setForegroundPaint(Color.darkGray);
		dialFrame.setStroke(new BasicStroke(3.0f));
		plot.setDialFrame(dialFrame);

		GradientPaint gp = new GradientPaint(new Point(), 
				new Color(255, 255, 255), new Point(), 
				new Color(240, 240, 240));
		DialBackground sdb = new DialBackground(gp);

		GradientPaintTransformType gradientPaintTransformType=GradientPaintTransformType.VERTICAL;
		if(horizontalView){
			gradientPaintTransformType=GradientPaintTransformType.HORIZONTAL;
		}

		sdb.setGradientPaintTransformer(new StandardGradientPaintTransformer(
				gradientPaintTransformType));
		plot.addLayer(sdb);

		if(! ( increment > 0) ){
			logger.warn("increment cannot be less than 0, put default to 0.1 ");
			increment=0.1;
		}


		StandardDialScale scale=null;
		if(!horizontalView){
			scale = new StandardDialScale(lower, upper, -8, 16.0, 
					increment, minorTickCount);
		}
		else{
			scale = new StandardDialScale(lower, upper, 115.0, 
					-50.0, increment, minorTickCount);
		}

		// sets intervals
		for (Iterator iterator = intervals.iterator(); iterator.hasNext();) {
			KpiInterval interval = (KpiInterval) iterator.next();
			StandardDialRange range = new StandardDialRange(interval.getMin(), interval.getMax(), 
					interval.getColor()); 
			range.setInnerRadius(0.70);
			range.setOuterRadius(0.75);
			plot.addLayer(range);

		}

		scale.setTickRadius(0.88);
		scale.setTickLabelOffset(0.07);
		//set tick label style
		Font tickLabelsFont = new Font(labelsTickStyle.getFontName(), Font.PLAIN, labelsTickStyle.getSize());
		scale.setTickLabelFont(tickLabelsFont);
		scale.setTickLabelPaint(labelsTickStyle.getColor());
		//scale.setMajorTickIncrement(25.0);
		plot.addScale(0, scale);

		DialPointer needle = new DialPointer.Pin();
		needle.setRadius(0.82);
		plot.addLayer(needle);
		DialValueIndicator dvi = new DialValueIndicator(0);
		dvi.setFont(new Font(labelsValueStyle.getFontName(), Font.PLAIN, labelsValueStyle.getSize()));
		dvi.setPaint(labelsValueStyle.getColor());
		plot.addLayer(dvi);

		JFreeChart chart1 = new JFreeChart(plot);
		chart1.setBackgroundPaint(color);

		TextTitle title = setStyleTitle(name, styleTitle);
		chart1.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart1.addSubtitle(subTitle);
		}


		logger.debug("OUT");
		return chart1;
	}




	/**
	 * Gets the increment.
	 * 
	 * @return the increment
	 */
	public double getIncrement() {
		return increment;
	}



	/**
	 * Sets the increment.
	 * 
	 * @param increment the new increment
	 */
	public void setIncrement(double increment) {
		this.increment = increment;
	}



	/**
	 * Gets the minor tick count.
	 * 
	 * @return the minor tick count
	 */
	public int getMinorTickCount() {
		return minorTickCount;
	}



	/**
	 * Sets the minor tick count.
	 * 
	 * @param minorTickCount the new minor tick count
	 */
	public void setMinorTickCount(int minorTickCount) {
		this.minorTickCount = minorTickCount;
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
		this.intervals.add(interval);
	}




	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#isChangeableView()
	 */
	public boolean isChangeableView() {
		return true;
	}



	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getPossibleChangePars()
	 */
	public List getPossibleChangePars() {
		List l=new Vector();
		if(!horizontalViewConfigured){
			l.add(CHANGE_VIEW_HORIZONTAL);}

		return l;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#setChangeViewsParameter(java.lang.String, boolean)
	 */
	public void setChangeViewsParameter(String changePar, boolean how) {
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_HORIZONTAL)){
			horizontalView=how;
		}

	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getChangeViewParameter(java.lang.String)
	 */
	public boolean getChangeViewParameter(String changePar) {
		boolean ret=false;
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_HORIZONTAL)){
			ret=horizontalView;
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getChangeViewParameterLabel(java.lang.String, int)
	 */
	public String getChangeViewParameterLabel(String changePar, int i) {
		String ret="";
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_HORIZONTAL)){
			if(i==0)	
				ret=CHANGE_VIEW_LABEL;
			else if(i==1) ret=CHANGE_VIEW_LABEL1;
			else if(i==2) ret=CHANGE_VIEW_LABEL2;

		}
		return ret;
	}




}
