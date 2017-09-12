package it.eng.spagobi.tools.alert.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiAlert extends SbiHibernateModel {

	private Integer id;
	private String name;
	private Character singleExecution;
	private Integer eventBeforeTriggerAction;
	private SbiAlertListener sbiAlertListener;
	private Integer listenerId;
	private String listenerOptions;

	/**
	 * @return the singleExecution
	 */
	public Character getSingleExecution() {
		return singleExecution;
	}

	/**
	 * @param singleExecution
	 *            the singleExecution to set
	 */
	public void setSingleExecution(Character singleExecution) {
		this.singleExecution = singleExecution;
	}

	/**
	 * @return the eventBeforeTriggerAction
	 */
	public Integer getEventBeforeTriggerAction() {
		return eventBeforeTriggerAction;
	}

	/**
	 * @param eventBeforeTriggerAction
	 *            the eventBeforeTriggerAction to set
	 */
	public void setEventBeforeTriggerAction(Integer eventBeforeTriggerAction) {
		this.eventBeforeTriggerAction = eventBeforeTriggerAction;
	}

	/**
	 * @return the listenerOptions
	 */
	public String getListenerOptions() {
		return listenerOptions;
	}

	/**
	 * @param listenerOptions
	 *            the listenerOptions to set
	 */
	public void setListenerOptions(String listenerOptions) {
		this.listenerOptions = listenerOptions;
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
	 * @return the sbiAlertListener
	 */
	public SbiAlertListener getSbiAlertListener() {
		return sbiAlertListener;
	}

	/**
	 * @param sbiAlertListener
	 *            the sbiAlertListener to set
	 */
	public void setSbiAlertListener(SbiAlertListener sbiAlertListener) {
		this.sbiAlertListener = sbiAlertListener;
	}

	/**
	 * @return the listenerId
	 */
	public Integer getListenerId() {
		return listenerId;
	}

	/**
	 * @param listenerId
	 *            the listenerId to set
	 */
	public void setListenerId(Integer listenerId) {
		this.listenerId = listenerId;
	}

}
