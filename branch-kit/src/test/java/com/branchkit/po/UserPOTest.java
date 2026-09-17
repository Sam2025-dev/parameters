package com.branchkit.po;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void parametersAssemblesPrimitiveAndCustomProperties() {
        UserPO user = new UserPO();
        assertNull(user.getUserName());
        assertTrue(user.isBoolean_true());
        assertEquals((byte) 1, user.getByte_1());
        assertEquals((short) 2, user.getShort_2());
        assertEquals(1, user.getInt_1());
        assertEquals(2, user.getInt_2());
        assertEquals(3, user.getInt_3());
        assertEquals(18L, user.getLong_18());
        assertEquals('U', user.getChar_U());
        assertEquals(1.5f, user.getFloat_1_5());
        assertEquals(2.25, user.getDouble_2_25());
        assertNull(user.getHomeAddress());

        Address home = new Address("Shanghai", "Nanjing Road");
        user.setUserName("alice");
        user.setHomeAddress(home);
        user.setBoolean_true(false);
        user.setInt_1(10);

        assertEquals("alice", user.getUserName());
        assertEquals(home, user.getHomeAddress());
        assertFalse(user.isBoolean_true());
        assertEquals(10, user.getInt_1());
    }

    @Test
    void assembledPropertiesLiveOnGeneratedSuperclass() throws Exception {
        Class<?> generated = UserPO.class.getSuperclass();
        assertEquals("UserPO__Parameters", generated.getSimpleName());

        Field userName = generated.getDeclaredField("userName");
        assertEquals(String.class, userName.getType());
        assertTrue(Modifier.isPrivate(userName.getModifiers()));
        assertFalse(Modifier.isStatic(userName.getModifiers()));

        assertEquals(Long.class, generated.getDeclaredField("id").getType());
        assertEquals(Address.class, generated.getDeclaredField("homeAddress").getType());
        assertEquals(boolean.class, generated.getDeclaredField("boolean_true").getType());
        assertEquals(long.class, generated.getDeclaredField("long_18").getType());
    }

    @Test
    void lombokAccessorsRemainAvailableAlongsideParameters() {
        UserPO user = new UserPO();
        user.setName("Dana");
        user.setUserName("dana");

        assertEquals("Dana", user.getName());
        assertEquals("dana", user.getUserName());
        assertTrue(user.toString().contains("Dana"));
        assertTrue(user.toString().contains("dana"));
    }
}
