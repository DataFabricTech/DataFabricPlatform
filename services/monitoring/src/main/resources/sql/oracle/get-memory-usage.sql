SELECT SUM(ROUND(value/1024/1024,2)) AS memory_usage_mb
FROM v$sga