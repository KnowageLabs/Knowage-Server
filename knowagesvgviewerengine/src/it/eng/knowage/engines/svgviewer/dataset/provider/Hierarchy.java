package it.eng.knowage.engines.svgviewer.dataset.provider;

import it.eng.spagobi.commons.utilities.StringUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class Hierarchy.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class Hierarchy {

	/** The name. */
	private String name;

	/** The type. */
	private String type;

	/** The table. */
	private String table;

	/** The level list. */
	private List levelList;

	/** The level map. */
	private Map levelMap;

	private static final String ALL_MEASURE_KEY = "__ALL_MEASURE_KEY__";

	public static transient Logger logger = Logger.getLogger(Hierarchy.class);

	/**
	 * Instantiates a new hierarchy.
	 *
	 * @param name
	 *            the name
	 */
	public Hierarchy(String name) {
		this.name = name;
		this.type = "custom";
		this.table = null;
		this.levelList = new ArrayList();
		this.levelMap = new HashMap();
	}

	/**
	 * Instantiates a new hierarchy.
	 *
	 * @param name
	 *            the name
	 * @param table
	 *            the table
	 */
	public Hierarchy(String name, String table) {
		this.name = name;
		this.type = "defualt";
		this.table = table;
		this.levelList = new ArrayList();
		this.levelMap = new HashMap();
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the table.
	 *
	 * @return the table
	 */
	public String getTable() {
		return table;
	}

	/**
	 * Sets the table.
	 *
	 * @param table
	 *            the new table
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Adds the level.
	 *
	 * @param level
	 *            the level
	 */
	public void addLevel(Level level) {
		levelList.add(level);
		levelMap.put(level.getName(), level);
	}

	/**
	 * Gets the level.
	 *
	 * @param levelName
	 *            the level name
	 *
	 * @return the level
	 */
	public Level getLevel(String levelName) {
		return (Level) levelMap.get(levelName);
	}

	/**
	 * Gets the levels.
	 *
	 * @return the levels
	 */
	public List getLevels() {
		return levelList;
	}

	/**
	 * Gets the sublevels.
	 *
	 * @param levelName
	 *            the level name
	 *
	 * @return the sublevels
	 */
	public List getSublevels(String levelName) {
		List levels = new ArrayList();
		boolean isSubLevel = false;
		for (int i = 0; i < levelList.size(); i++) {
			Level level = (Level) levelList.get(i);
			if (isSubLevel) {
				levels.add(level);
			} else {
				if (level.getName().equalsIgnoreCase(levelName))
					isSubLevel = true;
			}
		}

		return levels;
	}

	/**
	 * The Class Level.
	 */
	public static class Level {

		/** The name. */
		private String name;

		/** The column id. */
		private String columnId;

		/** The column desc. */
		private String columnDesc;

		/** The feature name. */
		private String featureName;

		/** The link. */
		private Map links;

		/**
		 * Instantiates a new level.
		 */
		public Level() {
			links = new HashMap();
		}

		/**
		 * Gets the column desc.
		 *
		 * @return the column desc
		 */
		public String getColumnDesc() {
			return columnDesc;
		}

		/**
		 * Sets the column desc.
		 *
		 * @param columnDesc
		 *            the new column desc
		 */
		public void setColumnDesc(String columnDesc) {
			this.columnDesc = columnDesc;
		}

		/**
		 * Gets the column id.
		 *
		 * @return the column id
		 */
		public String getColumnId() {
			return columnId;
		}

		/**
		 * Sets the column id.
		 *
		 * @param columnId
		 *            the new column id
		 */
		public void setColumnId(String columnId) {
			this.columnId = columnId;
		}

		/**
		 * Gets the feature name.
		 *
		 * @return the feature name
		 */
		public String getFeatureName() {
			return featureName;
		}

		/**
		 * Sets the feature name.
		 *
		 * @param featureName
		 *            the new feature name
		 */
		public void setFeatureName(String featureName) {
			this.featureName = featureName;
		}

		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Sets the name.
		 *
		 * @param name
		 *            the new name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Gets the link.
		 *
		 * @return the link
		 */
		public Link getLink(String key) {
			Link link;

			link = (Link) links.get(key.toUpperCase());
			if (link == null) {
				link = (Link) links.get(ALL_MEASURE_KEY);
			}

			return link;
		}

		/**
		 * Sets the link.
		 *
		 * @param link
		 *            the new link
		 */
		public void setLink(String key, Link link) {
			if (StringUtilities.isEmpty(key)) {
				this.links.put(ALL_MEASURE_KEY, link);
				logger.debug("impossible to add link on " + key);
			} else {
				this.links.put(key.toUpperCase(), link);
				logger.debug("added link on " + key);
			}

		}
	}

	/**
	 * Gets the level names.
	 *
	 * @return the level names
	 */
	public Set getLevelNames() {
		return levelMap.keySet();
	}
}
