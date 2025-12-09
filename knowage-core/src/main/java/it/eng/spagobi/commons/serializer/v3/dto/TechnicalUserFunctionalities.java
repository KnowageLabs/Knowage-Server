
package it.eng.spagobi.commons.serializer.v3.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TechnicalUserFunctionalities {
	private final List<GroupItem> groups;

	public TechnicalUserFunctionalities(List<GroupItem> groups) {
		this.groups = Collections.unmodifiableList(new ArrayList<>(groups));
	}

	public List<GroupItem> getGroups() {
		return groups;
	}
}
