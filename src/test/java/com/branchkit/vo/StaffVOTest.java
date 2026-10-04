package com.branchkit.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class StaffVOTest {

    @Test
    void generatedClassHoldsAssembledProperties() {
        Class<?> type = StaffVO.class;
        assertEquals("StaffVO", type.getSimpleName());
        assertEquals(Object.class, type.getSuperclass());
        assertEquals(0, StaffVO.class.getInterfaces().length);
    }

    @Test
    void generatedAccessorsRoundTripFields() {
        StaffVO staff = new StaffVO();
        staff.setUserName("sam");
        staff.setTitle("engineer");

        assertEquals("sam", staff.getUserName());
        assertEquals("engineer", staff.getTitle());
    }

    @Test
    void equalsUsesAssembledFields() {
        StaffVO left = new StaffVO();
        left.setUserName("sam");
        left.setTitle("engineer");

        StaffVO right = new StaffVO();
        right.setUserName("sam");
        right.setTitle("engineer");
        assertEquals(left, right);

        right.setTitle("manager");
        assertNotEquals(left, right);
    }
}
