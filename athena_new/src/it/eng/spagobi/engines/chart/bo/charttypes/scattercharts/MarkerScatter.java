/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.scattercharts;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;


public class MarkerScatter extends ScatterCharts {

	String xRangeLow=null;
	String xRangeHigh=null;
	String yRangeLow=null;
	String yRangeHigh=null;
	String xMarkerStartInt=null;
	String xMarkerEndInt=null;
	String yMarkerStartInt=null;
	String yMarkerEndInt=null;
	String xMarkerValue=null;
	String yMarkerValue=null;
	String xMarkerColor=null;
	String yMarkerColor=null;
	String xMarkerIntColor=null;
	String yMarkerIntColor=null;
	String xMarkerLabel=null;
	String yMarkerLabel=null;
	
	
	
	private static transient Logger logger=Logger.getLogger(MarkerScatter.class);
	
	public void configureChart(SourceBean content) {
		logger.debug("IN");
		super.configureChart(content);
		SourceBean param = (SourceBean)content.getAttribute("CONF");
		
		List parameters = content.getAttributeAsList("PARAMETER");
		if(parameters==null){
			parameters =content.getAttributeAsList("CONF.PARAMETER");
		}
		if(parameters!=null){
 
			for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
				SourceBean att = (SourceBean) iterator.next();
				String name=(att.getAttribute("name")==null)?"":(String)att.getAttribute("name");
				if (name.equalsIgnoreCase("x_range")){
					xRangeLow = (att.getAttribute("value_low")==null)?"0":(String)att.getAttribute("value_low");
					xRangeHigh =(att.getAttribute("value_high")==null)?"0":(String)att.getAttribute("value_high");
				}
				if (name.equalsIgnoreCase("y_range")){
					yRangeLow = (att.getAttribute("value_low")==null)?"0":(String)att.getAttribute("value_low");
					yRangeHigh =(att.getAttribute("value_high")==null)?"0":(String)att.getAttribute("value_high");
				}
				if (name.equalsIgnoreCase("x_marker")){
					xMarkerStartInt = (att.getAttribute("value_start_int")==null)?"0":(String)att.getAttribute("value_start_int");
					xMarkerEndInt = (att.getAttribute("value_end_int")==null)?"0":(String)att.getAttribute("value_end_int");
					xMarkerValue = (att.getAttribute("value_marker")==null)?"0":(String)att.getAttribute("value_marker");
					xMarkerColor = (att.getAttribute("color")==null)?"0":(String)att.getAttribute("color");
					xMarkerIntColor = (att.getAttribute("color_int")==null)?"0":(String)att.getAttribute("color_int");
					xMarkerLabel = (att.getAttribute("label")==null)?"":(String)att.getAttribute("label");
				}
				if (name.equalsIgnoreCase("y_marker")){
					yMarkerStartInt = (att.getAttribute("value_start_int")==null)?"0":(String)att.getAttribute("value_start_int");
					yMarkerEndInt = (att.getAttribute("value_end_int")==null)?"0":(String)att.getAttribute("value_end_int");
					yMarkerValue = (att.getAttribute("value_marker")==null)?"0":(String)att.getAttribute("value_marker");
					yMarkerColor = (att.getAttribute("color")==null)?"0":(String)att.getAttribute("color");
					yMarkerIntColor = (att.getAttribute("color_int")==null)?"0":(String)att.getAttribute("color_int");
					yMarkerLabel = (att.getAttribute("label")==null)?"":(String)att.getAttribute("label");
				}
			}
		}
		
		logger.debug("OUT");	
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
				if (getSubName()!= null && getSubName().indexOf("$F") >= 0){
					setSubTitleParameter(atts);
				}
				if (yMarkerLabel != null && yMarkerLabel.indexOf("$F{") >= 0){
					setYMarkerLabel(atts);
				}
				first=false;
			}
			
			//defines real dimension of attributes x1,y1,x2,y2...the number must be the same for x and y column
			int  numAttsX = 0;
			int  numAttsY = 0;
			
			for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();

				String name=new String(object.getKey());
				if(!name.equalsIgnoreCase("x")) {
					if (String.valueOf(name.charAt(0)).equalsIgnoreCase("x")) 				
						numAttsX++;
					else if (String.valueOf(name.charAt(0)).equalsIgnoreCase("y")) 		
						numAttsY++;
				}
			}
			int  maxNumAtts = (numAttsX < numAttsY)? numAttsY : numAttsX;
			//double[] x=new double[atts.size()];
			//double[] y=new double[atts.size()];
			double[] x=new double[maxNumAtts];
			double[] y=new double[maxNumAtts];

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
					
					//String pos=name;
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
						//num++;
						x[num]=valueD;

						if(firstX){
							xTempMin=valueD;
							xTempMax=valueD;
							firstX=false;
						}
						if(valueD<xMin)xMin=valueD;
						if(valueD>xMax)xMax=valueD;


					}
					else if(pos.equalsIgnoreCase("y")){
						y[num]=valueD;

						if(firstY){
							yTempMin=valueD;
							yTempMax=valueD;
							firstY=false;
						}
						if(valueD<yMin)yMin=valueD;
						if(valueD>yMax)yMax=valueD;		
					}
				}
			}

			double[][] seriesT = new double[][] { y, x};

			dataset.addSeries(catValue, seriesT);
			series.add(catValue);
			//add annotations on the chart if requested
			if (viewAnnotations != null && viewAnnotations.equalsIgnoreCase("true")){
					double tmpx = seriesT[1][0];
					double tmpy = seriesT[0][0];
					annotationMap.put(catValue, String.valueOf(tmpx)+"__"+String.valueOf(tmpy));
			}
		}
		logger.debug("OUT");
		DatasetMap datasets=new DatasetMap();
		datasets.addDataset("1",dataset);
		return datasets;
	}

	public JFreeChart createChart(DatasetMap datasets) {

		DefaultXYDataset dataset=(DefaultXYDataset)datasets.getDatasets().get("1");

		JFreeChart chart = ChartFactory.createScatterPlot(
				name, yLabel, xLabel, dataset, 
				PlotOrientation.HORIZONTAL, false, true, false);

		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		chart.setBackgroundPaint(Color.white);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setForegroundAlpha(0.65f);
		

		XYItemRenderer renderer = plot.getRenderer();

		//defines colors 
		int seriesN=dataset.getSeriesCount();
		if((colorMap!=null && colorMap.size() > 0) || (defaultColor != null && !defaultColor.equals(""))){
			for (int i = 0; i < seriesN; i++) {
				String serieName=(String)dataset.getSeriesKey(i);
				Color color = new Color(Integer.decode(defaultColor).intValue());
				if (colorMap != null && colorMap.size() > 0) color=(Color)colorMap.get(serieName);
				if(color!=null) renderer.setSeriesPaint(i, color);					
			}
		}
		
	   
	   // add un interval  marker for the Y axis...
		if (yMarkerStartInt != null && yMarkerEndInt != null && !yMarkerStartInt.equals("") && !yMarkerEndInt.equals("")){
		    Marker intMarkerY = new IntervalMarker(Double.parseDouble(yMarkerStartInt), Double.parseDouble(yMarkerEndInt));
		    intMarkerY.setLabelOffsetType(LengthAdjustmentType.EXPAND);
		    intMarkerY.setPaint(new Color(Integer.decode((yMarkerIntColor.equals(""))?"0":yMarkerIntColor).intValue()));
		    //intMarkerY.setLabel(yMarkerLabel);
		    intMarkerY.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
		    intMarkerY.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
	        plot.addDomainMarker (intMarkerY, Layer.BACKGROUND);
		}
		 // add un interval  marker for the X axis...
        if (xMarkerStartInt != null && xMarkerEndInt != null && !xMarkerStartInt.equals("") && !xMarkerEndInt.equals("")){
	        Marker intMarkerX = new IntervalMarker(Double.parseDouble(xMarkerStartInt), Double.parseDouble(xMarkerEndInt));
	        intMarkerX.setLabelOffsetType(LengthAdjustmentType.EXPAND);
	        intMarkerX.setPaint(new Color(Integer.decode((xMarkerIntColor.equals(""))?"0":xMarkerIntColor).intValue()));
		    //intMarkerX.setLabel(xMarkerLabel);
		    intMarkerX.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
		    intMarkerX.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
	        plot.addRangeMarker(intMarkerX, Layer.BACKGROUND);
        }
     // add a labelled marker for the Y axis...
        if (yMarkerValue != null && !yMarkerValue.equals("")){        	
	        Marker markerY = new ValueMarker(Double.parseDouble(yMarkerValue));
	        markerY.setLabelOffsetType(LengthAdjustmentType.EXPAND);
	        if (!yMarkerColor.equals(""))  markerY.setPaint(new Color(Integer.decode(yMarkerColor).intValue()));
	        markerY.setLabel(yMarkerLabel);
	        markerY.setLabelFont(new Font("Arial", Font.BOLD, 11));
	        markerY.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
	        markerY.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
	        plot.addDomainMarker(markerY, Layer.BACKGROUND);
        }
     // add a labelled marker for the X axis...
        if (xMarkerValue != null && !xMarkerValue.equals("")){
	        Marker markerX = new ValueMarker(Double.parseDouble(xMarkerValue));
	        markerX.setLabelOffsetType(LengthAdjustmentType.EXPAND);
	        if (!xMarkerColor.equals("")) markerX.setPaint(new Color(Integer.decode(xMarkerColor).intValue()));
	        markerX.setLabel(xMarkerLabel);
	        markerX.setLabelAnchor(RectangleAnchor.BOTTOM_RIGHT);
	        markerX.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
	        plot.addRangeMarker(markerX, Layer.BACKGROUND);
        }
        
        if (xRangeLow != null && !xRangeLow.equals("") && xRangeHigh != null && !xRangeHigh.equals("")){
        	if (Double.valueOf(xRangeLow).doubleValue() > xMin) xRangeLow = String.valueOf(xMin);
            if (Double.valueOf(xRangeHigh).doubleValue() < xMax) xRangeHigh = String.valueOf(xMax);
	        ValueAxis rangeAxis = plot.getRangeAxis();
		    //rangeAxis.setRange(Double.parseDouble(xRangeLow), Double.parseDouble(xRangeHigh));
		    rangeAxis.setRangeWithMargins(Double.parseDouble(xRangeLow), Double.parseDouble(xRangeHigh));
        }
        else{
        	NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    		rangeAxis.setAutoRange(true);
    		rangeAxis.setRange(xMin,xMax);
        }
        
        if (yRangeLow != null && !yRangeLow.equals("") && yRangeHigh != null && !yRangeHigh.equals("")){
        	if (Double.valueOf(yRangeLow).doubleValue() > yMin) yRangeLow = String.valueOf(yMin);
            if (Double.valueOf(yRangeHigh).doubleValue() < yMax) yRangeHigh = String.valueOf(yMax);
            NumberAxis domainAxis = (NumberAxis)plot.getDomainAxis();
		    //domainAxis.setRange(Double.parseDouble(yRangeLow), Double.parseDouble(yRangeHigh));
		    domainAxis.setRangeWithMargins(Double.parseDouble(yRangeLow), Double.parseDouble(yRangeHigh));
		    domainAxis.setAutoRangeIncludesZero(false);
        }
        else{
        	NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
    		domainAxis.setAutoRange(true);
    		domainAxis.setRange(yMin, yMax);
    		domainAxis.setAutoRangeIncludesZero(false);
        }
       
        //add annotations if requested
        if (viewAnnotations != null && viewAnnotations.equalsIgnoreCase("true")){
        	if (annotationMap == null || annotationMap.size() == 0)
        		logger.error("Annotations on the chart are requested but the annotationMap is null!");
        	else{
        		int cont = 1;
        		for (Iterator iterator = annotationMap.keySet().iterator(); iterator.hasNext();) {        			
    				String text = (String) iterator.next();
    				String pos = (String)annotationMap.get(text);
    				double x = Double.parseDouble(pos.substring(0, pos.indexOf("__")));
    				double y = Double.parseDouble(pos.substring(pos.indexOf("__")+2));
    				//default up position
    				XYTextAnnotation annotation = new XYTextAnnotation(text, y-1, x+((text.length()>20)?text.length()/3+1:text.length()/2+1));
    				if (cont % 2 != 0)
    					//dx
    					annotation = new XYTextAnnotation(text, y, x+((text.length()>20)?text.length()/3+1:text.length()/2+1));
    				else
    					//sx
    					//annotation = new XYTextAnnotation(text, y, x-((text.length()%2==0)?text.length():text.length()-1));
    					annotation = new XYTextAnnotation(text, y, x-(text.length()-1));
	        		
	                annotation.setFont(new Font("SansSerif", Font.PLAIN, 11));
	                //annotation.setRotationAngle(Math.PI / 4.0);
	                annotation.setRotationAngle(0.0); // horizontal
	                plot.addAnnotation(annotation);
	                cont ++;
        		}
        		renderer.setShape(new Ellipse2D.Double(-3, -5, 8, 8));
        	}        	
        }
        else  if (viewAnnotations != null && viewAnnotations.equalsIgnoreCase("false")){
    		renderer.setShape(new Ellipse2D.Double(-3, -5, 8, 8));
    	}
        if(legend==true){
			
			drawLegend(chart);
		}
		return chart;
	}

	public void setYMarkerLabel(List atts) {
		try{
			String tmpYLabel=new String(yMarkerLabel);
			if (tmpYLabel.indexOf("$F{") >= 0){
				String fieldName = tmpYLabel.substring(tmpYLabel.indexOf("$F{")+3, tmpYLabel.indexOf("}"));

				for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
					SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();

					String nameP=new String(object.getKey());
					String value=new String((String)object.getValue());
					if(nameP.equalsIgnoreCase(fieldName))
					{
						int pos = tmpYLabel.indexOf("$F{"+fieldName+"}") + (fieldName.length()+4);
						yMarkerLabel = yMarkerLabel.replace("$F{" + fieldName + "}", value);
						tmpYLabel = tmpYLabel.substring(pos);
						break;
					}
				}

			}
		}
		catch (Exception e) {
			logger.error("Error in Y Marker Label");
		}

	}
}
