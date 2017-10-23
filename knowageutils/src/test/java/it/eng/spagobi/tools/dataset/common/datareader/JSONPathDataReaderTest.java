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
package it.eng.spagobi.tools.dataset.common.datareader;

import static java.util.Calendar.AUGUST;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.JULY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.IRecordMatcher;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.HelperForTest;
import junit.framework.TestCase;

public class JSONPathDataReaderTest extends TestCase {

	private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	public void testReadDirectly() throws IOException, ParseException {
		String json = HelperForTest.readFile("dataReader-test-directly.json", this.getClass());
		List<JSONPathAttribute> jsonPathAttributes = new ArrayList<JSONPathDataReader.JSONPathAttribute>();
		JSONPathDataReader reader = new JSONPathDataReader("$.contextResponses[*]", jsonPathAttributes, true, false);
		IDataStore read = reader.read(json);
		assertEquals(2, read.getRecordsCount());

		IMetaData md = read.getMetaData();

		IRecord rec0 = read.getRecordAt(0);
		testRecord0(md, rec0);
		assertEquals(5, rec0.getFields().size());

		IRecord rec1 = read.getRecordAt(1);
		testRecord1(md, rec1);
		assertEquals(5, rec1.getFields().size());
	}

	public void testNGSI() throws IOException, ParseException {
		String json = HelperForTest.readFile("dataReader-test.json", this.getClass());
		JSONPathDataReader reader = getJSONPathDataReaderNGSI();
		IDataStore read = reader.read(json);
		assertEquals(6, read.getRecordsCount());
		List<IRecord> records = read.findRecords(getRecordMatcher());
		assertEquals(1, records.size());
		IRecord record = records.get(0);
		boolean[] done = new boolean[4];
		IMetaData metaData = read.getMetaData();
		List<IField> fields = record.getFields();
		for (int i = 0; i < metaData.getFieldCount(); i++) {
			IFieldMetaData fm = metaData.getFieldMeta(i);
			if ("downstreamActivePower".equals(fm.getName())) {
				assertEquals(4.8, (Double) fields.get(i).getValue(), 10E-6);
				done[0] = true;
				continue;
			}
			if ("atTime".equals(fm.getName())) {
				assertEquals(fields.get(i).getValue(), new SimpleDateFormat(TIME_FORMAT).parse("2015-07-14T17:19:14.014+0200"));
				done[1] = true;
				continue;
			}
			if ("upstreamActivePower".equals(fm.getName())) {
				assertEquals(0, (Double) fields.get(i).getValue(), 10E-6);
				done[2] = true;
				continue;
			}
			if ("id".equals(fm.getName())) {
				assertEquals(fields.get(i).getValue(), "pros6_Meter");
				done[3] = true;
				continue;
			}
		}
		for (int i = 0; i < done.length; i++) {
			assertTrue(Integer.toString(i), done[i]);
		}
	}

	private IRecordMatcher getRecordMatcher() {
		return new IRecordMatcher() {

			@Override
			public boolean match(IRecord record) {
				for (IField field : record.getFields()) {
					if ("pros6_Meter".equals(field.getValue())) {
						return true;
					}
				}
				return false;
			}
		};
	}

	public void testReadDirectlyndJSONAttributes() throws IOException, ParseException {
		String json = HelperForTest.readFile("dataReader-test-directly-with-jsonattributes.json", this.getClass());
		List<JSONPathAttribute> jsonPathAttributes = getJsonPathAttributesDirectly();
		JSONPathDataReader reader = new JSONPathDataReader("$.contextResponses[*].contextElement", jsonPathAttributes, true, false);
		IDataStore read = reader.read(json);
		assertEquals(2, read.getRecordsCount());

		IMetaData md = read.getMetaData();

		IRecord rec0 = read.getRecordAt(0);
		testRecord0(md, rec0);

		assertField("atTime", new SimpleDateFormat(TIME_FORMAT).parse("2015-07-14T17:19:14.014+0200"), rec0, md);
		assertField("downstreamActivePower", Double.parseDouble("4.8"), rec0, md);

		IRecord rec1 = read.getRecordAt(1);
		testRecord1(md, rec1);

		assertField("atTime", new SimpleDateFormat(TIME_FORMAT).parse("2015-07-14T17:16:14.014+0200"), rec1, md);
		assertField("downstreamActivePower", Double.parseDouble("3.5"), rec1, md);
	}

	private void testRecord1(IMetaData md, IRecord rec1) {
		assertField("a", 2.1, rec1, md);
		assertField("c", null, rec1, md);
		assertField("e", "r", rec1, md);
		assertField("f", "s", rec1, md);
		assertField("b", "q", rec1, md);
	}

	private void testRecord0(IMetaData md, IRecord rec0) {
		assertField("a", 3.0, rec0, md);
		assertField("c", "d", rec0, md);
		assertField("e", "f", rec0, md);
		assertField("f", null, rec0, md);
		assertField("b", null, rec0, md);
	}

	private void assertField(String key, Object value, IRecord rec, IMetaData md) {
		for (int i = 0; i < md.getFieldCount(); i++) {
			IFieldMetaData fm = md.getFieldMeta(i);
			if (fm.getName().equals(key)) {
				if (Double.class.equals(fm.getType())) {
					assertEquals((Double) value, (Double) rec.getFieldAt(i).getValue(), 10E-6);
				} else {
					assertEquals(value, rec.getFieldAt(i).getValue());
				}
				return;
			}
		}
		fail(key + " " + value + " " + toString(md));

	}

	private String toString(IMetaData md) {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < md.getFieldCount(); i++) {
			res.append(md.getFieldMeta(i).getName());
			res.append('\n');
		}
		return res.toString();
	}

	public void testReadDates() throws IOException, ParseException {
		String json = HelperForTest.readFile("dataReader-test-dates.json", this.getClass());
		List<JSONPathAttribute> jsonPathAttributes = getJsonPathAttributesDates();
		JSONPathDataReader reader = new JSONPathDataReader("$.contextResponses[*].contextElement", jsonPathAttributes, false, false);
		IDataStore read = reader.read(json);
		assertEquals(1, read.getRecordsCount());
		assertEqualsDates(read, "atTime", new int[] { HOUR_OF_DAY, MINUTE, SECOND }, new int[] { 18, 19, 14 });
		assertEqualsDates(read, "atDate", new int[] { YEAR, MONTH, DAY_OF_MONTH }, new int[] { 2015, AUGUST, 14 });
		assertEqualsDates(read, "atTimestamp", new int[] { YEAR, MONTH, DAY_OF_MONTH, HOUR_OF_DAY, MINUTE, SECOND }, new int[] { 2015, JULY, 21, 14, 49, 46 });
		assertEqualsDates(read, "atDateTime", new int[] { YEAR, MONTH, DAY_OF_MONTH, HOUR_OF_DAY, MINUTE, SECOND }, new int[] { 2015, JULY, 22, 14, 49, 46 });
		assertEqualsDates(read, "atDateCustom", new int[] { YEAR, MONTH, DAY_OF_MONTH }, new int[] { 1986, JANUARY, 20 });

	}

	private static void assertEqualsDates(IDataStore read, String field, int[] calendarFields, int[] values) {
		IMetaData md = read.getMetaData();
		for (int i = 0; i < md.getFieldCount(); i++) {
			IFieldMetaData fm = md.getFieldMeta(i);
			String name = fm.getName();
			if (field.equals(name)) {
				IRecord rec = read.getRecordAt(0); // only 1 record
				Date d = (Date) rec.getFieldAt(i).getValue();
				Calendar c = Calendar.getInstance();
				c.setTime(d);
				for (int j = 0; j < calendarFields.length; j++) {
					assertEquals(values[j], c.get(calendarFields[j]));
				}
				return;
			}
		}
		fail();
	}

	public void testRead() throws IOException, ParseException {
		String json = HelperForTest.readFile("dataReader-test.json", this.getClass());
		JSONPathDataReader reader = getJSONPathDataReaderOrion();
		IDataStore read = reader.read(json);
		assertEquals(6, read.getRecordsCount());
		List<IRecord> records = read.findRecords(new IRecordMatcher() {

			@Override
			public boolean match(IRecord record) {
				for (IField field : record.getFields()) {
					if ("pros6_Meter".equals(field.getValue())) {
						return true;
					}
				}
				return false;
			}
		});
		assertEquals(1, records.size());
		IRecord record = records.get(0);
		boolean[] done = new boolean[5];
		IMetaData metaData = read.getMetaData();
		List<IField> fields = record.getFields();
		for (int i = 0; i < metaData.getFieldCount(); i++) {
			IFieldMetaData fm = metaData.getFieldMeta(i);
			if ("downstreamActivePower".equals(fm.getName())) {
				assertEquals(4.8, (Double) fields.get(i).getValue(), 10E-6);
				done[0] = true;
				continue;
			}
			if ("atTime".equals(fm.getName())) {
				assertEquals(fields.get(i).getValue(), new SimpleDateFormat(TIME_FORMAT).parse("2015-07-14T17:19:14.014+0200"));
				done[1] = true;
				continue;
			}
			if ("upstreamActivePower".equals(fm.getName())) {
				assertEquals(0, (Double) fields.get(i).getValue(), 10E-6);
				done[2] = true;
				continue;
			}
			if ("id".equals(fm.getName())) {
				assertEquals(fields.get(i).getValue(), "pros6_Meter");
				done[3] = true;
				continue;
			}
			if ("isPattern".equals(fm.getName())) {
				assertEquals(fields.get(i).getValue(), Boolean.FALSE);
				done[4] = true;
				continue;
			}
		}
		for (int i = 0; i < done.length; i++) {
			assertTrue(Integer.toString(i), done[i]);
		}
	}

	public static JSONPathDataReader getJSONPathDataReaderOrion() {
		JSONPathDataReader reader = new JSONPathDataReader("$.contextResponses[*].contextElement", getJsonPathAttributes(), false, false);
		return reader;
	}

	public static JSONPathDataReader getJSONPathDataReaderNGSI() {
		JSONPathDataReader reader = new JSONPathDataReader(null, new ArrayList<JSONPathDataReader.JSONPathAttribute>(), false, true);
		return reader;
	}

	private static List<JSONPathAttribute> getJsonPathAttributes() {
		List<JSONPathAttribute> res = new ArrayList<JSONPathDataReader.JSONPathAttribute>();
		JSONPathAttribute jpa = new JSONPathAttribute("downstreamActivePower", "$.attributes[?(@.name==downstreamActivePower)].value",
				"$.attributes[?(@.name==downstreamActivePower)].type");
		res.add(jpa);

		JSONPathAttribute jpa2 = new JSONPathAttribute("atTime", "$.attributes[?(@.name==atTime)].value", "timestamp " + TIME_FORMAT);
		res.add(jpa2);

		JSONPathAttribute jpa3 = new JSONPathAttribute("upstreamActivePower", "$.attributes[?(@.name==upstreamActivePower)].value", "double");
		res.add(jpa3);

		JSONPathAttribute jpa4 = new JSONPathAttribute("id", "$.id", "string");
		res.add(jpa4);

		JSONPathAttribute jpa5 = new JSONPathAttribute("isPattern", "$.isPattern", "boolean");
		res.add(jpa5);
		return res;
	}

	private static List<JSONPathAttribute> getJsonPathAttributesDates() {
		List<JSONPathAttribute> res = new ArrayList<JSONPathDataReader.JSONPathAttribute>();
		res.add(getJSONPathType("atTime"));
		res.add(getJSONPathType("atDate"));
		res.add(getJSONPathType("atTimestamp"));
		res.add(getJSONPathType("atDateTime"));
		res.add(getJSONPathType("atDateCustom"));
		return res;
	}

	private static JSONPathAttribute getJSONPathType(String name) {
		return new JSONPathAttribute(name, "$.attributes[?(@.name==" + name + ")].value", "$.attributes[?(@.name==" + name + ")].type");
	}

	private static List<JSONPathAttribute> getJsonPathAttributesDirectly() {
		List<JSONPathAttribute> res = new ArrayList<JSONPathDataReader.JSONPathAttribute>();
		JSONPathAttribute jpa = new JSONPathAttribute("downstreamActivePower", "$.attributes[?(@.name==downstreamActivePower)].value",
				"$.attributes[?(@.name==downstreamActivePower)].type");
		res.add(jpa);

		JSONPathAttribute jpa2 = new JSONPathAttribute("atTime", "$.attributes[?(@.name==atTime)].value", "timestamp " + TIME_FORMAT);
		res.add(jpa2);

		return res;
	}

	public void testReadFail() throws IOException, ParseException {
		String json = getJSONData();
		List<JSONPathAttribute> jsonPathAttributes = getJsonPathAttributes();
		JSONPathDataReader reader = new JSONPathDataReader("$.contextRespo", jsonPathAttributes, false, false);
		boolean done = false;
		try {
			reader.read(json);
		} catch (JSONPathDataReaderException e) {
			done = true;
		}
		assertTrue(done);
	}

	public static String getJSONData() throws IOException {
		return HelperForTest.readFile("dataReader-test.json", JSONPathDataReaderTest.class);
	}

}
