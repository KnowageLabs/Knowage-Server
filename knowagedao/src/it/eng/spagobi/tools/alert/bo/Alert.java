package it.eng.spagobi.tools.alert.bo;

public class Alert {

	private Integer id;
	private Integer name;
	private AlertListener alertListener;

	private String crono;
	private Long startDate;
	private Long endDate;
	private String startTime;
	private String endTime;

	public Alert() {
	}

	public Alert(Integer id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public Integer getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(Integer name) {
		this.name = name;
	}

	/**
	 * @return the alertListener
	 */
	public AlertListener getAlertListener() {
		return alertListener;
	}

	/**
	 * @param alertListener
	 *            the alertListener to set
	 */
	public void setAlertListener(AlertListener alertListener) {
		this.alertListener = alertListener;
	}

	/**
	 * @return the crono
	 */
	public String getCrono() {
		return crono;
	}

	/**
	 * @param crono
	 *            the crono to set
	 */
	public void setCrono(String crono) {
		this.crono = crono;
	}

	/**
	 * @return the startDate
	 */
	public Long getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Long getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the endTime
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime
	 *            the endTime to set
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return the startTime
	 */
	public String getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime
	 *            the startTime to set
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Alert other = (Alert) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
