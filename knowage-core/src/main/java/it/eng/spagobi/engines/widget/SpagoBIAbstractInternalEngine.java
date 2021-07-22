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
package it.eng.spagobi.engines.widget;

import java.util.Locale;
import java.util.Locale.Builder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.InternalEngineIFace;
import it.eng.spagobi.engines.drivers.exceptions.InvalidOperationRequest;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class SpagoBIAbstractInternalEngine implements InternalEngineIFace {

	RequestContainer requestContainer;
	BIObject obj;
	SourceBean response;
	Object subObjectInfo;

	private static transient Logger logger = Logger.getLogger(SpagoBIAbstractInternalEngine.class);

	@Override
	public void execute(RequestContainer requestContainer, BIObject obj, SourceBean response) throws EMFUserError {
		setRequestContainer(requestContainer);
		setObj(obj);
		setResponse(response);

		try {
			doExecute();
		} catch (Throwable t) {
			handleExecutionException(t);
		}
	}

	public abstract void doExecute();

	public void handleExecutionException(Throwable t) {
		handleException(t);
	}

	@Override
	public void executeSubObject(RequestContainer requestContainer, BIObject obj, SourceBean response, Object subObjectInfo) throws EMFUserError {
		setRequestContainer(requestContainer);
		setObj(obj);
		setResponse(response);
		setSubObjectInfo(subObjectInfo);

		try {
			doExecuteSubObject();
		} catch (Throwable t) {
			handleSubObjectExecutionException(t);
		}
	}

	public abstract void doExecuteSubObject();

	public void handleSubObjectExecutionException(Throwable t) {
		handleException(t);
	}

	@Override
	public void handleNewDocumentTemplateCreation(RequestContainer requestContainer, BIObject obj, SourceBean response)
			throws EMFUserError, InvalidOperationRequest {
		setRequestContainer(requestContainer);
		setObj(obj);
		setResponse(response);

		try {
			doCreateDocumentTemplate();
		} catch (Throwable t) {
			handleCreateDocumentTemplateException(t);
		}

	}

	public abstract void doCreateDocumentTemplate();

	public void handleCreateDocumentTemplateException(Throwable t) {
		handleException(t);
	}

	@Override
	public void handleDocumentTemplateEdit(RequestContainer requestContainer, BIObject obj, SourceBean response) throws EMFUserError, InvalidOperationRequest {

		setRequestContainer(requestContainer);
		setObj(obj);
		setResponse(response);

		try {
			doModifyDocumentTemplate();
		} catch (Throwable t) {
			handleModifyDocumentTemplateException(t);
		}
	}

	public abstract void doModifyDocumentTemplate();

	public void handleModifyDocumentTemplateException(Throwable t) {
		handleException(t);
	}

	public void handleException(Throwable t) {
		throw new SpagoBIEngineRuntimeException(t);
	}

	// ============================================================================================
	// UTILITY METHODS
	// ============================================================================================
	public IEngUserProfile getUserProfile() {
		return (IEngUserProfile) getSession().getPermanentContainer().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	}

	public JSONObject getTemplateAsJSONObject() throws JSONException {
		return new JSONObject(getTemplateAsString());
	}

	public String getTemplateAsString() {
		return new String(getTemplate());
	}

	public byte[] getTemplate() {

		byte[] contentBytes = null;

		try {
			ObjTemplate template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(getObj().getId());
			if (template == null)
				throw new Exception("Active Template null");
			contentBytes = template.getContent();
			if (contentBytes == null) {
				EMFUserError userError = new EMFUserError(EMFErrorSeverity.ERROR, 2007);
				userError.setBundle("messages");
				throw userError;
			}

			String contentStr = new String();

		} catch (Throwable t) {
			throw new SpagoBIEngineRuntimeException(t);
		}

		return contentBytes;
	}

	public IDataSet getDataSet() {
		IDataSet dataSet;
		IDataSetDAO dataSetDAO;

		dataSet = null;

		try {
			dataSetDAO = DAOFactory.getDataSetDAO();
			dataSet = dataSetDAO.loadDataSetById(getObj().getDataSetId());
		} catch (Throwable t) {
			throw new SpagoBIEngineRuntimeException(t);
		}

		return dataSet;
	}

	public Locale getLocale() {
		Locale locale = null;

		SessionContainer permanentSession = getSession().getPermanentContainer();
		String currLanguage = (String) permanentSession.getAttribute(SpagoBIConstants.AF_LANGUAGE);
		String currCountry = (String) permanentSession.getAttribute(SpagoBIConstants.AF_COUNTRY);
		String currScript = (String) permanentSession.getAttribute(SpagoBIConstants.AF_SCRIPT);
		if (currLanguage != null && currCountry != null) {
			Builder tmpLocale = new Locale.Builder().setLanguage(currLanguage).setRegion(currCountry);

			if (StringUtils.isNotBlank(currScript)) {
				tmpLocale.setScript(currScript);
			}

			locale = tmpLocale.build();
		} else
			locale = GeneralUtilities.getDefaultLocale();

		return locale;
	}

	// ============================================================================================
	// ACCESS METHODS
	// ============================================================================================

	protected RequestContainer getRequestContainer() {
		return requestContainer;
	}

	private void setRequestContainer(RequestContainer requestContainer) {
		this.requestContainer = requestContainer;
	}

	protected BIObject getObj() {
		return obj;
	}

	private void setObj(BIObject obj) {
		this.obj = obj;
	}

	protected SourceBean getResponse() {
		return response;
	}

	private void setResponse(SourceBean response) {
		this.response = response;
	}

	protected Object getSubObjectInfo() {
		return subObjectInfo;
	}

	private void setSubObjectInfo(Object subObjectInfo) {
		this.subObjectInfo = subObjectInfo;
	}

	protected SessionContainer getSession() {
		return requestContainer.getSessionContainer();
	}
}
