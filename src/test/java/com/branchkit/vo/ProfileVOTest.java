package com.branchkit.vo;

import com.branchkit.po.Address;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileVOTest {

    @Test
    void defaultsAndCollectionsAreAssembledOnTheGeneratedSuperclass() throws Exception {
        ProfileVO vo = new ProfileVO();
        assertNull(vo.getUserName());
        assertEquals(0, vo.getCountryCode());
        assertNull(vo.getHomeAddress());
        assertEquals(Collections.emptyList(), vo.getRoles());
        assertNull(vo.getAddresses());
        assertNull(vo.getAttributes());
        assertEquals("ACTIVE", vo.getStatus());

        vo.setUserName("erin");
        vo.setRoles(Collections.singletonList("admin"));
        vo.setAddresses(Collections.singletonMap("home", new Address("Beijing", "Chang'an Avenue")));

        assertEquals("erin", vo.getUserName());
        assertEquals("admin", vo.getRoles().get(0));
        assertEquals("Beijing", vo.getAddresses().get("home").getCity());
    }

    @Test
    void generatedFieldsKeepGenericsDefaultsAndMarkerAnnotations() throws Exception {
        Class<?> generated = ProfileVO.class.getSuperclass();
        assertEquals("ProfileVO__Parameters", generated.getSimpleName());

        Field roles = generated.getDeclaredField("roles");
        assertTrue(roles.getGenericType() instanceof ParameterizedType);
        assertEquals("java.util.List<java.lang.String>", roles.getGenericType().getTypeName());

        Field addresses = generated.getDeclaredField("addresses");
        assertEquals(
                "java.util.Map<java.lang.String, com.branchkit.po.Address>",
                addresses.getGenericType().getTypeName());

        Field attributes = generated.getDeclaredField("attributes");
        assertEquals(
                "java.util.List<java.util.Map<java.lang.String, java.lang.String>>",
                attributes.getGenericType().getTypeName());

        Field status = generated.getDeclaredField("status");
        assertEquals(String.class, status.getType());
        assertNotNull(status.getAnnotation(Deprecated.class));
    }
}
