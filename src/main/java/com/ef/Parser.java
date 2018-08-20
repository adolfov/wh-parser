package com.ef;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Parser {

  private static final String ACCESS_LOG = "accesslog";
  private static final String START_DATE = "startDate";
  private static final String DURATION = "duration";
  private static final String THRESHOLD = "threshold";
  private static final String DURATION_HOURLY = "hourly";
  private static final String DURATION_DAILY = "daily";
  private static final String LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
  private static final String CL_DATE_FORMAT = "yyyy-MM-dd.HH:mm:ss";
  private static final char DELIMITER = '|';

  private LogEntryRepository leRepository;

  private static final Logger log = LoggerFactory.getLogger(Parser.class);

   public Parser(LogEntryRepository repository) {
     this.leRepository = repository;
  }

  public static void main(String[] args) {
    SpringApplication.run(Parser.class, args);
  }

  @Bean
  public CommandLineRunner run(LogEntryRepository repository) throws Exception {
    return (args) -> {
      Options options = generateCLOptions();

      CommandLineParser clParser = new DefaultParser();
      CommandLine commandLine;
      try {
        commandLine = clParser.parse(options, args);
      } catch (ParseException parseException) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Parser", options);
        return;
      }

      String accessLogFileName = commandLine.getOptionValue(ACCESS_LOG);
      String startDateString = commandLine.getOptionValue(START_DATE);
      String duration = commandLine.getOptionValue(DURATION);
      int threshold = Integer.valueOf(commandLine.getOptionValue(THRESHOLD));
      DateFormat format = new SimpleDateFormat(CL_DATE_FORMAT);
      Date startDate = format.parse(startDateString);
      Parser parser = new Parser(repository);
      try {
        parser.loadFileToDb(accessLogFileName);
        List<String> logEntries = parser.findMatches(startDate, duration, threshold);
        log.debug("Found " + logEntries.size() + " matches");
        logEntries.forEach(logEntry -> log.debug(logEntry));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    };
  }

  public List<String> findMatches(Date startDate, String duration, int threshold) {
    log.debug("Finding matches for startDate: " + startDate + ", duration: " + duration + ", threshold: " + threshold);
    List<String> logEntries = new ArrayList<String>();
    Date endDate = null;
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    
    if (duration.equalsIgnoreCase(DURATION_HOURLY)) {
      calendar.add(Calendar.HOUR_OF_DAY, 1);
    } else if (duration.equalsIgnoreCase(DURATION_DAILY)) {
      calendar.add(Calendar.DATE, 1);
    }
    endDate = calendar.getTime();
    log.debug("Finding matches between startDate: " + startDate + ", endDate: " + endDate);
    logEntries = this.leRepository.findByDateBetween(startDate, endDate, threshold);

    return logEntries;
  }

  public void loadFileToDb(String fileName) throws IOException {
	  log.debug("Started loading file: " + fileName);
    Reader reader = new FileReader(fileName);
    CSVFormat format = CSVFormat.DEFAULT.withDelimiter(DELIMITER);
    Iterable<CSVRecord> records = format.parse(reader);
    for (CSVRecord record : records) {
      LogEntry logEntry = parseLogEntry(record);
      storeLogEntry(logEntry);
    }
    log.debug("Finished loading file: " + fileName);
  }

  private void storeLogEntry(LogEntry logEntry) {
    this.leRepository.save(logEntry);
  }

  private LogEntry parseLogEntry(CSVRecord record) {
    LogEntry logEntry = new LogEntry();
    logEntry.date = parseDate(record.get(0));
    logEntry.ip = record.get(1);
    logEntry.request = record.get(2);
    logEntry.status = Integer.valueOf(record.get(3));
    logEntry.userAgent = record.get(4);
    return logEntry;
  }

  private Date parseDate(String dateString) {
    Date date = null;
    SimpleDateFormat sdf = new SimpleDateFormat(LOG_DATE_FORMAT);
    try {
      date = sdf.parse(dateString);
    } catch (Exception e) {
      // swallow it
    }
    return date;
  }

  private static Options generateCLOptions() {
    final Option accesslogOption = Option.builder().required().hasArg().longOpt(ACCESS_LOG).desc("/path/to/file")
        .build();
    final Option startDateOption = Option.builder().required().hasArg().longOpt(START_DATE).desc("2017-01-01.13:00:00")
        .build();
    final Option durationOption = Option.builder().required().hasArg().longOpt(DURATION).desc("hourly").build();
    final Option thresholdOption = Option.builder().required().hasArg().longOpt(THRESHOLD).desc("100").build();

    final Options options = new Options();
    options.addOption(accesslogOption);
    options.addOption(startDateOption);
    options.addOption(durationOption);
    options.addOption(thresholdOption);
    return options;
  }

}
