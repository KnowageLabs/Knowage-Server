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
package it.eng.spagobi.engines.chart.bo.charttypes.XYCharts;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.bo.ChartImpl;
import it.eng.spagobi.engines.chart.bo.charttypes.barcharts.BarCharts;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;
import it.eng.spagobi.engines.chart.utils.StyleLabel;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYZDataset;

/**
 * @author chiarelli
 */
public class XYCharts extends ChartImpl{

	String xLabel="";
	String yLabel="";
	String zLabel="";
	
	String xrangeMin="";
	String xrangeMax="";
	String yrangeMin="";
	String yrangeMax="";

	String blockW ="";
	String blockH = "";
	String outboundColor = "";
	
	
	String zrangeMin="";	
	String zrangeMax="";	
	String[] legendLabels =null;
	String[] yLabels =null;
	double[] zvalues = null;
	String colours = "";
	String add_labels = "false";
	
	Map confParameters;
	HashMap colorRangeMap=new HashMap();  // keeps user selected colors// serie position - color

	private static transient Logger logger=Logger.getLogger(BarCharts.class);
	StyleLabel addLabelsStyle;

	/**
	 * Inherited by IChart: calculates chart value.
	 * 
	 * @return the dataset
	 * 
	 * @throws Exception the exception
	 */


		/**
		 * Utility method called by createDataset().
		 * 
		 * @param data  the data array.
		 * @param c  the column.
		 * @param r  the row.
		 * @param value  the value.
		 */
		private static void setValueInData(double[][] data, int xMaxValue, int yStartValue, int c, int r, double value) {
		    
		    data[0][(r - yStartValue) * xMaxValue + c] = c;
		    data[1][(r - yStartValue) * xMaxValue + c] = r;
		    data[2][(r - yStartValue) * xMaxValue + c] = value;
		    
		}
	

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
				xLabel="X";
			}
			if(confParameters.get("y_label")!=null){	
				yLabel=(String)confParameters.get("y_label");
			}
			else
			{
				yLabel="Y";
			}
			if(confParameters.get("z_label")!=null){	
				zLabel=(String)confParameters.get("z_label");
			}
			else
			{
				zLabel="Z";
			}
			
			
			
			SourceBean styleLabelsSB = (SourceBean)content.getAttribute("STYLE_LABELS");
			if(styleLabelsSB!=null){

				String fontS = (String)content.getAttribute("STYLE_LABELS.font");
				String sizeS = (String)content.getAttribute("STYLE_LABELS.size");
				String colorS = (String)content.getAttribute("STYLE_LABELS.color");
				String orientationS = (String)content.getAttribute("STYLE_LABELS.orientation");


				try{
					Color color=Color.decode(colorS);
					int size=Integer.valueOf(sizeS).intValue();
					addLabelsStyle=new StyleLabel(fontS,size,color,orientationS);

				}
				catch (Exception e) {
					logger.error("Wrong style labels settings, use default");
				}

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

}
