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

package it.eng.spagobi.engines.network;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.engines.network.bean.INetwork;
import it.eng.spagobi.engines.network.bean.JSONNetwork;
import it.eng.spagobi.engines.network.bo.NetworkDefinition;
import it.eng.spagobi.engines.network.template.NetworkTemplate;
import it.eng.spagobi.engines.network.template.NetworkTemplateParser;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.AbstractEngineInstance;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineAnalysisState;


/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 */
public class NetworkEngineInstance extends AbstractEngineInstance {
	
	INetwork net;
	IDataSet dataSet;
	NetworkTemplate template;
	private JSONObject guiSettings;
	private List<String> includes;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(NetworkEngineInstance.class);


	protected NetworkEngineInstance(Object template, Map env) throws NetworkEngineException {
		this( NetworkTemplateParser.getInstance().parse(template, env), env );
	}

	protected NetworkEngineInstance(NetworkTemplate template, Map env) throws NetworkEngineException {
		super( env );
		logger.debug("IN");
		this.template = template;
		logger.debug("OUT");
	}

	public void validate() throws NetworkEngineException {
		return;
	}

	public NetworkTemplate getTemplate() {
		return template;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.utilities.engines.IEngineInstance#getAnalysisState()
	 */
	//@Override
	public IEngineAnalysisState getAnalysisState() {
		return this.getTemplate().getNetworkDefinition();
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.utilities.engines.IEngineInstance#setAnalysisState(it.eng.spagobi.utilities.engines.IEngineAnalysisState)
	 */
	//@Override
	public void setAnalysisState(IEngineAnalysisState analysisState) {
		this.getTemplate().setNetworkDefinition((NetworkDefinition)analysisState);
	}

	public IDataSet getDataSet() {
		return dataSet; 

	}

	public void setDataSet(IDataSet dataSet) {
		this.dataSet = dataSet; 
	}

	public JSONObject getGuiSettings() {
		return guiSettings;
	}
	
	public List getIncludes() {
		return includes;
	}
	
	public IDataSource getDataSource() {
		return (IDataSource)this.getEnv().get(EngineConstants.ENV_DATASOURCE);
	}
		
	public Locale getLocale() {
		return (Locale)this.getEnv().get(EngineConstants.ENV_LOCALE);
	}
	
	public AuditServiceProxy getAuditServiceProxy() {
		return (AuditServiceProxy)this.getEnv().get(EngineConstants.ENV_AUDIT_SERVICE_PROXY);
	}
	
	public EventServiceProxy getEventServiceProxy() {
		return (EventServiceProxy)this.getEnv().get(EngineConstants.ENV_EVENT_SERVICE_PROXY);
	}

	public INetwork getNet() {
		return net;
	}

	public void setNet(INetwork net) {
		this.net = net;
	}
	

}
