/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2009 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.engines.datamining;

import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.model.DataMiningScript;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplate;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplateParser;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.engines.AbstractEngineInstance;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Monica Franceschini
 */
public class DataMiningEngineInstance extends AbstractEngineInstance {

	private final List<String> includes;
	// ENVIRONMENT VARIABLES
	private final String[] lstEnvVariables = { "SBI_EXECUTION_ID", "SBICONTEXT", "SBI_COUNTRY", "SBI_LANGUAGE", "SBI_SPAGO_CONTROLLER", "SBI_EXECUTION_ROLE",
			"SBI_HOST", "country", "language", "user_id", "DOCUMENT_ID", "DOCUMENT_LABEL", "DOCUMENT_NAME", "DOCUMENT_IS_PUBLIC", "DOCUMENT_COMMUNITIES",
			"DOCUMENT_DESCRIPTION", "SPAGOBI_AUDIT_ID", "DOCUMENT_USER", "DOCUMENT_IS_VISIBLE", "DOCUMENT_AUTHOR", "DOCUMENT_FUNCTIONALITIES",
			"DOCUMENT_VERSION", };
	private final List<DataMiningCommand> commands;
	private List<DataMiningDataset> datasets;
	private final List<DataMiningScript> scripts;

	public static transient Logger logger = Logger.getLogger(DataMiningEngineInstance.class);

	protected DataMiningEngineInstance(Object template, Map env) {
		this(DataMiningTemplateParser.getInstance() != null ? DataMiningTemplateParser.getInstance().parse(template) : null, env);
	}

	public DataMiningEngineInstance(DataMiningTemplate template, Map env) {

		super(env);
		logger.debug("IN");

		includes = DataMiningEngine.getConfig().getIncludes();

		// IEngUserProfile profile = (IEngUserProfile)
		// env.get(EngineConstants.ENV_USER_PROFILE);

		datasets = template.getDatasets();
		commands = template.getCommands();
		scripts = template.getScripts();

		logger.debug("OUT");
	}

	public List<DataMiningCommand> getCommands() {
		return commands;
	}

	public List<DataMiningScript> getScripts() {
		return scripts;
	}

	//
	// public IDataSource getDataSource() {
	// return (IDataSource) this.getEnv().get(EngineConstants.ENV_DATASOURCE);
	// }

	public IDataSet getDataSet() {
		return (IDataSet) this.getEnv().get(EngineConstants.ENV_DATASET);
	}

	public Locale getLocale() {
		return (Locale) this.getEnv().get(EngineConstants.ENV_LOCALE);
	}

	public AuditServiceProxy getAuditServiceProxy() {
		return (AuditServiceProxy) this.getEnv().get(EngineConstants.ENV_AUDIT_SERVICE_PROXY);
	}

	public EventServiceProxy getEventServiceProxy() {
		return (EventServiceProxy) this.getEnv().get(EngineConstants.ENV_EVENT_SERVICE_PROXY);
	}

	// -- unimplemented methods
	// ------------------------------------------------------------

	public IEngineAnalysisState getAnalysisState() {
		throw new DataMiningEngineRuntimeException("Unsupported method [getAnalysisState]");
	}

	public void setAnalysisState(IEngineAnalysisState analysisState) {
		throw new DataMiningEngineRuntimeException("Unsupported method [setAnalysisState]");
	}

	public void validate() throws SpagoBIEngineException {
		throw new DataMiningEngineRuntimeException("Unsupported method [validate]");
	}

	public List<DataMiningDataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<DataMiningDataset> datasets) {
		this.datasets = datasets;
	}

	public Map getAnalyticalDrivers() {
		Map toReturn = new HashMap();
		Iterator it = getEnv().keySet().iterator();
		while (it.hasNext()) {
			String parameterName = (String) it.next();
			Object parameterValue = getEnv().get(parameterName);

			if (parameterValue != null && parameterValue.getClass().getName().equals("java.lang.String") && // test
																											// necessary
																											// for
																											// don't
																											// pass
																											// complex
																											// objects
																											// like
																											// proxy,...
					isAnalyticalDriver(parameterName)) {
				toReturn.put(parameterName, parameterValue);
			}
		}
		return toReturn;
	}

	private boolean isAnalyticalDriver(String parName) {
		for (int i = 0; i < lstEnvVariables.length; i++) {
			if (lstEnvVariables[i].equalsIgnoreCase(parName)) {
				return false;
			}
		}
		return true;
	}
}
