/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.kpi;

import it.eng.spago.security.IEngUserProfile;

import java.util.Locale;

public class KpiEnginData {
	public KpiEnginData() {
		super();
		// TODO Auto-generated constructor stub
	}

	private Locale locale;
	private IEngUserProfile profile;
	private String internationalizedFormat;
	private String formatServer;
	private String lang;
	private String country;
	private boolean executionModalityScheduler;

	public KpiEnginData(Locale locale,
			IEngUserProfile profile, String internationalizedFormat,
			String formatServer, String lang, String country,
			boolean executionModalityScheduler) {
		this.locale = locale;
		this.profile = profile;
		this.internationalizedFormat = internationalizedFormat;
		this.formatServer = formatServer;
		this.lang = lang;
		this.country = country;
		this.executionModalityScheduler = executionModalityScheduler;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public IEngUserProfile getProfile() {
		return profile;
	}

	public void setProfile(IEngUserProfile profile) {
		this.profile = profile;
	}

	public String getInternationalizedFormat() {
		return internationalizedFormat;
	}

	public void setInternationalizedFormat(String internationalizedFormat) {
		this.internationalizedFormat = internationalizedFormat;
	}

	public String getFormatServer() {
		return formatServer;
	}

	public void setFormatServer(String formatServer) {
		this.formatServer = formatServer;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public boolean isExecutionModalityScheduler() {
		return executionModalityScheduler;
	}

	public void setExecutionModalityScheduler(
			boolean executionModalityScheduler) {
		this.executionModalityScheduler = executionModalityScheduler;
	}
}
