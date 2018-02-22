select count(1) from information_schema.tables
where table_schema = 'public'
and table_type = 'BASE TABLE'
and table_name = lower( ? )
