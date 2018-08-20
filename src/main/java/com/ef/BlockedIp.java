package com.ef;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class BlockedIp {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Integer id;
  String ip;
  String comments;

  @Override
  public String toString() {
    return ip + "|" + comments;
  }

}