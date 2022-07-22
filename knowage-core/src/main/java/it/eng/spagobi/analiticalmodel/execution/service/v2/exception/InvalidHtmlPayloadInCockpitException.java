/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.analiticalmodel.execution.service.v2.exception;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import java.util.Locale;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.messages.EngineMessageBundle;

public class InvalidHtmlPayloadInCockpitException extends SpagoBIRuntimeException {

	private static final long serialVersionUID = 7238971352468593356L;

	private final String serviceName = "";
	private final String localizationCode = "10001";
	private final String messageBundle = "MessageFiles.messages";
	private final String payload;
	private final String sheetName;

	public String getServiceName() {
		return serviceName;
	}

	private Locale locale = Locale.US;

	public InvalidHtmlPayloadInCockpitException(String sheetName, String payload) {
		super((Throwable) null);
		this.sheetName = sheetName;
		this.payload = payload;
	}

	public String getLocalizationCode() {
		return localizationCode;
	}

	@Override
	public String getLocalizedMessage() {
		String localizedMessage = EngineMessageBundle.getMessage(getLocalizationCode(), getMessageBundle(), getLocale(), new String[] { sheetName, escapeHtml4(payload) });
		return localizedMessage;
	}

	public Locale getLocale() {
		if (locale == null) {
			locale = Locale.US;
		}
		return locale;
	}

	public String getMessageBundle() {
		return messageBundle;
	}

}
