package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.behavioral;

import javax.jmi.reflect.RefPackage;

public abstract interface BehavioralPackage
  extends RefPackage
{
  public abstract CwmArgumentClass getCwmArgument();
  
  public abstract CwmBehavioralFeatureClass getCwmBehavioralFeature();
  
  public abstract CwmCallActionClass getCwmCallAction();
  
  public abstract CwmEventClass getCwmEvent();
  
  public abstract CwmInterfaceClass getCwmInterface();
  
  public abstract CwmMethodClass getCwmMethod();
  
  public abstract CwmOperationClass getCwmOperation();
  
  public abstract CwmParameterClass getCwmParameter();
  
  public abstract BehavioralFeatureParameter getBehavioralFeatureParameter();
  
  public abstract CallArguments getCallArguments();
  
  public abstract EventParameter getEventParameter();
  
  public abstract CalledOperation getCalledOperation();
  
  public abstract OperationMethod getOperationMethod();
  
  public abstract ParameterType getParameterType();
}
