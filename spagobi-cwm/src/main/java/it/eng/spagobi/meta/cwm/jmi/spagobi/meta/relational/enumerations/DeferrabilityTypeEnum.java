package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class DeferrabilityTypeEnum
  implements DeferrabilityType
{
  public static final DeferrabilityTypeEnum INITIALLY_DEFERRED = new DeferrabilityTypeEnum("initiallyDeferred");
  


  public static final DeferrabilityTypeEnum INITIALLY_IMMEDIATE = new DeferrabilityTypeEnum("initiallyImmediate");
  


  public static final DeferrabilityTypeEnum NOT_DEFERRABLE = new DeferrabilityTypeEnum("notDeferrable");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Relational");
    temp.add("Enumerations");
    temp.add("DeferrabilityType");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private DeferrabilityTypeEnum(String literalName) {
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
    if ((o instanceof DeferrabilityTypeEnum)) return o == this;
    if ((o instanceof DeferrabilityType)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static DeferrabilityType forName(String name)
  {
    if (name.equals("initiallyDeferred")) return INITIALLY_DEFERRED;
    if (name.equals("initiallyImmediate")) return INITIALLY_IMMEDIATE;
    if (name.equals("notDeferrable")) return NOT_DEFERRABLE;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Relational.Enumerations.DeferrabilityType'");
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
