package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.datatypes;

import javax.jmi.reflect.RefPackage;

public abstract interface DataTypesPackage
  extends RefPackage
{
  public abstract CwmEnumerationClass getCwmEnumeration();
  
  public abstract CwmEnumerationLiteralClass getCwmEnumerationLiteral();
  
  public abstract CwmQueryExpressionClass getCwmQueryExpression();
  
  public abstract CwmTypeAliasClass getCwmTypeAlias();
  
  public abstract CwmUnionClass getCwmUnion();
  
  public abstract CwmUnionMemberClass getCwmUnionMember();
  
  public abstract ClassifierAlias getClassifierAlias();
  
  public abstract EnumerationLiterals getEnumerationLiterals();
  
  public abstract UnionDiscriminator getUnionDiscriminator();
}
