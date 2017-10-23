package it.eng.spagobi.tools.dataset.graph.associativity.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.jgrapht.graph.Pseudograph;

import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.associativity.AssociativeDatasetContainer;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.Selection;

public class AssociativeLogicUtils {

	public static String getUnlimitedInClauseValues(Set<String> values) {
		Set<String> newValues = new HashSet<>();
		for (String value : values) {
			newValues.add(value.replaceFirst("\\(", "(1,"));
		}
		return StringUtils.join(newValues.iterator(), ",");
	}

	public static Set<String> getTupleOfValues(ResultSet rs) throws SQLException {
		String tuple;
		String stringDelimiter = "'";
		Set<String> tuples = new HashSet<>();
		while (rs.next()) {
			tuple = "(";
			for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
				if (i != 1) {
					tuple += ",";
				}
				Object item = rs.getObject(i);
				tuple += stringDelimiter + (item == null ? null : getProperValueString(item)) + stringDelimiter;
			}
			tuple += ")";
			tuples.add(tuple);
		}
		return tuples;
	}

	public static Set<String> getTupleOfValues(DataSet ds) {
		String tuple;
		String stringDelimiter = "'";
		Set<String> tuples = new HashSet<>();
		while (ds.next()) {
			tuple = "(";
			for (int i = 0; i < ds.getSelectItems().length; i++) {
				Row row = ds.getRow();
				if (i > 0) {
					tuple += ",";
				}
				Object item = row.getValue(i);
				tuple += stringDelimiter + (item == null ? null : getProperValueString(item)) + stringDelimiter;
			}
			tuple += ")";
			tuples.add(tuple);
		}
		return tuples;
	}

	private static String getProperValueString(Object item) {
		if (Date.class.isAssignableFrom(item.getClass())) {
			SimpleDateFormat formatter = new SimpleDateFormat(JSONDataWriter.CACHE_DATE_TIME_FORMAT);
			return formatter.format(item);
		} else {
			return item.toString();
		}
	}

	public static EdgeGroup getOrCreate(Set<EdgeGroup> groups, EdgeGroup newGroup) {
		if (groups.contains(newGroup)) {
			for (EdgeGroup group : groups) {
				if (group.equals(newGroup)) {
					return group;
				}
			}
		}
		return newGroup;
	}

	public static void unresolveDatasetContainers(Collection<AssociativeDatasetContainer> containers) {
		for (AssociativeDatasetContainer container : containers) {
			container.unresolve();
			container.unresolveGroups();
		}
	}

	public static Config buildConfig(String strategy, Pseudograph<String, LabeledEdge<String>> graph, Map<String, Map<String, String>> datasetToAssociations,
			List<Selection> selections, Set<String> nearRealtimeDatasets, Map<String, Map<String, String>> datasetParameters, Set<String> documents) {
		Config config = new Config();
		config.setStrategy(strategy);
		config.setGraph(graph);
		config.setDatasetToAssociations(datasetToAssociations);
		config.setSelections(selections);
		config.setNearRealtimeDatasets(nearRealtimeDatasets);
		config.setDatasetParameters(datasetParameters);
		config.setDocuments(documents);
		return config;
	}
}
