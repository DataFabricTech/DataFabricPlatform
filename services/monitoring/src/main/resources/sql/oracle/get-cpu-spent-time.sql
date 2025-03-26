SELECT SUM(ss.value) AS cpu_used
FROM v$session s
         JOIN v$sql q ON s.sql_id = q.sql_id
         JOIN v$process p ON s.paddr = p.addr
         JOIN v$sesstat ss ON s.sid = ss.sid
         JOIN v$statname sn ON ss.statistic# = sn.statistic#
ORDER BY cpu_used DESC