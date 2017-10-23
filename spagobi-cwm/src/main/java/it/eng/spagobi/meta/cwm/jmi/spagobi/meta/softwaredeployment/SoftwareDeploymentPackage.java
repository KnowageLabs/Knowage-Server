package it.eng.spagobi.meta.cwm.jmi.spagobi.meta.softwaredeployment;

import javax.jmi.reflect.RefPackage;

public abstract interface SoftwareDeploymentPackage
  extends RefPackage
{
  public abstract CwmSiteClass getCwmSite();
  
  public abstract CwmMachineClass getCwmMachine();
  
  public abstract CwmSoftwareSystemClass getCwmSoftwareSystem();
  
  public abstract CwmDeployedComponentClass getCwmDeployedComponent();
  
  public abstract CwmDeployedSoftwareSystemClass getCwmDeployedSoftwareSystem();
  
  public abstract CwmDataManagerClass getCwmDataManager();
  
  public abstract CwmDataProviderClass getCwmDataProvider();
  
  public abstract CwmProviderConnectionClass getCwmProviderConnection();
  
  public abstract CwmComponentClass getCwmComponent();
  
  public abstract CwmPackageUsageClass getCwmPackageUsage();
  
  public abstract RelatedSites getRelatedSites();
  
  public abstract ComponentsOnMachine getComponentsOnMachine();
  
  public abstract SiteMachines getSiteMachines();
  
  public abstract DataProviderConnections getDataProviderConnections();
  
  public abstract DataManagerConnections getDataManagerConnections();
  
  public abstract SoftwareSystemDeployments getSoftwareSystemDeployments();
  
  public abstract DataManagerDataPackage getDataManagerDataPackage();
  
  public abstract DeployedSoftwareSystemComponents getDeployedSoftwareSystemComponents();
  
  public abstract ComponentDeployments getComponentDeployments();
  
  public abstract SystemTypespace getSystemTypespace();
  
  public abstract ComponentDesign getComponentDesign();
  
  public abstract DeployedComponentUsage getDeployedComponentUsage();
}
