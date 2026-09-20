package com.branchkit.vo;

import com.branchkit.po.Address;
import com.pojo.parameters.Parameters;
import com.pojo.parameters.Parameters.Of;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Richer view object covering defaults, marker annotations, {@code List}/{@code Map},
 * and nested generics on {@code @Of}.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Parameters(
        String = {"userName"},
        int_ = {"countryCode"},
        of = {
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
                        names = {"attributes"}),
                @Of(
                        Class = String.class,
                        names = {"status"},
                        initializer = "\"ACTIVE\"",
                        annotations = {Deprecated.class})
        }
)
public class ProfileVO extends ProfileVO__Parameters {
}
