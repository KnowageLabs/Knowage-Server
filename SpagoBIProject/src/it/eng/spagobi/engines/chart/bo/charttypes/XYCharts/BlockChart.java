/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.XYCharts;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;
import it.eng.spagobi.engines.chart.utils.StyleLabel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;



/**
 * @author chiarelli
 */
public class BlockChart extends XYCharts {
	
	String rootUrl=null;
	String mode="";
	String drillLabel="";
	HashMap drillParameter=null;
	String categoryUrlName="";
	String serieUrlname="";

	boolean cumulative=false;
	HashMap colorMap=null;  // keeps user selected colors
	boolean additionalLabels=false;
	boolean percentageValue=false;
	HashMap catSerLabels=null;
	
	private static transient Logger logger=Logger.getLogger(BlockChart.class);

	
	public void configureChart(SourceBean content) {
		logger.debug("IN");
		super.configureChart(content);
		
		
		SourceBean zrange = (SourceBean)content.getAttribute("ZRANGES");
		if(zrange==null){
			zrange =(SourceBean)content.getAttributeAsList("CONF.ZRANGES");
		}
		SourceBean yrange = (SourceBean)content.getAttribute("YRANGES");
		if(yrange==null){
			yrange =(SourceBean)content.getAttributeAsList("CONF.YRANGES");
		}
		


		

		
		
		// get grid dimensions (changed way in 3.4)		
		Object gridWidth = confParameters.get("grid_width");
		Object gridHeight = confParameters.get("grid_height");
		if(gridWidth != null){
			blockW = gridWidth.toString();
		}
		if(gridHeight != null){
			blockH = gridHeight.toString();
		}
		
		// get x and y range dimensions (changed way in 3.4)
		Object xLow = confParameters.get("xrange_value_low");
		Object xHigh = confParameters.get("xrange_value_high");
		Object yLow = confParameters.get("yrange_value_low");
		Object yHigh = confParameters.get("yrange_value_high");
		if(xLow != null){
			xrangeMin = xLow.toString();
		}
		if(xHigh != null){
			xrangeMax = xHigh.toString();
		}
		if(yLow != null){
			yrangeMin = yLow.toString();
		}
		if(yHigh != null){
			yrangeMax = yHigh.toString();
		}
		
		
		// chenge
//		List confAttrsList2 = content.getAttributeAsList("PARAMETER");

//		Iterator confAttrsIter2 = confAttrsList2.iterator();
//		while(confAttrsIter2.hasNext()) {
//			SourceBean param = (SourceBean)confAttrsIter2.next();
//			String nameParam = (String)param.getAttribute("name");
//			
//			if (nameParam.equals("xrange")){
//				xrangeMin = (String)param.getAttribute("value_low");
//				xrangeMax = (String)param.getAttribute("value_high");
//			}else if (nameParam.equals("yrange")){
//				yrangeMin = (String)param.getAttribute("value_low");
//				yrangeMax = (String)param.getAttribute("value_high");
//			}
//			
//			String valueParam = (String)param.getAttribute("value");
//			confParameters.put(nameParam, valueParam);
//		}	
		
		if(yrange!=null) {		
			List ranges = yrange.getAttributeAsList("RANGE");
			int rangesNum = ranges.size();
			yLabels= new String[rangesNum+1];
			yLabels[0]="";
			Iterator rangesIter = ranges.iterator();
			
			int j = 0;
			while(rangesIter.hasNext()) {
				SourceBean range = (SourceBean)rangesIter.next();
				String nameParam = (String)range.getAttribute("label");				
				String label = "";
				if(nameParam!=null){
					label = nameParam;
				}
				yLabels[j+1]=label;
				j++;				
			}	
		}
		
		if(zrange==null) return;		
		List ranges = zrange.getAttributeAsList("RANGE");
		int rangesNum = ranges.size();
		legendLabels= new String[rangesNum];
		legendLabels[0]="";
		zvalues = new double[rangesNum-1];
		Iterator rangesIter = ranges.iterator();
		
		int j = 0;
		while(rangesIter.hasNext()) {
			SourceBean range = (SourceBean)rangesIter.next();
			String nameParam = (String)range.getAttribute("label");
			String colour = "";
			String label = "";
			label = nameParam;
			
			
			colour = (String)range.getAttribute("colour");
			Color col=new Color(Integer.decode(colour).intValue());
			
			
			if (!nameParam.equals("outbound")){
				String low = (String)range.getAttribute("value_low");
				double lowz = new Double(low).doubleValue();
				String high = (String)range.getAttribute("value_high");
				double highz = new Double(high).doubleValue();
				String low_high = low+","+high;
				legendLabels[j+1]=label;
				colorRangeMap.put(new Double( highz-((highz-lowz))),col);
				zvalues[j]=highz-((highz-lowz));
			}else if (nameParam.equals("outbound")){
				String val = (String)range.getAttribute("value");
				zrangeMax = val;
				outboundColor = "#FFFFFF";
			}
			j++;
			
		}	
		
		

		logger.debug("OUT");
	}

	
    /**
     * Creates a chart for the specified dataset.
     * 
     * @param dataset  the dataset.
     * 
     * @return A chart instance.
     */
	public JFreeChart createChart(DatasetMap datasets) {
    	XYZDataset dataset=(XYZDataset)datasets.getDatasets().get("1");
    	//Creates the xAxis with its label and style
        NumberAxis xAxis = new NumberAxis(xLabel);
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        xAxis.setLabel(xLabel);
        if(addLabelsStyle!=null && addLabelsStyle.getFont()!=null){
	        xAxis.setLabelFont(addLabelsStyle.getFont());
	        xAxis.setLabelPaint(addLabelsStyle.getColor());
        }
        //Creates the yAxis with its label and style
        NumberAxis yAxis = new NumberAxis(yLabel);
       
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setInverted(false);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        yAxis.setTickLabelsVisible(true);
        yAxis.setLabel(yLabel);
        if(addLabelsStyle!=null && addLabelsStyle.getFont()!=null){
        	yAxis.setLabelFont(addLabelsStyle.getFont());
        	yAxis.setLabelPaint(addLabelsStyle.getColor());
        }
       yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        Color outboundCol = new Color(Integer.decode(outboundColor).intValue());
        
        //Sets the graph paint scale and the legend paintscale
        LookupPaintScale paintScale = new LookupPaintScale(zvalues[0], (new Double(zrangeMax)).doubleValue(),outboundCol);
        LookupPaintScale legendPaintScale = new LookupPaintScale(0.5, 0.5+zvalues.length, outboundCol);
        
        for (int ke=0; ke<=(zvalues.length-1) ; ke++){
        	Double key =(new Double(zvalues[ke]));
        	Color temp =(Color)colorRangeMap.get(key);
        	paintScale.add(zvalues[ke],temp);
        	legendPaintScale.add(0.5+ke, temp);
        }     
        //Configures the renderer
        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setPaintScale(paintScale);
        double blockHeight =	(new Double(blockH)).doubleValue();
        double blockWidth =	(new Double(blockW)).doubleValue();
        renderer.setBlockWidth(blockWidth);
        renderer.setBlockHeight(blockHeight);
        
        //configures the plot with title, subtitle, axis ecc.
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.black);
        plot.setRangeGridlinePaint(Color.black);
        plot.setDomainCrosshairPaint(Color.black);
        
        plot.setForegroundAlpha(0.66f);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        JFreeChart chart = new JFreeChart(plot);
        TextTitle title =setStyleTitle(name, styleTitle);
        chart.setTitle(title);
        if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        
        //Sets legend labels
        SymbolAxis scaleAxis = new SymbolAxis(null,legendLabels);
        scaleAxis.setRange(0.5, 0.5+zvalues.length);
        scaleAxis.setPlot(new PiePlot());
        scaleAxis.setGridBandsVisible(false);
        scaleAxis.setLabel(zLabel);
        //scaleAxis.setLabelAngle(3.14/2);
        scaleAxis.setLabelFont(addLabelsStyle.getFont());
        scaleAxis.setLabelPaint(addLabelsStyle.getColor());
      
        //draws legend as chart subtitle
        PaintScaleLegend psl = new PaintScaleLegend(legendPaintScale, scaleAxis);
        psl.setAxisOffset(2.0);
        psl.setPosition(RectangleEdge.RIGHT);
        psl.setMargin(new RectangleInsets(5, 1, 5, 1));        
        chart.addSubtitle(psl);
        
        if(yLabels!=null){
	        //Sets y legend labels
	        LookupPaintScale legendPaintScale2 = new LookupPaintScale(0, (yLabels.length-1), Color.white);
	        
	        for (int ke=0; ke<yLabels.length ; ke++){
	        	Color temp =Color.white;
	        	legendPaintScale2.add(1+ke, temp);
	        } 
	        
	        SymbolAxis scaleAxis2 = new SymbolAxis(null,yLabels);
	        scaleAxis2.setRange(0, (yLabels.length-1));
	        scaleAxis2.setPlot(new PiePlot());
	        scaleAxis2.setGridBandsVisible(false);
	      
	        //draws legend as chart subtitle
	        PaintScaleLegend psl2 = new PaintScaleLegend(legendPaintScale2, scaleAxis2);
	        psl2.setAxisOffset(5.0);
	        psl2.setPosition(RectangleEdge.LEFT);
	        psl2.setMargin(new RectangleInsets(8, 1, 40, 1));   
	        psl2.setStripWidth(0);
	        psl2.setStripOutlineVisible(false);
	        chart.addSubtitle(psl2);
        }
        
        return chart;
    }    

	
	
	
	
	
	
	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");
		
		
		
		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);

		// XYZDataset dataset = createDataset();
		int xMaxValue = (new Integer(xrangeMax)).intValue();
		int xMinValue = (new Integer(xrangeMin)).intValue();
		int yMaxValue = (new Integer(yrangeMax)).intValue();
		int yMinValue = (new Integer(yrangeMin)).intValue();
		int blockWidth =(new Integer(blockW)).intValue();
		int blockHeight =	(new Integer(blockH)).intValue();
	

		SourceBean sbRows=SourceBean.fromXMLString(res);
		List listAtts=sbRows.getAttributeAsList("ROW");

		DefaultXYZDataset dataset = new DefaultXYZDataset();
		int rangex = (xMaxValue-xMinValue)/blockWidth;
		int rangey = (yMaxValue-yMinValue)/blockHeight;
		
		double[] xvalues = new double[rangey * rangex];
        double[] yvalues = new double[rangey * rangex];        
        double[] zvalues = new double[rangey * rangex];
        
        
        double[][] data = new double[][] {xvalues, yvalues, zvalues};
        
        int xVal = 0;
        int yVal = 0;
		int col = 0;
		int row = 0;
		int cell = 0;
		double zVal = 0;
		
		boolean first=true;
		
		
    	for (int r = yMinValue/blockHeight; r < rangey; r++)  {
			for (int c = xMinValue/blockWidth; c < rangex; c ++) {
            	
            	cell = c+r+(r*(rangex-1));
            	data[0][cell] = (new Double(((c+1)*blockWidth)).doubleValue())-(new Double(blockWidth).doubleValue()/2);
    			data[1][cell] = (new Double(((r+1)*blockHeight)).doubleValue())-(new Double(blockHeight).doubleValue()/2);
    			data[2][cell] = (new Double(zrangeMax)).doubleValue()*2;
            }
        }
		
		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBean category = (SourceBean) iterator.next();
			List atts=category.getContainedAttributes();

			String nameP="";
			String value="";
			
			
			for (Iterator iterator2 = atts.iterator(); iterator2.hasNext();) {
				SourceBeanAttribute object = (SourceBeanAttribute) iterator2.next();

				nameP=new String(object.getKey());
				value=new String((String)object.getValue());
				if(nameP.equalsIgnoreCase("x"))
				{						
						xVal = new Double(value).intValue();	
						col = (xVal/blockWidth)-1;
				}
				if(nameP.equalsIgnoreCase("y"))
				{
					    yVal = new Double(value).intValue();	
					    row = (yVal/blockHeight)-1;
				}
				if(nameP.equalsIgnoreCase("z"))
				{
						zVal = new Double(value).doubleValue();				
				}			    
			   
				
				}
			cell=col+row+(row*(rangex-1));
			if((rangex*rangey)> cell){
				data[0][cell] = xVal-(new Double(blockWidth).doubleValue()/2);
				data[1][cell] = yVal-(new Double(blockHeight).doubleValue()/2);
				data[2][cell] = zVal;				
				  // setValueInData(data, xMaxValue , yMinValue, xVal, yVal, zVal);
			}
			
			}
			
        dataset.addSeries("Series 1", data);
        
        //XYZDataset dataset = createDataset();
        
		DatasetMap datasets=new DatasetMap();
		datasets.addDataset("1",dataset);
		logger.debug("OUT");
		return datasets;
	}

	
	
}
