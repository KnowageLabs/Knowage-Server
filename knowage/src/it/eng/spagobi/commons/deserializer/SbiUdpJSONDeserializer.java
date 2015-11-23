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
