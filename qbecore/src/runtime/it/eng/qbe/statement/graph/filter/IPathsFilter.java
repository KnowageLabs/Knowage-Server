package it.eng.qbe.statement.graph.filter;

import it.eng.qbe.statement.graph.ModelFieldPaths;

import java.util.Map;
import java.util.Set;

public interface IPathsFilter {
	public void filterPaths(Set<ModelFieldPaths> paths, Map<String, Object> properties);
}
