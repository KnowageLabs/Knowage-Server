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
package it.eng.spagobi.tools.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dbaccess.sql.DataRow;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterValuesRetriever;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;

/**
 *
 * This class retrieves values for the input BIObjectParameter using the modality related to the roleToBeUsed field and the user profile specified by the
 * userIndentifierToBeUsed field.
 *
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class RuntimeLoadingParameterValuesRetriever extends ParameterValuesRetriever {

	private static Logger logger = Logger.getLogger(RuntimeLoadingParameterValuesRetriever.class);

	private String roleToBeUsed;

	private UserProfile profile;

	@Override
	public List<String> retrieveValues(BIObjectParameter parameter) throws Exception {
		logger.debug("IN");
		List<String> toReturn = new ArrayList<String>();
		IParameterUseDAO parusedao;
		try {
			parusedao = DAOFactory.getParameterUseDAO();
			ParameterUse paruse = parusedao.loadByParameterIdandRole(parameter.getParID(), roleToBeUsed);
			Integer manInp = paruse.getManualInput();
			if (manInp.intValue() == 1) {
				// modality is manual input, no values can be retrieved
				return toReturn;
			} else {
				// modality is not manual input, load list of values from lov
				toReturn = loadList(parameter.getParID());
			}
		} catch (Exception e) {
			logger.error(e);
			throw e;
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	private List<String> loadList(Integer parId) throws Exception {
		logger.debug("IN");
		List<String> toReturn = new ArrayList<String>();
		try {
			IParameterDAO pardao = DAOFactory.getParameterDAO();
			Parameter par = pardao.loadForExecutionByParameterIDandRoleName(parId, roleToBeUsed, false);
			ModalitiesValue modVal = par.getModalityValue();
			// get the lov provider
			String looProvider = modVal.getLovProvider();
			// get from the request the type of lov
			ILovDetail lovDetail = LovDetailFactory.getLovFromXML(looProvider);
			String result = lovDetail.getLovResult(profile, null, null, null);
			SourceBean rowsSourceBean = SourceBean.fromXMLString(result);
			List rows = null;
			if (rowsSourceBean != null) {
				rows = rowsSourceBean.getAttributeAsList(DataRow.ROW_TAG);
				if (rows != null && rows.size() != 0) {
					Iterator it = rows.iterator();
					while (it.hasNext()) {
						SourceBean row = (SourceBean) it.next();
						Object value = row.getAttribute(lovDetail.getValueColumnName());
						if (value != null) {
							toReturn.add(value.toString());
						}
					}
				}
			}
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	public String getRoleToBeUsed() {
		return roleToBeUsed;
	}

	public void setRoleToBeUsed(String roleToBeUsed) {
		this.roleToBeUsed = roleToBeUsed;
	}

	public UserProfile getUserProfile() {
		return profile;
	}

	public void setUserProfile(UserProfile profile) {
		this.profile = profile;
	}

}
