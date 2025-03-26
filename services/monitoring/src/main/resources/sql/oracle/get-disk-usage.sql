SELECT ROUND(SUM(bytes) / 1024 / 1024, 2) AS total_db_size_mb
FROM dba_data_files