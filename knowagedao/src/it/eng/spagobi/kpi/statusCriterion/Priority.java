package it.eng.spagobi.kpi.statusCriterion;

import it.eng.spagobi.kpi.bo.IScorecardCriterion;
import it.eng.spagobi.kpi.bo.ScorecardStatus;
import it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS;

import java.util.List;

public class Priority implements IScorecardCriterion {

	@Override
	public STATUS evaluate(List<ScorecardStatus> statusList) {
		for (ScorecardStatus scorecardStatus : statusList) {
			if (scorecardStatus.isPriority()) {
				return scorecardStatus.getStatus();
			}
		}
		return STATUS.GRAY;
	}

}
