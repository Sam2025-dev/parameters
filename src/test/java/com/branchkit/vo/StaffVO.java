package com.branchkit.vo;

import com.pojo.parameters.Parameters;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * View object whose generated superclass extends {@link BaseVO} and implements
 * {@link Named} plus {@link Serializable}.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Parameters(
        String = {"userName", "title"},
        Extends = BaseVO.class,
        Implements = {Named.class, Serializable.class}
)
public class StaffVO extends StaffVO__Parameters {
}
