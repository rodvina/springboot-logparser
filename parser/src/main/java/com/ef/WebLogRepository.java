package com.ef;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

@Repository
public class WebLogRepository {

	private static final String INSERT = "INSERT INTO dbo.WEB_LOG(LOG_DT, IP_ADDR, REQUEST, STATUS, USER_AGENT) "
			+ "VALUES (:date, :ip, :request, :status, :userAgent) ";
	
	private static final String SELECT_BY_IP = "SELECT LOG_DT, IP_ADDR, REQUEST, STATUS, USER_AGENT FROM dbo.WEB_LOG WHERE IP_ADDR = :ip";
	
	private static final String SELECT_BY_DATE_AND_THRESHHOLD = "SELECT IP_ADDR IP, count(IP_ADDR) CNT " + 
			"	FROM dbo.WEB_LOG " + 
			"	where LOG_DT between ':startDt' and ':endDt' " + 
			"	group by IP " + 
			"	having count(IP_ADDR) > :threshhold ";
	
	
	
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	/**
	 * Performs a batch insert of the list of records
	 * @param recordList
	 * @return
	 */
	public int[] save(List<WebAccessLogFileRecord> recordList) {
		
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(recordList.toArray());
		int[] updateCounts = jdbcTemplate.batchUpdate(INSERT, batch);
		return updateCounts;
		
	}
	
	public List<WebAccessLogFileRecord> findByIpAddr(String ipAddr) {
		
		MapSqlParameterSource paramSource = new MapSqlParameterSource();
		paramSource.addValue("ip", ipAddr);
		
		return jdbcTemplate.query(SELECT_BY_IP, paramSource, (rs, rowNum) -> {

				WebAccessLogFileRecord record = new WebAccessLogFileRecord();
				record.setDate(rs.getString("LOG_DT"));
				record.setIp(rs.getString("IP_ADDR"));
				record.setRequest(rs.getString("REQUEST"));
				record.setStatus(rs.getString("STATUS"));
				record.setUserAgent(rs.getString("USER_AGENT"));
				return record;
			}
		);
		
//		COMMENT OUT: Replaced anonymous class with lamda expression		
		
//		return jdbcTemplate.query(SELECT_BY_IP, paramSource, new RowMapper<WebAccessLogFileRecord>() {
//
//			@Override
//			public WebAccessLogFileRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
//				WebAccessLogFileRecord record = new WebAccessLogFileRecord();
//				record.setDate(rs.getString("LOG_DT"));
//				record.setIp(rs.getString("IP_ADDR"));
//				record.setRequest(rs.getString("REQUEST"));
//				record.setStatus(rs.getString("STATUS"));
//				record.setUserAgent(rs.getString("USER_AGENT"));
//				
//				return record;
//			}
//		} );
	}
}
