package com.branchkit.vo;

import com.branchkit.po.Address;
import com.pojo.parameters.Of;
import com.pojo.parameters.Parameters;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * View object assembled from {@code @Parameters} plus Lombok {@code @Data}.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Parameters(
        String = {"userName"},
        int_ = {1, 2, 3},
        of = @Of(Class = Address.class, names = {"homeAddress"})
)
public class UserVO extends UserVO__Parameters {
}
