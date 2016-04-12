package it.eng.spagobi.kpi.statusCriterion;

import it.eng.spagobi.kpi.bo.IScorecardCriterion;
import it.eng.spagobi.kpi.bo.ScorecardStatus;
import it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS;

import java.util.List;

public class MajorityWithPriority implements IScorecardCriterion {

	@Override
	public STATUS evaluate(List<ScorecardStatus> statusList) {
		// TODO Auto-generated method stub
		return STATUS.GRAY;
	}

}
