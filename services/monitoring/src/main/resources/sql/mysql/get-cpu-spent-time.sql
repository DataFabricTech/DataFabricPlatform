SELECT SUM(`TIME`) as cpu_used
FROM information_schema.PROCESSLIST
ORDER BY TIME DESC;