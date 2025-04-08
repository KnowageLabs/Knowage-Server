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
package it.eng.spagobi.tools.crossnavigation.dao;

import java.util.List;

import org.hibernate.Session;
import org.json.JSONArray;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.crossnavigation.bo.NavigationDetail;
import it.eng.spagobi.tools.crossnavigation.bo.SimpleNavigation;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigation;
import it.eng.spagobi.tools.crossnavigation.metadata.SbiCrossNavigationPar;

public interface ICrossNavigationDAO extends ISpagoBIDao {

	List<SimpleNavigation> listNavigation();

	List<SimpleNavigation> listNavigationLight();

	NavigationDetail loadNavigation(Integer id);

	void update(NavigationDetail nd);

	void insert(NavigationDetail nd);

	void delete(Integer id);

	JSONArray loadNavigationByDocument(String label);

	boolean documentIsCrossable(String docLabel);

	List<SbiCrossNavigation> listNavigationsByDocumentAndParameters(Integer documentId, List<Integer> inputParameters, List<Integer> outputParameters,
			Session session);

	List<SbiCrossNavigationPar> listNavigationsByInputParameters(Integer paramId);

	List<SbiCrossNavigationPar> listNavigationsByInputParameters(Integer paramId, Session session);

	List<SbiCrossNavigationPar> listNavigationsByOutputParameters(Integer paramId);

	List<SbiCrossNavigationPar> listNavigationsByOutputParameters(Integer paramId, Session session);

	List<SbiCrossNavigationPar> listNavigationsByCrossNavParId(Integer crossNavId, Session session);

	void deleteByBIObjectParameter(BIObjectParameter biObjectParameter, Session session);

	void deleteByDocument(BIObject document, Session session);

	SbiCrossNavigation loadSbiCrossNavigationById(Integer id, Session session);

	List listNavigationsByAnalyticalDriverID(Integer analyticalDriverId);

	List listNavigationsByAnalyticalDriverID(Integer analyticalDriverId, Session session);

}
