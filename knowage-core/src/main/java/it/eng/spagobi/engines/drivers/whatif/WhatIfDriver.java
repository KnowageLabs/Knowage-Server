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
package it.eng.spagobi.engines.drivers.whatif;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.EngineUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter;
import it.eng.spagobi.engines.drivers.DefaultOutputParameter.TYPE;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;
import it.eng.spagobi.engines.drivers.whatif.manager.WhatIfWorkflowManager;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class WhatIfDriver extends GenericDriver {

	static private Logger logger = Logger.getLogger(WhatIfDriver.class);

	@Override
	public Map getParameterMap(Object biobject, IEngUserProfile profile, String roleName) {
		Map pars = super.getParameterMap(biobject, profile, roleName);
		byte[] template = this.getTemplateAsByteArray(biobject);
		BIObject b = (BIObject) biobject;
		int documentId = b.getId();
		Engine engine = b.getEngine();
		pars = addArtifactVersionId(template, pars, profile, documentId, engine);
		pars = addArtifactId(template, pars, profile);
		pars = applyDatasourceForWriting(pars, (BIObject) biobject);
		return pars;
	}

	@Override
	public Map getParameterMap(Object biobject, Object subObject, IEngUserProfile profile, String roleName) {
		Map pars = super.getParameterMap(biobject, subObject, profile, roleName);
		byte[] template = this.getTemplateAsByteArray(biobject);
		BIObject b = (BIObject) biobject;
		int documentId = b.getId();
		Engine engine = b.getEngine();
		pars = addArtifactVersionId(template, pars, profile, documentId, engine);
		pars = addArtifactId(template, pars, profile);
		pars = applyDatasourceForWriting(pars, (BIObject) biobject);
		return pars;
	}

	protected ObjTemplate getTemplate(Object biobject) {
		ObjTemplate template = null;
		try {
			BIObject biobj = (BIObject) biobject;
			template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(biobj.getId());
			if (template == null)
				throw new Exception("Active Template null");
		} catch (Exception e) {
			throw new RuntimeException("Error while getting document's template", e);
		}
		return template;
	}

	protected byte[] getTemplateAsByteArray(Object biobject) {
		ObjTemplate template = this.getTemplate(biobject);
		byte[] bytes;
		try {
			bytes = template.getContent();
		} catch (Exception e) {
			throw new RuntimeException("Error while getting document's template", e);
		}
		if (bytes == null)
			throw new RuntimeException("Content of the Active template null");
		return bytes;
	}

	@Override
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		logger.debug("IN");
		BIObject obj = null;
		try {
			obj = (BIObject) biobject;
		} catch (ClassCastException cce) {
			logger.error("The input object is not a BIObject type", cce);
			return null;
		}
		Engine engine = obj.getEngine();
		String url = engine.getUrl() + "";
		Map parameters = new HashMap();

		String documentId = obj.getId().toString();
		String documentLabel = obj.getLabel();
		String engineName = obj.getEngineLabel();

		byte[] template1 = this.getTemplateAsByteArray(biobject);
		JSONObject json = this.getJsonFromTemplate(template1);
		parameters = addArtifactVersionId(template1, parameters, profile, obj.getId(), engine);
		parameters = addArtifactId(template1, parameters, profile);
		parameters = addTemplateInformation(template1, parameters);
		parameters.put("document", documentId);
		parameters.put("DOCUMENT_LABEL", documentLabel);
		parameters.put("ENGINE", engineName);
		parameters.put(EngineStartServletIOManager.ON_EDIT_MODE, "");
		parameters.put("template", json);
		// CREATE EXECUTION ID
		String sbiExecutionId = null;
		UUIDGenerator uuidGen = UUIDGenerator.getInstance();
		UUID uuidObj = uuidGen.generateTimeBasedUUID();
		sbiExecutionId = uuidObj.toString();
		sbiExecutionId = sbiExecutionId.replaceAll("-", "");
		parameters.put("SBI_EXECUTION_ID", sbiExecutionId);

		applySecurity(parameters, profile);

		EngineURL engineURL = new EngineURL(url, parameters);
		logger.debug("OUT");
		return engineURL;
	}

	@Override
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		logger.debug("IN");
		BIObject obj = null;
		try {
			obj = (BIObject) biobject;
		} catch (ClassCastException cce) {
			logger.error("The input object is not a BIObject type", cce);
			return null;
		}
		Engine engine = obj.getEngine();
		String url = engine.getUrl() + "/edit";
		HashMap parameters = new HashMap();

		String documentId = obj.getId().toString();
		String documentLabel = obj.getLabel();
		String engineName = obj.getEngineLabel();
		parameters.put("document", documentId);
		parameters.put("DOCUMENT_LABEL", documentLabel);
		parameters.put("ENGINE", engineName);
		parameters.put("mode", "edit");
		// CREATE EXECUTION ID
		String sbiExecutionId = null;
		UUIDGenerator uuidGen = UUIDGenerator.getInstance();
		UUID uuidObj = uuidGen.generateTimeBasedUUID();
		sbiExecutionId = uuidObj.toString();
		sbiExecutionId = sbiExecutionId.replaceAll("-", "");
		parameters.put("SBI_EXECUTION_ID", sbiExecutionId);
		applySecurity(parameters, profile);
		EngineURL engineURL = new EngineURL(url, parameters);
		logger.debug("OUT");
		return engineURL;
	}

	protected Map addArtifactVersionId(byte[] template, Map pars, IEngUserProfile profile, int documentId, Engine engine) {
		SourceBean sb = null;
		try {
			sb = SourceBean.fromXMLString(new String(template));
		} catch (SourceBeanException e) {
			logger.error("Error while parsing document's template", e);
			throw new SpagoBIRuntimeException("Template is not a valid XML file", e);
		}
		SourceBean cubeSb = (SourceBean) sb.getAttribute(SpagoBIConstants.MONDRIAN_CUBE);
		Assert.assertNotNull(cubeSb, "Template is missing \"" + SpagoBIConstants.MONDRIAN_CUBE + "\" definition");
		String reference = (String) cubeSb.getAttribute(SpagoBIConstants.MONDRIAN_REFERENCE);
		Assert.assertNotNull(reference,
				"Template is missing \"" + SpagoBIConstants.MONDRIAN_REFERENCE + "\" property, that is the reference to the Mondrian schema");
		IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
		Artifact artifact = dao.loadArtifactByNameAndType(reference, SpagoBIConstants.MONDRIAN_SCHEMA);
		Assert.assertNotNull(artifact, "Mondrian schema with name [" + reference + "] was not found");
		Content content = dao.loadActiveArtifactContent(artifact.getId());
		Assert.assertNotNull(content, "Mondrian schema with name [" + reference + "] has no content");

		pars.put(SpagoBIConstants.SBI_ARTIFACT_VERSION_ID, content.getId());

		if (EngineUtilities.isWhatIf(engine)) {
			// add info if artifact is locked
			addArtifactStausInfo(pars, artifact.getId(), profile, documentId);
		}

		return pars;

	}

	protected Map addTemplateInformation(byte[] template, Map pars) {
		SourceBean sb = null;
		try {
			sb = SourceBean.fromXMLString(new String(template));
		} catch (SourceBeanException e) {
			logger.error("Error while parsing document's template", e);
			throw new SpagoBIRuntimeException("Template is not a valid XML file", e);
		}
		SourceBean cubeSb = (SourceBean) sb.getAttribute(SpagoBIConstants.MONDRIAN_CUBE);
		String reference = (String) cubeSb.getAttribute(SpagoBIConstants.MONDRIAN_REFERENCE);
		IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
		Artifact artifact = dao.loadArtifactByNameAndType(reference, SpagoBIConstants.MONDRIAN_SCHEMA);
		Assert.assertNotNull(artifact, "Mondrian schema with name [" + reference + "] was not found");

		pars.put("schemaID", artifact.getId());
		pars.put("schemaName", reference);
		return pars;
	}

	protected Map addArtifactId(byte[] template, Map pars, IEngUserProfile profile) {
		SourceBean sb = null;
		try {
			sb = SourceBean.fromXMLString(new String(template));
		} catch (SourceBeanException e) {
			logger.error("Error while parsing document's template", e);
			throw new SpagoBIRuntimeException("Template is not a valid XML file", e);
		}
		SourceBean cubeSb = (SourceBean) sb.getAttribute(SpagoBIConstants.MONDRIAN_CUBE);
		Assert.assertNotNull(cubeSb, "Template is missing \"" + SpagoBIConstants.MONDRIAN_CUBE + "\" definition");
		String reference = (String) cubeSb.getAttribute(SpagoBIConstants.MONDRIAN_REFERENCE);
		Assert.assertNotNull(reference,
				"Template is missing \"" + SpagoBIConstants.MONDRIAN_REFERENCE + "\" property, that is the reference to the Mondrian schema");
		IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
		Artifact artifact = dao.loadArtifactByNameAndType(reference, SpagoBIConstants.MONDRIAN_SCHEMA);
		Assert.assertNotNull(artifact, "Mondrian schema with name [" + reference + "] was not found");

		pars.put(SpagoBIConstants.SBI_ARTIFACT_ID, artifact.getId());

		return pars;

	}

	public Map addArtifactStausInfo(Map pars, Integer artifactId, IEngUserProfile profile, int documentId) {
		logger.debug("IN");

		String statusToReturn = null;
		String userId = ((UserProfile) profile).getUserId().toString();
		String locker;
		logger.debug("User Id is " + userId);
		logger.debug("Artifact Id is " + artifactId);

		IArtifactsDAO artifactsDAO = DAOFactory.getArtifactsDAO();

		Artifact artifact = artifactsDAO.loadArtifactById(artifactId);

		if (artifact == null) {
			logger.error("Artifact referring to id [" + artifactId + "] could not be loaded");
			throw new RuntimeException("Artifact with id [" + artifactId + "] could not be loaded", null);
		}

		logger.debug("Artifact id is " + artifactId);

		int did = artifact.getId();

		// Boolean locked = artifact.getModelLocked();

		try {
			if (!profile.getFunctionalities().contains("WorkFlowManagment")) {
				statusToReturn = SpagoBIConstants.SBI_ARTIFACT_VALUE_LOCKED_BY_USER;
				locker = userId;
			} else {
				WhatIfWorkflowManager wfm = new WhatIfWorkflowManager();
				locker = wfm.getActiveUser(did);
				if (locker == null) {
					logger.debug("Artifact with id " + artifactId + " is unlocked");
					statusToReturn = SpagoBIConstants.SBI_ARTIFACT_VALUE_UNLOCKED;
				} else {
					if (locker != null && locker.equals(userId)) {
						statusToReturn = SpagoBIConstants.SBI_ARTIFACT_VALUE_LOCKED_BY_USER;
					} else {
						statusToReturn = SpagoBIConstants.SBI_ARTIFACT_VALUE_LOCKED_BY_OTHER;
					}

				}
			}
			;
		} catch (EMFInternalError e) {
			logger.error("Error checking functionality", e);
			throw new SpagoBIRuntimeException("Error checking functionality", e);
		} catch (EMFUserError e) {
			logger.error("Error loading locker user", e);
			throw new SpagoBIRuntimeException("Error loading locker user", e);
		}

		logger.debug("Status of artifact is " + statusToReturn);
		pars.put(SpagoBIConstants.SBI_ARTIFACT_STATUS, statusToReturn);
		pars.put(SpagoBIConstants.SBI_ARTIFACT_LOCKER, locker != null ? locker : "");
		return pars;
	}

	private Map applyDatasourceForWriting(Map parameters, BIObject biObject) {
		IDataSource datasource;
		try {
			datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while loading default datasource for writing", e);
		}
		if (datasource != null) {
			parameters.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
		} else {
			logger.debug("There is no default datasource for writing");
		}
		return parameters;
	}

	@Override
	public List<DefaultOutputParameter> getSpecificOutputParameters(List categories) {
		List<DefaultOutputParameter> ret = new ArrayList<>();

		for (int i = 0; i < categories.size(); i++) {
			ret.add(new DefaultOutputParameter(categories.get(i) + "", TYPE.String));
		}

		return ret;
	}

}
