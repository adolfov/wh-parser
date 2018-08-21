package com.ef;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.ef.model.LogEntry;
import com.ef.repository.BlockedIpRepository;
import com.ef.repository.LogEntryRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@DataJpaTest
public class ParserServiceTest {

  @TestConfiguration
  static class EmployeeServiceImplTestContextConfiguration {
    @Autowired
    private LogEntryRepository leRepository;
    @Autowired
    private BlockedIpRepository biRepository;

    @Bean
    public ParserService parserService() {
      return new ParserService(this.leRepository, this.biRepository);
    }
  }

  @Autowired
  private ParserService parserService;
  @Autowired
  private LogEntryRepository leRepository;

  @Before
  public void init() {
  }

  @Test
  public void testLoadFileToDb() {
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      File file = new File(classLoader.getResource("access-small.log").getFile());
      this.parserService.loadFileToDb(file.getAbsolutePath());
      long recordCount = this.leRepository.count();
      assertEquals(5, recordCount);
    } catch (IOException e) {
      fail("LoadFileToDb failed " + e.getMessage());
    }
  }

  @Test(expected = FileNotFoundException.class)
  public void testLoadFileToDbInvalidFile() throws FileNotFoundException, IOException {
    this.parserService.loadFileToDb("access-small0123123.log");
  }

  @Test
  public void testFindMatches() {
    Date today = Calendar.getInstance().getTime();

    LogEntry logEntry1 = new LogEntry(today, "127.0.0.1", "GET", 200, "Safari");
    LogEntry logEntry2 = new LogEntry(today, "127.0.0.1", "GET", 200, "Safari");
    LogEntry logEntry3 = new LogEntry(today, "127.0.0.1", "GET", 200, "Safari");
    LogEntry logEntry4 = new LogEntry(today, "127.0.0.2", "GET", 200, "Safari");
    LogEntry logEntry5 = new LogEntry(today, "127.0.0.3", "GET", 200, "Safari");

    List<LogEntry> entries = new ArrayList<LogEntry>();
    entries.add(logEntry1);
    entries.add(logEntry2);
    entries.add(logEntry3);
    entries.add(logEntry4);
    entries.add(logEntry5);
    this.leRepository.saveAll(entries);

    List<String> ips = this.parserService.findMatches(today, Constants.DURATION_HOURLY, 2);
    assertEquals(1, ips.size());
  }

}