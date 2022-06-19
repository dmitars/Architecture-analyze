package com.company.storage;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodsDependency extends Dependency{
    Set<CtInvocation<?>> methods;

    public MethodsDependency(String to) {
        super(to);
    }

    public MethodsDependency(String to, Set<CtInvocation<?>> methods){
        super(to);
        this.methods = methods;
    }

    public MethodsDependency(String to, Set<CtInvocation<?>> methods, boolean isStatic){
        super(to, isStatic);
        this.methods = methods;
    }

    public List<String> getMethods() {
        return methods.stream().map(CtInvocation::getExecutable).map(CtReference::getSimpleName).collect(Collectors.toList());
    }

    public boolean hasMethodWithName(String methodName){
        return methods.stream().anyMatch(method -> method.getExecutable().getSimpleName().equals(methodName));
    }

    public boolean hasMethodWithParams(List<CtTypeReference<?>>params,CtExecutableReference<?> superMethod){
        var allowedMethods = methods;
        if(superMethod!=null) {
            allowedMethods = allowedMethods.stream().filter(allowedMethod -> {
                var allowedSuperMethod = allowedMethod.getParent(CtExecutableReference.class);
                if (allowedSuperMethod == null)
                    return false;
                return allowedSuperMethod.getSimpleName().equals(superMethod.getSimpleName());
            }).collect(Collectors.toSet());
        }
        for(var param:params){
            if (param == null || param.getPackage() == null || param.getQualifiedName() == null || param.getQualifiedName().startsWith("java.")){
                continue;
            }
            for(var method: allowedMethods){
                List<CtTypeReference<?>> methodParams = method.getExecutable().getParameters();
                for(var methodParam:methodParams){
                    if(methodParam.getQualifiedName().equals(param.getQualifiedName()))
                        return true;
                }
            }
        }
        return false;
    }

    public void addMethodToDependencies(CtInvocation method){
        methods.add(method);
    }
}
