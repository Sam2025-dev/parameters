package com.branchkit.po;

import com.pojo.parameters.Data;
import com.pojo.parameters.Data.Of;

import java.io.Serializable;

/**
 * Persistent object for the user table.
 *
 * <p>Instance-level user attributes are declared on {@link Data} and merged
 * into the annotated class together with extra primitive and custom-typed
 * properties.
 */
@Data(
        of = {
                @Of(Class = String.class, names = {"userName", "name", "address", "phone"}),
                @Of(Class = Long.class, names = {"id"}),
                @Of(Class = boolean.class, names = {"active"}),
                @Of(Class = byte.class, names = {"level"}),
                @Of(Class = short.class, names = {"rank"}),
                @Of(Class = int.class, names = {"countryCode", "cityCode", "areaCode"}),
                @Of(Class = long.class, names = {"age"}),
                @Of(Class = char.class, names = {"grade"}),
                @Of(Class = float.class, names = {"score"}),
                @Of(Class = double.class, names = {"amount"}),
                @Of(Class = Address.class, names = {"homeAddress"})
        }
)
public class UserPO implements Serializable {

    private static final long serialVersionUID = 1L;

    public UserPO() {
    }

    public UserPO(Long id, String name, String address, String phone) {
        setId(id);
        setName(name);
        setAddress(address);
        setPhone(phone);
    }
}
