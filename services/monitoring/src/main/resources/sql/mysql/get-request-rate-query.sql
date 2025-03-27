SELECT
    SUM(SUM_ERRORS) AS failed_queries,
    SUM(COUNT_STAR) - SUM(SUM_ERRORS) AS success_queries
FROM performance_schema.events_statements_summary_by_digest;