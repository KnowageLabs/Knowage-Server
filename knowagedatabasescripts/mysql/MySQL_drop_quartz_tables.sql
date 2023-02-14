#
# In your Quartz properties file, you''ll need to set 
# org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.StdJDBCDelegate
#
#
# By: Ron Cordell - roncordell
#  I didn''t see this anywhere, so I thought I''d post it here. 
#  This is the script from Quartz to create the tables in a MySQL database, modified to use INNODB instead of MYISAM.
#

DROP TABLE QRTZ_LOCKS;
DROP TABLE QRTZ_SCHEDULER_STATE;
DROP TABLE QRTZ_FIRED_TRIGGERS;
DROP TABLE QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE QRTZ_CALENDARS;
DROP TABLE QRTZ_BLOB_TRIGGERS;
DROP TABLE QRTZ_SIMPROP_TRIGGERS;
DROP TABLE QRTZ_CRON_TRIGGERS;
DROP TABLE QRTZ_SIMPLE_TRIGGERS;
DROP TABLE QRTZ_TRIGGERS;
DROP TABLE QRTZ_JOB_DETAILS;

COMMIT; 
