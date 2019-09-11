/**
 *
 */
package it.eng.knowage.document.cockpit.template;

import org.hibernate.HibernateException;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.DAOFactory;

/**
 * @author Dragan Pirkovic
 *
 */
public class DBCockpitTemplateRetriver implements ICockpitTemplateRetriver {

	private Integer documentId;

	/**
	 * @param documentId
	 */
	public DBCockpitTemplateRetriver(Integer documentId) {
		this.documentId = documentId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.ICockpitTemplateRetriver#getTemplate()
	 */
	@Override
	public JSONObject getTemplate() {
		try {
			ObjTemplate activeTemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId);
			return new JSONObject(new String(activeTemplate.getContent()));
		} catch (EMFInternalError e) {

		} catch (HibernateException e) {

		} catch (JSONException e) {

		}
		return null;
	}

	/**
	 * @return the documentId
	 */
	public Integer getDocumentId() {
		return documentId;
	}

	/**
	 * @param documentId
	 *            the documentId to set
	 */
	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

}
