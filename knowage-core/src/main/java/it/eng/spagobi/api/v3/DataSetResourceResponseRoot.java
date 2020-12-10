package it.eng.spagobi.api.v3;

import java.util.ArrayList;
import java.util.Collection;

class DataSetResourceResponseRoot<T> {
	private final Collection<T> root = new ArrayList<>();

	public DataSetResourceResponseRoot(Collection<T> root) {
		this.root.addAll(root);
	}

	public Collection<T> getRoot() {
		return root;
	}
}