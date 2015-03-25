/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.chart.bo.charttypes.piecharts;

/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */


import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.awt.Font;
import java.util.List;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

public class SimplePie extends PieCharts{

	boolean threeD=false; //false is 2D, true is 3D
	boolean isThreedViewConfigured=false;
	boolean percentage=false;
	boolean isPercentageConfigured=false;


	public static final String CHANGE_VIEW_3D_LABEL="Set View Dimension";
	public static final String CHANGE_VIEW_3D_LABEL1="Set 2D";
	public static final String CHANGE_VIEW_3D_LABEL2="Set 3D";


	public static final String CHANGE_VIEW_3D="threeD";

	public static final String CHANGE_VIEW_PERCENTAGE_LABEL="Set Percentage Mode";
	public static final String CHANGE_VIEW_PERCENTAGE_LABEL1="Absolute Values";
	public static final String CHANGE_VIEW_PERCENTAGE_LABEL2="Percentage Values";


	public static final String CHANGE_VIEW_PERCENTAGE="percentage";



	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.piecharts.PieCharts#configureChart(it.eng.spago.base.SourceBean)
	 */
	public void configureChart(SourceBean content) {
		// TODO Auto-generated method stub
		super.configureChart(content);
		if(confParameters.get("dimensions")!=null){	
			String orientation=(String)confParameters.get("dimensions");
			if(orientation.equalsIgnoreCase("3D")){
				threeD=true;
				isThreedViewConfigured=true;
			}
			else if(orientation.equalsIgnoreCase("2D")){
				threeD=false;
				isThreedViewConfigured=true;
			}
		}
		if(confParameters.get("values")!=null){	
			String orientation=(String)confParameters.get("values");
			if(orientation.equalsIgnoreCase("percentage")){
				percentage=true;
				isPercentageConfigured=true;
			}
			else if(orientation.equalsIgnoreCase("absolute")){
				percentage=false;
				isPercentageConfigured=true;
			}
		}

		
		
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.piecharts.PieCharts#createChart(java.lang.String, org.jfree.data.general.Dataset)
	 */
	public JFreeChart createChart(DatasetMap datasets) {

		Dataset dataset=(Dataset)datasets.getDatasets().get("1");
		
		JFreeChart chart=null; 

		if(!threeD){
			chart = ChartFactory.createPieChart(
					name,  
					(PieDataset)dataset,             // data
					legend,                // include legend
					true,
					false
			);


			chart.setBackgroundPaint(color);

			TextTitle title = chart.getTitle();
			title.setToolTipText("A title tooltip!");


			PiePlot plot = (PiePlot) chart.getPlot();
			plot.setLabelFont(new Font(defaultLabelsStyle.getFontName(), Font.PLAIN, defaultLabelsStyle.getSize()));
			plot.setCircular(false);
			plot.setLabelGap(0.02);
			plot.setNoDataMessage("No data available");

			if(percentage==false){
				plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0} ({1})"));}
			else
			{
				plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0} ({2})"));
			}

		}
		else{
			chart = ChartFactory.createPieChart3D(
					name,  
					(PieDataset)dataset,             // data
					true,                // include legend
					true,
					false
			);


			chart.setBackgroundPaint(color);

			TextTitle title = chart.getTitle();
			title.setToolTipText("A title tooltip!");


			PiePlot3D plot = (PiePlot3D) chart.getPlot();

			plot.setDarkerSides(true);
			plot.setStartAngle(290);
			plot.setDirection(Rotation.CLOCKWISE);
			plot.setForegroundAlpha(1.0f);
			plot.setDepthFactor(0.2);

			plot.setLabelFont(new Font(defaultLabelsStyle.getFontName(), Font.PLAIN, defaultLabelsStyle.getSize()));
			// plot.setNoDataMessages("No data available");
			plot.setCircular(false);
			plot.setLabelGap(0.02);
			plot.setNoDataMessage("No data available");

			if(percentage==false){
				plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0} ({1})"));}
			else
			{
				plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
				"{0} ({2})"));
			}
		}

		TextTitle title =setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if(subName!= null && !subName.equals("")){
			TextTitle subTitle =setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}


		return chart;

	}




	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#isChangeableView()
	 */
	public boolean isChangeableView() {
		return true;	
	}





	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#setChangeViewsParameter(java.lang.String, boolean)
	 */
	public void setChangeViewsParameter(String changePar, boolean how) {
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_3D)){
			threeD=how;
		}
		else if(changePar.equalsIgnoreCase(CHANGE_VIEW_PERCENTAGE)){
			percentage =how;
		}


	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getChangeViewParameter(java.lang.String)
	 */
	public boolean getChangeViewParameter(String changePar) {
		boolean ret=false;
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_3D)){
			ret=threeD;
		}
		else if(changePar.equalsIgnoreCase(CHANGE_VIEW_PERCENTAGE)){
			ret=percentage;
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getChangeViewParameterLabel(java.lang.String, int)
	 */
	public String getChangeViewParameterLabel(String changePar, int i) {
		String ret="";
		if(changePar.equalsIgnoreCase(CHANGE_VIEW_3D)){
			if(i==0) ret=CHANGE_VIEW_3D_LABEL;
			else if(i==1) ret=CHANGE_VIEW_3D_LABEL1;
			else if(i==2) ret=CHANGE_VIEW_3D_LABEL2;
		}
		else if(changePar.equalsIgnoreCase(CHANGE_VIEW_PERCENTAGE)){
			if(i==0) ret=CHANGE_VIEW_PERCENTAGE_LABEL;
			else if(i==1) ret=CHANGE_VIEW_PERCENTAGE_LABEL1;
			else if(i==2) ret=CHANGE_VIEW_PERCENTAGE_LABEL2;
		}

		return ret;
	}


	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#getPossibleChangePars()
	 */
	public List getPossibleChangePars() {
		List l=new Vector();
		if(!isThreedViewConfigured)	{l.add(CHANGE_VIEW_3D); }
		if(!isPercentageConfigured){ l.add(CHANGE_VIEW_PERCENTAGE); }
		return l;
	}


}
