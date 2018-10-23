package it.eng.qbe.statement.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.JoinType;

public class JoinRelationshipTypeMapping {

	private final Map<String, JoinType> joinRelationshipTypes;

	public JoinRelationshipTypeMapping() {
		joinRelationshipTypes = new HashMap<>();
		joinRelationshipTypes.put("one-to-many", JoinType.LEFT);
		joinRelationshipTypes.put("many-to-one", JoinType.INNER);
		joinRelationshipTypes.put("many-to-many", JoinType.LEFT);
		joinRelationshipTypes.put("one-to-one", JoinType.INNER);
		joinRelationshipTypes.put("optional-one-to-one", JoinType.LEFT);
		joinRelationshipTypes.put("one-to-optional-one", JoinType.INNER);
		joinRelationshipTypes.put("optional-many-to-one", JoinType.LEFT);
		joinRelationshipTypes.put("many-to-optional-one", JoinType.INNER);
		joinRelationshipTypes.put("optional-one-to-many", JoinType.LEFT);
		joinRelationshipTypes.put("one-to-optional-many", JoinType.INNER);

	}

	public JoinType getJoinType(String relationshipType) {
		return joinRelationshipTypes.get(relationshipType);
	}

}
