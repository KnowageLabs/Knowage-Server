/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.whatif.template;

import it.eng.spagobi.engines.whatif.crossnavigation.SpagoBICrossNavigationConfig;
import it.eng.spagobi.engines.whatif.crossnavigation.TargetClickable;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.writeback4j.SbiAliases;
import it.eng.spagobi.writeback4j.SbiScenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public class WhatIfTemplate {

	private String mondrianSchema;
	private String mdxQuery;
	private String mondrianMdxQuery;
	private final HashMap<String, Object> properties;
	private SbiScenario scenario;
	private SbiAliases aliases;
	private List<Parameter> parameters;
	private List<String> profilingUserAttributes;
	private List<String> toolbarVisibleButtons;
	private List<String> toolbarMenuButtons;
	private IDataSource standAloneConnection;
	private Map<String, String> xmlaServerProperties;
	private SpagoBICrossNavigationConfig crossNavigation;
	List<TargetClickable> targetsClickable = new ArrayList<TargetClickable>();

	public WhatIfTemplate() {
		properties = new HashMap<String, Object>();
	}

	public void setProperty(String pName, Object pValue) {
		properties.put(pName, pValue);
	}

	public Object getProperty(String pName) {
		return properties.get(pName);
	}

	public String getMondrianSchema() {
		return mondrianSchema;
	}

	public void setMondrianSchema(String mondrianSchema) {
		this.mondrianSchema = mondrianSchema;
	}

	public String getMdxQuery() {
		return mdxQuery;
	}

	public void setMdxQuery(String mdxQuery) {
		this.mdxQuery = mdxQuery;
	}

	public String getMondrianMdxQuery() {
		return mondrianMdxQuery;
	}

	public void setMondrianMdxQuery(String mondrianMdxQuery) {
		this.mondrianMdxQuery = mondrianMdxQuery;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public SbiScenario getScenario() {
		return scenario;
	}

	public void setScenario(SbiScenario scenario) {
		this.scenario = scenario;
	}

	/**
	 * @return the aliases
	 */
	public SbiAliases getAliases() {
		return aliases;
	}

	/**
	 * @param aliases
	 *            the aliases to set
	 */
	public void setAliases(SbiAliases aliases) {
		this.aliases = aliases;
	}

	public List<String> getProfilingUserAttributes() {
		return profilingUserAttributes;
	}

	public void setProfilingUserAttributes(List<String> profilingnUserAttributes) {
		this.profilingUserAttributes = profilingnUserAttributes;
	}

	public List<String> getToolbarVisibleButtons() {
		return toolbarVisibleButtons;
	}

	public void setToolbarVisibleButtons(List<String> toolbarVisibleButtons) {
		this.toolbarVisibleButtons = toolbarVisibleButtons;
	}

	public List<String> getToolbarMenuButtons() {
		return toolbarMenuButtons;
	}

	public void setToolbarMenuButtons(List<String> toolbarMenuButtons) {
		this.toolbarMenuButtons = toolbarMenuButtons;
	}

	public IDataSource getStandAloneConnection() {
		return standAloneConnection;
	}

	public void setStandAloneConnection(IDataSource standAloneConnection) {
		this.standAloneConnection = standAloneConnection;
	}

	public boolean isStandAlone() {
		return this.standAloneConnection != null;
	}

	public Map<String, String> getXmlaServerProperties() {
		return xmlaServerProperties;
	}

	public void setXmlaServerProperties(Map<String, String> xmlaServerProperties) {
		this.xmlaServerProperties = xmlaServerProperties;
	}

	public class Parameter {
		private String name;
		private String alias;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

	}

	public SpagoBICrossNavigationConfig getCrossNavigation() {
		return crossNavigation;
	}

	public void setCrossNavigation(SpagoBICrossNavigationConfig crossNavigation) {
		this.crossNavigation = crossNavigation;
	}

	public List<TargetClickable> getTargetsClickable() {
		return targetsClickable;
	}

	public void setTargetsClickable(List<TargetClickable> targetsClickable) {
		this.targetsClickable = targetsClickable;
	}

}
