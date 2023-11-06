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
package it.eng.spagobi.analiticalmodel.document.dao;

import java.text.ParseException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjTemplates;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

public interface IObjTemplateDAO extends ISpagoBIDao {

	/**
	 * Gets the bI object active template.
	 *
	 * @param biobjId the biobj id
	 *
	 * @return the bI object active template
	 *
	 * @throws EMFInternalError the EMF internal error
	 */
	ObjTemplate getBIObjectActiveTemplate(Integer biobjId) throws EMFInternalError;

	/**
	 * Gets the bI object active template starting by document label
	 *
	 * @param biobjLabel the BiObject label
	 *
	 * @return the bI object active template
	 *
	 * @throws EMFInternalError the EMF internal error
	 */
	ObjTemplate getBIObjectActiveTemplateByLabel(String label) throws EMFInternalError;

	/**
	 * Gets the bI object template list.
	 *
	 * @param biobjId the biobj id
	 *
	 * @return the bI object template list
	 *
	 * @throws EMFInternalError the EMF internal error
	 */
	List<ObjTemplate> getBIObjectTemplateList(Integer biobjId) throws EMFInternalError;

	/**
	 * Load bi object template.
	 *
	 * @param tempId the temp id
	 *
	 * @return the obj template
	 *
	 * @throws EMFInternalError the EMF internal error
	 */
	ObjTemplate loadBIObjectTemplate(Integer tempId) throws EMFInternalError;

	/**
	 * Gets the next prog for template.
	 *
	 * @param biobjId the biobj id
	 *
	 * @return the next prog for template
	 *
	 * @throws EMFInternalError the EMF internal error
	 */
	List<ObjTemplate> getAllTemplateWithoutActive(String data) throws EMFInternalError, ParseException;

	void removeTemplates(JSONArray documents) throws EMFInternalError, JSONException;

	Integer getNextProgForTemplate(Integer biobjId) throws EMFInternalError;

	/**
	 * Delete bi object template.
	 *
	 * @param tempId the temp id
	 *
	 * @throws EMFInternalError the EMF internal error
	 */
	void deleteBIObjectTemplate(Integer tempId) throws EMFInternalError;

	/**
	 * Setting active bi object template.
	 *
	 * @param objTemplate the new template
	 *
	 * @throws EMFInternalError the EMF internal error
	 */
	void setTemplateActive(ObjTemplate objTemplate, BIObject biObject) throws EMFUserError, EMFInternalError;

	/**
	 * Insert a new bi object template.
	 *
	 * @param objTemplate the new template
	 *
	 * @throws EMFInternalError the EMF internal error
	 */
	void insertBIObjectTemplate(ObjTemplate objTemplate, BIObject biObject);

	/**
	 * Change activity of doc's template.
	 *
	 * @param biObjId biobj id
	 * @param tempId  temp id
	 *
	 * @throws EMFInternalError the EMF internal error
	 */
	void setPreviousTemplateActive(Integer biObjId, Integer tempId) throws EMFInternalError;

	ObjTemplate toObjTemplate(SbiObjTemplates hibObjTemp);

}
