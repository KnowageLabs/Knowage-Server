package it.eng.knowage.meta.model.serializer;

import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.business.BusinessColumn;
import it.eng.knowage.meta.model.business.BusinessModelFactory;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.impl.BusinessColumnImpl;
import it.eng.knowage.meta.model.business.impl.BusinessTableImpl;
import it.eng.knowage.meta.model.impl.ModelPropertyImpl;
import it.eng.knowage.meta.model.impl.ModelPropertyMapEntryImpl;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

public class ModelPropertyFactory extends AbstractFactory {

	@Override
	public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
		if ((parent instanceof BusinessTableImpl) && "relationships".equals(name)) {
			BusinessTableImpl bt = ((BusinessTableImpl) parent);
			BusinessRelationship br = BusinessModelFactory.eINSTANCE.createBusinessRelationship();
			bt.getModel().getRelationships().add(br);
			return true;
		} else if (parent instanceof ModelPropertyImpl) {
			ModelPropertyImpl mp = ((ModelPropertyImpl) parent);
			mp.setPropertyType(ModelFactory.eINSTANCE.createModelPropertyType());
			return true;
		} else if ((parent instanceof BusinessColumn)) {
			BusinessColumnImpl bc = ((BusinessColumnImpl) parent);
			bc.getProperties();
			bc.getPropertyTypes();
			return true;
		} else if ((parent instanceof ModelPropertyMapEntryImpl)) {
			ModelPropertyMapEntryImpl map = ((ModelPropertyMapEntryImpl) parent);
			map.setKey(name);
			return true;
		}
		return false;
	}
}
