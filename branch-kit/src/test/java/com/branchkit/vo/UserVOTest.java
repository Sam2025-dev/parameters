package com.branchkit.vo;

import com.branchkit.po.Address;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserVOTest {

    @Test
    void dataAndParametersAssembleTogether() {
        UserVO vo = new UserVO();
        assertNull(vo.getUserName());
        assertNull(vo.getHomeAddress());
        assertEquals(1, vo.getInt_1());
        assertEquals(2, vo.getInt_2());
        assertEquals(3, vo.getInt_3());

        vo.setUserName("erin");
        vo.setInt_2(22);
        vo.setHomeAddress(new Address("Beijing", "Chang'an Avenue"));

        assertEquals("erin", vo.getUserName());
        assertEquals(22, vo.getInt_2());
        assertEquals("Beijing", vo.getHomeAddress().getCity());
    }

    @Test
    void lombokEqualsIncludesAssembledSuperclassState() {
        UserVO left = new UserVO();
        UserVO right = new UserVO();
        assertEquals(left, right);

        left.setUserName("left");
        right.setUserName("right");
        assertNotEquals(left, right);
        assertNotEquals(left.getUserName(), right.getUserName());
    }
}
