package com.company.processors;

import com.company.storage.Graph;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public class AssignmentProcessor extends AbstractProcessor<CtAssignment> {
    @Override
    public void process(CtAssignment element) {
        if (element.getAssigned() instanceof CtFieldWrite<?>) {
            var  fieldAssigned = (CtFieldWrite<?>)element.getAssigned();
            var classType = element.getParent(CtClass.class);
            var method = element.getParent(CtMethod.class);
            if (classType != null && method != null) {
                if (fieldAssigned.getType().getQualifiedName().equals(classType.getQualifiedName())) {
                    Graph.markMethodAsFieldAccessor(classType.getQualifiedName(), method.getSimpleName(), fieldAssigned.getVariable().isStatic());
                } else {
                    Graph.markClassAsStaticFieldAccessor(classType.getQualifiedName(), fieldAssigned.getType().getQualifiedName());
                }
            }
        }
    }
}
