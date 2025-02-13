SELECT CONCAT(
               'SELECT "', table_name, '" AS table_name, "', table_schema, '" AS schema_name, COUNT(*) AS row_count FROM ', table_schema, '.', table_name, ';'
       ) as query
FROM information_schema.tables;
