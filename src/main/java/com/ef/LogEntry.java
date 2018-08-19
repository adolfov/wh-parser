package com.ef;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class LogEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Integer id;
  Date date;
  String ip;
  String request;
  int status;
  String userAgent;

  @Override
  public String toString() {
    return date + "|" + ip + "|" + request + "|" + status + "|" + userAgent;
  }

}