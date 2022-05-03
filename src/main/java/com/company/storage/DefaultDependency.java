package com.company.storage;

public class DefaultDependency extends Dependency{
    public DefaultDependency(String to) {
        super(to);
    }

    public DefaultDependency(String to, boolean isStatic){
        super(to, isStatic);
    }
}
