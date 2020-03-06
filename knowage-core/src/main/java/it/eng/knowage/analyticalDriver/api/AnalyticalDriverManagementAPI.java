package it.eng.knowage.analyticalDriver.api;

import java.util.List;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.crossnavigation.dao.ICrossNavigationDAO;

public class AnalyticalDriverManagementAPI {

	public boolean isUsedInCrossNavigations(Parameter driver) {
		/* Check if there are CrossNavs the modified parameter */
		ICrossNavigationDAO s = DAOFactory.getCrossNavigationDAO();
		List in = s.listNavigationsByAnalyticalDriverID(driver.getId());

		return in.size() > 0;
	}
}
