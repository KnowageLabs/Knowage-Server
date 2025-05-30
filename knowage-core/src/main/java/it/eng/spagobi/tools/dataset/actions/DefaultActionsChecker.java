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
package it.eng.spagobi.tools.dataset.actions;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.exceptions.ActionNotPermittedException;

public class DefaultActionsChecker extends AbstractDatasetActionsChecker {

	public DefaultActionsChecker(UserProfile userProfile, IDataSet dataset) {
		super(userProfile, dataset);
	}

	@Override
	public void canLoadData() throws ActionNotPermittedException {
		// no checks to be performed
	}

	@Override
	public void canEdit() throws ActionNotPermittedException {
		// no checks to be performed
	}

	@Override
	public void canSave() throws ActionNotPermittedException {
		// no checks to be performed
	}

	@Override
	public void canShare() throws ActionNotPermittedException {
		// no checks to be performed
	}

}
