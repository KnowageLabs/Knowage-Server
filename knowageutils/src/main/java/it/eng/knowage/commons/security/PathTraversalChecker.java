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
package it.eng.knowage.commons.security;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.knowage.commons.security.exceptions.PathTraversalAttackException;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class PathTraversalChecker {

	private static Logger logger = Logger.getLogger(PathTraversalChecker.class);

	private PathTraversalChecker() {
		throw new IllegalStateException("This class provides utility methods. It cannot be instantiated");
	}

	/**
	 * Utility method for Path Traversal Attacks prevention. It checks that input fine is inside the desired directory or within sub-directory of the desired
	 * directory. In case this is not satisfied, a PathTraversalAttackException is thrown. It is useful when desiredDirectory is known and safe, while file to
	 * be checked is created combining some user inputs.
	 *
	 * @param fileToBeChecked  the file to be checked
	 * @param desiredDirectory the desired directory that is supposed to contain (at any sub-level) the file
	 */
	public static void preventPathTraversalAttack(File fileToBeChecked, File desiredDirectory) {
		LogMF.debug(logger, "IN : fileToBeChecked = [{0}], desiredDirectory = [{1}]", fileToBeChecked, desiredDirectory);
		try {
			Assert.assertNotNull(fileToBeChecked, "File to be checked cannot be null");
			Assert.assertNotNull(desiredDirectory, "Desired directory cannot be null");
			Assert.assertTrue(desiredDirectory.exists() && desiredDirectory.isDirectory(), "Desired directory must be an existing folder");

			boolean isInDesiredDirectory = isInDesiredDirectory(fileToBeChecked, desiredDirectory);

			if (!isInDesiredDirectory) {
				UserProfile profile = UserProfileManager.getProfile();
				LogMF.error(logger, "User [{0}] is trying to access the file [{1}] that is not inside [{2}]!!!",
						new Object[] { profile, fileToBeChecked.getAbsolutePath(), desiredDirectory.getAbsolutePath() });
				throw new PathTraversalAttackException("User [" + profile + "] is trying to access the file [" + fileToBeChecked.getAbsolutePath()
						+ "] that is not inside [" + desiredDirectory.getAbsolutePath() + "]!!!");
			}
		} finally {
			logger.debug("OUT");
		}
	}

	private static boolean isInDesiredDirectory(File fileToBeChecked, File desiredDirectory) {
		try {
			fileToBeChecked = fileToBeChecked.getCanonicalFile();
			desiredDirectory = desiredDirectory.getCanonicalFile();
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Error while converting input files into canonical ones", e);
		}

		File parent = fileToBeChecked.getParentFile();
		boolean toReturn = false;
		while (parent != null) {
			if (desiredDirectory.equals(parent)) {
				LogMF.debug(logger, "Desired directory [{0} matches parent folder of input file]", desiredDirectory);
				toReturn = true;
				break;
			}
			parent = parent.getParentFile();
		}
		return toReturn;
	}

}
