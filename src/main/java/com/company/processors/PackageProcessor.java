package com.company.processors;


import com.company.storage.Graph;
import spoon.compiler.Environment;
import spoon.processing.AbstractProcessor;
import spoon.processing.FactoryAccessor;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.ReferenceTypeFilter;

import java.util.*;

public class PackageProcessor extends AbstractProcessor<CtPackage> {

    private List<CtTypeReference<?>> ignoredTypes = new ArrayList<>();

    @Override
    public void init() {
        ignoredTypes.add(getFactory().Type().createReference(Environment.class));
        ignoredTypes.add(getFactory().Type().createReference(Factory.class));
        ignoredTypes.add(getFactory().Type().createReference(FactoryAccessor.class));
    }

    Map<CtPackageReference, Set<CtPackageReference>> packRefs = new HashMap<>();

    public void process(CtPackage element) {
        CtPackageReference pack = element.getReference();
        for (CtType t : element.getTypes()) {
            List<CtTypeReference<?>> listReferences = Query.getReferences(t, new ReferenceTypeFilter<>(CtTypeReference.class));

            for (CtTypeReference<?> tref : listReferences) {
                if (tref.getPackage() != null && !tref.getPackage().equals(pack)) {
                    if (ignoredTypes.contains(tref))
                        continue;
                    Graph.addDependencyForPackage(pack, tref.getPackage());
                }
            }
        }
    }

    @Override
    public void processingDone() {
        for (CtPackageReference p : packRefs.keySet()) {
            Stack<CtPackageReference> path = new Stack<>();
            path.push(p);
            scanDependencies(path);
        }
    }

    Set<CtPackageReference> scanned = new HashSet<>();

    void scanDependencies(Stack<CtPackageReference> path) {
        CtPackageReference ref = path.peek();
        // return if already scanned
        if (scanned.contains(ref)) {
            return;
        }
        scanned.add(ref);
        Set<CtPackageReference> refs = packRefs.get(ref);
        if (refs != null) {
            for (CtPackageReference p : refs) {
                if (path.contains(p)) {
                    List<CtPackageReference> circularPath = new ArrayList<>(
                            path.subList(path.indexOf(p), path.size()));
                    circularPath.add(p);

                    Graph.addCircularPath(circularPath);
                    break;
                } else {
                    path.push(p);
                    scanDependencies(path);
                    path.pop();
                }
            }
        }
    }

}