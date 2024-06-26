package it.eng.spagobi.api.v2.documentdetails.subresources;

import java.util.ArrayList;

/**
 * @author albnale
 * @since 2020/03/06
 */

import java.util.List;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.commons.dao.DAOFactory;

public class OutputParameterResourseAPI {

	public boolean isUsedInCrossNavigations(OutputParameter outputParameter) {
		List<OutputParameter> list = new ArrayList<OutputParameter>();
		list.add(outputParameter);
		return foundRelatedCrossNavs(list);
	}

	public boolean isUsedInCrossNavigations(BIObject document) {
		return foundRelatedCrossNavs(document.getOutputParameters());
	}

	private boolean foundRelatedCrossNavs(List<OutputParameter> outputParameters) {
		for (OutputParameter outputParameter : outputParameters) {

			List crossNavigationsFound = DAOFactory.getCrossNavigationDAO().listNavigationsByOutputParameters(outputParameter.getId());

			if (crossNavigationsFound.size() > 0)
				return true;
		}

		return false;

	}
}
