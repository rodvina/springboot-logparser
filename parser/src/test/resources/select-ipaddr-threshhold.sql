
SELECT IP_ADDR IP, count(IP_ADDR) CNT 
	FROM dbo.WEB_LOG
	where LOG_DT between '2017-01-01.15:00:00' and '2017-01-01.16:00:00'
	group by IP
	having count(IP_ADDR) > 200;
