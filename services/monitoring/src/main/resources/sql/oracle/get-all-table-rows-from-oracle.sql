SELECT 'SELECT ''' || table_name || ''' AS table_name, '''
           || owner || ''' AS schema_name, COUNT(*) AS row_count FROM '
           || owner || '.' || table_name AS query
FROM all_tables