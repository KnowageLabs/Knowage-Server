/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.spagobi.workspace.dao;

import java.util.List;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.workspace.bo.FunctionsOrganizer;
import it.eng.spagobi.workspace.metadata.SbiFunctionsOrganizer;

/**
 * @deprecated Replaced by KNOWAGE_TM-513
 * TODO : Delete
 */
@Deprecated
public interface IFunctionsOrganizerDAO extends ISpagoBIDao {

	public List<FunctionsOrganizer> loadFolderByUser();

	public SbiFunctionsOrganizer createFolder(SbiFunctionsOrganizer folder);

	public void deleteFolder(Integer folderId);

}
