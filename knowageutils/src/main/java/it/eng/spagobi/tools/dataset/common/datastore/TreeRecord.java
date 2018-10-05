package it.eng.spagobi.tools.dataset.common.datastore;

import java.util.ArrayList;
import java.util.List;

public class TreeRecord {
	private List<TreeRecord> childrenNodes = new ArrayList<TreeRecord>();
	private TreeRecord parentNode = null;
	private Field field = null;

	public TreeRecord() {
	}

	public TreeRecord(Field field) {
		this.field = field;
	}

	public TreeRecord(Field field, TreeRecord parent) {
		this.field = field;
		this.parentNode = parent;
	}

	public TreeRecord(Field field, TreeRecord parent, List<TreeRecord> children) {
		this.field = field;
		this.parentNode = parent;
		this.childrenNodes = children;
	}

	public void setParent(TreeRecord parent) {
		parent.setChild(this);
		this.setParent(parent);
	}

	public void setChild(TreeRecord child) {
		this.childrenNodes.add(child);
		child.setParent(this);
	}

	public void setField(Field field) {
		this.field = field;
	}

	public Object getFieldValue(TreeRecord treeRecord) {
		Field field = this.field;
		return field.getValue();
	}

}
