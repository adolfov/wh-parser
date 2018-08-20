package com.ef.repository;

import org.springframework.data.repository.CrudRepository;

import com.ef.model.BlockedIp;

public interface BlockedIpRepository extends CrudRepository<BlockedIp, Integer> {

}
