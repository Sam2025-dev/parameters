package com.branchkit.vo;

import com.pojo.parameters.Parameters;
import lombok.Data;

/**
 * View object assembled from {@code @Parameters} plus Lombok {@code @Data}.
 */
@Data
@Parameters(String = {"userName"}, int_ = {1, 2, 3})
public class UserVO extends UserVO__Parameters {
}
