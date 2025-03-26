SELECT
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS total_db_size_mb
FROM information_schema.tables;