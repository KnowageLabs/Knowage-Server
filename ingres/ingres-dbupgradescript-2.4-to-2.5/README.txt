This is an sql file to use to upgrade your SpagoBI 2.4.0 INGRES database to a SpagoBI 2.5.0 INGRES database.

Execute this script in your INGRES query browser.  

Pay attention to CREATION_DATE and LAST_CHANGE_DATE : they should have timestamp value as default, but Ingres can goes in exception when it tries to do this. Make this setting manually.
Pay attention to SBI_EXT_ROLES.SAVE_METADATA  : it should has 1 value as default, but Ingres can goes in exception when it tries to do this. Make this setting manually.