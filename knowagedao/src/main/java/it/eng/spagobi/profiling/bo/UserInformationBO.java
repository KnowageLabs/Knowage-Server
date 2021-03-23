/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.profiling.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.tenant.TenantManager;

/**
 * Business Object used to retrieve basic informations about current the user
 * 
 * @since 2021/03/05
 * @author albnale
 */

public class UserInformationBO {

	private Integer id;
	private String userId;
	private String fullName;
	private Boolean isSuperadmin;
	private Date dtLastAccess;
	private Integer defaultRoleId;
	private boolean blockedByFailedLoginAttempts;
	private Map<String, Object> attributes;
	private JSONObject locale;
	private String organization;

	public UserInformationBO(UserBO user) throws EMFUserError {
		this.id = user.getId();
		this.userId = user.getUserId();
		this.fullName = user.getFullName();
		this.isSuperadmin = user.getIsSuperadmin();
		this.dtLastAccess = user.getDtLastAccess();
		this.defaultRoleId = user.getDefaultRoleId();
		this.blockedByFailedLoginAttempts = user.getBlockedByFailedLoginAttempts();
		this.organization = TenantManager.getTenant().getName();

		Locale defaultLocale = GeneralUtilities.getDefaultLocale();

		JSONObject localeJSON = new JSONObject();
		try {
			localeJSON.put("country", defaultLocale.getCountry());
			localeJSON.put("language", defaultLocale.getLanguage());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.locale = localeJSON;

		Map<String, Object> userAttributes = new HashMap<String, Object>();
		HashMap<Integer, HashMap<String, String>> sbiUserAttributes = user.getSbiUserAttributeses();
		ISbiAttributeDAO objDao = DAOFactory.getSbiAttributeDAO();
		for (Integer attributeId : sbiUserAttributes.keySet()) {

			SbiAttribute sbiAttribute = objDao.loadSbiAttributeById(attributeId);

			HashMap<String, String> attributeValuesMap = sbiUserAttributes.get(attributeId);
			Iterator<String> it = attributeValuesMap.keySet().iterator();

			while (it.hasNext()) {
				String key = it.next();

				Object o = attributeValuesMap.get(key);
				String s = String.valueOf(o);

				Object value = s;
				if (sbiAttribute.getMultivalue() != null && sbiAttribute.getMultivalue() == 1) {

					if (s.contains("{")) {
						/* {,{Percorso 1 names,Percorso 2 names}} */

						Pattern pattern = Pattern.compile("(\\{([.,;#]{1})\\{)([a-zA-Z0-9;,.# ]*)(\\}\\})", Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(s);
						boolean matchFound = matcher.find();
						if (matchFound) {
							value = matcher.group(3).split(matcher.group(2));
						}

					} else {
						if (o instanceof List) {
							value = new ArrayList<String>((List) o);
						} else if (o instanceof Set) {
							value = new HashSet<>((Set) o);
						}
					}
				}
				userAttributes.put(key, value);

			}
		}
		this.attributes = userAttributes;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Boolean getIsSuperadmin() {
		return isSuperadmin;
	}

	public void setIsSuperadmin(Boolean isSuperadmin) {
		this.isSuperadmin = isSuperadmin;
	}

	public Date getDtLastAccess() {
		return dtLastAccess;
	}

	public void setDtLastAccess(Date dtLastAccess) {
		this.dtLastAccess = dtLastAccess;
	}

	public Integer getDefaultRoleId() {
		return defaultRoleId;
	}

	public void setDefaultRoleId(Integer defaultRoleId) {
		this.defaultRoleId = defaultRoleId;
	}

	public boolean isBlockedByFailedLoginAttempts() {
		return blockedByFailedLoginAttempts;
	}

	public void setBlockedByFailedLoginAttempts(boolean blockedByFailedLoginAttempts) {
		this.blockedByFailedLoginAttempts = blockedByFailedLoginAttempts;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public JSONObject getLocale() {
		return locale;
	}

	public void setLocale(JSONObject locale) {
		this.locale = locale;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

}