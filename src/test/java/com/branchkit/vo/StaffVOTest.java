package com.branchkit.vo;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaffVOTest {

    @Test
    void generatedSuperclassExtendsDomainParentAndImplementsContracts() {
        Class<?> generated = StaffVO.class.getSuperclass();
        assertEquals("StaffVO__Parameters", generated.getSimpleName());
        assertEquals(BaseVO.class, generated.getSuperclass());
        assertTrue(Named.class.isAssignableFrom(generated));
        assertTrue(Serializable.class.isAssignableFrom(generated));
        assertEquals(0, StaffVO.class.getInterfaces().length);
    }

    @Test
    void generatedAccessorsFulfillNamedAndInheritBaseFields() {
        StaffVO staff = new StaffVO();
        Named named = staff;
        named.setUserName("sam");
        staff.setTitle("engineer");
        staff.setVersion(7L);

        assertEquals("sam", staff.getUserName());
        assertEquals("engineer", staff.getTitle());
        assertEquals(7L, staff.getVersion());
    }

    @Test
    void equalsIncludesParentStateWhenParentOverridesEquals() {
        StaffVO left = new StaffVO();
        left.setUserName("sam");
        left.setTitle("engineer");
        left.setVersion(1L);

        StaffVO right = new StaffVO();
        right.setUserName("sam");
        right.setTitle("engineer");
        right.setVersion(1L);
        assertEquals(left, right);

        right.setVersion(2L);
        assertNotEquals(left, right);
    }
}
