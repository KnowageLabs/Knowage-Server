package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relationships;

import javax.jmi.reflect.RefPackage;

public abstract interface RelationshipsPackage
  extends RefPackage
{
  public abstract CwmAssociationClass getCwmAssociation();
  
  public abstract CwmAssociationEndClass getCwmAssociationEnd();
  
  public abstract CwmGeneralizationClass getCwmGeneralization();
  
  public abstract ChildElement getChildElement();
  
  public abstract ParentElement getParentElement();
}
