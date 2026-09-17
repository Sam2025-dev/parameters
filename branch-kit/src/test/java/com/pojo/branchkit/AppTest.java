package com.pojo.branchkit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    @Test
    void greetingReturnsHelloMaven() {
        assertEquals("Hello, Maven!", new App().greeting());
    }
}
