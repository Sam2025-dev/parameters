package com.branchkit.vo;

import com.branchkit.po.Address;
import com.pojo.parameters.Data;
import com.pojo.parameters.Data.Of;

import java.util.List;
import java.util.Map;

/**
 * Richer view object covering defaults, marker annotations, {@code List}/{@code Map},
 * and nested generics on {@code @Of}.
 */
@Data(
        of = {
                @Of(Class = String.class, names = {"userName"}),
                @Of(Class = int.class, names = {"countryCode"}),
                @Of(Class = Address.class, names = {"homeAddress"}),
                @Of(
                        Class = List.class,
                        typeArgs = {String.class},
                        names = {"roles"},
                        initializer = "java.util.Collections.emptyList()"),
                @Of(
                        Class = Map.class,
                        typeArgs = {String.class, Address.class},
                        names = {"addresses"}),
                @Of(
                        type = "java.util.List<java.util.Map<String, String>>",
                        names = {"attributes"})
        }
)
public class ProfileVO {
    @Deprecated
    private String status = "ACTIVE";
}
