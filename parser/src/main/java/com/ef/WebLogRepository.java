package com.ef;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WebLogRepository {

	private static final String INSERT = "INSERT INTO dbo.WEB_LOG(LOG_DT, IP_ADDR, REQUEST, STATUS, USER_AGENT) "
			+ "VALUES (:logDt, :ip, :request, :status, :userAgent) ";
	
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	public void save(WebAccessLogFileRecord record) {
		
		MapSqlParameterSource paramMap = new MapSqlParameterSource();
		paramMap.addValue("logDt", record.getDate());
		paramMap.addValue("ip", record.getIp());
		paramMap.addValue("request", record.getRequest());
		paramMap.addValue("status", record.getStatus());
		paramMap.addValue("userAgent", record.getUserAgent());
		
		jdbcTemplate.update(INSERT, paramMap);
		
	}
}
