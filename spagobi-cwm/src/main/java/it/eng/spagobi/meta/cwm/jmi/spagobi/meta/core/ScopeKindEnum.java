package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class ScopeKindEnum
  implements ScopeKind
{
  public static final ScopeKindEnum SK_INSTANCE = new ScopeKindEnum("sk_instance");
  


  public static final ScopeKindEnum SK_CLASSIFIER = new ScopeKindEnum("sk_classifier");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Core");
    temp.add("ScopeKind");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private ScopeKindEnum(String literalName) {
    this.literalName = literalName;
  }
  



  public List refTypeName()
  {
    return typeName;
  }
  



  public String toString()
  {
    return literalName;
  }
  



  public int hashCode()
  {
    return literalName.hashCode();
  }
  





  public boolean equals(Object o)
  {
    if ((o instanceof ScopeKindEnum)) return o == this;
    if ((o instanceof ScopeKind)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static ScopeKind forName(String name)
  {
    if (name.equals("sk_instance")) return SK_INSTANCE;
    if (name.equals("sk_classifier")) return SK_CLASSIFIER;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Core.ScopeKind'");
  }
  
  protected Object readResolve()
    throws ObjectStreamException
  {
    try
    {
      return forName(literalName);
    } catch (IllegalArgumentException e) {
      throw new InvalidObjectException(e.getMessage());
    }
  }
}
