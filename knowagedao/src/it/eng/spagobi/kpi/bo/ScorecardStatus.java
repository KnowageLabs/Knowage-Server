package it.eng.spagobi.kpi.bo;

public class ScorecardStatus {

	public static enum STATUS {
		RED, YELLOW, GREEN, GRAY
	};

	private STATUS status;
	private boolean priority;

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

	/**
	 * @return the priority
	 */
	public boolean isPriority() {
		return priority;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(boolean priority) {
		this.priority = priority;
	}

}
