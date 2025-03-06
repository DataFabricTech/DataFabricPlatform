SELECT relname AS table_name,
       n_tup_ins AS inserts,
       n_tup_upd AS updates,
       n_tup_del AS deletes
FROM pg_stat_all_tables