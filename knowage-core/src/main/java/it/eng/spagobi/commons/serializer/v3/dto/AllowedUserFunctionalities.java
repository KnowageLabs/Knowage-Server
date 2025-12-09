package it.eng.spagobi.commons.serializer.v3.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AllowedUserFunctionalities {
	private final String id;
	private final List<ItemMenu> items;

	public AllowedUserFunctionalities(String id, List<ItemMenu> items) {
		this.id = id;
		this.items = Collections.unmodifiableList(new ArrayList<>(items));
	}

	public String getId() {
		return id;
	}

	public List<ItemMenu> getItems() {
		return items;
	}
}
