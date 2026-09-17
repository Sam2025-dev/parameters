package com.branchkit.po;

import com.pojo.parameters.Parameters;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Persistent object for the user table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Parameters(String = {"userName"}, int_ = {1, 2, 3})
public class UserPO extends UserPO__Parameters implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String address;
    private String phone;
}
