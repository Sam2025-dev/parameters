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
        String = {"userName", "name", "address", "phone"},
        Long = {"id"},
        boolean_ = {"active"},
        byte_ = {"level"},
        short_ = {"rank"},
        int_ = {"countryCode", "cityCode", "areaCode"},
        long_ = {"age"},
        char_ = {"grade"},
        float_ = {"score"},
        double_ = {"amount"},
        of = {
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
