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
 * properties.
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Parameters(
        String = {"userName", "name", "address", "phone"},
        boolean_ = {true},
        byte_ = {1},
        short_ = {2},
        int_ = {1, 2, 3},
        long_ = {18L},
        char_ = {'U'},
        float_ = {1.5f},
        double_ = {2.25},
        of = {
                @Of(Class = Long.class, names = {"id"}),
                @Of(Class = Address.class, names = {"homeAddress"})
        }
)
public class UserPO extends UserPO__Parameters implements Serializable {

    private static final long serialVersionUID = 1L;

    public UserPO(Long id, String name, String address, String phone) {
        setId(id);
        setName(name);
        setAddress(address);
        setPhone(phone);
    }
}
