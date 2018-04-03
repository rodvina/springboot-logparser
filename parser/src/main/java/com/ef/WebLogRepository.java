package com.ef;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

@Repository
public class WebLogRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebLogRepository.class);
	
	private static final String INSERT = "INSERT INTO dbo.WEB_LOG (LOG_DT, IP_ADDR, REQUEST, STATUS, USER_AGENT) "
			+ "VALUES (:date, :ip, :request, :status, :userAgent) ";
	private static final String INSERT_HOURLY_100 = "INSERT INTO dbo.WEB_LOG_HRLY_100 (IP_ADDR, COUNT, COMMENTS) "
			+ "VALUES (:ip, :count, :comments) ";	
	private static final String INSERT_DAILY_250 = "INSERT INTO dbo.WEB_LOG_DLY_250 (IP_ADDR, COUNT, COMMENTS) "
			+ "VALUES (:ip, :count, :comments) ";	
	private static final String SELECT_BY_IP = "SELECT LOG_DT, IP_ADDR, REQUEST, STATUS, USER_AGENT FROM dbo.WEB_LOG WHERE IP_ADDR = :ip";
	
	private static final String SELECT_BY_DATE_AND_THRESHHOLD = "SELECT IP_ADDR, count(IP_ADDR) CNT " + 
			"	FROM dbo.WEB_LOG " + 
			"	where LOG_DT between :startDt and :endDt " + 
			"	group by IP_ADDR " + 
			"	having count(IP_ADDR) >= :threshhold ";
	
	
	
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
	
	public int[] saveToHourly100(List<IpAddrCountRecord> recordList) {

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(recordList.toArray());
		int[] updateCounts = jdbcTemplate.batchUpdate(INSERT_HOURLY_100, batch);
		return updateCounts;
		
	}
	
	public int[] saveToDaily250(List<IpAddrCountRecord> recordList) {

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(recordList.toArray());
		int[] updateCounts = jdbcTemplate.batchUpdate(INSERT_DAILY_250, batch);
		return updateCounts;
		
	}
	
	public List<IpAddrCountRecord> findIPByDateAndThreshhold(LocalDateTime start, LocalDateTime end, int threshhold) {
		MapSqlParameterSource paramSource = new MapSqlParameterSource();
		paramSource.addValue("startDt", start);
		paramSource.addValue("endDt", end);
		paramSource.addValue("threshhold", threshhold);
		
		LOGGER.info("sql="+SELECT_BY_DATE_AND_THRESHHOLD);
		
		LOGGER.info("params="+paramSource.getValues());
		
		return jdbcTemplate.query(SELECT_BY_DATE_AND_THRESHHOLD, paramSource, (rs, rowNum) -> {
				IpAddrCountRecord record = new IpAddrCountRecord();
				record.setIp(rs.getString("IP_ADDR"));
				record.setCount(rs.getInt("CNT"));
				
				return record;
			}
		);
		
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
