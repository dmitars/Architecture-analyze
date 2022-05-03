package com.company.processors;

import com.company.storage.Graph;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtReference;
import spoon.reflect.reference.CtTypeReference;

public class TypeReferenceProcessor extends AbstractProcessor<CtReference> {
    @Override
    public void process(CtReference ctReference) {
        if (ctReference instanceof CtTypeReference<?> ctTypeReference) {
            var parent = ctTypeReference.getParent(CtClass.class);
            if (parent != null && ctTypeReference.getPackage() != null) {
                Graph.addDependencyForClass(parent.getQualifiedName(), ctTypeReference, false);
            }
        }
    }
}
