UPDATE SBI_CONFIG SET CATEGORY = 'PASSWORD' WHERE LABEL IN ('MAIL.PROFILES.keyStore.password','MAIL.PROFILES.kpi_alarm.password','MAIL.PROFILES.scheduler.password','MAIL.PROFILES.user.password','MAIL.PROFILES.trustedStore.password');

DELETE FROM SBI_CONFIG WHERE LABEL = 'internal.security.encript.password';