package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class ParameterDirectionKindEnum
  implements ParameterDirectionKind
{
  public static final ParameterDirectionKindEnum PDK_IN = new ParameterDirectionKindEnum("pdk_in");
  


  public static final ParameterDirectionKindEnum PDK_INOUT = new ParameterDirectionKindEnum("pdk_inout");
  


  public static final ParameterDirectionKindEnum PDK_OUT = new ParameterDirectionKindEnum("pdk_out");
  


  public static final ParameterDirectionKindEnum PDK_RETURN = new ParameterDirectionKindEnum("pdk_return");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Behavioral");
    temp.add("ParameterDirectionKind");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private ParameterDirectionKindEnum(String literalName) {
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
    if ((o instanceof ParameterDirectionKindEnum)) return o == this;
    if ((o instanceof ParameterDirectionKind)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static ParameterDirectionKind forName(String name)
  {
    if (name.equals("pdk_in")) return PDK_IN;
    if (name.equals("pdk_inout")) return PDK_INOUT;
    if (name.equals("pdk_out")) return PDK_OUT;
    if (name.equals("pdk_return")) return PDK_RETURN;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Behavioral.ParameterDirectionKind'");
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
