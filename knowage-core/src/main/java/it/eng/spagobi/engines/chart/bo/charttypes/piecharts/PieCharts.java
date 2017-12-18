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

package it.eng.spagobi.engines.chart.bo.charttypes.piecharts;

/**   @author Giulio Gavardi
 *     giulio.gavardi@eng.it
 */

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.engines.chart.bo.ChartImpl;
import it.eng.spagobi.engines.chart.utils.DataSetAccessFunctions;
import it.eng.spagobi.engines.chart.utils.DatasetMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class PieCharts extends ChartImpl {

	Map confParameters;
	private static transient Logger logger=Logger.getLogger(PieCharts.class);

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#configureChart(it.eng.spago.base.SourceBean)
	 */
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
		logger.debug("OUT");
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#createChart(java.lang.String, org.jfree.data.general.Dataset)
	 */
	public JFreeChart createChart(DatasetMap dataset) {
		// TODO Auto-generated method stub
		return super.createChart(dataset);



	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.engines.chart.bo.ChartImpl#calculateValue()
	 */
	public DatasetMap calculateValue() throws Exception {
		logger.debug("IN");
		String res=DataSetAccessFunctions.getDataSetResultFromId(profile, getData(),parametersObject);

		SourceBean sbRows=SourceBean.fromXMLString(res);
		SourceBean sbRow=(SourceBean)sbRows.getAttribute("ROW");
		List listAtts=sbRow.getContainedAttributes();
		DefaultPieDataset dataset = new DefaultPieDataset();


		List atts=new Vector();
		atts.add(res);
		if (name.indexOf("$F{") >= 0){					
			setTitleParameter(atts);
		}
		if (getSubName() != null && getSubName().indexOf("$F") >= 0){
			setSubTitleParameter(atts);
		}

		for (Iterator iterator = listAtts.iterator(); iterator.hasNext();) {
			SourceBeanAttribute att = (SourceBeanAttribute) iterator.next();
			String name=att.getKey();
			String valueS=(String)att.getValue();

			//try Double and Integer Conversion

			Double valueD=null;
			try{
				valueD=Double.valueOf(valueS);
			}
			catch (Exception e) {}

			Integer valueI=null;
			if(valueD==null){
				valueI=Integer.valueOf(valueS);
			}

			if(name!=null && valueD!=null){
				dataset.setValue(name, valueD);
			}
			else if(name!=null && valueI!=null){
				dataset.setValue(name, valueI);
			}

		}
		logger.debug("OUT");
		DatasetMap datasets=new DatasetMap();
		datasets.addDataset("1",dataset);
		return datasets;
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


}
