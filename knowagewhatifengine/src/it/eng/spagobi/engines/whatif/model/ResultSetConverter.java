package it.eng.spagobi.engines.whatif.model;

import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

public class ResultSetConverter {

	/**
	 * Convert a result set into a JSON Array
	 * 
	 * @param resultSet
	 * @return a JSONArray
	 * @throws Exception
	 */
	public static JSONArray convertResultSetIntoJSON(ResultSet resultSet) throws Exception {
		JSONArray jsonArray = new JSONArray();
		while (resultSet.next()) {
			int total_rows = resultSet.getMetaData().getColumnCount();
			JSONObject obj = new JSONObject();
			for (int i = 0; i < total_rows; i++) {
				String columnName = resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase();
				Object columnValue = resultSet.getObject(i + 1);
				// if value in DB is null, then we set it to default value
				if (columnValue == null) {
					columnValue = "null";
				}
				/*
				 * Next if block is a hack. In case when in db we have values
				 * like price and price1 there's a bug in jdbc - both this names
				 * are getting stored as price in ResulSet. Therefore when we
				 * store second column value, we overwrite original value of
				 * price. To avoid that, i simply add 1 to be consistent with
				 * DB.
				 */
				if (obj.has(columnName)) {
					columnName += "1";
				}
				obj.put(columnName, columnValue);
			}
			jsonArray.put(obj);
		}
		return jsonArray;
	}

	public static int converBooleanIntoInt(boolean bool) {
		if (bool)
			return 1;
		else
			return 0;
	}

	public static int convertBooleanStringIntoInt(String bool) {
		if (bool.equals("false"))
			return 0;
		else if (bool.equals("true"))
			return 1;
		else {
			throw new IllegalArgumentException("wrong value is passed to the method. Value is " + bool);
		}
	}

	public static double getDoubleOutOfString(String value, String format, Locale locale) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(locale);
		otherSymbols.setDecimalSeparator('.');
		DecimalFormat f = new DecimalFormat(format, otherSymbols);
		String formattedValue = f.format(Double.parseDouble(value));
		double number = Double.parseDouble(formattedValue);
		return Math.round(number * 100.0) / 100.0;
	}

}
