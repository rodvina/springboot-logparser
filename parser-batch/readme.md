### Prerequisites

1.  Connect to MySql and execute the schema-mysql.sql DDL to create the schema and tables.
2.  Update the ./config/credential.properties with your MySql username and password

		spring.datasource.username=weblog
		spring.datasource.password=weblog_ro
	
3.  Run the app by passing the command line arguments as VM arguments instead using the -D option:

		-Daccesslog=/path to your log file/access.log -DstartDate=2017-01-01.15:00:00 -Dduration=hourly -Dthreshhold=100
	
4.  The app can be run via gradle wrapper or the java command.  You'll have to set the spring.config.location so it will pick up the external configuration for the credentials

		java -Daccesslog="file:///Users/rodneyodvina/Developer/egit_repo/wallethub-test/parser-batch/dist/access.log" -DstartDate=2017-01-01.15:00:00 -Dduration=hourly -Dthreshhold=100 -Dspring.config.location=classpath:/application.properties,./config/credentials.properties -jar ./parser.jar
	
5.  If no accesslog arg is specified, the app will skip reading and loading the log file to the db, and just query the db based on the parameters passed

		java -DstartDate=2017-01-01.15:00:00 -Dduration=hourly -Dthreshhold=100 -jar build/libs/parser.jar
	
NOTE: source for this app can be found in the parser-source.jar or on [https://github.com/rodvina/springboot-logparser/tree/master/parser-batch](https://github.com/rodvina/springboot-logparser/tree/master/parser-batch)
	
	