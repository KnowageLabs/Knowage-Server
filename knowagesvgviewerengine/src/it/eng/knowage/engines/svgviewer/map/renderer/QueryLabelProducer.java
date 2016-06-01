package it.eng.knowage.engines.svgviewer.map.renderer;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineConstants;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class QueryLabelProducer.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QueryLabelProducer extends AbstractLabelProducer {

	/** The data source. */
	private IDataSource dataSource;

	/** The query. */
	private String query;

	/** The text. */
	private String text;

	/** The param names. */
	private Set paramNames;

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.engines.geo.map.renderer.LabelProducer#init(it.eng.spago.base.SourceBean)
	 */
	@Override
	public void init(SourceBean conf) {
		super.init(conf);
		SourceBean dataSourceSB = (SourceBean) conf.getAttribute("DATASOURCE");
		dataSource = new DataSource();

		String type = (String) dataSourceSB.getAttribute(SvgViewerEngineConstants.DATASET_TYPE_ATTRIBUTE);
		if ("connection".equalsIgnoreCase(type)) {
			dataSource.setJndi((String) dataSourceSB.getAttribute(SvgViewerEngineConstants.DATASET_NAME_ATTRIBUTE));
			dataSource.setDriver((String) dataSourceSB.getAttribute(SvgViewerEngineConstants.DATASET_DRIVER_ATTRIBUTER));
			dataSource.setPwd((String) dataSourceSB.getAttribute(SvgViewerEngineConstants.DATASET_PWD_ATTRIBUTE));
			dataSource.setUser((String) dataSourceSB.getAttribute(SvgViewerEngineConstants.DATASET_USER_ATTRIBUTE));
			dataSource.setUrlConnection((String) dataSourceSB.getAttribute(SvgViewerEngineConstants.DATASET_URL_ATTRIBUTE));
		}

		SourceBean querySB = (SourceBean) conf.getAttribute("QUERY");
		query = querySB.getCharacters();
		SourceBean textSB = (SourceBean) conf.getAttribute("TEXT");
		text = textSB.getCharacters();

		paramNames = new HashSet();
		int fromIndex = 0;
		int beginIndex = -1;
		while ((beginIndex = text.indexOf("${", fromIndex)) != -1) {
			int endIndex = text.indexOf("}", beginIndex);
			String param = text.substring(beginIndex + 2, endIndex);
			paramNames.add(param);
			fromIndex = endIndex;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.engines.geo.map.renderer.LabelProducer#getLabel()
	 */
	@Override
	public String getLabel() {
		String label = text;
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Statement statement = connection.createStatement();
			statement.execute(query);
			ResultSet resultSet = statement.getResultSet();
			if (resultSet.next()) {
				Iterator it = paramNames.iterator();
				while (it.hasNext()) {
					String pName = (String) it.next();
					int col_index = resultSet.findColumn(pName);
					String pValue = resultSet.getString(col_index);
					if (pValue == null)
						pValue = "";
					label = label.replaceAll("\\$\\{" + pName + "\\}", pValue);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return label;
	}
}
