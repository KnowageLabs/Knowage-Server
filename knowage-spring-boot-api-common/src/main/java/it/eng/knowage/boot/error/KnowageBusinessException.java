/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.boot.error;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import it.eng.knowage.boot.utils.EngineMessageBundle;

/**
 * @author Marco Libanori
 */
public class KnowageBusinessException extends KnowageException {

	/*
	 * Locale
	 */
	private Locale locale = Locale.US;

	/*
	 * Status
	 */
	private Status status = Response.Status.INTERNAL_SERVER_ERROR;

	/*
	 * Error Code
	 */
	private String code = "";

	/*
	 * User oriented description of the exception. It is usually prompted to the user. Instead the message passed to the constructor is developer oriented and
	 * it should be just logged.
	 */
	private String description = "";

	/*
	 * A list of possible solutions to the problem that have caused the exception
	 */
	private List hints;

	private static final long serialVersionUID = 2696409463468997530L;

	public KnowageBusinessException(String message, Throwable ex) {
		super(message, ex);
	}

	public KnowageBusinessException(KnowageBusinessException ex, Locale locale) {
		super(ex);
		this.code = ex.getCode();
		this.status = ex.getStatus();
		this.description = ex.getDescription();
		this.hints = ex.getHints();
		this.locale = locale;

	}

	public KnowageBusinessException(String message) {
		super(message);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public List getHints() {
		return hints;
	}

	public void setHints(List hints) {
		this.hints = hints;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public String getLocalizedMessage() {
		String localizedMessage = EngineMessageBundle.getMessage(this.getCode(), this.getLocale());
		return localizedMessage;
	}
}
