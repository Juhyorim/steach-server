package com.twentyone.steachserver.domain.test;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
@Service
public class TxTestService {
    private final TxTestRepository txTestRepository;

    public TxTestEntity createTxEntity(TxTestEntity testEntity) {
        return txTestRepository.save(testEntity);
    }

    public TxTestEntity getEntityById(String id) {
        return txTestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("찾을 수 없는 아이디"));
    }

    public TxTestEntity rollbackTest(String id, String changeName) {
        TxTestEntity txTestEntity = txTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("찾을 수 없는 아이디"));
        txTestEntity.setName(changeName);

        throw new IllegalArgumentException("예외발생");
    }
}
