package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class EventManipulationTypeEnum
  implements EventManipulationType
{
  public static final EventManipulationTypeEnum INSERT = new EventManipulationTypeEnum("insert");
  


  public static final EventManipulationTypeEnum DELETE = new EventManipulationTypeEnum("delete");
  


  public static final EventManipulationTypeEnum UPDATE = new EventManipulationTypeEnum("update");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Relational");
    temp.add("Enumerations");
    temp.add("EventManipulationType");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private EventManipulationTypeEnum(String literalName) {
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
    if ((o instanceof EventManipulationTypeEnum)) return o == this;
    if ((o instanceof EventManipulationType)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static EventManipulationType forName(String name)
  {
    if (name.equals("insert")) return INSERT;
    if (name.equals("delete")) return DELETE;
    if (name.equals("update")) return UPDATE;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Relational.Enumerations.EventManipulationType'");
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
