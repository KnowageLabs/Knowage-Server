package it.eng.knowage.meta.model.filter;

import it.eng.knowage.meta.model.ModelObject;

import java.util.List;

public class GenericFilter<T> implements IModelObjectFilter {

	List<T> objects;

	public GenericFilter(List<T> objects) {
		this.objects = objects;
	}

	@Override
	public boolean filter(ModelObject o) {
		if (o != null && this.objects != null && this.objects.contains(o))
			return true;
		return false;
	}

}
