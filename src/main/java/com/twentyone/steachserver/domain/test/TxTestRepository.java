package com.twentyone.steachserver.domain.test;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TxTestRepository extends MongoRepository<TxTestEntity, String> {
}
