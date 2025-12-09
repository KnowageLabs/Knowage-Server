package it.eng.spagobi.commons.serializer.v3;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import it.eng.spagobi.commons.serializer.v3.dto.AllowedUserFunctionalities;
import it.eng.spagobi.commons.serializer.v3.dto.CommonUserFunctionalities;
import it.eng.spagobi.commons.serializer.v3.dto.GroupItem;
import it.eng.spagobi.commons.serializer.v3.dto.ItemMenu;
import it.eng.spagobi.commons.serializer.v3.dto.StaticMenu;
import it.eng.spagobi.commons.serializer.v3.dto.TechnicalUserFunctionalities;

public final class StaticMenuRegistry {

	private static StaticMenuRegistry INSTANCE;

	private static final String CONFIG_FILE = "conf/static_menu.xml";

	private final StaticMenu cache;

	private StaticMenuRegistry() {
		try {
			this.cache = parse();
		} catch (Exception e) {
			throw new ExceptionInInitializerError("Errore caricando conf/static_menu.xml: " + e.getMessage());
		}
	}

	public static synchronized StaticMenuRegistry getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new StaticMenuRegistry();
		}
		return INSTANCE;
	}


	public StaticMenu getMenu() {
		return cache;
	}

	// ======== PARSER ========

	private StaticMenu parse() {
		try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// Hardening XXE
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			dbf.setXIncludeAware(false);
			dbf.setExpandEntityReferences(false);
			dbf.setNamespaceAware(false);

			Document doc = dbf.newDocumentBuilder().parse(input);
			Element root = doc.getDocumentElement();

			// TECHNICAL_USER_FUNCTIONALITIES
			TechnicalUserFunctionalities technical = null;
			Element techEl = firstChild(root, "TECHNICAL_USER_FUNCTIONALITIES");
			if (techEl != null) {
				List<GroupItem> groups = new ArrayList<>();
				for (Element gEl : directChildren(techEl, "GROUP_ITEM")) {
					String gid = attr(gEl, "id");
					String glabel = attr(gEl, "label");
					String gicon = attr(gEl, "iconCls");
					String gto = attr(gEl, "to");
					String glicensed = attr(gEl, "toBeLicensed");

					List<ItemMenu> items = new ArrayList<>();
					for (Element itEl : directChildren(gEl, "ITEM")) {
						items.add(parseItem(itEl));
					}
					groups.add(new GroupItem(gid, glabel, gicon, gto, glicensed, items));
				}
				technical = new TechnicalUserFunctionalities(groups);
			}

			// COMMON_USER_FUNCTIONALITIES
			CommonUserFunctionalities common = null;
			Element comEl = firstChild(root, "COMMON_USER_FUNCTIONALITIES");
			if (comEl != null) {
				List<ItemMenu> items = new ArrayList<>();
				for (Element itEl : directChildren(comEl, "ITEM")) {
					items.add(parseItem(itEl));
				}
				common = new CommonUserFunctionalities(items);
			}

			// ALLOWED_USER_FUNCTIONALITIES
			AllowedUserFunctionalities allowed = null;
			Element allEl = firstChild(root, "ALLOWED_USER_FUNCTIONALITIES");
			if (allEl != null) {
				String containerId = attr(allEl, "id");
				List<ItemMenu> items = new ArrayList<>();
				for (Element itEl : directChildren(allEl, "ITEM")) {
					items.add(parseItem(itEl));
				}
				allowed = new AllowedUserFunctionalities(containerId, items);
			}

			return new StaticMenu(technical, common, allowed);
		} catch (Exception e) {
			throw new IllegalStateException("Errore nel parsing del menu statico: " + e.getMessage(), e);
		}
	}

	private ItemMenu parseItem(Element itEl) {
		return new ItemMenu(attr(itEl, "id"), attr(itEl, "label"), attr(itEl, "requiredFunctionality"), attr(itEl, "to"), attr(itEl, "command"),
				attr(itEl, "iconCls"), attr(itEl, "condition"), attr(itEl, "conditionedView"), attr(itEl, "toBeAuthorized"), attr(itEl, "toBeLicensed"));
	}

	// ======== HELPERS XML & IO ========

	private static String attr(Element e, String name) {
		return e.hasAttribute(name) ? emptyToNull(e.getAttribute(name)) : null;
	}

	private static String emptyToNull(String s) {
		return (s == null || s.isBlank()) ? null : s;
	}


	private static Element firstChild(Element parent, String tag) {
		for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE && tag.equals(((Element) n).getTagName())) {
				return (Element) n;
			}
		}
		return null;
	}

	private static List<Element> directChildren(Element parent, String tag) {
		List<Element> out = new ArrayList<>();
		for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) n;
				if (tag.equals(el.getTagName())) {
					out.add(el);
				}
			}
		}
		return out;
	}

}
