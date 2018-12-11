package it.eng.spagobi.utilities.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

// https://stackoverflow.com/questions/47037849/hive-connection-using-hibernate-in-spring-boot
// The closest dialect seems to be Hive's syntax is mysql's dialect
public class Hive2DataBase extends HiveDataBase implements MetaDataBase {

	public Hive2DataBase(IDataSource dataSource) {
		super(dataSource);
	}

	@Override
	public String getAliasDelimiter() {
		return "`";
	}

	@Override
	public String getSchema(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT current_database()");
			try {
				rs.next();
				return rs.getString(1);
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
	}

	@Override
	public String getCatalog(Connection conn) throws SQLException {
		return conn.getCatalog();
	}

}
