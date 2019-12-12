delete

from sbi_role_type_user_func

where user_funct_id in (select user_funct_id from sbi_user_func where  name in ('ProfileAttributeManagement','ProfileManagement'))

and role_type_id = (select value_id from sbi_domains where  DOMAIN_CD='ROLE_TYPE' and value_cd='DEV_ROLE')