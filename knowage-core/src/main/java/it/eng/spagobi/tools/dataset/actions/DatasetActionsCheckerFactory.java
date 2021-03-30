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

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.utilities.assertion.Assert;

public class DatasetActionsCheckerFactory {

	public static IDatasetActionsChecker getDatasetActionsChecker(UserProfile userProfile, IDataSet dataSet) {
		Assert.assertNotNull(userProfile, "UserProfile in input is null!!");
		Assert.assertNotNull(dataSet, "Dataset in input is null!!");

		if (dataSet instanceof VersionedDataSet) {
			return getDatasetActionsChecker(userProfile, ((VersionedDataSet) dataSet).getWrappedDataset());
		}

		// we check class, because subclasses of QbeDataSet may have a different logic (example FederatedDataSet)
		if (dataSet.getClass().equals(QbeDataSet.class)) {
			return new QbeDataSetActionsChecker(userProfile, dataSet);
		}

		return new EmptyActionsChecker(userProfile, dataSet);

	}

}
