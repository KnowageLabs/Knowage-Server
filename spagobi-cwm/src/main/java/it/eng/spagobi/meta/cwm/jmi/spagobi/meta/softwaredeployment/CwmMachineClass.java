package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import it.eng.spagobi.meta.cwm.jmi.spagobi.meta.core.VisibilityKind;
import java.util.List;
import javax.jmi.reflect.RefClass;

public abstract interface CwmMachineClass
  extends RefClass
{
  public abstract CwmMachine createCwmMachine();
  
  public abstract CwmMachine createCwmMachine(String paramString1, VisibilityKind paramVisibilityKind, List paramList1, List paramList2, String paramString2);
}
