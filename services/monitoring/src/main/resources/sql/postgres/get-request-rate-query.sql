SELECT
    SUM(xact_commit) AS success_queries,
    SUM(xact_rollback) AS failed_queries
FROM pg_stat_database;