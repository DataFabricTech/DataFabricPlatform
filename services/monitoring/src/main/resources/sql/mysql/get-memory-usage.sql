SELECT
    ROUND(SUM(VARIABLE_VALUE) * 16 / 1024, 2) AS memory_usage_mb
FROM performance_schema.global_status
WHERE VARIABLE_NAME IN (
                        'Innodb_buffer_pool_pages_total',
                        'Innodb_buffer_pool_pages_free',
                        'Innodb_buffer_pool_pages_dirty'
    );