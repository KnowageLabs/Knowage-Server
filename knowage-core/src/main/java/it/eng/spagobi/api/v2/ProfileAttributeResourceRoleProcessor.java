package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.profiling.bean.SbiUserAttributesId;
import it.eng.spagobi.profiling.bo.UserBO;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;

public class ProfileAttributeResourceRoleProcessor extends AbstractSpagoBIResource {

	public ArrayList<Integer> getHiddenAttributesIds() throws EMFUserError {
		ArrayList<Integer> hiddenAttributesIds = new ArrayList<>();
		ISbiAttributeDAO objDao = DAOFactory.getSbiAttributeDAO();
		objDao.setUserProfile(getUserProfile());
		List<SbiAttribute> attrList = objDao.loadSbiAttributes();

		for (SbiAttribute hiddenAttribute : attrList) {
			if (hiddenAttribute.getAllowUser() != null && hiddenAttribute.getAllowUser() == 0) {
				hiddenAttributesIds.add(hiddenAttribute.getAttributeId());
			}
		}

		return hiddenAttributesIds;
	}

	public void removeHiddenAttributes(ArrayList<Integer> hiddenAttributesIds, UserBO user) {
		for (Integer attributeId : hiddenAttributesIds) {
			if (user.getSbiUserAttributeses().containsKey(attributeId)) {
				user.getSbiUserAttributeses().remove(attributeId);
			}
		}
	}

	public void setAttributeHiddenFromUser(SbiUser sbiUserOriginal, Set<SbiUserAttributes> attributes, List<SbiAttribute> attrList) {
		for (SbiUserAttributes userAttribute : sbiUserOriginal.getSbiUserAttributeses()) {
			for (SbiAttribute hiddenAttribute : attrList) {
				SbiUserAttributes attributeHidden = new SbiUserAttributes();
				SbiUserAttributesId attid = new SbiUserAttributesId();
				if (hiddenAttribute.getAllowUser() != null && hiddenAttribute.getAllowUser() == 0
						&& userAttribute.getId().getAttributeId() == hiddenAttribute.getAttributeId()) {
					attid.setAttributeId(hiddenAttribute.getAttributeId());
					attributeHidden.setId(attid);
					attributeHidden.setAttributeValue(userAttribute.getAttributeValue());
					attributes.add(attributeHidden);
				}
			}

		}
	}
}
