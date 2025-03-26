SELECT ROUND(SUM(setting::bigint) / 1024.0, 2) AS memory_usage_mb
FROM pg_settings
WHERE name IN ('shared_buffers', 'work_mem', 'maintenance_work_mem', 'temp_buffers');