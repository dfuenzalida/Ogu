package org.ogu.lang.resolvers;

import org.ogu.lang.definitions.TypeDefinition;
import org.ogu.lang.parser.ast.invocables.FunctionDefinitionNode;

import java.util.List;
import java.util.Optional;

/**
 * TODO Implements for real
 * Created by ediaz on 20-01-16.
 */
public class ComposedTypeResolver implements TypeResolver {

    private List<TypeResolver> elements;

    public ComposedTypeResolver(List<TypeResolver> elements) {
        this.elements = elements;
        this.elements.forEach((e)->e.setRoot(ComposedTypeResolver.this));
    }
    @Override
    public TypeResolver root() {
        return null;
    }

    @Override
    public void setRoot(TypeResolver root) {

    }

    @Override
    public Optional<TypeDefinition> resolveAbsoluteTypeName(String typeName) {
        return null;
    }

    @Override
    public Optional<FunctionDefinitionNode> resolveAbsoluteFunctionName(String typeName) {
        return null;
    }

    @Override
    public boolean existPackage(String packageName) {
        return false;
    }

    @Override
    public SymbolResolver symbolResolver() {
        return null;
    }

    @Override
    public void setSymbolResolver(SymbolResolver symbolResolver) {

    }
}
