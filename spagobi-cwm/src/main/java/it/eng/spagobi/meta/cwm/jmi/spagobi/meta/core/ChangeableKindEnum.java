package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class ChangeableKindEnum
  implements ChangeableKind
{
  public static final ChangeableKindEnum CK_CHANGEABLE = new ChangeableKindEnum("ck_changeable");
  


  public static final ChangeableKindEnum CK_FROZEN = new ChangeableKindEnum("ck_frozen");
  


  public static final ChangeableKindEnum CK_ADD_ONLY = new ChangeableKindEnum("ck_addOnly");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Core");
    temp.add("ChangeableKind");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private ChangeableKindEnum(String literalName) {
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
    if ((o instanceof ChangeableKindEnum)) return o == this;
    if ((o instanceof ChangeableKind)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static ChangeableKind forName(String name)
  {
    if (name.equals("ck_changeable")) return CK_CHANGEABLE;
    if (name.equals("ck_frozen")) return CK_FROZEN;
    if (name.equals("ck_addOnly")) return CK_ADD_ONLY;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Core.ChangeableKind'");
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
