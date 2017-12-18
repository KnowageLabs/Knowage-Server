package it.eng.spagobi.kpi.statusCriterion;

import it.eng.spagobi.kpi.bo.IScorecardCriterion;
import it.eng.spagobi.kpi.bo.ScorecardStatus;
import it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS;

import java.util.ArrayList;
import java.util.List;

public class MajorityWithPriority extends Majority implements IScorecardCriterion {

	@Override
	public STATUS evaluate(List<ScorecardStatus> statusList) {
		List<ScorecardStatus> priorityList = new ArrayList<>();
		for (ScorecardStatus ss : statusList) {
			if (ss.isPriority()) {
				priorityList.add(ss);
			}
		}
		STATUS priorityStatus = super.evaluate(priorityList);
		if (priorityStatus.equals(STATUS.GREEN)) {
			return super.evaluate(statusList);
		} else {
			return priorityStatus;
		}
	}

}
