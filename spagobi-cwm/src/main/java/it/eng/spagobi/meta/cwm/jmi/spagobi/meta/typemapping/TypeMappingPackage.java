package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.typemapping;

import javax.jmi.reflect.RefPackage;

public abstract interface TypeMappingPackage
  extends RefPackage
{
  public abstract CwmTypeMappingClass getCwmTypeMapping();
  
  public abstract CwmTypeSystemClass getCwmTypeSystem();
  
  public abstract MappingSource getMappingSource();
  
  public abstract MappingTarget getMappingTarget();
}
