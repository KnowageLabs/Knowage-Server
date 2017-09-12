package it.eng.spagobi.kpi.metadata.generator;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.ObjectNameNormalizer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.engine.transaction.IsolatedWork;
import org.hibernate.engine.transaction.Isolater;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;
import org.hibernate.util.PropertiesHelper;
import org.hibernate.util.StringHelper;

public abstract class IdOrVersionGenerator extends TableGenerator {

	private static final String OBJECT_ID_COLUMN = "object_id_column";
	private static final String OBJECT_VERSION_COLUMN = "object_version_column";
	private static final String OBJECT_TABLE_NAME = "object_table_name";

	private String idColumn;
	private String versionColumn;
	private String tableName;

	abstract boolean mustIncrementVersion(Object obj);

	abstract Serializable createEntityId(Integer id, Integer version);

	abstract Integer getId(Object obj);

	@Override
	public synchronized Serializable generate(SessionImplementor session, Object obj) {
		if (mustIncrementVersion(obj)) {
			// Generate Version
			Work work = new Work(getId(obj), session, versionColumn, idColumn, tableName);
			Isolater.doIsolatedWork(work, session);
			return createEntityId(getId(obj), work.version);
		} else {
			// Generate Id
			Integer newid = (Integer) super.generate(session, obj);
			return createEntityId(newid, 0);
		}
	}

	@Override
	public void configure(Type type, Properties params, Dialect dialect) throws MappingException {
		this.idColumn = getParam(dialect, OBJECT_ID_COLUMN, params);
		this.versionColumn = getParam(dialect, OBJECT_VERSION_COLUMN, params);
		this.tableName = getParam(dialect, OBJECT_TABLE_NAME, params);
		super.configure(IntegerType.INSTANCE, params, dialect);
	}

	private String getParam(Dialect dialect, String paramName, Properties params) {
		ObjectNameNormalizer normalizer = (ObjectNameNormalizer) params.get(IDENTIFIER_NORMALIZER);
		String name = PropertiesHelper.getString(paramName, params, null);
		if (name == null) {
			throw new MappingException("Param [" + paramName + "] not found in hbm");
		}
		return dialect.quote(normalizer.normalizeIdentifierQuoting(name));
	}
}

class Work implements IsolatedWork {
	int version = 0;
	Integer id;
	String sql;

	public Work(Integer id, final SessionImplementor session, String versionName, String idName, String tableName) {
		this.id = id;
		this.sql = buildSelectVersionQuery(session, versionName, idName, tableName);
	}

	@Override
	public void doWork(Connection conn) throws HibernateException {
		try {
			PreparedStatement ps = conn.prepareStatement(this.sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				version = rs.getInt(1) + 1;
			}
		} catch (SQLException sqle) {
			throw new HibernateException(sqle);
		}
	}

	private String buildSelectVersionQuery(SessionImplementor session, String versionName, String idName, String tableName) {
		Dialect dialect = session.getFactory().getDialect();
		final String alias = "tbl";
		List<String> maxArgs = new ArrayList<>();
		maxArgs.add(StringHelper.qualify(alias, versionName));
		String maxFunction = dialect.getFunctions().get("max").render(IntegerType.INSTANCE, maxArgs, session.getFactory());
		String sql = "select " + maxFunction + " from " + tableName + ' ' + alias + " where " + StringHelper.qualify(alias, idName) + " = ?";
		return sql;
	}
}