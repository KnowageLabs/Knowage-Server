/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.drivers.whatif;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Map;

import org.apache.log4j.Logger;

public class WhatIfDriver extends GenericDriver {

	static private Logger logger = Logger.getLogger(WhatIfDriver.class);

	@Override
	public Map getParameterMap(Object biobject, IEngUserProfile profile,
			String roleName) {
		Map pars = super.getParameterMap(biobject, profile, roleName);
		byte[] template = this.getTemplateAsByteArray( biobject );
		pars = addArtifactVersionId(template, pars, profile);
		pars = addArtifactId(template, pars, profile);
		pars = applyDatasourceForWriting(pars, (BIObject)biobject);
		return pars;
	}

	@Override
	public Map getParameterMap(Object biobject, Object subObject,
			IEngUserProfile profile, String roleName) {
		Map pars = super.getParameterMap(biobject, subObject, profile, roleName);
		byte[] template = this.getTemplateAsByteArray( biobject );
		pars = addArtifactVersionId(template, pars, profile);
		pars = addArtifactId(template, pars, profile);
		pars = applyDatasourceForWriting(pars, (BIObject)biobject);
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
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject,
			IEngUserProfile profile) throws InvalidOperationRequest {
		return super.getEditDocumentTemplateBuildUrl(biobject, profile);
	}

	@Override
	public EngineURL getNewDocumentTemplateBuildUrl(Object biobject,
			IEngUserProfile profile) throws InvalidOperationRequest {
		return super.getNewDocumentTemplateBuildUrl(biobject, profile);
	}

	protected Map addArtifactVersionId(byte[] template, Map pars, IEngUserProfile profile) {
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
		Assert.assertNotNull(reference, "Template is missing \"" + SpagoBIConstants.MONDRIAN_REFERENCE + "\" property, that is the reference to the Mondrian schema");
		IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
		Artifact artifact = dao.loadArtifactByNameAndType(reference,
				SpagoBIConstants.MONDRIAN_SCHEMA);
		Assert.assertNotNull(artifact, "Mondrian schema with name [" +  reference + "] was not found");
		Content content = dao.loadActiveArtifactContent(artifact.getId());
		Assert.assertNotNull(content, "Mondrian schema with name [" +  reference + "] has no content");

		pars.put(SpagoBIConstants.SBI_ARTIFACT_VERSION_ID, content.getId());

		// add info if artifact is locked			
		addArtifactStausInfo(pars, artifact.getId(), profile);


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
		Assert.assertNotNull(reference, "Template is missing \"" + SpagoBIConstants.MONDRIAN_REFERENCE + "\" property, that is the reference to the Mondrian schema");
		IArtifactsDAO dao = DAOFactory.getArtifactsDAO();
		Artifact artifact = dao.loadArtifactByNameAndType(reference,
				SpagoBIConstants.MONDRIAN_SCHEMA);
		Assert.assertNotNull(artifact, "Mondrian schema with name [" +  reference + "] was not found");

		pars.put(SpagoBIConstants.SBI_ARTIFACT_ID, artifact.getId());

		return pars;

	}





	public Map addArtifactStausInfo(Map pars, Integer artifactId, IEngUserProfile profile){
		logger.debug("IN");

		String statusToReturn = null;
		String userId = profile.getUserUniqueIdentifier().toString();		

		logger.debug("User Id is "+userId);
		logger.debug("Artifact Id is "+artifactId);

		IArtifactsDAO artifactsDAO = DAOFactory.getArtifactsDAO();

		Artifact artifact = artifactsDAO.loadArtifactById(artifactId);

		if(artifact == null)	{
			logger.error("Artifact referring to id [" + artifactId +"] could not be loaded");
			throw new RuntimeException("Artifact with id [" + artifactId + "] could not be loaded", null);
		}


		logger.debug("Artifact id is "+artifactId);

		Boolean locked = artifact.getModelLocked();
		String locker = artifact.getModelLocker();

		if( locked==null || locked==false){
			logger.debug("Artifact with id "+artifactId+" is unlocked");
			statusToReturn=SpagoBIConstants.SBI_ARTIFACT_VALUE_UNLOCKED;
		}
		else{
			if(locker != null && locker.equals(userId)){
				statusToReturn=SpagoBIConstants.SBI_ARTIFACT_VALUE_LOCKED_BY_USER;	
			}
			else{
				statusToReturn=SpagoBIConstants.SBI_ARTIFACT_VALUE_LOCKED_BY_OTHER;	
			}


		}

		logger.debug("Status of artifact is "+statusToReturn);
		pars.put(SpagoBIConstants.SBI_ARTIFACT_STATUS, statusToReturn);
		pars.put(SpagoBIConstants.SBI_ARTIFACT_LOCKER, locker != null ? locker : "");
		return pars;
	}

	private Map applyDatasourceForWriting(Map parameters, BIObject biObject) {
		IDataSource datasource;
		try {
			datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException(
					"Error while loading default datasource for writing", e);
		}
		if (datasource != null) {
			parameters.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
		} else {
			logger.debug("There is no default datasource for writing");
		}
		return parameters;
	}


}
