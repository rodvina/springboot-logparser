DROP SCHEMA IF EXISTS dbo;
CREATE SCHEMA dbo;

CREATE TABLE dbo.WEB_LOG (
	LOG_DT		datetime NOT NULL,
	IP_ADDR		varchar(30) NOT NULL,
	REQUEST		varchar(30) NOT NULL,
	STATUS		varchar(10) NOT NULL,
	USER_AGENT	varchar(200) NOT NULL

)
;

CREATE TABLE dbo.WEB_LOG_HRLY_100 (
	IP_ADDR		varchar(30) NOT NULL,
	COMMENTS		varchar(200) NOT NULL

)
;

CREATE TABLE dbo.WEB_LOG_DLY_250 (
	IP_ADDR		varchar(30) NOT NULL,
	COMMENTS		varchar(200) NOT NULL

)
;