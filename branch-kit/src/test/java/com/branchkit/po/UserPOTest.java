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
        assertFalse(user.isActive());
        assertEquals((byte) 0, user.getLevel());
        assertEquals((short) 0, user.getRank());
        assertEquals(0, user.getCountryCode());
        assertEquals(0, user.getCityCode());
        assertEquals(0, user.getAreaCode());
        assertEquals(0L, user.getAge());
        assertEquals('\0', user.getGrade());
        assertEquals(0f, user.getScore());
        assertEquals(0d, user.getAmount());
        assertNull(user.getHomeAddress());

        Address home = new Address("Shanghai", "Nanjing Road");
        user.setUserName("alice");
        user.setHomeAddress(home);
        user.setActive(true);
        user.setCountryCode(86);

        assertEquals("alice", user.getUserName());
        assertEquals(home, user.getHomeAddress());
        assertTrue(user.isActive());
        assertEquals(86, user.getCountryCode());
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
        assertEquals(boolean.class, generated.getDeclaredField("active").getType());
        assertEquals(long.class, generated.getDeclaredField("age").getType());
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
