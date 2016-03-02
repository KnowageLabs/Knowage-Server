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
package it.eng.spagobi.commons.deserializer;

import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class SbiUdpJSONDeserializer implements Deserializer {

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String NAME = "name";

	public static final String TYPE_STR = "type";
	public static final String FAMILY_STR = "family";
	public static final String DESCRIPTION = "description";
	public static final String IS_MULTIVALUE = "multivalue";

	private static Logger logger = Logger.getLogger(SbiUdpJSONDeserializer.class);

	@Override
	public Object deserialize(Object o, Class clazz) throws DeserializationException {

		SbiUdp sbiUdp;

		Integer udpId = null;
		String typeStr = "";
		String familyStr = "";
		String label = "";
		String name = "";
		String description = "";
		boolean isMultivalue = false;

		logger.debug("IN");

		sbiUdp = new SbiUdp();

		try {
			Assert.assertNotNull(o, "Input parameter [" + o + "] cannot be null");
			Assert.assertNotNull(o, "Input parameter [" + clazz + "] cannot be null");

			JSONObject json = null;
			if (o instanceof JSONObject) {
				json = (JSONObject) o;
			} else if (o instanceof String) {
				json = new JSONObject((String) o);
			} else {
				throw new DeserializationException("Impossible to deserialize from an object of type [" + o.getClass().getName() + "]");
			}

			if (json.getString(ID).length() > 0) {
				udpId = Integer.parseInt(json.getString(ID));
			}
			label = json.getString(LABEL);
			name = json.getString(NAME);
			description = json.getString(DESCRIPTION);
			if (json.getString(TYPE_STR).length() > 0) {
				typeStr = json.getString(TYPE_STR);
			}
			if (json.getString(FAMILY_STR).length() > 0) {
				familyStr = json.getString(FAMILY_STR);
			}
			if (json.getString(IS_MULTIVALUE).length() > 0) {
				isMultivalue = Boolean.getBoolean(json.getString(FAMILY_STR));
			}

			Integer typeId = null;
			Integer familyId = null;
			List<Domain> tmpDomains = DAOFactory.getDomainDAO().loadListDomains();
			for (Domain domain : tmpDomains) {
				if (domain.getValueCd().toUpperCase().equals(typeStr.toUpperCase())) {
					typeId = domain.getValueId();
				}
				if (domain.getValueCd().toUpperCase().equals(familyStr.toUpperCase())) {
					familyId = domain.getValueId();
				}
			}

			sbiUdp.setUdpId(udpId);
			sbiUdp.setName(name);
			sbiUdp.setLabel(label);
			sbiUdp.setDescription(description);
			sbiUdp.setIsMultivalue(isMultivalue);
			sbiUdp.setTypeId(typeId);
			sbiUdp.setFamilyId(familyId);

		} catch (Throwable t) {
			throw new DeserializationException("An error occurred while deserializing object: " + o, t);
		} finally {
			logger.debug("OUT");
		}

		return sbiUdp;
	}
}
