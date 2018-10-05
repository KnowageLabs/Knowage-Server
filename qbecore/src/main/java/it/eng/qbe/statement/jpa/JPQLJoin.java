package it.eng.qbe.statement.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.JoinType;

import it.eng.spagobi.utilities.StringUtils;

public class JPQLJoin {

	private JoinType joinType;
	private JPQLJoinPath joinPath;
	private String targetEntityAllias;
	private static final String JOIN = "JOIN";

	public JoinType getJoinType() {
		return joinType;
	}

	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	public JPQLJoinPath getJoinPath() {
		return joinPath;
	}

	public void setJoinPath(JPQLJoinPath joinPath) {
		this.joinPath = joinPath;
	}

	public String getTargetEntityAllias() {
		return targetEntityAllias;
	}

	public void setTargetEntityAllias(String targetEntityAllias) {
		this.targetEntityAllias = targetEntityAllias;
	}

	@Override
	public String toString() {
		List<String> joinElements = new ArrayList<>();
		joinElements.add(joinType.toString());
		joinElements.add(JOIN);
		joinElements.add(this.joinPath.toString());
		joinElements.add(this.targetEntityAllias);

		return StringUtils.join(joinElements, " ");
	}

}
