package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class NullableTypeEnum
  implements NullableType
{
  public static final NullableTypeEnum COLUMN_NO_NULLS = new NullableTypeEnum("columnNoNulls");
  


  public static final NullableTypeEnum COLUMN_NULLABLE = new NullableTypeEnum("columnNullable");
  


  public static final NullableTypeEnum COLUMN_NULLABLE_UNKNOWN = new NullableTypeEnum("columnNullableUnknown");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Relational");
    temp.add("Enumerations");
    temp.add("NullableType");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private NullableTypeEnum(String literalName) {
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
    if ((o instanceof NullableTypeEnum)) return o == this;
    if ((o instanceof NullableType)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static NullableType forName(String name)
  {
    if (name.equals("columnNoNulls")) return COLUMN_NO_NULLS;
    if (name.equals("columnNullable")) return COLUMN_NULLABLE;
    if (name.equals("columnNullableUnknown")) return COLUMN_NULLABLE_UNKNOWN;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Relational.Enumerations.NullableType'");
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
