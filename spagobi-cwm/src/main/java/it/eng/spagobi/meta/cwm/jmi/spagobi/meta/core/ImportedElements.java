package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface ImportedElements
  extends RefAssociation
{
  public abstract boolean exists(CwmModelElement paramCwmModelElement, CwmPackage paramCwmPackage);
  
  public abstract Collection getImportedElement(CwmPackage paramCwmPackage);
  
  public abstract Collection getImporter(CwmModelElement paramCwmModelElement);
  
  public abstract boolean add(CwmModelElement paramCwmModelElement, CwmPackage paramCwmPackage);
  
  public abstract boolean remove(CwmModelElement paramCwmModelElement, CwmPackage paramCwmPackage);
}
