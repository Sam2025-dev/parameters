package com.branchkit.po;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void parametersAssemblesStringAndIntProperties() {
        UserPO user = new UserPO();
        assertEquals(1, user.getInt_1());
        assertEquals(2, user.getInt_2());
        assertEquals(3, user.getInt_3());

        user.setUserName("alice");
        user.setInt_1(10);
        user.setInt_2(20);
        user.setInt_3(30);

        assertEquals("alice", user.getUserName());
        assertEquals(10, user.getInt_1());
        assertEquals(20, user.getInt_2());
        assertEquals(30, user.getInt_3());
    }

    @Test
    void assembledPropertiesLiveOnGeneratedSuperclass() throws Exception {
        assertEquals("UserPO__Parameters", UserPO.class.getSuperclass().getSimpleName());

        Field userName = UserPO.class.getSuperclass().getDeclaredField("userName");
        assertEquals(String.class, userName.getType());
        assertTrue(Modifier.isPrivate(userName.getModifiers()));
        assertFalse(Modifier.isStatic(userName.getModifiers()));
    }

    @Test
    void lombokAccessorsRemainAvailableAlongsideParameters() {
        UserPO user = new UserPO();
        user.setName("Dana");
        user.setUserName("dana");

        assertEquals("Dana", user.getName());
        assertEquals("dana", user.getUserName());
        assertTrue(user.toString().contains("Dana"));
    }
}
