
package it.eng.spagobi.commons.serializer.v3.dto;

import java.util.HashMap;
import java.util.Map;

public final class StaticMenu {
	private final TechnicalUserFunctionalities technical;
	private final CommonUserFunctionalities common;
	private final AllowedUserFunctionalities allowed;

	public StaticMenu(TechnicalUserFunctionalities technical, CommonUserFunctionalities common, AllowedUserFunctionalities allowed) {
		this.technical = technical;
		this.common = common;
		this.allowed = allowed;

		Map<String, ItemMenu> itemsIdx = new HashMap<>();
		Map<String, GroupItem> groupsIdx = new HashMap<>();
		if (technical != null) {
			for (GroupItem g : technical.getGroups()) {
				if (g.getId() != null) {
					groupsIdx.put(g.getId(), g);
				}
				for (ItemMenu it : g.getItems()) {
					if (it.getId() != null) {
						itemsIdx.put(it.getId(), it);
					}
				}
			}
		}
		if (common != null) {
			for (ItemMenu it : common.getItems()) {
				if (it.getId() != null) {
					itemsIdx.put(it.getId(), it);
				}
			}
		}
		if (allowed != null) {
			for (ItemMenu it : allowed.getItems()) {
				if (it.getId() != null) {
					itemsIdx.put(it.getId(), it);
				}
			}
		}

	}

	public TechnicalUserFunctionalities getTechnical() {
		return technical;
	}

	public CommonUserFunctionalities getCommon() {
		return common;
	}

	public AllowedUserFunctionalities getAllowed() {
		return allowed;
	}
}
