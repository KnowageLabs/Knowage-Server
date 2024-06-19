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

package it.eng.spagobi.engines.chart.bo.charttypes.barcharts;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.DrillParameter;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.FilterZeroStandardCategoryItemLabelGenerator;
import it.eng.spagobi.engines.chart.bo.charttypes.utils.MyCategoryUrlGenerator;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

/**
 * @author Giulio Gavardi giulio.gavardi@eng.it
 */

public class LinkableBar extends BarCharts implements ILinkableChart {

	private static final Logger LOGGER = Logger.getLogger(LinkableBar.class);

	String rootUrl = null;
	String mode = "";
	String drillLabel = "";
	HashMap<String, DrillParameter> drillParametersMap = null;
	String categoryUrlName = "";
	String serieUrlname = "";
	boolean horizontalView = false; // false is vertical, true is horizontal
	boolean horizontalViewConfigured = false;
	/** Orientation of the chart: horizontal, vertical */
	public static final String ORIENTATION = "orientation";
	String drillDocTitle = null;
	String target = "self";

	@Override
	public void configureChart(SourceBean content) {
		LOGGER.debug("IN");
		super.configureChart(content);

		if (confParameters.get(ORIENTATION) != null) {
			String orientation = (String) confParameters.get(ORIENTATION);
			if (orientation.equalsIgnoreCase("vertical")) {
				horizontalViewConfigured = true;
				horizontalView = false;
			} else if (orientation.equalsIgnoreCase("horizontal")) {
				horizontalViewConfigured = true;
				horizontalView = true;
			}
		}

		SourceBean drillSB = (SourceBean) content.getAttribute("DRILL");
		if (drillSB == null) {
			drillSB = (SourceBean) content.getAttribute("CONF.DRILL");
		}
		if (drillSB != null) {
			String lab = (String) drillSB.getAttribute("document");
			if (lab != null)
				drillLabel = lab;
			else {
				LOGGER.error("Drill label not found");
			}

			List parameters = drillSB.getAttributeAsList("PARAM");
			if (parameters != null) {
				drillParametersMap = new HashMap<>();

				for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
					SourceBean att = (SourceBean) iterator.next();
					String name = (String) att.getAttribute("name");
					String type = (String) att.getAttribute("type");
					String value = (String) att.getAttribute("value");

					// default is relative
					if (type != null && type.equalsIgnoreCase("absolute"))
						type = "absolute";
					else
						type = "relative";

					if (name.equalsIgnoreCase("seriesurlname"))
						serieUrlname = value;
					else if (name.equalsIgnoreCase("target")) {
						if (value != null && value.equalsIgnoreCase("tab")) {
							setTarget("tab");
						} else {
							setTarget("self");
						}
					} else if (name.equalsIgnoreCase("title")) {
						if (value != null && !value.equals("")) {
							setDrillDocTitle(value);
						}
					} else if (name.equalsIgnoreCase("categoryurlname"))
						categoryUrlName = value;
					else {
						if (this.getParametersObject().get(name) != null) {
							value = (String) getParametersObject().get(name);
						}

						DrillParameter drillPar = new DrillParameter(name, type, value);
						drillParametersMap.put(name, drillPar);
					}
				}
			}
		}
		LOGGER.debug("OUT");
	}

	/**
	 * Inherited by IChart.
	 *
	 * @param chartTitle the chart title
	 * @param dataset    the dataset
	 *
	 * @return the j free chart
	 */

	@Override
	public JFreeChart createChart(DatasetMap datasets) {
		LOGGER.debug("IN");
		CategoryDataset dataset = (CategoryDataset) datasets.getDatasets().get("1");

		CategoryAxis categoryAxis = new CategoryAxis(categoryLabel);
		ValueAxis valueAxis = new NumberAxis(valueLabel);
		if (rangeIntegerValues) {
			valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		}

		org.jfree.chart.renderer.category.BarRenderer renderer = new org.jfree.chart.renderer.category.BarRenderer();

		renderer.setToolTipGenerator(new StandardCategoryToolTipGenerator());

		if (showValueLabels) {
			renderer.setBaseItemLabelsVisible(true);
			renderer.setBaseItemLabelGenerator(new FilterZeroStandardCategoryItemLabelGenerator());
			renderer.setBaseItemLabelFont(
					new Font(styleValueLabels.getFontName(), Font.PLAIN, styleValueLabels.getSize()));
			renderer.setBaseItemLabelPaint(styleValueLabels.getColor());
		}

		if (maxBarWidth != null) {
			renderer.setMaximumBarWidth(maxBarWidth.doubleValue());
		}

		boolean documentComposition = false;
		if (mode.equalsIgnoreCase(SpagoBIConstants.DOCUMENT_COMPOSITION))
			documentComposition = true;

		MyCategoryUrlGenerator mycatUrl = new MyCategoryUrlGenerator(rootUrl);
		mycatUrl.setDocument_composition(documentComposition);
		mycatUrl.setCategoryUrlLabel(categoryUrlName);
		mycatUrl.setSerieUrlLabel(serieUrlname);
		mycatUrl.setDrillDocTitle(drillDocTitle);
		mycatUrl.setTarget(target);

		renderer.setItemURLGenerator(mycatUrl);

		CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
		plot.setOrientation(PlotOrientation.VERTICAL);
		if (horizontalView) {
			plot.setOrientation(PlotOrientation.HORIZONTAL);
		}

		JFreeChart chart = new JFreeChart(name, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);

		TextTitle title = setStyleTitle(name, styleTitle);
		chart.setTitle(title);
		if (subName != null && !subName.equals("")) {
			TextTitle subTitle = setStyleTitle(subName, styleSubTitle);
			chart.addSubtitle(subTitle);
		}

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(color);

		// get a reference to the plot for further customisation...
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		NumberFormat nf = NumberFormat.getNumberInstance(locale);

		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setTickLabelFont(new Font(styleXaxesLabels.getFontName(), Font.PLAIN, styleXaxesLabels.getSize()));
		rangeAxis.setTickLabelPaint(styleXaxesLabels.getColor());
		rangeAxis.setNumberFormatOverride(nf);

		if (rangeAxisLocation != null) {
			if (rangeAxisLocation.equalsIgnoreCase("BOTTOM_OR_LEFT")) {
				plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
			} else if (rangeAxisLocation.equalsIgnoreCase("BOTTOM_OR_RIGHT")) {
				plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_RIGHT);
			} else if (rangeAxisLocation.equalsIgnoreCase("TOP_OR_RIGHT")) {
				plot.setRangeAxisLocation(0, AxisLocation.TOP_OR_RIGHT);
			} else if (rangeAxisLocation.equalsIgnoreCase("TOP_OR_LEFT")) {
				plot.setRangeAxisLocation(0, AxisLocation.TOP_OR_LEFT);
			}
		}

		// disable bar outlines...
		renderer.setDrawBarOutline(false);

		int seriesN = dataset.getRowCount();

		if (orderColorVector != null && !orderColorVector.isEmpty()) {
			LOGGER.debug("color serie by SERIES_ORDER_COLORS template specification");
			for (int i = 0; i < seriesN; i++) {
				if (orderColorVector.get(i) != null) {
					Color color = orderColorVector.get(i);
					renderer.setSeriesPaint(i, color);
				}
			}
		} else if (colorMap != null) {
			for (int i = 0; i < seriesN; i++) {
				String serieName = (String) dataset.getRowKey(i);
				Color color = (Color) colorMap.get(serieName);
				if (color != null) {
					renderer.setSeriesPaint(i, color);
				}
			}
		}

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
		domainAxis.setLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setLabelPaint(styleYaxesLabels.getColor());
		domainAxis.setTickLabelFont(new Font(styleYaxesLabels.getFontName(), Font.PLAIN, styleYaxesLabels.getSize()));
		domainAxis.setTickLabelPaint(styleYaxesLabels.getColor());

		if (legend)
			drawLegend(chart);

		LOGGER.debug("OUT");
		return chart;

	}

	/**
	 * Gets document parameters and return a string in the form &param1=value1&param2=value2 ...
	 *
	 * @param drillParameters the drill parameters
	 *
	 * @return the document_ parameters
	 */

	@Override
	public String getDocument_Parameters(HashMap<String, DrillParameter> drillParametersMap) {
		LOGGER.debug("IN");
		String documentParameter = "";
		if (drillParametersMap != null) {
			for (Iterator<String> iterator = drillParametersMap.keySet().iterator(); iterator.hasNext();) {
				String name = iterator.next();
				DrillParameter drillPar = drillParametersMap.get(name);
				String value = drillPar.getValue();
				if (name != null && !name.equals("") && value != null && !value.equals("")) {
					documentParameter += "%26" + name + "%3D" + value;
				}
			}
		}
		LOGGER.debug("OUT");
		return documentParameter;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getRootUrl()
	 */
	@Override
	public String getRootUrl() {
		return rootUrl;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setRootUrl(java.lang.String)
	 */
	@Override
	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#isLinkable()
	 */
	@Override
	public boolean isLinkable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getMode()
	 */
	@Override
	public String getMode() {
		return mode;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setMode(java.lang.String)
	 */
	@Override
	public void setMode(String mode) {
		this.mode = mode;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getDrillLabel()
	 */
	@Override
	public String getDrillLabel() {
		return drillLabel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setDrillLabel(java.lang.String)
	 */
	@Override
	public void setDrillLabel(String drillLabel) {
		this.drillLabel = drillLabel;
	}

	@Override
	public HashMap<String, DrillParameter> getDrillParametersMap() {
		return drillParametersMap;
	}

	@Override
	public void setDrillParametersMap(HashMap<String, DrillParameter> drillParametersMap) {
		this.drillParametersMap = drillParametersMap;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getCategoryUrlName()
	 */
	@Override
	public String getCategoryUrlName() {
		return categoryUrlName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setCategoryUrlName(java.lang.String)
	 */
	@Override
	public void setCategoryUrlName(String categoryUrlName) {
		this.categoryUrlName = categoryUrlName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#getSerieUrlname()
	 */
	@Override
	public String getSerieUrlname() {
		return serieUrlname;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.chart.bo.charttypes.ILinkableChart#setSerieUrlname(java.lang.String)
	 */
	@Override
	public void setSerieUrlname(String serieUrlname) {
		this.serieUrlname = serieUrlname;
	}

	@Override
	public String getDrillDocTitle() {
		return drillDocTitle;
	}

	@Override
	public void setDrillDocTitle(String drillDocTitle) {
		this.drillDocTitle = drillDocTitle;
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public void setTarget(String target) {
		this.target = target;
	}

}
