SELECT
    GREATEST(SUM(EXTRACT(EPOCH FROM (now() - query_start))), 0) AS cpuUsed
FROM pg_stat_activity
WHERE state = 'active';