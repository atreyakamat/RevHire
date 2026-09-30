package com.revhire.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EurekaServerApplicationTests {

    @Test
    void contextLoads() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(EurekaServerApplication::new);
    }
}
