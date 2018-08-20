package com.ef.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.ef.model.LogEntry;

public interface LogEntryRepository extends CrudRepository<LogEntry, Integer> {

  @Query(
    value = "SELECT ip FROM (SELECT ip, COUNT(*) AS count FROM log_entry WHERE date BETWEEN ?1 AND ?2 GROUP BY ip HAVING count > ?3) AS x",
    nativeQuery = true)
  public List<String> findByDateBetween(Date startDate, Date endDate, Integer threshold);

}
