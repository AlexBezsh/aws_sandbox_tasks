package com.task11.models;

import java.math.BigInteger;

public class CreateTableResponse {

    private BigInteger id;

    public CreateTableResponse(BigInteger id) {
        this.id = id;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

}
