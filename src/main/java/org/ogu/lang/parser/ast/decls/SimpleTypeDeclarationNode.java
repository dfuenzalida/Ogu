package org.ogu.lang.parser.ast.decls;

import com.google.common.collect.ImmutableList;
import org.ogu.lang.parser.ast.Node;
import org.ogu.lang.parser.ast.TypeIdentifierNode;
import org.ogu.lang.parser.ast.typeusage.TypeNode;

import java.util.List;

/**
 * A type like type T = ..
 * Created by ediaz on 24-01-16.
 */
public class SimpleTypeDeclarationNode extends TypedefDeclarationNode {

    public SimpleTypeDeclarationNode(TypeIdentifierNode name, TypeNode type, List<DecoratorNode> decoratorNodes) {
        super(name, type, decoratorNodes);
    }


    @Override
    public String toString() {
        return "SimpleTypeDeclaration{"+
                "name="+name+
                ", type="+type+
                ", decorators="+ decoratorNodes +
                '}';
    }


    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder()
                .add(name)
                .add(type)
                .addAll(decoratorNodes).build();
    }
}