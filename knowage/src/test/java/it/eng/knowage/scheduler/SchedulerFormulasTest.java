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
package it.eng.knowage.scheduler;

import static org.junit.Assert.assertEquals;

import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.junit.Test;

/**
 * Comparation between old Scheduler Formulas and the new ones with java.time.
 *
 * @author Marco Libanori
 */
public class SchedulerFormulasTest {

	@Test
	public void getCurrentDayOfTheMonth() {
		// Old code
		Date now = new Date();
		Integer aDay = now.getDate();
		String a = aDay.toString();

		// New code
		Integer bDay = java.time.ZonedDateTime.now().getDayOfMonth();
		String b = bDay.toString();

		assertEquals(a, b);
	}

	@Test
	public void getCurrentMonthOfTheYear() {
		// Old code
		Date now = new Date();
		Integer aMonth = now.getMonth() + 1;
		String a = aMonth.toString();

		// New code
		Integer bMonth = java.time.ZonedDateTime.now().getMonth().getValue();
		String b = bMonth.toString();

		assertEquals(a, b);
	}

	@Test
	public void getCurrentYear() {
		// Old code
		Date now = new Date();
		Integer aYear = now.getYear() + 1900;
		String a = aYear.toString();

		// New code
		Integer bYear = java.time.ZonedDateTime.now().getYear();
		String b = bYear.toString();

		assertEquals(a, b);
	}

	@Test
	public void getCurrentDate() {
		final String DATE_FORMAT = "dd/MM/yyyy";

		// Old code
		Date now = new Date();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
		String a = sdf.format(now);

		// New code
		String b = java.time.ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));

		assertEquals(a, b);
	}

	@Test
	public void getCurrentDateTime() {
		final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

		// Old code
		Date now = new Date();
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
		String a = sdf.format(now);

		// New code
		String b = java.time.ZonedDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));

		assertEquals(a, b);
	}

}
