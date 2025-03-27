SELECT
    SUM(CASE WHEN name = 'user rollbacks' THEN value END) AS failed_queries,
    SUM(CASE WHEN name IN ('user commits', 'user calls') THEN value END) AS success_queries
FROM v$sysstat