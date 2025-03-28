package com.twentyone.steachserver.domain.test;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "txTest")
public class TxTestEntity {
    @Id
    private String id;
    private String name;
    private String email;

    public TxTestEntity(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
