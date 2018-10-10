/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.knowage.api.dossier.utils;

import java.io.File;

import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class DossierExecutionUtilities {

	public static final String RESOURCE_DOSSIER_EXECUTION_FOLDER = "dossierExecution";

	public static File getDossierExecutionFolder() {

		String resourcePath = SpagoBIUtilities.getResourcePath();

		File file = new File(resourcePath);
		if (!file.exists()) {
			throw new SpagoBIRuntimeException("Could not find resource directory, searching in " + resourcePath, null);
		}

		if (!resourcePath.endsWith(File.separator)) {
			resourcePath += File.separator;
		}

		resourcePath += RESOURCE_DOSSIER_EXECUTION_FOLDER;

		File directory = new File(resourcePath);
		if (!directory.exists()) {
			directory.mkdir();
		}

		return directory;

	}

}
