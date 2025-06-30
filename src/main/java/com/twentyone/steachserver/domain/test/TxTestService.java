package com.twentyone.steachserver.domain.test;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class TxTestService {
    private final TxTestRepository txTestRepository;

    @Transactional("mongoTx")
    public TxTestEntity createTxEntity(TxTestEntity testEntity) {
        return txTestRepository.save(testEntity);
    }

    public TxTestEntity getEntityById(String id) {
        return txTestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("찾을 수 없는 아이디"));
    }

    @Transactional("mongoTx")
    public TxTestEntity rollbackTestWithongoTx(String id, String changeName) {
        TxTestEntity txTestEntity = txTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("찾을 수 없는 아이디"));
        txTestEntity.setName(changeName);
        txTestRepository.save(txTestEntity);

        throw new IllegalArgumentException("예외발생");
    }

    @Transactional //MySQL 트랜잭션만 적용, mongo x
    public TxTestEntity rollbackTest(String id, String changeName) {
        TxTestEntity txTestEntity = txTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("찾을 수 없는 아이디"));
        txTestEntity.setName(changeName);
        txTestRepository.save(txTestEntity);

        throw new IllegalArgumentException("예외발생");
    }
}
