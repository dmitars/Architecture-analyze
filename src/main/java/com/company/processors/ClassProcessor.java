package com.company.processors;

import com.company.storage.Graph;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;


public class ClassProcessor extends AbstractProcessor<CtClass> {
    @Override
    public void process(CtClass ctClass) {
        var superClass = ctClass.getSuperclass();
        if (superClass != null) {
            Graph.processInheritance(superClass.getQualifiedName(), ctClass.getQualifiedName());
        }
        var interfaces = ctClass.getSuperInterfaces();
        interfaces.forEach(interfaceValue -> Graph.processInheritance(interfaceValue.getQualifiedName(), ctClass.getQualifiedName()));
        if (interfaces.isEmpty() && superClass == null)
            Graph.pushElementToDsu(ctClass.getQualifiedName());
    }
}
