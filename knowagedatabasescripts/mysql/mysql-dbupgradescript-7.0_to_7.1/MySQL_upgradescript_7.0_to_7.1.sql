DELETE FROM SBI_ROLE_TYPE_USER_FUNC WHERE USER_FUNCT_ID IN (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME IN ('ProfileAttributeManagement','ProfileManagement')) AND ROLE_TYPE_ID = (SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD='ROLE_TYPE' AND VALUE_CD='DEV_ROLE');

-- 2020/04/10 Alberto Nale
UPDATE SBI_ALERT_LISTENER SET TEMPLATE='angular_1.4/tools/alert/listeners/kpiListener/templates/kpiListener.html' WHERE NAME='KPI Listener';
UPDATE SBI_ALERT_ACTION SET TEMPLATE='angular_1.4/tools/alert/actions/executeETL/templates/executeETL.html' WHERE NAME= 'Execute ETL Document';
UPDATE SBI_ALERT_ACTION SET TEMPLATE='angular_1.4/tools/alert/actions/sendMail/templates/sendMail.html' WHERE NAME= 'Send mail';
UPDATE SBI_ALERT_ACTION SET TEMPLATE='angular_1.4/tools/alert/actions/contextBroker/templates/contextBroker.html' WHERE NAME= 'Context Broker';