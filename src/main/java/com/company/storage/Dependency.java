package com.company.storage;

import java.util.Objects;

public abstract class Dependency implements Comparable<Dependency>{
    private final String dependencyName;
    private boolean isStatic = false;

    public Dependency(String dependencyName) {
        this.dependencyName = dependencyName;
    }

    public Dependency(String dependencyName, boolean isStatic) {
        this.dependencyName = dependencyName;
        this.isStatic = isStatic;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    @Override
    public int compareTo(Dependency o) {
        return getDependencyName().compareTo(o.getDependencyName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(dependencyName, that.dependencyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyName, this.getClass());
    }
}
