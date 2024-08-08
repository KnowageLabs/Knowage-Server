/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.view.metadata;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.CompareToBuilder;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
public class SbiViewHierarchy extends SbiHibernateModel implements Comparable<SbiViewHierarchy>, Serializable {

	private static final long serialVersionUID = -6000259920498982988L;

	@NotNull
	private String id;

	private SbiViewHierarchy parent;

	@NotNull
	private String name;

	@Nullable
	private String descr;

	@NotNull
	private Integer progr;

	private Set<SbiViewHierarchy> children = new TreeSet<>();

	@Override
	public int compareTo(SbiViewHierarchy o) {
		// @formatter:off
		return new CompareToBuilder()
				.append(this.parent, o.parent)
				.append(this.progr, o.progr)
				.append(this.id, o.id)
				.toComparison();
		// @formatter:on
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiViewHierarchy other = (SbiViewHierarchy) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;

		return true;
	}

	/**
	 * @return the children
	 */
	public Set<SbiViewHierarchy> getChildren() {
		return children;
	}

	/**
	 * @return the desc
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the parent
	 */
	public SbiViewHierarchy getParent() {
		return parent;
	}

	/**
	 * @return the progr
	 */
	public Integer getProgr() {
		return progr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(Set<SbiViewHierarchy> children) {
		this.children = children;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDescr(String desc) {
		this.descr = desc;
	}

	/**
	 * @param id the id to set
	 */
	private void setId(String id) {
		this.id = id;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(SbiViewHierarchy parent) {
		this.parent = parent;
	}

	/**
	 * @param progr the progr to set
	 */
	public void setProgr(Integer progr) {
		this.progr = progr;
	}

	@Override
	public String toString() {
		return "SbiViewHierarchy [id=" + id + ", name=" + name + "]";
	}

}
