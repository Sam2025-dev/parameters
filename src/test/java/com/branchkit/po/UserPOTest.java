package com.branchkit.po;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UserPOTest {

    @Test
    void settersAndGettersRoundTripFields() {
        UserPO user = new UserPO();
        user.setId(1L);
        user.setName("Alice");
        user.setAddress("1 Main Street");
        user.setPhone("13800001111");

        assertEquals(1L, user.getId());
        assertEquals("Alice", user.getName());
        assertEquals("1 Main Street", user.getAddress());
        assertEquals("13800001111", user.getPhone());
    }

    @Test
    void allArgsConstructorPopulatesFields() {
        UserPO user = new UserPO(2L, "Bob", "2 Oak Avenue", "13900002222");

        assertEquals(2L, user.getId());
        assertEquals("Bob", user.getName());
        assertEquals("2 Oak Avenue", user.getAddress());
        assertEquals("13900002222", user.getPhone());
    }

    @Test
    void equalsAndHashCodeUseAllFields() {
        UserPO left = new UserPO(3L, "Carol", "3 Pine Road", "13700003333");
        UserPO right = new UserPO(3L, "Carol", "3 Pine Road", "13700003333");
        UserPO differentPhone = new UserPO(3L, "Carol", "3 Pine Road", "13600004444");

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, differentPhone);
    }
}
