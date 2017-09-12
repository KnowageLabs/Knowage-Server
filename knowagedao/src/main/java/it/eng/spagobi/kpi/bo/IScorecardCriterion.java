package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS;

import java.util.List;

public interface IScorecardCriterion {

	public STATUS evaluate(List<ScorecardStatus> statusList);

}
