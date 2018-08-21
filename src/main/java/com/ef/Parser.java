package com.ef;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.ef.repository.BlockedIpRepository;
import com.ef.repository.LogEntryRepository;

@SpringBootApplication
public class Parser {

  private static final Logger log = LoggerFactory.getLogger(Parser.class);

  public static void main(String[] args) {
    SpringApplication.run(Parser.class, args);
  }

  @Bean
  public CommandLineRunner run(LogEntryRepository leRepository, BlockedIpRepository biRepository) throws Exception {
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

      String accessLogFileName = commandLine.getOptionValue(Constants.ACCESS_LOG);
      String startDateString = commandLine.getOptionValue(Constants.START_DATE);
      String duration = commandLine.getOptionValue(Constants.DURATION);
      String thresholdString = commandLine.getOptionValue(Constants.THRESHOLD);
      
      validateParams(startDateString, duration, thresholdString);

      int threshold = Integer.valueOf(thresholdString);
      DateFormat format = new SimpleDateFormat(Constants.CL_DATE_FORMAT);
      Date startDate = format.parse(startDateString);
      
      ParserService parserService = new ParserService(leRepository, biRepository);
      try {
        if (accessLogFileName != null && !accessLogFileName.isEmpty()) {
          parserService.loadFileToDb(accessLogFileName);
        }
        List<String> logEntries = parserService.findMatches(startDate, duration, threshold);
        log.debug("Found " + logEntries.size() + " matches");
        parserService.storeEntries(startDateString, duration, threshold, logEntries);
      } catch (FileNotFoundException e) {
        log.error("Exception with file: " + accessLogFileName, e);
        throw e;
      } catch (IOException e) {
        log.error("Exception with file: " + accessLogFileName, e);
        throw e;
      }
    };
  }

  private static Options generateCLOptions() {
    final Option accesslogOption = Option.builder().required(false).hasArg().longOpt(Constants.ACCESS_LOG)
        .desc("/path/to/file").build();
    final Option startDateOption = Option.builder().required().hasArg().longOpt(Constants.START_DATE)
        .desc("2017-01-01.13:00:00").build();
    final Option durationOption = Option.builder().required().hasArg().longOpt(Constants.DURATION).desc("hourly")
        .build();
    final Option thresholdOption = Option.builder().required().hasArg().longOpt(Constants.THRESHOLD).desc("100")
        .build();

    final Options options = new Options();
    options.addOption(accesslogOption);
    options.addOption(startDateOption);
    options.addOption(durationOption);
    options.addOption(thresholdOption);
    return options;
  }

  private static void validateParams(String startDate, String duration, String threshold) throws Exception {
    DateFormat format = new SimpleDateFormat(Constants.CL_DATE_FORMAT);
    format.parse(startDate);

    if (!duration.equalsIgnoreCase(Constants.DURATION_DAILY) && !duration.equalsIgnoreCase(Constants.DURATION_HOURLY)) {
    	throw new Exception("Invalid duration, valid durations are 'hourly' or 'daily', provided duration is: " + duration);
    }
    
    Integer.valueOf(threshold);
  }

}
