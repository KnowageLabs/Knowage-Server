package it.eng.spagobi.tools.alert.bo;

import it.eng.spagobi.tools.scheduler.bo.Frequency;

public class Alert {

	private Integer id;
	private String name;
	private String jsonOptions;
	private AlertListener alertListener;

	private Frequency frequency;

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
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
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
	 * @return the frequency
	 */
	public Frequency getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency
	 *            the frequency to set
	 */
	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}

	/**
	 * @return the jsonOptions
	 */
	public String getJsonOptions() {
		return jsonOptions;
	}

	/**
	 * @param jsonOptions
	 *            the jsonOptions to set
	 */
	public void setJsonOptions(String jsonOptions) {
		this.jsonOptions = jsonOptions;
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
