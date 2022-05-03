package com.company.processors;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.Filter;

public class AbstractionFilter<T extends CtElement> implements Filter<T> {
    @Override
    public boolean matches(CtElement element) {
        if(element instanceof CtInterface)
            return true;
        if(element instanceof CtClass){
            return ((CtClass<?>)element).getModifiers().contains(ModifierKind.ABSTRACT);
        }
        return false;
    }
}
