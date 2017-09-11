package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class OrderingKindEnum
  implements OrderingKind
{
  public static final OrderingKindEnum OK_UNORDERED = new OrderingKindEnum("ok_unordered");
  


  public static final OrderingKindEnum OK_ORDERED = new OrderingKindEnum("ok_ordered");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Core");
    temp.add("OrderingKind");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private OrderingKindEnum(String literalName) {
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
    if ((o instanceof OrderingKindEnum)) return o == this;
    if ((o instanceof OrderingKind)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static OrderingKind forName(String name)
  {
    if (name.equals("ok_unordered")) return OK_UNORDERED;
    if (name.equals("ok_ordered")) return OK_ORDERED;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Core.OrderingKind'");
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
