-- (1) Write MySQL query to find IPs that mode more than a certain number of requests for a given time period.

-- Get all ips that have more than 50 requests between '2017-01-01 10:00:00' and '2017-01-01 11:00:00'
-- this is equivalent to startDate = '2017-01-01 10:00:00 and duration = 'hourly'
SELECT ip, COUNT(*) AS count 
FROM wh_parser.log_entry 
WHERE date BETWEEN STR_TO_DATE('2017-01-01 10:00:00', '%Y-%m-%d %H:%i:%s') AND STR_TO_DATE('2017-01-01 11:00:00', '%Y-%m-%d %H:%i:%s')
GROUP BY ip HAVING (count > 50);

-- (2) Write MySQL query to find requests made by a given IP.
SELECT *
FROM wh_parser.log_entry 
WHERE ip LIKE '192.168.164.146';