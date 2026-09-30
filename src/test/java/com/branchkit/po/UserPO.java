package com.branchkit.po;

import com.pojo.parameters.Parameters;
import com.pojo.parameters.Parameters.Of;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Persistent object for the user table.
 *
 * <p>Instance-level user attributes are declared on {@link Parameters} and merged
 * into the generated superclass together with extra primitive and custom-typed
 * properties. {@code Serializable} is implemented by that generated type via
 * {@link Parameters#Implements()}.
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Parameters(
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
        },
        Implements = {Serializable.class}
)
public class UserPO extends UserPO__Parameters {

    private static final long serialVersionUID = 1L;

    public UserPO(Long id, String name, String address, String phone) {
        setId(id);
        setName(name);
        setAddress(address);
        setPhone(phone);
    }
}
