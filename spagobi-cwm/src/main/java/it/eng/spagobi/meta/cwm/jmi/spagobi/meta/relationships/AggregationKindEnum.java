package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relationships;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class AggregationKindEnum
  implements AggregationKind
{
  public static final AggregationKindEnum AK_NONE = new AggregationKindEnum("ak_none");
  


  public static final AggregationKindEnum AK_AGGREGATE = new AggregationKindEnum("ak_aggregate");
  


  public static final AggregationKindEnum AK_COMPOSITE = new AggregationKindEnum("ak_composite");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Relationships");
    temp.add("AggregationKind");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private AggregationKindEnum(String literalName) {
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
    if ((o instanceof AggregationKindEnum)) return o == this;
    if ((o instanceof AggregationKind)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static AggregationKind forName(String name)
  {
    if (name.equals("ak_none")) return AK_NONE;
    if (name.equals("ak_aggregate")) return AK_AGGREGATE;
    if (name.equals("ak_composite")) return AK_COMPOSITE;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Relationships.AggregationKind'");
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
