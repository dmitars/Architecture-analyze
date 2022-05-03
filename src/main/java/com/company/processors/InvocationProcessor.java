package com.company.processors;


import com.company.storage.Graph;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtExecutableReference;

public class InvocationProcessor extends AbstractProcessor<CtInvocation> {
    @Override
    public void process(CtInvocation element) {
        if (element.getTarget() != null) {
            var targetType = element.getTarget().getType();
            if (targetType != null) {
                var parent = element.getParent(CtClass.class);
                if (parent != null) {
                    Graph.addMethodDependencyForClass(parent.getQualifiedName(), targetType, element);
                }
            }
        }
    }
}
