/**
 *
 */
package it.eng.spagobi.engines.whatif.model.transform.slicer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olap4j.OlapException;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.pivot4j.transform.ChangeSlicer;

import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Dragan Pirkovic
 *
 */
public class SlicerManager {

	public static transient Logger logger = Logger.getLogger(SlicerManager.class);
	private final SpagoBIPivotModel model;
	private final ChangeSlicer ph;

	/**
	 *
	 */
	public SlicerManager(SpagoBIPivotModel model) {
		this.model = model;
		ph = model.getTransform(ChangeSlicer.class);
	}

	public void setSlicers(String hierarchyUniqueName, List<String> memberUniqueNames) {

		ph.setSlicer(getHierarchy(hierarchyUniqueName), getMembers(hierarchyUniqueName, memberUniqueNames));
	}

	public void clearSlicers(String hierarchyUniqueName) {
		getSlicers(getHierarchy(hierarchyUniqueName)).clear();
	}

	private List<Member> getSlicers(Hierarchy hierarchy) {
		return ph.getSlicer(hierarchy);
	}

	public List<Member> getSlicers(String hierarchyUniqueName) {
		return getSlicers(getHierarchy(hierarchyUniqueName));
	}

	private Hierarchy getHierarchy(String hierarchyUniqueName) {
		try {
			return CubeUtilities.getHierarchy(model.getCube(), hierarchyUniqueName);
		} catch (OlapException e) {
			logger.debug("Error getting  hierarchy " + hierarchyUniqueName, e);
			throw new SpagoBIEngineRuntimeException("Error getting  hierarchy " + hierarchyUniqueName, e);

		}
	}

	private Member getMember(Hierarchy hierarchy, String memberUniqueName) {
		try {

			return CubeUtilities.getMember(hierarchy, memberUniqueName);
		} catch (OlapException e) {
			logger.debug("Error getting the member " + memberUniqueName + " from the hierarchy " + hierarchy.getUniqueName(), e);
			throw new SpagoBIEngineRuntimeException("Error getting the member " + memberUniqueName + " from the hierarchy " + hierarchy.getUniqueName(), e);
		}
	}

	private Member getMember(String hierarchyUniqueName, String memberUniqueName) {

		return getMember(getHierarchy(hierarchyUniqueName), memberUniqueName);

	}

	private List<Member> getMembers(String hierarchyUniqueName, List<String> memberUniqueNames) {
		List<Member> members = new ArrayList<>();
		for (String memberUniqueName : memberUniqueNames) {
			members.add(getMember(hierarchyUniqueName, memberUniqueName));
		}

		return members;
	}

}
