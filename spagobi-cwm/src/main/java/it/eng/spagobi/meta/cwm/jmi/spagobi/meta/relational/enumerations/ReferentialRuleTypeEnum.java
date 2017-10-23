package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.relational.enumerations;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.jmi.reflect.RefEnum;














public final class ReferentialRuleTypeEnum
  implements ReferentialRuleType
{
  public static final ReferentialRuleTypeEnum IMPORTED_KEY_NO_ACTION = new ReferentialRuleTypeEnum("importedKeyNoAction");
  


  public static final ReferentialRuleTypeEnum IMPORTED_KEY_CASCADE = new ReferentialRuleTypeEnum("importedKeyCascade");
  


  public static final ReferentialRuleTypeEnum IMPORTED_KEY_SET_NULL = new ReferentialRuleTypeEnum("importedKeySetNull");
  


  public static final ReferentialRuleTypeEnum IMPORTED_KEY_RESTRICT = new ReferentialRuleTypeEnum("importedKeyRestrict");
  


  public static final ReferentialRuleTypeEnum IMPORTED_KEY_SET_DEFAULT = new ReferentialRuleTypeEnum("importedKeySetDefault");
  private static final List typeName;
  private final String literalName;
  
  static
  {
    ArrayList temp = new ArrayList();
    temp.add("Pentaho");
    temp.add("Meta");
    temp.add("Relational");
    temp.add("Enumerations");
    temp.add("ReferentialRuleType");
    typeName = Collections.unmodifiableList(temp);
  }
  
  private ReferentialRuleTypeEnum(String literalName) {
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
    if ((o instanceof ReferentialRuleTypeEnum)) return o == this;
    if ((o instanceof ReferentialRuleType)) return o.toString().equals(literalName);
    return ((o instanceof RefEnum)) && (((RefEnum)o).refTypeName().equals(typeName)) && (o.toString().equals(literalName));
  }
  




  public static ReferentialRuleType forName(String name)
  {
    if (name.equals("importedKeyNoAction")) return IMPORTED_KEY_NO_ACTION;
    if (name.equals("importedKeyCascade")) return IMPORTED_KEY_CASCADE;
    if (name.equals("importedKeySetNull")) return IMPORTED_KEY_SET_NULL;
    if (name.equals("importedKeyRestrict")) return IMPORTED_KEY_RESTRICT;
    if (name.equals("importedKeySetDefault")) return IMPORTED_KEY_SET_DEFAULT;
    throw new IllegalArgumentException("Unknown literal name '" + name + "' for enumeration 'Pentaho.Meta.Relational.Enumerations.ReferentialRuleType'");
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
