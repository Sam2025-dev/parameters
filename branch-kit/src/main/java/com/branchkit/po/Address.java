package com.branchkit.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Custom value type used to verify {@code @Parameters} assembly of non-JDK classes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String city;
    private String street;
}
