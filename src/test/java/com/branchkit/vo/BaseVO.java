package com.branchkit.vo;

import java.util.Objects;

/**
 * Domain superclass used to verify {@code @Parameters(Extends = ...)}.
 */
public abstract class BaseVO {

    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseVO)) {
            return false;
        }
        BaseVO that = (BaseVO) o;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public String toString() {
        return "BaseVO{version=" + version + '}';
    }
}
