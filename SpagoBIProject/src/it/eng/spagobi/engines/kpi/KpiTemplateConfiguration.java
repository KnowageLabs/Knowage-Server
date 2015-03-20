/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.engines.kpi.utils.StyleLabel;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class KpiTemplateConfiguration {
	private String publisher_Name;
	private String metadata_publisher_Name;
	private String trend_publisher_Name;
	private boolean closed_tree;
	private String model_title;
	private String threshold_image_title;
	private String bullet_chart_title;
	private String value_title;
	private String kpi_title;
	private String weight_title;
	private boolean use_ou;
	private boolean display_semaphore;
	private boolean display_bullet_chart;
	private boolean display_threshold_image;
	private boolean display_weight;
	private boolean display_alarm;
	private boolean register_values;
	private boolean recalculate_anyway;
	private boolean register_par_setted;
	private boolean show_axis;
	private boolean weighted_values;
	private boolean dataset_multires;
	
	//new : set the tick interval for the new speedometer
	private String tickInterval;
	

	//new for custom detail charts 
	private String custom_chart_name;

	protected HashMap confMap;// HashMap with all the config parameters
	static transient Logger logger = Logger	.getLogger(KpiValueComputation.class);
	
	public KpiTemplateConfiguration(String publisher_Name,
			String metadata_publisher_Name, String trend_publisher_Name,
			boolean closed_tree, String model_title,
			String threshold_image_title, String bullet_chart_title,
			String value_title, String kpi_title, String weight_title,
			boolean use_ou, boolean display_semaphore,
			boolean display_bullet_chart, boolean display_threshold_image,
			boolean display_weight, boolean display_alarm,
			boolean register_values, boolean recalculate_anyway,
			boolean register_par_setted, boolean show_axis,
			boolean weighted_values, boolean dataset_multires, 
			String custom_chart_name, String tickInterval) {
		this.publisher_Name = publisher_Name;
		this.metadata_publisher_Name = metadata_publisher_Name;
		this.trend_publisher_Name = trend_publisher_Name;
		this.closed_tree = closed_tree;
		this.model_title = model_title;
		this.threshold_image_title = threshold_image_title;
		this.bullet_chart_title = bullet_chart_title;
		this.value_title = value_title;
		this.kpi_title = kpi_title;
		this.weight_title = weight_title;
		this.use_ou = use_ou;
		this.display_semaphore = display_semaphore;
		this.display_bullet_chart = display_bullet_chart;
		this.display_threshold_image = display_threshold_image;
		this.display_weight = display_weight;
		this.display_alarm = display_alarm;
		this.register_values = register_values;
		this.recalculate_anyway = recalculate_anyway;
		this.register_par_setted = register_par_setted;
		this.show_axis = show_axis;
		this.weighted_values = weighted_values;
		this.dataset_multires = dataset_multires;
		this.custom_chart_name = custom_chart_name;
		this.tickInterval = tickInterval;
	}

	public String getTickInterval() {
		return tickInterval;
	}

	public void setTickInterval(String tickInterval) {
		this.tickInterval = tickInterval;
	}

	public String getCustom_chart_name() {
		return custom_chart_name;
	}

	public void setCustom_chart_name(String custom_chart_name) {
		this.custom_chart_name = custom_chart_name;
	}
	public String getPublisher_Name() {
		return publisher_Name;
	}

	public void setPublisher_Name(String publisher_Name) {
		this.publisher_Name = publisher_Name;
	}

	public String getMetadata_publisher_Name() {
		return metadata_publisher_Name;
	}

	public void setMetadata_publisher_Name(String metadata_publisher_Name) {
		this.metadata_publisher_Name = metadata_publisher_Name;
	}

	public String getTrend_publisher_Name() {
		return trend_publisher_Name;
	}

	public void setTrend_publisher_Name(String trend_publisher_Name) {
		this.trend_publisher_Name = trend_publisher_Name;
	}

	public boolean isClosed_tree() {
		return closed_tree;
	}

	public void setClosed_tree(boolean closed_tree) {
		this.closed_tree = closed_tree;
	}

	public String getModel_title() {
		return model_title;
	}

	public void setModel_title(String model_title) {
		this.model_title = model_title;
	}

	public String getThreshold_image_title() {
		return threshold_image_title;
	}

	public void setThreshold_image_title(String threshold_image_title) {
		this.threshold_image_title = threshold_image_title;
	}

	public String getBullet_chart_title() {
		return bullet_chart_title;
	}

	public void setBullet_chart_title(String bullet_chart_title) {
		this.bullet_chart_title = bullet_chart_title;
	}

	public String getValue_title() {
		return value_title;
	}

	public void setValue_title(String value_title) {
		this.value_title = value_title;
	}

	public String getKpi_title() {
		return kpi_title;
	}

	public void setKpi_title(String kpi_title) {
		this.kpi_title = kpi_title;
	}

	public String getWeight_title() {
		return weight_title;
	}

	public void setWeight_title(String weight_title) {
		this.weight_title = weight_title;
	}

	public boolean isUse_ou() {
		return use_ou;
	}

	public void setUse_ou(boolean use_ou) {
		this.use_ou = use_ou;
	}

	public boolean isDisplay_semaphore() {
		return display_semaphore;
	}

	public void setDisplay_semaphore(boolean display_semaphore) {
		this.display_semaphore = display_semaphore;
	}

	public boolean isDisplay_bullet_chart() {
		return display_bullet_chart;
	}

	public void setDisplay_bullet_chart(boolean display_bullet_chart) {
		this.display_bullet_chart = display_bullet_chart;
	}

	public boolean isDisplay_threshold_image() {
		return display_threshold_image;
	}

	public void setDisplay_threshold_image(boolean display_threshold_image) {
		this.display_threshold_image = display_threshold_image;
	}

	public boolean isDisplay_weight() {
		return display_weight;
	}

	public void setDisplay_weight(boolean display_weight) {
		this.display_weight = display_weight;
	}

	public boolean isDisplay_alarm() {
		return display_alarm;
	}

	public void setDisplay_alarm(boolean display_alarm) {
		this.display_alarm = display_alarm;
	}

	public boolean isRegister_values() {
		return register_values;
	}

	public void setRegister_values(boolean register_values) {
		this.register_values = register_values;
	}

	public boolean isRecalculate_anyway() {
		return recalculate_anyway;
	}

	public void setRecalculate_anyway(boolean recalculate_anyway) {
		this.recalculate_anyway = recalculate_anyway;
	}

	public boolean isRegister_par_setted() {
		return register_par_setted;
	}

	public void setRegister_par_setted(boolean register_par_setted) {
		this.register_par_setted = register_par_setted;
	}

	public boolean isShow_axis() {
		return show_axis;
	}

	public void setShow_axis(boolean show_axis) {
		this.show_axis = show_axis;
	}

	public boolean isWeighted_values() {
		return weighted_values;
	}

	public void setWeighted_values(boolean weighted_values) {
		this.weighted_values = weighted_values;
	}

	public boolean isDataset_multires() {
		return dataset_multires;
	}

	public void setDataset_multires(boolean dataset_multires) {
		this.dataset_multires = dataset_multires;
	}

	/**
	 * Function that sets the basic values getting them from the xml template
	 * @param spagoBIKpiInternalEngine TODO
	 * @param content The template SourceBean containing parameters configuration
	 */
	public void getSetConf(SpagoBIKpiInternalEngine spagoBIKpiInternalEngine, SourceBean content) {
		SpagoBIKpiInternalEngine.logger.debug("IN");
		spagoBIKpiInternalEngine.confMap = new HashMap();
	
		//Getting TITLE and replacing eventual parameters
		if (content.getAttribute("name") != null) {
			String titleChart = (String) content.getAttribute("name");
			titleChart = replaceParsInString(titleChart, spagoBIKpiInternalEngine);
			spagoBIKpiInternalEngine.setName(titleChart);
		} 
	
		//Setting title style
		SourceBean styleTitleSB = (SourceBean) content.getAttribute("STYLE_TITLE");
		if (styleTitleSB != null) {
			String fontS = (String) content.getAttribute("STYLE_TITLE.font");
			String sizeS = (String) content.getAttribute("STYLE_TITLE.size");
			String colorS = (String) content.getAttribute("STYLE_TITLE.color");
			try {
				Color color = Color.decode(colorS);
				int size = Integer.valueOf(sizeS).intValue();
				spagoBIKpiInternalEngine.styleTitle = new StyleLabel(fontS, size, color);
			} catch (Exception e) {
				SpagoBIKpiInternalEngine.logger.error("Wrong style Title settings, use default",e);
			}
	
		} else {
			spagoBIKpiInternalEngine.styleTitle = new StyleLabel("Arial", 16, new Color(255, 0, 0));
		}
		spagoBIKpiInternalEngine.confMap.put("styleTitle", spagoBIKpiInternalEngine.styleTitle);
	
		//Getting SUBTITLE and setting its style
		SourceBean styleSubTitleSB = (SourceBean) content.getAttribute("STYLE_SUBTITLE");
		if (styleSubTitleSB != null) {
	
			String subTitle = (String) content.getAttribute("STYLE_SUBTITLE.name");
			subTitle = replaceParsInString(subTitle, spagoBIKpiInternalEngine);
			spagoBIKpiInternalEngine.setSubName(subTitle);		
			String fontS = (String) content.getAttribute("STYLE_SUBTITLE.font");
			String sizeS = (String) content.getAttribute("STYLE_SUBTITLE.size");
			String colorS = (String) content.getAttribute("STYLE_SUBTITLE.color");
			try {
				Color color = Color.decode(colorS);
				int size = Integer.valueOf(sizeS).intValue();
				spagoBIKpiInternalEngine.styleSubTitle = new StyleLabel(fontS, size, color);
			} catch (Exception e) {
				SpagoBIKpiInternalEngine.logger.error("Wrong style SubTitle settings, use default");
			}
		} else {
			spagoBIKpiInternalEngine.styleSubTitle = new StyleLabel("Arial", 12, new Color(0, 0, 0));
		}
		spagoBIKpiInternalEngine.confMap.put("styleSubTitle", spagoBIKpiInternalEngine.styleSubTitle);
	
		// get all the other template parameters
		try {
			Map dataParameters = new HashMap();
			SourceBean dataSB = (SourceBean) content.getAttribute("CONF");
			List dataAttrsList = dataSB.getContainedSourceBeanAttributes();
			Iterator dataAttrsIter = dataAttrsList.iterator();
			while (dataAttrsIter.hasNext()) {
				SourceBeanAttribute paramSBA = (SourceBeanAttribute) dataAttrsIter.next();
				SourceBean param = (SourceBean) paramSBA.getValue();
				String nameParam = (String) param.getAttribute("name");
				String valueParam = (String) param.getAttribute("value");
				dataParameters.put(nameParam, valueParam);
			}
	
			setClosed_tree(true);
			if (dataParameters.get("closed_tree") != null
					&& !(((String) dataParameters.get("closed_tree")).equalsIgnoreCase(""))) {
				String leg = (String) dataParameters.get("closed_tree");
				if (leg.equalsIgnoreCase("false"))
					setClosed_tree(false);
			}
			spagoBIKpiInternalEngine.confMap.put("closed_tree", isClosed_tree());
	
			setDisplay_semaphore(true);
			if (dataParameters.get("display_semaphore") != null
					&& !(((String) dataParameters.get("display_semaphore")).equalsIgnoreCase(""))) {
				String leg = (String) dataParameters.get("display_semaphore");
				if (leg.equalsIgnoreCase("false"))
					setDisplay_semaphore(false);
			}
			spagoBIKpiInternalEngine.confMap.put("display_semaphore", isDisplay_semaphore());
	
			setDisplay_bullet_chart(true);
			if (dataParameters.get("display_bullet_chart") != null
					&& !(((String) dataParameters.get("display_bullet_chart")).equalsIgnoreCase(""))) {
				String fil = (String) dataParameters.get("display_bullet_chart");
				if (fil.equalsIgnoreCase("false"))
					setDisplay_bullet_chart(false);
			}
			spagoBIKpiInternalEngine.confMap.put("display_bullet_chart", isDisplay_bullet_chart());
	
			setDisplay_threshold_image(false);
			if (dataParameters.get("display_threshold_image") != null
					&& !(((String) dataParameters.get("display_threshold_image")).equalsIgnoreCase(""))) {
				String fil = (String) dataParameters.get("display_threshold_image");
				if (fil.equalsIgnoreCase("true"))
					setDisplay_threshold_image(true);
			}
			spagoBIKpiInternalEngine.confMap.put("display_threshold_image", isDisplay_threshold_image());
	
			setDisplay_weight(true);
			if (dataParameters.get("display_weight") != null
					&& !(((String) dataParameters.get("display_weight")).equalsIgnoreCase(""))) {
				String fil = (String) dataParameters.get("display_weight");
				if (fil.equalsIgnoreCase("false"))
					setDisplay_weight(false);
			}
			spagoBIKpiInternalEngine.confMap.put("display_weight", isDisplay_weight());
	
			setDisplay_alarm(true);
			if (dataParameters.get("display_alarm") != null
					&& !(((String) dataParameters.get("display_alarm")).equalsIgnoreCase(""))) {
				String fil = (String) dataParameters.get("display_alarm");
				if (fil.equalsIgnoreCase("false"))
					setDisplay_alarm(false);
			}
			spagoBIKpiInternalEngine.confMap.put("display_alarm", isDisplay_alarm());
			
			setUse_ou(false);
			if (dataParameters.get("use_ou") != null
					&& !(((String) dataParameters.get("use_ou")).equalsIgnoreCase(""))) {
				String fil = (String) dataParameters.get("use_ou");
				if (fil.equalsIgnoreCase("true"))
					setUse_ou(true);
			}
			spagoBIKpiInternalEngine.confMap.put("use_ou", isUse_ou());
	
			if(!isRegister_par_setted()){//the spagobi register_values if setted has priority
				setRegister_values(true);
				if (dataParameters.get("register_values") != null
						&& !(((String) dataParameters.get("register_values")).equalsIgnoreCase(""))) {
					String fil = (String) dataParameters.get("register_values");
					if (fil.equalsIgnoreCase("false"))
						setRegister_values(false);
				}
				spagoBIKpiInternalEngine.confMap.put("register_values", isRegister_values());
			}
	
			setShow_axis(false);
			if (dataParameters.get("show_axis") != null
					&& !(((String) dataParameters.get("show_axis")).equalsIgnoreCase(""))) {
				String fil = (String) dataParameters.get("show_axis");
				if (fil.equalsIgnoreCase("true"))
					setShow_axis(true);
			}
			spagoBIKpiInternalEngine.confMap.put("show_axis", isShow_axis());
	
			setWeighted_values(false);
			if (dataParameters.get("weighted_values") != null
					&& !(((String) dataParameters.get("weighted_values")).equalsIgnoreCase(""))) {
				String fil = (String) dataParameters.get("weighted_values");
				if (fil.equalsIgnoreCase("true"))
					setWeighted_values(true);
			}
			spagoBIKpiInternalEngine.confMap.put("weighted_values", isWeighted_values());
	
			if (dataParameters.get("model_title") != null
					&& !(((String) dataParameters.get("model_title")).equalsIgnoreCase(""))) {
				String fil = (String) dataParameters.get("model_title");
				if (fil!=null) setModel_title(fil);
			}else{
				MessageBuilder msgBuild=new MessageBuilder();
				setModel_title(msgBuild.getMessage("sbi.kpi.modelLineTitle", spagoBIKpiInternalEngine.data.getLocale()));					
			}
			spagoBIKpiInternalEngine.confMap.put("model_title", getModel_title());
	
			if (dataParameters.get("kpi_title") != null) {
				String fil = (String) dataParameters.get("kpi_title");
				if (fil!=null) setKpi_title(fil);
			}
			spagoBIKpiInternalEngine.confMap.put("kpi_title", getKpi_title());
	
			if (dataParameters.get("weight_title") != null) {
				String fil = (String) dataParameters.get("weight_title");
				if (fil!=null) setWeight_title(fil);
			}
			spagoBIKpiInternalEngine.confMap.put("weight_title", getWeight_title());
	
			if (dataParameters.get("bullet_chart_title") != null) {
				String fil = (String) dataParameters.get("bullet_chart_title");
				if (fil!=null) setBullet_chart_title(fil);
			}
			spagoBIKpiInternalEngine.confMap.put("bullet_chart_title", getBullet_chart_title());
	
			if (dataParameters.get("threshold_image_title") != null) {
				String fil = (String) dataParameters.get("threshold_image_title");
				if (fil!=null) setThreshold_image_title(fil);
			}
			spagoBIKpiInternalEngine.confMap.put("threshold_image_title", getThreshold_image_title());
	
			if (dataParameters.get("value_title") != null) {
				String fil = (String) dataParameters.get("value_title");
				if (fil!=null) setValue_title(fil);
			}
			spagoBIKpiInternalEngine.confMap.put("value_title", getValue_title());
	
			if (dataParameters.get(SpagoBIConstants.PUBLISHER_NAME) != null && dataParameters.get(SpagoBIConstants.PUBLISHER_NAME) != "") {
				String fil = (String) dataParameters.get(SpagoBIConstants.PUBLISHER_NAME);
				if (fil!=null) setPublisher_Name(fil);
			}
	
			if (dataParameters.get("metadata_publisher_Name") != null && dataParameters.get("metadata_publisher_Name") != "") {
				String fil = (String) dataParameters.get("metadata_publisher_Name");
				if (fil!=null) setMetadata_publisher_Name(fil);
			}
	
			if (dataParameters.get("trend_publisher_Name") != null && dataParameters.get("trend_publisher_Name") != "") {
				String fil = (String) dataParameters.get("trend_publisher_Name");
				if (fil!=null) setTrend_publisher_Name(fil);
			}
			
			if (dataParameters.get("custom_chart_name") != null && dataParameters.get("custom_chart_name") != "") {
				String fil = (String) dataParameters.get("custom_chart_name");
				if (fil!=null) setCustom_chart_name(fil);
			}
			if (dataParameters.get("tickInterval") != null && dataParameters.get("tickInterval") != "") {
				String interv = (String) dataParameters.get("tickInterval");
				if (interv!=null) setTickInterval(interv);
			}
		} catch (Exception e) {
			SpagoBIKpiInternalEngine.logger.error("error in reading template parameters");
		}
	}
	String replaceParsInString(String title, SpagoBIKpiInternalEngine spagoBIKpiInternalEngine){
		logger.debug("IN");
		SimpleDateFormat f = new SimpleDateFormat();
		f.applyPattern(spagoBIKpiInternalEngine.data.getInternationalizedFormat());
		SimpleDateFormat fServ = new SimpleDateFormat();
		fServ.applyPattern(spagoBIKpiInternalEngine.data.getFormatServer());
		if (title != null) {
			String tmpTitle = title;
			while (!tmpTitle.equals("")) {
				if (tmpTitle.indexOf("$P{") >= 0) {
					String parName = tmpTitle.substring(tmpTitle.indexOf("$P{") + 3, tmpTitle.indexOf("}"));
					String parValue = (spagoBIKpiInternalEngine.parameters.getParametersObject().get(parName) == null) ? "" : (String) spagoBIKpiInternalEngine.parameters.getParametersObject()
							.get(parName);
					parValue = parValue.replaceAll("\'", "");
					if (parValue.equals("%"))
						parValue = "";
					int pos = tmpTitle.indexOf("$P{" + parName + "}") + (parName.length() + 4);
					if(parName.equalsIgnoreCase("ParKpiDate")){
						try {
							if(parValue!=null && !parValue.equalsIgnoreCase("")){
								Date d = fServ.parse(parValue);
								parValue = f.format(d);
							}
						} catch (ParseException e) {
							logger.error(e);
							e.printStackTrace();
						}
					}
					title = title.replace("$P{" + parName + "}", parValue);
					tmpTitle = tmpTitle.substring(pos);
				} else
					tmpTitle = "";
			}
		} else{
			title = "";
		}
		logger.debug("OUT");
		return title;
	}
}
