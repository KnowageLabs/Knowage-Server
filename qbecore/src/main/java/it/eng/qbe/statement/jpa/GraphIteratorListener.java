/**
 *
 */
package it.eng.qbe.statement.jpa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.persistence.criteria.JoinType;

import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListenerAdapter;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.statement.graph.bean.Relationship;

/**
 * @author dpirkovic
 *
 */
public class GraphIteratorListener extends TraversalListenerAdapter<IModelEntity, Relationship> {

	List<String> joinStatments = new ArrayList<>();

	/**
	 * @return the joinStatments
	 */
	public List<String> getJoinStatments() {
		return joinStatments;
	}

	Map entityAliasesMaps;
	Map<String, String> queryEntityAliases;

	/**
	 * @param parentStatement
	 * @param entityAliasesMaps
	 * @param queryEntityAliases
	 */
	public GraphIteratorListener(Map entityAliasesMaps, Map<String, String> queryEntityAliases) {
		this.entityAliasesMaps = entityAliasesMaps;
		this.queryEntityAliases = queryEntityAliases;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.jgrapht.event.TraversalListenerAdapter#edgeTraversed(org.jgrapht.event.EdgeTraversalEvent)
	 */
	@Override
	public void edgeTraversed(EdgeTraversalEvent<IModelEntity, Relationship> e) {

		Relationship relationship = e.getEdge();
		JoinType joinType = relationship.getJoinType();

		String sourceEntityAlias = getAlias(entityAliasesMaps, queryEntityAliases, relationship.getSourceEntity());
		String targetEntityAlias = getAlias(entityAliasesMaps, queryEntityAliases, relationship.getTargetEntity());
		String entityJoinPath = relationship.getTargetJoinPath();
		StringTokenizer st1 = new StringTokenizer(entityJoinPath, ".");
		st1.nextToken();
		String targetPropertyName = st1.nextToken();
		JPQLJoinPath joinPath = new JPQLJoinPath(sourceEntityAlias, targetPropertyName);
		JPQLJoin join = new JPQLJoin();

		join.setJoinType(joinType);
		join.setJoinPath(joinPath);
		join.setTargetEntityAllias(targetEntityAlias);
		joinStatments.add(join.toString());
	}

	private String getAlias(Map entityAliasesMaps, Map<String, String> queryEntityAliases, IModelEntity entity) {
		String alias;
		alias = queryEntityAliases.get(entity.getUniqueName());
		if (alias == null) {
			alias = getNextAlias(entityAliasesMaps);

			queryEntityAliases.put(entity.getUniqueName(), alias);
		}
		return alias;
	}

	private String getNextAlias(Map entityAliasesMaps) {
		int aliasesCount = 0;
		Iterator<String> it = entityAliasesMaps.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			Map entityAliases = (Map) entityAliasesMaps.get(key);
			aliasesCount += entityAliases.keySet().size();
		}

		return "t_" + aliasesCount;
	}

}
