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
package it.eng.knowage.boot.validation;

import java.util.Arrays;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FilesValidator implements ConstraintValidator<FilesCheck, List<String>> {

	public static final Character[] INVALID_WINDOWS_SPECIFIC_CHARS = { '"', '*', ':', '<', '>', '?', '\\', '|', 0x7F };
	public static final Character[] INVALID_UNIX_SPECIFIC_CHARS = { '\000' };

	@Override
	public void initialize(FilesCheck arg0) {
	}

	@Override
	public boolean isValid(List<String> toValidate, ConstraintValidatorContext constraintContext) {

		for (String value : toValidate) {
			if (!validateStringFilenameUsingContains(value)) {
				return false;
			}

		}

		return true;
	}

	public static boolean validateStringFilenameUsingContains(String filename) {
		if (filename == null || filename.isEmpty() || filename.length() > 255) {
			return false;
		}
		return Arrays.stream(getInvalidCharsByOS()).noneMatch(ch -> filename.contains(ch.toString()));
	}

	public static Character[] getInvalidCharsByOS() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			return INVALID_WINDOWS_SPECIFIC_CHARS;
		} else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
			return INVALID_UNIX_SPECIFIC_CHARS;
		} else {
			return new Character[] {};
		}
	}

}
