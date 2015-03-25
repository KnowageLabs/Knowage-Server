/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.tools.hierarchiesmanagement;

import java.sql.Date;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class HierarchyTreeNodeData {

	String nodeCode;
	String nodeName;
	String leafId;
	String leafParentCode;
	String leafOriginalParentCode;
	String leafParentName;
	String depth;
	Date beginDt;
	Date endDt;
	Integer signLev1;
	Integer signLev2;
	Integer signLev3;
	Integer signLev4;
	Integer signLev5;
	Integer signLev6;
	Integer signLev7;
	Integer signLev8;
	Integer signLev9;
	Integer signLev10;
	Integer signLev11;
	Integer signLev12;
	Integer signLev13;
	Integer signLev14;
	Integer signLev15;

	/**
	 * @param nodeCode
	 * @param nodeName
	 */
	public HierarchyTreeNodeData(String nodeCode, String nodeName) {
		this(nodeCode, nodeName, "", "", "", "");
	}

	public HierarchyTreeNodeData(String nodeCode, String nodeName, String leafId, String leafParentCode, String leafParentName, String leafOriginalParentCode) {
		super();
		this.nodeCode = nodeCode;
		this.nodeName = nodeName;
		this.leafId = leafId;
		this.leafParentCode = leafParentCode;
		this.leafParentName = leafParentName;
		this.leafOriginalParentCode = leafOriginalParentCode;
	}

	/**
	 * @return the nodeCode
	 */
	public String getNodeCode() {
		return nodeCode;
	}

	/**
	 * @param nodeCode
	 *            the nodeCode to set
	 */
	public void setNodeCode(String nodeCode) {
		this.nodeCode = nodeCode;
	}

	/**
	 * @return the nodeName
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * @param nodeName
	 *            the nodeName to set
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * @return the leafId
	 */
	public String getLeafId() {
		return leafId;
	}

	/**
	 * @param leafId
	 *            the leafId to set
	 */
	public void setLeafId(String leafId) {
		this.leafId = leafId;
	}

	/**
	 * @return the leafParentCode
	 */
	public String getLeafParentCode() {
		return leafParentCode;
	}

	/**
	 * @param leafParentCode
	 *            the leafParentCode to set
	 */
	public void setLeafParentCode(String leafParentCode) {
		this.leafParentCode = leafParentCode;
	}

	/**
	 * @return the leafParentNm
	 */
	public String getLeafParentName() {
		return leafParentName;
	}

	/**
	 * @param leafParentNm
	 *            the leafParentNm to set
	 */
	public void setLeafParentName(String leafParentName) {
		this.leafParentName = leafParentName;
	}

	/**
	 * @return the leafOriginalParentCode
	 */
	public String getLeafOriginalParentCode() {
		return leafOriginalParentCode;
	}

	/**
	 * @param leafOriginalParentCode
	 *            the leafOriginalParentCode to set
	 */
	public void setLeafOriginalParentCode(String leafOriginalParentCode) {
		this.leafOriginalParentCode = leafOriginalParentCode;
	}

	/**
	 * @return the depth
	 */
	public String getDepth() {
		return depth;
	}

	/**
	 * @param depth
	 *            the depth to set
	 */
	public void setDepth(String depth) {
		this.depth = depth;
	}

	/**
	 * @return the beginDt
	 */
	public Date getBeginDt() {
		return beginDt;
	}

	/**
	 * @param beginDt
	 *            the beginDt to set
	 */
	public void setBeginDt(Date beginDt) {
		this.beginDt = beginDt;
	}

	/**
	 * @return the endDt
	 */
	public Date getEndDt() {
		return endDt;
	}

	/**
	 * @param endDt
	 *            the endDt to set
	 */
	public void setEndDt(Date endDt) {
		this.endDt = endDt;
	}

	/**
	 * @return the signLev1
	 */
	public Integer getSignLev1() {
		return signLev1;
	}

	/**
	 * @param signLev1
	 *            the signLev1 to set
	 */
	public void setSignLev1(Integer signLev1) {
		this.signLev1 = signLev1;
	}

	/**
	 * @return the signLev2
	 */
	public Integer getSignLev2() {
		return signLev2;
	}

	/**
	 * @param signLev2
	 *            the signLev2 to set
	 */
	public void setSignLev2(Integer signLev2) {
		this.signLev2 = signLev2;
	}

	/**
	 * @return the signLev3
	 */
	public Integer getSignLev3() {
		return signLev3;
	}

	/**
	 * @param signLev3
	 *            the signLev3 to set
	 */
	public void setSignLev3(Integer signLev3) {
		this.signLev3 = signLev3;
	}

	/**
	 * @return the signLev4
	 */
	public Integer getSignLev4() {
		return signLev4;
	}

	/**
	 * @param signLev4
	 *            the signLev4 to set
	 */
	public void setSignLev4(Integer signLev4) {
		this.signLev4 = signLev4;
	}

	/**
	 * @return the signLev5
	 */
	public Integer getSignLev5() {
		return signLev5;
	}

	/**
	 * @param signLev5
	 *            the signLev5 to set
	 */
	public void setSignLev5(Integer signLev5) {
		this.signLev5 = signLev5;
	}

	/**
	 * @return the signLev6
	 */
	public Integer getSignLev6() {
		return signLev6;
	}

	/**
	 * @param signLev6
	 *            the signLev6 to set
	 */
	public void setSignLev6(Integer signLev6) {
		this.signLev6 = signLev6;
	}

	/**
	 * @return the signLev7
	 */
	public Integer getSignLev7() {
		return signLev7;
	}

	/**
	 * @param signLev7
	 *            the signLev7 to set
	 */
	public void setSignLev7(Integer signLev7) {
		this.signLev7 = signLev7;
	}

	/**
	 * @return the signLev8
	 */
	public Integer getSignLev8() {
		return signLev8;
	}

	/**
	 * @param signLev8
	 *            the signLev8 to set
	 */
	public void setSignLev8(Integer signLev8) {
		this.signLev8 = signLev8;
	}

	/**
	 * @return the signLev9
	 */
	public Integer getSignLev9() {
		return signLev9;
	}

	/**
	 * @param signLev9
	 *            the signLev9 to set
	 */
	public void setSignLev9(Integer signLev9) {
		this.signLev9 = signLev9;
	}

	/**
	 * @return the signLev10
	 */
	public Integer getSignLev10() {
		return signLev10;
	}

	/**
	 * @param signLev10
	 *            the signLev10 to set
	 */
	public void setSignLev10(Integer signLev10) {
		this.signLev10 = signLev10;
	}

	/**
	 * @return the signLev11
	 */
	public Integer getSignLev11() {
		return signLev11;
	}

	/**
	 * @param signLev11
	 *            the signLev11 to set
	 */
	public void setSignLev11(Integer signLev11) {
		this.signLev11 = signLev11;
	}

	/**
	 * @return the signLev12
	 */
	public Integer getSignLev12() {
		return signLev12;
	}

	/**
	 * @param signLev12
	 *            the signLev12 to set
	 */
	public void setSignLev12(Integer signLev12) {
		this.signLev12 = signLev12;
	}

	/**
	 * @return the signLev13
	 */
	public Integer getSignLev13() {
		return signLev13;
	}

	/**
	 * @param signLev13
	 *            the signLev13 to set
	 */
	public void setSignLev13(Integer signLev13) {
		this.signLev13 = signLev13;
	}

	/**
	 * @return the signLev14
	 */
	public Integer getSignLev14() {
		return signLev14;
	}

	/**
	 * @param signLev14
	 *            the signLev14 to set
	 */
	public void setSignLev14(Integer signLev14) {
		this.signLev14 = signLev14;
	}

	/**
	 * @return the signLev15
	 */
	public Integer getSignLev15() {
		return signLev15;
	}

	/**
	 * @param signLev15
	 *            the signLev15 to set
	 */
	public void setSignLev15(Integer signLev15) {
		this.signLev15 = signLev15;
	}

}
