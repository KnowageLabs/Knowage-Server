package it.eng.spagobi.commons.serializer.v3.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CommonUserFunctionalities {
	private final List<ItemMenu> items;

	public CommonUserFunctionalities(List<ItemMenu> items) {
		this.items = Collections.unmodifiableList(new ArrayList<>(items));
	}

	public List<ItemMenu> getItems() {
		return items;
	}
}