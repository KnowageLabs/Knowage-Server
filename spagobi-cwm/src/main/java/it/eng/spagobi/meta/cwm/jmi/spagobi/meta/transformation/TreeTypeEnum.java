package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.transformation;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class TreeTypeEnum
  implements TreeType
{
  public static final TreeTypeEnum TFM_UNARY = new TreeTypeEnum("tfm_unary");
  


  public static final TreeTypeEnum TFM_BINARY = new TreeTypeEnum("tfm_binary");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Transformation");
    temp.add("TreeType");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private TreeTypeEnum(String literalName) {
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
    if ((o instanceof TreeTypeEnum)) return o == this;
    if ((o instanceof TreeType)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static TreeType forName(String name)
  {
    if (name.equals("tfm_unary")) return TFM_UNARY;
    if (name.equals("tfm_binary")) return TFM_BINARY;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Transformation.TreeType'");
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
