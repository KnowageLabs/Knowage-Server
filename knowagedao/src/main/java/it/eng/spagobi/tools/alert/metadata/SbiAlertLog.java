package it.eng.spagobi.tools.alert.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiAlertLog extends SbiHibernateModel {

	private Integer id;
	private Integer listenerId;
	private Integer actionId;
	private String listenerParams;
	private String actionParams;
	private String detail;
	private SbiAlertAction sbiAlertAction;
	private SbiAlertListener sbiAlertListener;

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

	/**
	 * @return the actionId
	 */
	public Integer getActionId() {
		return actionId;
	}

	/**
	 * @param actionId
	 *            the actionId to set
	 */
	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}

	/**
	 * @return the listenerParams
	 */
	public String getListenerParams() {
		return listenerParams;
	}

	/**
	 * @param listenerParams
	 *            the listenerParams to set
	 */
	public void setListenerParams(String listenerParams) {
		this.listenerParams = listenerParams;
	}

	/**
	 * @return the actionParams
	 */
	public String getActionParams() {
		return actionParams;
	}

	/**
	 * @param actionParams
	 *            the actionParams to set
	 */
	public void setActionParams(String actionParams) {
		this.actionParams = actionParams;
	}

	/**
	 * @return the detail
	 */
	public String getDetail() {
		return detail;
	}

	/**
	 * @param detail
	 *            the detail to set
	 */
	public void setDetail(String detail) {
		this.detail = detail;
	}

	/**
	 * @return the sbiAlertAction
	 */
	public SbiAlertAction getSbiAlertAction() {
		return sbiAlertAction;
	}

	/**
	 * @param sbiAlertAction
	 *            the sbiAlertAction to set
	 */
	public void setSbiAlertAction(SbiAlertAction sbiAlertAction) {
		this.sbiAlertAction = sbiAlertAction;
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

}
