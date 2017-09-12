/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.writeback4j;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * 
 */
public class SbiAliases implements Serializable {

	private static final long serialVersionUID = 6589950662601952335L;
	public static String DIMENSION_TAG = "DIMENSION";
	public static String HIERARCHY_TAG = "HIERARCHY";
	public static String ALIAS_TAG = "ALIAS";

	private List<SbiAlias> aliases;

	public SbiAliases() {
		super();
	}

	/**
	 * @return the aliases
	 */
	public List<SbiAlias> getAliases() {
		return aliases;
	}

	/**
	 * @param aliases
	 *            the aliases to set
	 */
	public void setAliases(List<SbiAlias> aliases) {
		this.aliases = aliases;
	}

	/**
	 * Get the alias corresponding to the dimension with name dimensionName.
	 * Return null if it can't find a alias for that dimension.
	 * 
	 * @param dimensionName
	 *            dimension or hierarchy Name
	 * @return the value of the alias or null if it can't find a alias for that
	 *         dimension
	 */
	public SbiAlias getAlias(String dimensionName) {
		if (this.aliases != null) {
			for (Iterator<SbiAlias> iterator = aliases.iterator(); iterator.hasNext();) {
				SbiAlias aAlias = iterator.next();
				if (aAlias.getName().equals(dimensionName)) {
					return aAlias;
				}
			}
		}
		return null;
	}

	/**
	 * Get the value of the alias corresponding to the dimension with name
	 * dimensionName. Return null if it can't find a alias for that dimension.
	 * 
	 * @param dimensionName
	 *            dimension or hierarchy Name
	 * @return the value of the alias or null if it can't find a alias for that
	 *         dimension
	 */
	public String getAliasValue(String dimensionName) {
		if (this.aliases != null) {
			for (Iterator<SbiAlias> iterator = aliases.iterator(); iterator.hasNext();) {
				SbiAlias aAlias = iterator.next();
				if (aAlias.getName().equalsIgnoreCase(dimensionName)) {
					return aAlias.getAlias();
				}
			}
		}
		return null;
	}

	/**
	 * Get the original name corresponding to the passed alias.
	 * 
	 * @param alias
	 *            the name of the alias
	 * @return original name corresponding to the alias or null if is impossible
	 *         to find that alias
	 */
	public String getDimensionNameFromAlias(String alias) {
		if (this.aliases != null) {
			for (Iterator<SbiAlias> iterator = aliases.iterator(); iterator.hasNext();) {
				SbiAlias aAlias = iterator.next();
				if (aAlias.getType().equals(DIMENSION_TAG)) {
					if (aAlias.getAlias().equalsIgnoreCase(alias)) {
						return aAlias.getName();
					}
				}

			}
		}
		return null;
	}

	/**
	 * Get the original name corresponding to the passed alias.
	 * 
	 * @param alias
	 *            the name of the alias
	 * @return original name corresponding to the alias or null if is impossible
	 *         to find that alias
	 */
	public String getHierarchyNameFromAlias(String alias) {
		if (this.aliases != null) {
			for (Iterator<SbiAlias> iterator = aliases.iterator(); iterator.hasNext();) {
				SbiAlias aAlias = iterator.next();
				if (aAlias.getType().equals(HIERARCHY_TAG)) {
					if (aAlias.getAlias().equalsIgnoreCase(alias)) {
						return aAlias.getName();
					}
				}

			}
		}
		return null;
	}

	/**
	 * Get the original name corresponding to the passed alias.
	 * 
	 * @param alias
	 *            the name of the alias
	 * @return original name corresponding to the alias or null if is impossible
	 *         to find that alias
	 */
	public String getGenericNameFromAlias(String alias) {
		if (this.aliases != null) {
			for (Iterator<SbiAlias> iterator = aliases.iterator(); iterator.hasNext();) {
				SbiAlias aAlias = iterator.next();
				if (aAlias.getType().equals(ALIAS_TAG)) {
					if (aAlias.getAlias().equalsIgnoreCase(alias)) {
						return aAlias.getName();
					}
				}

			}
		}
		return null;
	}

}
