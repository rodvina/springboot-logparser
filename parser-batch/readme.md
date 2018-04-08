### Prerequisites

1.  Connect to MySql and execute the schema-mysql.sql DDL to create the schema and tables.
2.  Update the application.properties with your MySql username and password

		spring.datasource.username=weblog
		spring.datasource.password=weblog_ro
	
3.  Run the app by passing the command line arguments as VM arguments instead using the -D option:

	-Daccesslog=/path to your log file/access.log -DstartDate=2017-01-01.15:00:00 -Dduration=hourly -Dthreshhold=100
	
4.  The app can be run via gradle wrapper or the java command

	java -Daccesslog=/Users/rodneyodvina/Developer/egit_repo/wallethub-test/parser-batch/src/test/resources/access.log -DstartDate=2017-01-01.15:00:00 -Dduration=hourly -Dthreshhold=100 -jar build/libs/parser.jar
	
5.  If no accesslog arg is specified, the app will skip reading and loading the log file to the db, and just query the db based on the parameters passed

	java -DstartDate=2017-01-01.15:00:00 -Dduration=hourly -Dthreshhold=100 -jar build/libs/parser.jar