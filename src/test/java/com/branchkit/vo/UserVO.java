package com.branchkit.vo;

import com.branchkit.po.Address;
import com.pojo.parameters.Parameters;
import com.pojo.parameters.Parameters.Of;
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
        of = {
                @Of(Class = String.class, names = {"userName"}),
                @Of(Class = int.class, names = {"countryCode", "cityCode", "areaCode"}),
                @Of(Class = Address.class, names = {"homeAddress"})
        }
)
public class UserVO extends UserVO__Parameters {
}
