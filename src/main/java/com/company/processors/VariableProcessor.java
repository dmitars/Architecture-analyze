package com.company.processors;


import com.company.storage.Graph;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;

public class VariableProcessor extends AbstractProcessor<CtVariable> {

    @Override
    public void process(CtVariable ctVariable) {
        var parent = ctVariable.getParent(CtClass.class);
        if(parent != null) {
            processType(ctVariable.getType(), parent.getQualifiedName(), ctVariable.isStatic());
        }
    }

    private void processType(CtTypeReference<?> reference, String parent, boolean isStatic) {
        Graph.addDependencyForClass(parent, reference, isStatic);
        for (var typeParameter : reference.getActualTypeArguments())
            processType(typeParameter, parent, isStatic);
    }

}