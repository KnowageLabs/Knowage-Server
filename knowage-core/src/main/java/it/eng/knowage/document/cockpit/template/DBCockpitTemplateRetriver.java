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
package it.eng.knowage.document.cockpit.template;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Dragan Pirkovic
 *
 */
public class DBCockpitTemplateRetriver implements ICockpitTemplateRetriver {

	private static Logger logger = Logger.getLogger(DBCockpitTemplateRetriver.class);
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
			logger.debug("getting template for document with id: " + documentId);
			ObjTemplate activeTemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId);
			return new JSONObject(new String(activeTemplate.getContent()));
		} catch (EMFInternalError | HibernateException | JSONException e) {
			logger.error("Error while getting template");
			throw new SpagoBIRuntimeException("Error while getting template");
		}
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
