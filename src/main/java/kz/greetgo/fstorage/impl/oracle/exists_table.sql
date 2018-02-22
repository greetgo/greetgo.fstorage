select count(1) from ALL_TABLES
where  table_name = upper( ? )
and owner = sys_context('USERENV','SESSION_SCHEMA')