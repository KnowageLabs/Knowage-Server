package it.eng.spagobi.commons.serializer.v3.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GroupItem {
	private final String id;
	private final String label;
	private final String iconCls;
	private final String to;
	private final String toBeLicensed;
	private final List<ItemMenu> items;

	public GroupItem(String id, String label, String iconCls, String to, String toBeLicensed, List<ItemMenu> items) {
		this.id = id;
		this.label = label;
		this.iconCls = iconCls;
		this.to = to;
		this.toBeLicensed = toBeLicensed;
		this.items = Collections.unmodifiableList(new ArrayList<>(items));
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getIconCls() {
		return iconCls;
	}

	public String getTo() {
		return to;
	}

	public String getToBeLicensed() {
		return toBeLicensed;
	}

	public List<ItemMenu> getItems() {
		return items;
	}
}
