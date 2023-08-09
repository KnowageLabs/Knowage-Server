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

package it.eng.spagobi.utilities.engines.rest;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.utilities.engines.EngineAnalysisMetadata;

public abstract class AbstractEngineStartRestService extends AbstractEngineRestService {

	private static final Logger LOGGER = LogManager.getLogger(AbstractEngineRestService.class);

	/**
	 * Gets the analysis metadata.
	 *
	 * @return the analysis metadata
	 */
	public EngineAnalysisMetadata getAnalysisMetadata() {
		if (analysisMetadata != null) {
			return analysisMetadata;
		}

		analysisMetadata = new EngineAnalysisMetadata();

		if (requestContainsAttribute(SUBOBJ_ID)) {

			Integer id = getAttributeAsInteger(SUBOBJ_ID);
			if (id == null) {
				LOGGER.warn("Value [{}] is not a valid subobject id", getAttribute(SUBOBJ_ID).toString());
			}
			analysisMetadata.setId(id);

			if (requestContainsAttribute(SUBOBJ_NAME)) {
				analysisMetadata.setName(getAttributeAsString(SUBOBJ_NAME));
			} else {
				LOGGER.warn("No name attribute available in request for subobject [{}]",
						getAttributeAsString(SUBOBJ_ID));
				analysisMetadata.setName(getAttributeAsString(SUBOBJ_ID));
			}

			if (requestContainsAttribute(SUBOBJ_DESCRIPTION)) {
				analysisMetadata.setDescription(getAttributeAsString(SUBOBJ_DESCRIPTION));
			} else {
				LOGGER.warn("No description attribute available in request for subobject [{}]",
						getAttributeAsString(SUBOBJ_ID));
				analysisMetadata.setDescription("");
			}

			if (requestContainsAttribute(SUBOBJ_VISIBILITY)) {
				if (requestContainsAttribute(SUBOBJ_VISIBILITY, "Public")) {
					analysisMetadata.setScope(EngineAnalysisMetadata.PUBLIC_SCOPE);
				} else {
					LOGGER.warn("No visibility attribute available in request for subobject [{}]",
							getAttributeAsString(SUBOBJ_ID));
					analysisMetadata.setScope(EngineAnalysisMetadata.PRIVATE_SCOPE);
				}
			}
		}

		return analysisMetadata;
	}

	/**
	 * Gets the analysis state row data.
	 *
	 * @return the analysis state row data
	 */
	public byte[] getAnalysisStateRowData() {
		Content spagoBISubObject;
		byte[] rowData;

		if (analysisStateRowData == null && getAnalysisMetadata().getId() != null) {

			spagoBISubObject = getContentServiceProxy().readSubObjectContent(getAnalysisMetadata().getId().toString());
			rowData = DatatypeConverter.parseBase64Binary(spagoBISubObject.getContent());
			analysisStateRowData = rowData;

		}

		return analysisStateRowData;
	}

}
