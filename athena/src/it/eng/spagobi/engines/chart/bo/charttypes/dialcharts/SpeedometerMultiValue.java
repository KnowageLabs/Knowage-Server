/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.dialcharts;


import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.KpiInterval;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyDialPlot;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;


/**
 * A class to generate Speedometer charts with multi-needle
 * @author Antonella Giachino
 * 
 */
public class SpeedometerMultiValue extends DialCharts{


	private static transient Logger logger=Logger.getLogger(SpeedometerMultiValue.class);

	
	double 	increment=0.0;
	int 	minorTickCount=0;
	boolean dialtextuse = false ;
	String 	dialtext = "";
	String  serieLegend=null;  	// keeps the name of the last serie that views the legend (only the last serie!)
	HashMap colorMap=null;  	// keeps user selected colors
	Vector 	intervals;
	Vector 	seriesNames=null;	//list series names
	Vector 	valuesNames = null; //list values names
	LegendItemCollection legendItems = null;

	
	/** Tag to color values*/
	public static final String VALUES_COLORS = "VALUES_COLORS";
	
	
	/** CONF PARAMETERS: */
	public static final String MINOR_TICK = "minor_tick";
	public static final String ORIENTATION = "orientation";
	public static final String INCREMENT = "increment";
	public static final String DIALTEXTUSE = "dialtextuse"; // true or false
	public static final String DIALTEXT = "dialtext";
	
	
	/**
	 * Instantiates a new sBI speedometer.
	 */
	public SpeedometerMultiValue() {
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

		super.configureChart(content);

		logger.debug("IN");

		if(!isLovConfDefined){
			logger.debug("Configuration set in template");
			if(confParameters.get(INCREMENT)!=null){	
				String increment=(String)confParameters.get(INCREMENT);
				setIncrement(Double.valueOf(increment).doubleValue());
			}
			else {
				logger.error("increment not defined");
				return;
			}
			if(confParameters.get(MINOR_TICK)!=null){	
				String minorTickCount=(String)confParameters.get(MINOR_TICK);
				setMinorTickCount(Integer.valueOf(minorTickCount).intValue());
			}
			else {
				setMinorTickCount(10);
			}
			
			if(confParameters.get(DIALTEXTUSE)!=null){	
				String dialtextusetemp=(String)confParameters.get(DIALTEXTUSE);
				if(dialtextusetemp.equalsIgnoreCase("true")){
					dialtextuse = true;
				}
				else dialtextuse = false;
			}
			
			if(dialtextuse && confParameters.get(DIALTEXT)!=null){
				dialtext=(String)confParameters.get(DIALTEXT);
			}


			//reading intervals information
			SourceBean intervalsSB = (SourceBean)content.getAttribute("INTERVALS");
			List intervalsAttrsList=null;
			if(intervalsSB!=null){
				intervalsAttrsList = intervalsSB.getContainedSourceBeanAttributes();
			}

			if(intervalsAttrsList==null || intervalsAttrsList.isEmpty()){ // if intervals are not defined realize a single interval
				logger.warn("intervals not defined; default settings");
				KpiInterval interval=new KpiInterval();
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
						interval.setColor(Color.WHITE);
					}
					addInterval(interval);
				}
			}
			
			//reading values colors if present
			SourceBean colors = (SourceBean)content.getAttribute(VALUES_COLORS);
			if(colors!=null){
				colorMap=new HashMap();
				List atts=colors.getContainedAttributes();
				String colorValue="";
				for (Iterator iterator = atts.iterator(); iterator.hasNext();) {
					SourceBeanAttribute object = (SourceBeanAttribute) iterator.next();

					String valueName=new String(object.getKey());
					colorValue=new String((String)object.getValue());
					Color col=new Color(Integer.decode(colorValue).intValue());
					if(col!=null){
						colorMap.put(valueName,col); 
					}
				}	
			}
		}
		else{
			logger.debug("configuration defined in LOV "+confDataset);
			String increment=(String)sbRow.getAttribute("increment");
			String minorTickCount=(String)sbRow.getAttribute("minor_tick");
			setIncrement(Double.valueOf(increment).doubleValue());
			setMinorTickCount(Integer.valueOf(minorTickCount).intValue());

		
			String intervalsNumber=(String)sbRow.getAttribute(INTERVALS_NUMBER);
			if(intervalsNumber==null || intervalsNumber.equals("") || intervalsNumber.equals("0")){ // if intervals are not specified
				KpiInterval interval=new KpiInterval();
				interval.setMin(getLower());
				interval.setMax(getUpper());
				interval.setColor(Color.WHITE);
				addInterval(interval);
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
			//reading values colors if present
			String valuesNumber=(String)sbRow.getAttribute("series_number");
			colorMap=new HashMap();
			if(valuesNumber!=null && !valuesNumber.equals("") && !valuesNumber.equals("0")){ 
				for(int i=1;i<=Integer.valueOf(valuesNumber).intValue();i++){
					String valueName=(String)sbRow.getAttribute("value_name"+(new Integer(i)).toString());
					String colorValue=(String)sbRow.getAttribute("value_color"+(new Integer(i)).toString());
					Color col=new Color(Integer.decode(colorValue).intValue());
					if(col!=null){
						colorMap.put(valueName,col); 
					}	
				}
			}
		}
		logger.debug("out");
	}


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

		DatasetMap datasets = new DatasetMap();
		

		SourceBean sbRows=SourceBean.fromXMLString(res);
		List listAtts=sbRows.getAttributeAsList("ROW");
		
		seriesNames=new Vector();
		valuesNames=new Vector();
		boolean first=true;

		// run all dataset rows
		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBean sbSeries = (SourceBean) iterator.next();
			List atts=sbSeries.getContainedAttributes();

			HashMap series = new LinkedHashMap();
			HashMap serValue = new LinkedHashMap();

			String name="";
			String value="";
			


			int contSer = 0;
			//run all the attributes in a row, to define series 
			for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();

				name=new String(object.getKey());
				value=new String((String)object.getValue());
				
				if (name.startsWith("serie")){
						// map containing the series
						series.put(name, value);
						contSer++;
				}
				else {
					serValue.put(name, value);
					if(!valuesNames.contains(name))
						valuesNames.add(name);
				}
			}
			
			// add series to dataset 
			for (Iterator iterator3 = series.keySet().iterator(); iterator3.hasNext();) {
				String nameS = (String) iterator3.next();

				String valueS=(String)series.get(nameS);
				if(valueS!=null && !valueS.equals("null") && !valueS.equals("")){
					Object[] arSer = serValue.keySet().toArray();
					for (int i=0; i<arSer.length;i++){
						datasets.getDatasets().put(valueS + "__" + arSer[i],new DefaultValueDataset(Double.valueOf((String)serValue.get(arSer[i]))));
					}
					if(!seriesNames.contains(valueS)){
						seriesNames.add(valueS);
					}
				}				
			}
			
		}
		serieLegend = (String)seriesNames.get(seriesNames.size()-1);
		logger.debug("OUT");
		
		
		
		return datasets;
	}




	/**
	 * Creates a chart of type speedometer.
	 * 
	 * @param chartTitle  the chart title.
	 * @param dataset  the dataset.
	 * 
	 * @return A chart speedometer.
	 */

	public JFreeChart createChart(DatasetMap datasets) {
		logger.debug("IN");
		JFreeChart chart = null;
		try{
			MyDialPlot plot = new MyDialPlot();
			
			HashMap hmDataset = datasets.getDatasets();
			Set keyDataset = hmDataset.keySet();
			int i = 0;
			
			Iterator itDataset = keyDataset.iterator();
			while(itDataset.hasNext()) {
				String key = (String) itDataset.next();
				Dataset dataset = (Dataset)hmDataset.get(key);
				plot.setDataset(i, (ValueDataset)dataset);
				if (key.indexOf("__") > 0)
					setName(key.substring(0, key.indexOf("__")));
				else
					setName(key);
				i++;
			}
	        
			plot.setDialFrame(new StandardDialFrame());
	
			plot.setBackground(new DialBackground());
			
			if(dialtextuse){
				DialTextAnnotation annotation1 = new DialTextAnnotation(dialtext);			
				annotation1.setFont(styleTitle.getFont());
				annotation1.setRadius(0.7);
	
				plot.addLayer(annotation1);
			}
			
			StandardDialScale scale = new StandardDialScale(lower, 
					upper, -120, -300, 10.0, 4);
			
			if(! ( increment > 0) ){
				logger.warn("increment cannot be less than 0, put default to 0.1 ");
				increment=0.1;
			}
			
			scale.setMajorTickIncrement(increment);
			scale.setMinorTickCount(minorTickCount);
			scale.setTickRadius(0.88);
			scale.setTickLabelOffset(0.15);
			//set tick label style
			scale.setTickLabelsVisible(true);
			Font tickLabelsFont = new Font(labelsTickStyle.getFontName(), Font.PLAIN, labelsTickStyle.getSize());
			scale.setTickLabelFont(tickLabelsFont);
			scale.setTickLabelPaint(labelsTickStyle.getColor());
			
			
			plot.addScale(0, scale);
	
			DialCap cap = new DialCap();
			plot.setCap(cap);
	
			// sets intervals
			for (Iterator iterator = intervals.iterator(); iterator.hasNext();) {
				KpiInterval interval = (KpiInterval) iterator.next();
				StandardDialRange range = new StandardDialRange(interval.getMin(), interval.getMax(), 
						interval.getColor()); 
				range.setInnerRadius(0.50);
				range.setOuterRadius(0.85);
				
				range.setPaint(interval.getColor());
				
				plot.addLayer(range);
	
			}

			plot.setBackground(new DialBackground());

			logger.debug("Set values color");
			Vector arValuesName =  getValuesNames();
			legendItems = new LegendItemCollection();
			for (int j = 0; j < arValuesName.size(); j++) {
				DialPointer.Pin p = new DialPointer.Pin(j);
				if(colorMap!=null){
					String valueName=(String)arValuesName.get(j);
					Color color=(Color)colorMap.get(valueName);
					if(color!=null) p.setPaint(color);	
									
					if (serieLegend.equalsIgnoreCase(name)){
						if (j < arValuesName.size()){
							LegendItem item = new LegendItem(valueName, "", "", "", new Ellipse2D.Double(-3, -5, 8, 8),color );
							if (item != null) {
					        	legendItems.add(item);
					        }
						}
						if (legend)
							super.height = super.height + (super.height*12/100);
					}
				}
				plot.addLayer(p);  
			}
			
			plot.setLegendItems(legendItems);
			chart = new JFreeChart(name, JFreeChart.DEFAULT_TITLE_FONT,plot, false);
			
			TextTitle title = setStyleTitle(name, styleTitle);
			chart.setTitle(title);
			if(subName!= null && !subName.equals("")){
				TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
				chart.addSubtitle(subTitle);
			}
			
			
			
			chart.setBackgroundPaint(color);
			if(legend==true) drawLegend(chart);
			
		}catch (Exception ex){
			logger.debug("Error while creating speedometer multivalue: " + ex);
		}
		logger.debug("OUT");
		return chart;
	}

	/**
	 * @return the valuesNames
	 */
	public Vector getValuesNames() {
		return valuesNames;
	}

	/**
	 * @param valuesNames the valuesNames to set
	 */
	public void setValuesNames(Vector valuesNames) {
		this.valuesNames = valuesNames;
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
	 * Defines the legend for multivalue Speedometer
	 * @return LegendItemCollection
	 */
	private LegendItemCollection createLegend(){
		LegendItemCollection legendItems = new LegendItemCollection();
		
		return legendItems;
	}





}
