package com.ef;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Parser {

  public static void main(String[] args) {

    Options options = generateCLOptions();

    CommandLineParser clParser = new DefaultParser();
    try {
      clParser.parse(options, args);
    } catch (ParseException parseException) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("Parser", options);
    }

  }

  private static Options generateCLOptions() {
    final Option accesslogOption = Option.builder().required().hasArg().longOpt("accesslog").desc("/path/to/file").build();
    final Option startDateOption = Option.builder().required().hasArg().longOpt("startDate").desc("2017-01-01.13:00:00").build();
    final Option durationOption = Option.builder().required().hasArg().longOpt("duration").desc("hourly").build();
    final Option thresholdOption = Option.builder().required().hasArg().longOpt("threshold").desc("100").build();

    final Options options = new Options();
    options.addOption(accesslogOption);
    options.addOption(startDateOption);
    options.addOption(durationOption);
    options.addOption(thresholdOption);
    return options;
  }

}
