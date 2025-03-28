package com.twentyone.steachserver.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.twentyone.steachserver.domain.test.TxTestEntity;
import com.twentyone.steachserver.domain.test.TxTestRepository;
import com.twentyone.steachserver.domain.test.TxTestService;
import com.twentyone.steachserver.integration.IntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@DisplayName("몽고디비 트랜잭션 테스트")
public class MongoTxTest extends IntegrationTest {
    @Autowired
    TxTestService txTestService;
    @Autowired
    TxTestRepository txTestRepository;

    @Test
    void 트랜잭션_성공_테스트() throws Exception {
        //given
        TxTestEntity original = new TxTestEntity("originalName", "originalEmail");
        TxTestEntity save = txTestRepository.save(original);

        //when
        TxTestEntity entityById = txTestService.getEntityById(save.getId());

        //then
        assertEquals(original.getId(), entityById.getId());
        assertEquals(original.getName(), entityById.getName());
    }

    @Test
    void 트랜잭션_실패_테스트() throws Exception {
        //given
        TxTestEntity original = new TxTestEntity("originalName", "originalEmail");
        TxTestEntity save = txTestRepository.save(original);

        //when
        //then
        assertThrows(IllegalArgumentException.class, () -> {
            txTestService.rollbackTest(save.getId(), "changeName");
        });

        TxTestEntity nextEntity = txTestRepository.findById(original.getId()).get();

        //변경되지 않았음을 체크 - 롤백이 잘 되었는지 체크
        assertEquals(original.getId(), nextEntity.getId());
        assertEquals(original.getName(), nextEntity.getName());
    }
}
