/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.exporter;

import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class QbeCSVExporter {
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(QbeCSVExporter.class);
    
    /**
     * The values separator string
     */
    public static final String VALUES_SEPARATOR = QbeEngineConfig.getInstance().getExportCsvSeparator() ;
    
	public QbeCSVExporter() {
		super();
	}

	public void export(File outputFile, Connection connection, String sqlStatement) {
		try {
			FileWriter writer = new FileWriter(outputFile);
			Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultSet = stmt.executeQuery( sqlStatement );
			int columnCount = resultSet.getMetaData().getColumnCount();
			while (resultSet.next()) {
				writeRecordInfoCsvFile(writer, resultSet, columnCount);
			}
			writer.flush();
			writer.close();
		} catch (Exception e) {
			logger.error("Error exporting in CSV", e);
			throw new SpagoBIEngineRuntimeException("Error exporting in CSV", e);
		}
	}
	
	private void writeRecordInfoCsvFile(FileWriter writer, ResultSet resultSet,
			int columnCount) throws SQLException, IOException {
		for (int i = 1; i <= columnCount; i++) {
			Object temp = resultSet.getObject(i);
			if (temp != null) {
				writer.append(temp.toString());
			}
			writer.append( VALUES_SEPARATOR );
		}
		writer.append("\n");
	}
	
}
