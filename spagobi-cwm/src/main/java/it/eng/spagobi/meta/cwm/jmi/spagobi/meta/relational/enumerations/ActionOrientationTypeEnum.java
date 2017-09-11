package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class ActionOrientationTypeEnum
  implements ActionOrientationType
{
  public static final ActionOrientationTypeEnum ROW = new ActionOrientationTypeEnum("row");
  


  public static final ActionOrientationTypeEnum STATEMENT = new ActionOrientationTypeEnum("statement");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Relational");
    temp.add("Enumerations");
    temp.add("ActionOrientationType");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private ActionOrientationTypeEnum(String literalName) {
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
    if ((o instanceof ActionOrientationTypeEnum)) return o == this;
    if ((o instanceof ActionOrientationType)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static ActionOrientationType forName(String name)
  {
    if (name.equals("row")) return ROW;
    if (name.equals("statement")) return STATEMENT;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Relational.Enumerations.ActionOrientationType'");
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
