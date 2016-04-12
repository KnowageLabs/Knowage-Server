package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS;

public class KpiExecution extends Kpi {

	private STATUS status;

	/**
	 * @return the status
	 */
	public STATUS getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(STATUS status) {
		this.status = status;
	}

}
