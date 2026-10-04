package com.branchkit.vo;

import com.branchkit.po.Address;
import com.pojo.parameters.Data;
import com.pojo.parameters.Data.Of;

/**
 * View object assembled from {@code @Data}.
 */
@Data(
        of = {
                @Of(Class = String.class, names = {"userName"}),
                @Of(Class = int.class, names = {"countryCode", "cityCode", "areaCode"}),
                @Of(Class = Address.class, names = {"homeAddress"})
        }
)
public class UserVO {
}
