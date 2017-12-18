package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class VisibilityKindEnum
  implements VisibilityKind
{
  public static final VisibilityKindEnum VK_PUBLIC = new VisibilityKindEnum("vk_public");
  


  public static final VisibilityKindEnum VK_PROTECTED = new VisibilityKindEnum("vk_protected");
  


  public static final VisibilityKindEnum VK_PRIVATE = new VisibilityKindEnum("vk_private");
  


  public static final VisibilityKindEnum VK_PACKAGE = new VisibilityKindEnum("vk_package");
  


  public static final VisibilityKindEnum VK_NOTAPPLICABLE = new VisibilityKindEnum("vk_notapplicable");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Core");
    temp.add("VisibilityKind");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private VisibilityKindEnum(String literalName) {
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
    if ((o instanceof VisibilityKindEnum)) return o == this;
    if ((o instanceof VisibilityKind)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static VisibilityKind forName(String name)
  {
    if (name.equals("vk_public")) return VK_PUBLIC;
    if (name.equals("vk_protected")) return VK_PROTECTED;
    if (name.equals("vk_private")) return VK_PRIVATE;
    if (name.equals("vk_package")) return VK_PACKAGE;
    if (name.equals("vk_notapplicable")) return VK_NOTAPPLICABLE;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Core.VisibilityKind'");
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
