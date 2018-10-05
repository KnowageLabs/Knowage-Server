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

package it.eng.spagobi.i18n.dao;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.i18n.metadata.SbiI18NMessageBody;
import it.eng.spagobi.i18n.metadata.SbiI18NMessages;

public interface I18NMessagesDAO extends ISpagoBIDao {

	public String getI18NMessages(Locale locale, String code) throws EMFUserError;

	public Map<String, String> getAllI18NMessages(Locale locale) throws EMFUserError;

	public List<SbiI18NMessages> getI18NMessages(String languageName);

	public SbiI18NMessages getSbiI18NMessageById(Integer id);

	public void insertI18NMessage(SbiI18NMessageBody message);

	public void updateI18NMessage(SbiI18NMessages message);

	public void updateNonDefaultI18NMessagesLabel(SbiI18NMessages oldMessage, SbiI18NMessages newMessage);

	public void deleteI18NMessage(Integer id);

	public void deleteNonDefaultI18NMessages(SbiI18NMessages message);

}
