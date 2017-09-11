package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import java.util.Collection;
import javax.jmi.reflect.RefAssociation;

public abstract interface EnumerationLiterals
  extends RefAssociation
{
  public abstract boolean exists(CwmEnumeration paramCwmEnumeration, CwmEnumerationLiteral paramCwmEnumerationLiteral);
  
  public abstract CwmEnumeration getEnumeration(CwmEnumerationLiteral paramCwmEnumerationLiteral);
  
  public abstract Collection getLiteral(CwmEnumeration paramCwmEnumeration);
  
  public abstract boolean add(CwmEnumeration paramCwmEnumeration, CwmEnumerationLiteral paramCwmEnumerationLiteral);
  
  public abstract boolean remove(CwmEnumeration paramCwmEnumeration, CwmEnumerationLiteral paramCwmEnumerationLiteral);
}
