--- START ---
DELETE FROM sbi_role_type_user_func WHERE user_funct_id IN (SELECT user_funct_id FROM sbi_user_func WHERE name IN ('ProfileAttributeManagement','ProfileManagement')) AND role_type_id = (SELECT value_id FROM sbi_domains WHERE DOMAIN_CD='ROLE_TYPE' AND value_cd='DEV_ROLE')
