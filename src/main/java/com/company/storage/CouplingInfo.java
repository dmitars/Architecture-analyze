package com.company.storage;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CouplingInfo {
    private String firstClassName;
    private String secondClassName;

    public CouplingInfo(){}

    public CouplingInfo(String firstClassName, String secondClassName) {
        this.firstClassName = firstClassName;
        this.secondClassName = secondClassName;
    }


    public String getFirstClassName() {
        return firstClassName;
    }

    public String getSecondClassName() {
        return secondClassName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CouplingInfo that = (CouplingInfo) o;
        return firstClassName.equals(that.firstClassName) && secondClassName.equals(that.secondClassName)
                 || firstClassName.equals(that.secondClassName) && secondClassName.equals(that.firstClassName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Stream.of(firstClassName,secondClassName).sorted().toList());
    }
}
