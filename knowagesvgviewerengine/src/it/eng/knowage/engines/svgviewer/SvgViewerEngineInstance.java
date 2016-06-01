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
package it.eng.knowage.engines.svgviewer;

import it.eng.knowage.engines.svgviewer.component.SvgViewerEngineComponentFactory;
import it.eng.knowage.engines.svgviewer.datamart.provider.IDataMartProvider;
import it.eng.knowage.engines.svgviewer.dataset.provider.Hierarchy;
import it.eng.knowage.engines.svgviewer.map.provider.IMapProvider;
import it.eng.knowage.engines.svgviewer.map.renderer.IMapRenderer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.AbstractEngineInstance;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.IEngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 */
public class SvgViewerEngineInstance extends AbstractEngineInstance {
	// ENVIRONMENT VARIABLES
	private String[] lstEnvVariables = { "SBI_EXECUTION_ID", "SBICONTEXT", "SBI_COUNTRY", "SBI_LANGUAGE", "SBI_SPAGO_CONTROLLER", "SBI_EXECUTION_ROLE",
			"SBI_HOST", "DOCUMENT_ID", "country", "language", "user_id" };
	private JSONObject guiSettings;
	private JSONObject docProperties;
	private List<String> includes;

	/** The map provider. */
	IMapProvider mapProvider;

	/** The dataset provider. */
	IDataMartProvider dataMartProvider;

	/** The map renderer. */
	IMapRenderer mapRenderer;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(SvgViewerEngineInstance.class);

	public SvgViewerEngineInstance(SourceBean template, Map env) {
		super(env);

		try {
			// TODO: MC uncomment this, just test
			// this.guiSettings = new JSONObject(template);

			logger.debug("IN");

			setMapProvider(SvgViewerEngineComponentFactory.buildMapProvider(template, env));
			setDataMartProvider(SvgViewerEngineComponentFactory.buildDataMartProvider(template, env));
			setMapRenderer(SvgViewerEngineComponentFactory.buildMapRenderer(template, env));

			logger.info("MapProvider class: " + getMapProvider().getClass().getName());
			logger.info("DatasetProvider class: " + getDataMartProvider().getClass().getName());
			logger.info("MapRenderer class: " + getMapRenderer().getClass().getName());

			validate();

			logger.debug("OUT");
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to parse template", t);
		}

		// includes = SvgViewerEngine.getConfig().getIncludes();
	}

	public JSONObject getGuiSettings() {
		return guiSettings;
	}

	public List getIncludes() {
		return includes;
	}

	public JSONObject getDocProperties() {
		return docProperties;
	}

	public void setDocProperties(JSONObject docProperties) {
		this.docProperties = docProperties;
	}

	public IDataSource getDataSource() {
		return (IDataSource) this.getEnv().get(EngineConstants.ENV_DATASOURCE);
	}

	public IDataSet getDataSet() {
		return (IDataSet) this.getEnv().get(EngineConstants.ENV_DATASET);
	}

	public Locale getLocale() {
		return (Locale) this.getEnv().get(EngineConstants.ENV_LOCALE);
	}

	public String getDocumentLabel() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_LABEL);
	}

	public String getDocumentVersion() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_VERSION);
	}

	public String getDocumentAuthor() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_AUTHOR);
	}

	public String getDocumentUser() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_USER);
	}

	public String getDocumentName() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_NAME);
	}

	public String getDocumentDescription() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_DESCRIPTION);
	}

	public String getDocumentIsVisible() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_IS_VISIBLE);
	}

	public String getDocumentPreviewFile() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_PREVIEW_FILE);
	}

	public IEngUserProfile getUserProfile() {
		return (IEngUserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
	}

	public String[] getDocumentCommunities() {
		try {
			String strCommunities = (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_COMMUNITIES);
			if (strCommunities == null)
				return null;
			else
				return JSONUtils.asStringArray(JSONUtils.toJSONArray(strCommunities));
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to get communities list", t);
		}
	}

	public List<Integer> getDocumentFunctionalities() {
		try {
			String strFunctionalities = (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_FUNCTIONALITIES);
			if (strFunctionalities == null)
				return null;
			else
				return JSONUtils.asList(JSONUtils.toJSONArray(strFunctionalities));
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to get functionalities list", t);
		}
	}

	public String getDocumentIsPublic() {
		return (String) this.getEnv().get(EngineConstants.ENV_DOCUMENT_IS_PUBLIC);
	}

	public AuditServiceProxy getAuditServiceProxy() {
		return (AuditServiceProxy) this.getEnv().get(EngineConstants.ENV_AUDIT_SERVICE_PROXY);
	}

	public EventServiceProxy getEventServiceProxy() {
		return (EventServiceProxy) this.getEnv().get(EngineConstants.ENV_EVENT_SERVICE_PROXY);
	}

	public Map getAnalyticalDrivers() {
		Map toReturn = new HashMap();
		Iterator it = getEnv().keySet().iterator();
		while (it.hasNext()) {
			String parameterName = (String) it.next();
			Object parameterValue = getEnv().get(parameterName);

			if (parameterValue != null && parameterValue.getClass().getName().equals("java.lang.String") && // test necessary for don't pass complex objects
																											// like proxy,...
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

	public File renderMap(String format) throws SvgViewerEngineException {
		return getMapRenderer().renderMap(getMapProvider(), getDataMartProvider(), format);
	}

	public IDataMartProvider getDataMartProvider() {
		return dataMartProvider;
	}

	public void setDataMartProvider(IDataMartProvider dataMartProvider) {
		this.dataMartProvider = dataMartProvider;
	}

	/**
	 * Gets the map provider.
	 *
	 * @return the map provider
	 */
	public IMapProvider getMapProvider() {
		return mapProvider;
	}

	/**
	 * Sets the map provider.
	 *
	 * @param mapProvider
	 *            the new map provider
	 */
	protected void setMapProvider(IMapProvider mapProvider) {
		this.mapProvider = mapProvider;
	}

	/**
	 * Gets the map renderer.
	 *
	 * @return the map renderer
	 */
	public IMapRenderer getMapRenderer() {
		return mapRenderer;
	}

	/**
	 * Sets the map renderer.
	 *
	 * @param mapRenderer
	 *            the new map renderer
	 */
	protected void setMapRenderer(IMapRenderer mapRenderer) {
		this.mapRenderer = mapRenderer;
	}

	// -- unimplemented methods ------------------------------------------------------------

	@Override
	public IEngineAnalysisState getAnalysisState() {
		throw new SvgViewerEngineRuntimeException("Unsupported method [getAnalysisState]");
	}

	@Override
	public void setAnalysisState(IEngineAnalysisState analysisState) {
		throw new SvgViewerEngineRuntimeException("Unsupported method [setAnalysisState]");
	}

	@Override
	public void validate() throws SpagoBIEngineException {
		String selectedHierarchyName = getDataMartProvider().getSelectedHierarchyName();
		if (selectedHierarchyName == null) {
			SvgViewerEngineException geoException;
			logger.error("Select hierarchy name is not defined");
			String description = "Select hierarchy name is not defined";
			geoException = new SvgViewerEngineException("Configuration error");
			geoException.setDescription(description);
			throw geoException;
		}

		Hierarchy selectedHierarchy = getDataMartProvider().getHierarchy(selectedHierarchyName);
		if (selectedHierarchy == null) {
			SvgViewerEngineException geoException;
			logger.error("Selected hierarchy [" + selectedHierarchyName + "] does not exist");
			String description = "Selected hierarchy [" + selectedHierarchyName + "] does not exist";
			List hints = new ArrayList();
			hints.add("Check if hierarchy name is correct");
			hints.add("Check if a hierarchy named " + selectedHierarchyName + "  has been defined. Defined hierarachy are: "
					+ Arrays.toString(getDataMartProvider().getHierarchyNames().toArray()));
			geoException = new SvgViewerEngineException("Configuration error");
			geoException.setDescription(description);
			// geoException.setHints(hints);
			throw geoException;
		}

		String selectedLevelName = getDataMartProvider().getSelectedLevelName();
		if (selectedLevelName == null) {
			SvgViewerEngineException geoException;
			logger.error("Select level name is not defined");
			String description = "Select level name is not defined";
			geoException = new SvgViewerEngineException("Configuration error");
			geoException.setDescription(description);
			throw geoException;
		}

		Hierarchy.Level selectedLevel = selectedHierarchy.getLevel(selectedLevelName);
		if (selectedLevel == null) {
			SvgViewerEngineException geoException;
			logger.error("Selected level [" + selectedHierarchyName + "] does not exist in selected hierarchy [" + selectedHierarchyName + "]");
			String description = "Selected level [" + selectedHierarchyName + "] does not exist in selected hierarchy [" + selectedHierarchyName + "]";
			List hints = new ArrayList();
			hints.add("Check if level name is correct");
			hints.add("Check if a level named " + selectedLevelName + "  is defined into hierarachy " + selectedHierarchyName + ". " + "Defined level are: "
					+ Arrays.toString(selectedHierarchy.getLevelNames().toArray()));
			geoException = new SvgViewerEngineException("Configuration error");
			geoException.setDescription(description);
			// geoException.setHints(hints);
			throw geoException;
		}
	}

	public boolean isVisibleDataSet() {
		IDataSet datSet = getDataSet();
		if (datSet != null) {
			IEngUserProfile profile = getUserProfile();
			return DataSetUtilities.isExecutableByUser(datSet, profile);
		}
		return true;
	}

}
