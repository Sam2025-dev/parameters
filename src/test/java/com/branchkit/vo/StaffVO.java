package com.branchkit.vo;

import com.pojo.parameters.Data;
import com.pojo.parameters.Data.Of;

/**
 * View object assembled from {@code @Data} properties.
 */
@Data(of = @Of(Class = String.class, names = {"userName", "title"}))
public class StaffVO {
}
