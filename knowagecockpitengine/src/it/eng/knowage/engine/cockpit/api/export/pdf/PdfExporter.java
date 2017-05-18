/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engine.cockpit.api.export.pdf;

import it.eng.spago.error.EMFAbstractError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */

public class PdfExporter {

	private static final Logger logger = Logger.getLogger(PdfExporter.class);

	private int documentId;
	private String requestUrl;
	private String userId;

	public PdfExporter(int documentId, String requestUrl, String userId) {
		this.documentId = documentId;
		this.requestUrl = requestUrl;
		this.userId = userId;
	}

	public byte[] getBinaryData() {
		int sheetCount = getSheetCount();
		return "This is not a PDF file!".getBytes(); // FIXME
	}

	private int getSheetCount() {
		try {
			ObjTemplate objTemplate = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId);
			if (objTemplate == null) {
				throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
			}

			String templateString = new String(objTemplate.getContent());
			JSONObject template = new JSONObject(templateString);
			JSONArray sheets = template.getJSONArray("sheets");

			return sheets.length();
		} catch (EMFAbstractError e) {
			throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid template for document with id [" + documentId + "]", e);
		}
	}
}
