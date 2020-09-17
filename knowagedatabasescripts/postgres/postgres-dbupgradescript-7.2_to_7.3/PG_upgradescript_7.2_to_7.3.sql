--- START ---
-- 2020-09-11 Adding Portuguese language
update SBI_CONFIG set VALUE_CHECK = CONCAT(VALUE_CHECK, ',[pt,BR]') WHERE LABEL = 'SPAGOBI.LANGUAGE_SUPPORTED.LANGUAGES' and VALUE_CHECK not like '%[pt,BR]%';
commit;
