package com.ef;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ef.model.BlockedIp;
import com.ef.model.LogEntry;
import com.ef.repository.BlockedIpRepository;
import com.ef.repository.LogEntryRepository;

@Component
public class ParserService {

	private LogEntryRepository leRepository;
	private BlockedIpRepository biRepository;

	private static final Logger log = LoggerFactory.getLogger(ParserService.class);

	public ParserService(LogEntryRepository leRepository, BlockedIpRepository biRepository) {
		this.leRepository = leRepository;
		this.biRepository = biRepository;
	}

	public void loadFileToDb(String fileName) throws FileNotFoundException, IOException {
		log.debug("Started loading file: " + fileName);
		Reader reader = new FileReader(fileName);
		CSVFormat format = CSVFormat.DEFAULT.withDelimiter(Constants.DELIMITER);
		Iterable<CSVRecord> records = format.parse(reader);
		final AtomicInteger loader = new AtomicInteger();

		StreamSupport.stream(records.spliterator(), false).peek(record -> {
			if (loader.incrementAndGet() % 5000 == 0) {
				log.debug("Records processed so far: " + loader.get() + "...");
			}
		}).forEach(record -> storeLogEntry(parseLogEntry(record)));
		log.debug("Finished loading file: " + fileName);
	}

	public List<String> findMatches(Date startDate, String duration, int threshold) {
		log.debug("Finding matches for startDate: " + startDate + ", duration: " + duration + ", threshold: " + threshold);
		List<String> logEntries = new ArrayList<String>();
		Date endDate = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);

		if (duration.equalsIgnoreCase(Constants.DURATION_HOURLY)) {
			calendar.add(Calendar.HOUR_OF_DAY, 1);
		} else if (duration.equalsIgnoreCase(Constants.DURATION_DAILY)) {
			calendar.add(Calendar.DATE, 1);
		}
		endDate = calendar.getTime();
		log.debug("Finding matches between startDate: " + startDate + ", endDate: " + endDate);
		logEntries = this.leRepository.findByDateBetween(startDate, endDate, threshold);

		return logEntries;
	}

	public void storeEntries(String startDate, String duration, int threshold, List<String> logEntries) {
		logEntries.forEach(ip -> {
			log.debug(ip);
			BlockedIp blockedIp = new BlockedIp();
			blockedIp.setIp(ip);
			blockedIp.setComments(String.format("Threshold exceeded. Start date=%s, Duration=%s, Threshold=%d", startDate,
					duration, threshold));
			storeBlockedIp(blockedIp);
		});
	}

	private void storeLogEntry(LogEntry logEntry) {
		this.leRepository.save(logEntry);
	}

	private void storeBlockedIp(BlockedIp blockedIp) {
		this.biRepository.save(blockedIp);
	}

	private LogEntry parseLogEntry(CSVRecord record) {
		LogEntry logEntry = new LogEntry(parseDate(record.get(0)), record.get(1), record.get(2),
				Integer.valueOf(record.get(3)), record.get(4));
		return logEntry;
	}

	private Date parseDate(String dateString) {
		Date date = null;
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.LOG_DATE_FORMAT);
		try {
			date = sdf.parse(dateString);
		} catch (Exception e) {
			log.warn("Unable to parse date: " + dateString);
		}
		return date;
	}

}
