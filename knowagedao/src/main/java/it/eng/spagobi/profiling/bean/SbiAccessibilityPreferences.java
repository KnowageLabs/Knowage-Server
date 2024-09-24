package it.eng.spagobi.profiling.bean;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiAccessibilityPreferences extends SbiHibernateModel {
	private int id;
	private SbiUser user;
	private boolean enableUio;
	private boolean enableRobobraille;
	private boolean enableGraphSonification;
	private boolean enableVoice;
	private String preferences;

	public SbiAccessibilityPreferences() {

	}

	public SbiAccessibilityPreferences(int id, SbiUser user, boolean enableUio, boolean enableRobobraille, boolean enableGraphSonification,
			boolean enableVoice, String preferences) {
		super();
		this.id = id;
		this.user = user;
		this.enableUio = enableUio;
		this.enableRobobraille = enableRobobraille;
		this.enableGraphSonification = enableGraphSonification;
		this.enableVoice = enableVoice;
		this.preferences = preferences;
	}

	public int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}

	public SbiUser getUser() {
		return user;
	}

	public void setUser(SbiUser user) {
		this.user = user;
	}

	public boolean isEnableUio() {
		return enableUio;
	}

	public void setEnableUio(boolean enableUio) {
		this.enableUio = enableUio;
	}

	public boolean isEnableRobobraille() {
		return enableRobobraille;
	}

	public void setEnableRobobraille(boolean enableRobobraille) {
		this.enableRobobraille = enableRobobraille;
	}

	public boolean isEnableGraphSonification() {
		return enableGraphSonification;
	}

	public void setEnableGraphSonification(boolean enableGraphSonification) {
		this.enableGraphSonification = enableGraphSonification;
	}

	public boolean isEnableVoice() {
		return enableVoice;
	}

	public void setEnableVoice(boolean enableVoice) {
		this.enableVoice = enableVoice;
	}

	public String getPreferences() {
		return preferences;
	}

	public void setPreferences(String preferences) {
		this.preferences = preferences;
	}

}
