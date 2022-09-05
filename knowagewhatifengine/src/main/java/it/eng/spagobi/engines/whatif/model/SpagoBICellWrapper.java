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

package it.eng.spagobi.engines.whatif.model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olap4j.AllocationPolicy;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Property;

import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.model.transform.CellRelation;
import it.eng.spagobi.utilities.assertion.UnreachableCodeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import mondrian.olap.Util;
import mondrian.util.Format;

public class SpagoBICellWrapper implements Cell {

	private final Cell cell;
	private final SpagoBICellSetWrapper cellSetWrapper;
	private Object value;
	private Member[] members;
	private Long leafsCount;

	public SpagoBICellWrapper(Cell cell, SpagoBICellSetWrapper cellSetWrapper) {
		this.cell = cell;
		this.cellSetWrapper = cellSetWrapper;
		this.value = cell.getValue();
		this.members = null;
	}

	@Override
	public CellSet getCellSet() {
		return this.cellSetWrapper;
	}

	@Override
	public int getOrdinal() {
		return cell.getOrdinal();
	}

	@Override
	public List<Integer> getCoordinateList() {
		return cell.getCoordinateList();
	}

	@Override
	public Object getPropertyValue(Property property) {
		return cell.getPropertyValue(property);
	}

	@Override
	public boolean isEmpty() {
		return cell.isEmpty();
	}

	@Override
	public boolean isError() {
		return cell.isEmpty();
	}

	@Override
	public boolean isNull() {
		return cell.isNull();
	}

	@Override
	public double getDoubleValue() throws OlapException {
		Object o = this.getValue();
		if (o instanceof Number) {
			Number number = (Number) o;
			return number.doubleValue();
		} else if (o == null) {
			return 0;
		}
		throw new OlapException("not a number");
	}

	@Override
	public String getErrorText() {
		return cell.getErrorText();
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public String getFormattedValue() {
		String formatString = (String) cell.getPropertyValue(Property.StandardCellProperty.FORMAT_STRING);
		SpagoBICellSetWrapper cellSet = (SpagoBICellSetWrapper) this.getCellSet();
		Locale locale = cellSet.getSpagoBIPivotModel().getLocale();
		Object value = this.getValue();
		if (value == Util.nullValue) {
			value = null;
		}
		if (value instanceof Throwable) {
			return "#ERR: " + value.toString();
		}
		Format format = Format.get(formatString, locale);
		return format.format(value);
	}

	@Override
	public ResultSet drillThrough() throws OlapException {
		return cell.drillThrough();
	}

	@Override
	public void setValue(Object value, AllocationPolicy allocationPolicy, Object... allocationArgs) throws OlapException {
		throw new UnreachableCodeException("You cannot invoke this method, since no AllocationPolicy is implemented");
	}

	public void setValue(Object value) {
		this.value = value;
		cellSetWrapper.notifyModifiedCell(this);
	}

	public Member[] getMembers() {
		if (this.members != null) { // members is a cache, just to ensure that
									// this calculation is performed once per
									// cell
			return this.members;
		}
		SpagoBICellSetWrapper cellSet = (SpagoBICellSetWrapper) this.getCellSet();
		NamedList<Hierarchy> allHierarchies = cellSet.getSpagoBIPivotModel().getCube().getHierarchies();
		// since a virtual cube contains duplicated dimensions, we remove
		// duplicates using a Set
		Set<Hierarchy> hierarchies = new HashSet<Hierarchy>();
		hierarchies.addAll(allHierarchies);

		// get members on axis
		List<Integer> coordinates = this.getCoordinateList();
		List<Member> members = new ArrayList<Member>();
		for (int i = 0; i < coordinates.size(); i++) {
			Integer aCoordinate = coordinates.get(i);
			CellSetAxis axis = this.getCellSet().getAxes().get(i);
			Position position = axis.getPositions().get(aCoordinate);
			List<Member> m = position.getMembers();
			members.addAll(m);

		}

		// get members on filters
		Iterator<Position> filtersPosition = this.getCellSet().getFilterAxis().iterator();
		while (filtersPosition.hasNext()) {
			Position position = filtersPosition.next();
			members.addAll(position.getMembers());
		}

		// calculate the visited hierarchies
		List<Hierarchy> visitedHierarchies = new ArrayList<Hierarchy>();
		Iterator<Member> membersIt = members.iterator();
		while (membersIt.hasNext()) {
			Member member = membersIt.next();
			visitedHierarchies.add(member.getHierarchy());
		}

		// remove visited hierarchies from complete list
		hierarchies.removeAll(visitedHierarchies);

		// for non visited hierarchies, get root members
		Iterator<Hierarchy> it = hierarchies.iterator();
		while (it.hasNext()) {
			Hierarchy aHierarchy = it.next();
			try {
				members.addAll(aHierarchy.getRootMembers());
			} catch (OlapException e) {
				throw new SpagoBIEngineRuntimeException("Could not retrieve root members for hierarchy " + aHierarchy.getUniqueName(), e);
			}
		}

		Member[] toReturn = new Member[members.size()];
		toReturn = members.toArray(toReturn);
		this.members = toReturn;
		return toReturn;
	}

	public CellRelation getRelationTo(SpagoBICellWrapper other) {
		return this.getRelationTo(other.getMembers());
	}

	public CellRelation getRelationTo(Member[] members) {
		int aboveCount = 0;
		int belowCount = 0;
		for (int i = 0; i < members.length; i++) {
			Member thatMember = members[i];
			Hierarchy hierarchy = thatMember.getHierarchy();
			Member[] thisMembers = this.getContextMembers(hierarchy);
			if (thisMembers.length > 1) {
				throw new RuntimeException("Cannot get cell relation in case members of hierarchy is more than one");
			}
			Member thisMember = thisMembers[0];
			// FIXME: isChildOrEqualTo is very inefficient. It should use
			// level depth as a guideline, at least.
			if (isChildOrEqualTo(thatMember, thisMember)) {
				if (thatMember.equals(thisMember)) {
					// thisMember equals member
				} else {
					// thisMember is ancestor of member
					++aboveCount;
					if (belowCount > 0) {
						return CellRelation.NONE;
					}
				}
			} else if (isChildOrEqualTo(thisMember, thatMember)) {
				// thisMember is descendant of member
				++belowCount;
				if (aboveCount > 0) {
					return CellRelation.NONE;
				}
			} else {
				return CellRelation.NONE;
			}
		}
		assert aboveCount == 0 || belowCount == 0;
		if (aboveCount > 0) {
			return CellRelation.ABOVE;
		} else if (belowCount > 0) {
			return CellRelation.BELOW;
		} else {
			return CellRelation.EQUAL;
		}
	}

	public static SpagoBICellWrapper wrap(Cell cell, SpagoBICellSetWrapper cellSetWrapper) {
		if (cell instanceof SpagoBICellWrapper) {
			return (SpagoBICellWrapper) cell;
		}
		return new SpagoBICellWrapper(cell, cellSetWrapper);
	}

	public Member[] getContextMembers(Hierarchy hierarchy) {
		List<Member> toReturn = new ArrayList<Member>();
		Member[] members = this.getMembers();
		for (int i = 0; i < members.length; i++) {
			Member member = members[i];
			Hierarchy aHierarchy = member.getHierarchy();
			if (aHierarchy.getUniqueName().equals(hierarchy.getUniqueName())) {
				toReturn.add(member);
			}
		}
		if (toReturn.isEmpty()) {
			throw new SpagoBIEngineRuntimeException("No member found on hierarchy " + hierarchy.getUniqueName());
		}
		return toReturn.toArray(new Member[0]);
	}

	public String getMeasureName() {

		Member[] members = this.getMembers();
		try {
			for (int i = 0; i < members.length; i++) {
				Member member = members[i];

				if (member.getDimension().getDimensionType().equals(Dimension.Type.MEASURE)) {
					return member.getName();
				}

			}
		} catch (OlapException e) {
			throw new SpagoBIEngineRuntimeException("Error getting the measure for the cell ", e);
		}
		throw new SpagoBIEngineRuntimeException("No measure found for the cell");
	}

	@Override
	public String toString() {
		StringBuffer membersString = new StringBuffer();
		Member[] members = getMembers();
		if (members != null) {
			for (int i = 0; i < members.length; i++) {
				membersString.append("[");
				membersString.append(members[i].getUniqueName());
				membersString.append("]");
			}
			membersString.setLength(membersString.length() - 1);
		}
		return "SpagoBICellWrapper [members=" + membersString.toString() + "]";
	}

	public long visitMembers() throws OlapException, SpagoBIEngineException {

		List<String> dimensionNames = new ArrayList<String>();

		long myLeafsNumeber = 1l;

		Member[] members = getMembers();

		for (int i = 0; i < members.length; i++) {
			Member aMember = members[i];
			String dimensionUniqueName = aMember.getDimension().getUniqueName();
			if (dimensionNames.contains(dimensionUniqueName)) {
				throw new SpagoBIEngineException("You can not apply this algorithm with multi hierarchies");
			}
			Long memberLeafs = CubeUtilities.getLeafs(aMember);
			myLeafsNumeber = myLeafsNumeber * memberLeafs;
		}

		return myLeafsNumeber;
	}

	public Long getLeafsCount() {
		return leafsCount;
	}

	public void setLeafsCount(Long leafsCount) {
		this.leafsCount = leafsCount;
	}

	/**
	 * See if member2 is ancestor of member1
	 */
	private boolean isChildOrEqualTo(Member member1, Member member2) {

		if (member1.equals(member2)) {
			return true;
		}

		if (member1.getLevel().getDepth() <= member2.getDepth())
			return false;

		Member aMember = member1.getParentMember();
		while (aMember.getLevel().getDepth() >= member2.getDepth() && !aMember.equals(member2)) {
			aMember = aMember.getParentMember();
		}
		return (aMember.equals(member2));

	}

}
