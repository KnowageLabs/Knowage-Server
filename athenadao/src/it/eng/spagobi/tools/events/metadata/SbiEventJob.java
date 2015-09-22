package it.eng.spagobi.tools.events.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiEventJob extends SbiHibernateModel {

	private Integer event_id;

	private String name;

	private String description;

	private String event_type;

	private boolean is_suspended;

	private Integer dataset;

	private Integer frequency;

	/**
	 * @param event_id
	 * @param name
	 * @param description
	 * @param event_type
	 * @param is_suspended
	 * @param dataset
	 * @param frequency
	 */

	public SbiEventJob() {
		super();
	}

	public SbiEventJob(Integer event_id, String name, String description, String event_type, boolean is_suspended, Integer dataset, Integer frequency) {
		super();
		this.event_id = event_id;
		this.name = name;
		this.description = description;
		this.event_type = event_type;
		this.is_suspended = is_suspended;
		this.dataset = dataset;
		this.frequency = frequency;
	}

	/**
	 * @return the event_id
	 */
	public Integer getEvent_id() {
		return event_id;
	}

	/**
	 * @param event_id
	 *            the event_id to set
	 */
	public void setEvent_id(Integer event_id) {
		this.event_id = event_id;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the event_type
	 */
	public String getEvent_type() {
		return event_type;
	}

	/**
	 * @param event_type
	 *            the event_type to set
	 */
	public void setEvent_type(String event_type) {
		this.event_type = event_type;
	}

	/**
	 * @return the is_suspended
	 */
	public boolean isIs_suspended() {
		return is_suspended;
	}

	/**
	 * @param is_suspended
	 *            the is_suspended to set
	 */
	public void setIs_suspended(boolean is_suspended) {
		this.is_suspended = is_suspended;
	}

	/**
	 * @return the dataset
	 */
	public Integer getDataset() {
		return dataset;
	}

	/**
	 * @param dataset
	 *            the dataset to set
	 */
	public void setDataset(Integer dataset) {
		this.dataset = dataset;
	}

	/**
	 * @return the frequency
	 */
	public Integer getFrequency() {
		return frequency;
	}

	/**
	 * @param frequency
	 *            the frequency to set
	 */
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

}
