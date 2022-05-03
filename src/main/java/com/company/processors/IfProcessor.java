package com.company.processors;

import com.company.storage.Graph;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtParameterReference;
import spoon.support.reflect.code.CtVariableReadImpl;

public class IfProcessor extends AbstractProcessor<CtIf> {
    @Override
    public void process(CtIf element) {
        var condition = element.getCondition();
        var method = element.getParent(CtMethod.class);
        var className = element.getParent(CtClass.class);
        if (className != null && method != null) {
            if (condition instanceof CtParameterReference<?>) {
                Graph.addMethodToManagedByParams(className.getQualifiedName(), method.getSimpleName());
            } else if (condition instanceof CtInvocation<Boolean>) {
                CtExpression<?> invocation = condition;
                while (invocation instanceof CtInvocation<?>) {
                    if (hasMethodParameterAsArgument((CtInvocation<?>) invocation)) {
                        Graph.addMethodToManagedByParams(className.getQualifiedName(), method.getSimpleName());
                        return;
                    }
                    invocation = ((CtInvocation<?>) invocation).getTarget();
                }
                if (invocation instanceof CtParameterReference<?>) {
                    Graph.addMethodToManagedByParams(className.getQualifiedName(), method.getSimpleName());
                }
            }
        }
    }

    private boolean hasMethodParameterAsArgument(CtInvocation<?> invocation) {
        return invocation.getArguments().stream()
                .anyMatch(argument -> argument instanceof CtVariableReadImpl<?>
                        && ((CtVariableReadImpl<?>) argument).getVariable() instanceof CtParameterReference<?>);
    }
}
