package com.ef;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;

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
  private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
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
      Parser parser = new Parser(repository);
      try {
        parser.loadFileToDb(accessLogFileName);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    };
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
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
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
