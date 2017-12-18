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
package it.eng.spagobi.engines.drivers.kpi;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.engines.chart.utils.StyleLabel;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.EngineURL;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;
import it.eng.spagobi.monitoring.dao.AuditManager;

/**
 *
 */

public class KpiDriver extends GenericDriver {
	static transient Logger logger = Logger.getLogger(KpiDriver.class);

	public static final String messageBundle = "MessageFiles.messages";

	protected static final String RESOURCE = "RES_NAME";

	protected String name = "";// Document's title
	protected String subName = "";// Document's subtitle
	protected StyleLabel styleTitle;// Document's title style
	protected StyleLabel styleSubTitle;// Document's subtitle style
	protected String userIdField = null;

	public List resources;// List of resources linked to the
	// ModelInstanceNode

	protected Integer periodInstID = null;

	protected Integer modelInstanceRootId = null;

	private String ouWarning = null;

	public HashMap confMap;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}

	// Method only called by a specific configuration of the scheduler created through the class KPIEngineJob.java
	public void executeByKpiEngineJob(RequestContainer requestContainer, SourceBean response) throws EMFUserError, SourceBeanException {
		logger.debug("IN");
		// setting locale, formats, profile

		String recalculate = (String) requestContainer.getAttribute("recalculate_anyway");

		String cascade = (String) requestContainer.getAttribute("cascade");

		// **************take informations on the modelInstance and its KpiValues*****************
		String modelNodeInstance = (String) requestContainer.getAttribute("model_node_instance");
		logger.info("ModelNodeInstance : " + modelNodeInstance);

		if (modelNodeInstance == null) {
			logger.error("The modelNodeInstance specified in the template is null");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "10106", messageBundle);
		}

		logger.debug("OUT");
	}

	/**
	 * Method used by basic execution and by the scheduler. Executes the document and populates the response.
	 *
	 * @param requestContainer
	 *            The <code>RequestContainer</code> object (the session can be retrieved from this object)
	 * @param obj
	 *            The <code>BIObject</code> representing the document to be executed
	 * @param response
	 *            The response <code>SourceBean</code> to be populated
	 * @throws EMFUserError
	 *             the EMF user error
	 */

	public void execute(RequestContainer requestContainer, BIObject obj, SourceBean response) throws EMFUserError {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("kpi.engines.SpagoBIKpiInternalEngine.execute");

		// AUDIT UPDATE
		Integer auditId = null;
		String auditIdStr = null;
		AuditManager auditManager = AuditManager.getInstance();

		if (requestContainer.getServiceRequest() != null) {
			auditIdStr = (String) requestContainer.getServiceRequest().getAttribute(AuditManager.AUDIT_ID);
			if (auditIdStr == null) {
				logger.warn("Audit record id not specified! No operations will be performed");
			} else {
				logger.debug("Audit id = [" + auditIdStr + "]");
				auditId = new Integer(auditIdStr);
			}

			if (auditId != null) {
				auditManager.updateAudit(auditId, new Long(System.currentTimeMillis()), null, "EXECUTION_STARTED", null, null);
			}
		}

		ResponseContainer responseContainer = ResponseContainer.getResponseContainer();
		EMFErrorHandler errorHandler = responseContainer.getErrorHandler();

		if (obj == null) {
			logger.error("The input object is null.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "100", messageBundle);
		}
		if (!obj.getBiObjectTypeCode().equalsIgnoreCase("KPI")) {
			logger.error("The input object is not a KPI.");
			throw new EMFUserError(EMFErrorSeverity.ERROR, "1001", messageBundle);
		}
		String userId = null;
		userId = userIdField;

		String documentId = obj.getId().toString();
		logger.debug("Loaded documentId:" + documentId);

		try {
			logger.debug("Successfull kpis creation");

			response.setAttribute(ObjectsTreeConstants.SESSION_OBJ_ATTR, obj);

			if (name != null) {
				response.setAttribute("title", name);
			}
			if (styleTitle != null) {
				response.setAttribute("styleTitle", styleTitle);
			}
			if (subName != null) {
				response.setAttribute("subName", subName);
			}
			if (styleSubTitle != null) {
				response.setAttribute("styleSubTitle", styleSubTitle);
			}

			if (auditId != null)
				response.setAttribute(AuditManager.AUDIT_ID, auditId);

		} catch (Exception eex) {
			logger.error("Exception", eex);
			EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 10107);
			userError.setBundle("messages");
			throw userError;
		}
		logger.debug("OUT");

	}

	/**
	 * The <code>SpagoBIDashboardInternalEngine</code> cannot manage subobjects so this method must not be invoked.
	 *
	 * @param requestContainer
	 *            The <code>RequestContainer</code> object (the session can be retrieved from this object)
	 * @param obj
	 *            The <code>BIObject</code> representing the document
	 * @param response
	 *            The response <code>SourceBean</code> to be populated
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void executeSubObject(RequestContainer requestContainer, BIObject obj, SourceBean response, Object subObjectInfo) throws EMFUserError {
		// it cannot be invoked
		logger.error("SpagoBIKpiInternalEngine cannot exec subobjects.");
		throw new EMFUserError(EMFErrorSeverity.ERROR, "101", messageBundle);
	}

	/**
	 * Function not implemented. Thid method should not be called
	 *
	 * @param requestContainer
	 *            The <code>RequestContainer</code> object (the session can be retrieved from this object)
	 * @param response
	 *            The response <code>SourceBean</code> to be populated
	 * @param obj
	 *            the obj
	 * @throws InvalidOperationRequest
	 *             the invalid operation request
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void handleNewDocumentTemplateCreation(RequestContainer requestContainer, BIObject obj, SourceBean response)
			throws EMFUserError, InvalidOperationRequest {
		logger.error("SpagoBIDashboardInternalEngine cannot build document template.");
		throw new InvalidOperationRequest();

	}

	/**
	 * Function not implemented. Thid method should not be called
	 *
	 * @param requestContainer
	 *            The <code>RequestContainer</code> object (the session can be retrieved from this object)
	 * @param response
	 *            The response <code>SourceBean</code> to be populated
	 * @param obj
	 *            the obj
	 * @throws InvalidOperationRequest
	 *             the invalid operation request
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void handleDocumentTemplateEdit(RequestContainer requestContainer, BIObject obj, SourceBean response) throws EMFUserError, InvalidOperationRequest {
		logger.error("SpagoBIDashboardInternalEngine cannot build document template.");
		throw new InvalidOperationRequest();
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
		String url = engine.getUrl();
		HashMap parameters = new HashMap();
		String documentId = obj.getId().toString();
		parameters.put("document", documentId);
		applySecurity(parameters, profile);
		EngineURL engineURL = new EngineURL(url.replace("/execute", "/edit"), parameters);
		logger.debug("OUT");
		return engineURL;
		// return new EngineURL("pippo.jsp", new HashMap<>());
	}

	@Override
	public EngineURL getEditDocumentTemplateBuildUrl(Object biobject, IEngUserProfile profile) throws InvalidOperationRequest {
		// TODO Auto-generated method stub
		logger.debug("IN");
		BIObject obj = null;
		try {
			obj = (BIObject) biobject;
		} catch (ClassCastException cce) {
			logger.error("The input object is not a BIObject type", cce);
			return null;
		}
		Engine engine = obj.getEngine();
		String url = engine.getUrl();
		HashMap parameters = new HashMap();
		String documentId = obj.getId().toString();
		parameters.put("document", documentId);
		applySecurity(parameters, profile);
		EngineURL engineURL = new EngineURL(url.replace("/execute", "/edit"), parameters);
		logger.debug("OUT");
		return engineURL;
	}
}