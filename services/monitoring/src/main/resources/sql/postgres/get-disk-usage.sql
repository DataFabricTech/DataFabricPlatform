SELECT ROUND(SUM(pg_database_size(datname)) / 1024 / 1024, 2) AS total_db_size_mb
FROM pg_database;