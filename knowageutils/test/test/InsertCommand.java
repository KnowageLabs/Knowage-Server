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
package test;

import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.StringUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class InsertCommand {

	public static transient Logger logger = Logger.getLogger(InsertCommand.class);

	IMetaData metadata;
	IRecord record;
	String tableName;

	public InsertCommand(IMetaData metadata, String tableName) {
		this.metadata = metadata;
		this.tableName = tableName;
	}

	public void setRecord(IRecord record) {
		this.record = record;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String createSQLQuery(List<String> selectedFields) {

		if (selectedFields == null) {
			selectedFields = new ArrayList<String>();
		}

		StringBuffer buffer = new StringBuffer("INSERT INTO " + this.tableName + " VALUES (");

		int count = this.metadata.getFieldCount();
		for (int i = 0; i < count; i++) {
			IFieldMetaData fieldMetadata = this.metadata.getFieldMeta(i);
			String fieldName = fieldMetadata.getName();
			if (selectedFields.isEmpty() || selectedFields.contains(fieldName)) {

				Class c = fieldMetadata.getType();
				IField field = this.record.getFieldAt(i);
				String value = field.getValue().toString();

				if (String.class.isAssignableFrom(c)) {
					value = StringUtils.escapeQuotes(value);
					buffer.append("'" + value + "'");
				} else {
					buffer.append(value);
				}

				buffer.append(",");

			}

		}

		buffer.delete(buffer.length() - 1, buffer.length()); // remove last ","

		buffer.append(")");
		String query = buffer.toString();
		logger.debug("Query is " + query);
		logger.debug("OUT");
		return query;
	}

	// public static void main (String[] args) {
	// StringBuffer buffer = new StringBuffer("ciao davide");
	// buffer.append(",");
	// buffer.delete(buffer.length() - 1, buffer.length());
	// String query = buffer.toString();
	// logger.debug("[" + query + "]");
	// }

}
