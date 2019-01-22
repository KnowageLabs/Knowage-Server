package it.eng.qbe.statement.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.JoinType;

public class JoinRelationshipTypeMapping {

	private final Map<String, JoinType> joinRelationshipTypes;

	public JoinRelationshipTypeMapping() {
		joinRelationshipTypes = new HashMap<>();
		joinRelationshipTypes.put("one-to-many", JoinType.INNER);
		joinRelationshipTypes.put("many-to-one", JoinType.INNER);
		// joinRelationshipTypes.put("one-to-one", JoinType.INNER);
		// joinRelationshipTypes.put("optional-one-to-one", JoinType.RIGHT);
		// joinRelationshipTypes.put("one-to-optional-one", JoinType.LEFT);
		// joinRelationshipTypes.put("optional-many-to-one", JoinType.RIGHT);
		joinRelationshipTypes.put("many-to-optional-one", JoinType.RIGHT);
		joinRelationshipTypes.put("optional-one-to-many", JoinType.LEFT);
		// joinRelationshipTypes.put("one-to-optional-many", JoinType.LEFT);

	}

	public JoinType getJoinType(String relationshipType) {
		return joinRelationshipTypes.get(relationshipType);
	}

}
