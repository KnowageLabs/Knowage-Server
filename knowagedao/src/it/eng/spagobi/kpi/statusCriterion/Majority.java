package it.eng.spagobi.kpi.statusCriterion;

import it.eng.spagobi.kpi.bo.IScorecardCriterion;
import it.eng.spagobi.kpi.bo.ScorecardStatus;
import it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS;

import java.util.List;

/**
 * It will produce a list with the only status that is present more times
 */
public class Majority implements IScorecardCriterion {

	@Override
	public STATUS evaluate(List<ScorecardStatus> statusList) {
		int countRed = 0;
		int countYellow = 0;
		int countGreen = 0;

		for (ScorecardStatus ss : statusList) {
			switch (ss.getStatus()) {
			case GREEN:
				countGreen++;
				break;
			case RED:
				countRed++;
				break;
			case YELLOW:
				countYellow++;
				break;
			case GRAY:
				// Nothing to do
			}
		}
		STATUS ret;
		if (countGreen > countYellow && countGreen > countRed) {
			ret = STATUS.GREEN;
		} else if (countYellow > countRed) {
			ret = STATUS.YELLOW;
		} else if (countRed > 0) {
			ret = STATUS.RED;
		} else {
			ret = STATUS.GRAY;
		}
		System.out.println("Resulting status " + ret.toString());
		return ret;
	}

}
