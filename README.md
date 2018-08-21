# AccessLog Analyzer Tool

## Introduction

The tool parses web server access log files, loads the requests to a MySQL db and checks if a given IP makes more than a certain number of requests for the given duration. 

The tool takes 4 parameters:
- --accesslog (optional) : if present, the tool will parse the file and load all the requests in it to the db
- --startDate (required): indicates the starting date and time to look for requests
- --duration (required): possible values are "hourly" and "daily"
    - hourly: use date range of 'startDate' and 'startDate + 1 hour' to search for requests 
    - daily: use date range of 'startDate' and 'startDate + 1 day' to search for requests 
- --threshold (required): indicates the number of requests from a single IP  

## How to build and run

1. __Setup db schema and user__: run '/src/main/resources/mysql-init.sql' script on local mysql 
  * Datasource properties configurable at 'application.properties'
2. ```mvn install```
3. ```mvn spring-boot:run -Dspring-boot.run.arguments=--accesslog=/path/to/accessLog.log--startDate=2017-01-01.01:00:00,--duration=daily,--threshold=500```


## How it works

```
mvn spring-boot:run -Dspring-boot.run.arguments=--startDate=2017-01-01.13:00:00,--duration=hourly,--threshold=100
```

> The tool will find any IPs that made more than 100 requests starting from 2017-01-01.13:00:00 to 2017-01-01.14:00:00 (one hour) and print them to console AND also load them to another MySQL table with comments on why it's blocked.

```
mvn spring-boot:run -Dspring-boot.run.arguments=--startDate=2017-01-01.13:00:00,--duration=daily,--threshold=250
```

> The tool will find any IPs that made more than 250 requests starting from 2017-01-01.13:00:00 to 2017-01-02.13:00:00 (24 hours) and print them to console AND also load them to another MySQL table with comments on why it's blocked.

## Access log format

Format:
> Date, IP, Request, Status, User Agent (pipe delimited, open the example file in text editor)

Date Format:
> "yyyy-MM-dd HH:mm:ss.SSS"

Sample content
```
2018-01-01 00:00:11.763|193.168.234.82|"GET / HTTP/1.1"|200|"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0"
2018-01-01 00:00:21.164|193.168.234.82|"GET / HTTP/1.1"|200|"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0"
2018-01-01 00:00:23.003|193.168.169.194|"GET / HTTP/1.1"|200|"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393"
2018-01-01 00:00:40.554|193.168.234.82|"GET / HTTP/1.1"|200|"swcd (unknown version) CFNetwork/808.2.16 Darwin/15.6.0"
2018-01-01 00:00:54.583|193.168.169.194|"GET / HTTP/1.1"|200|"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393"
```
