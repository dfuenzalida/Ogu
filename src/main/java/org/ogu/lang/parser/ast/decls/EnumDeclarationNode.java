package org.ogu.lang.parser.ast.decls;

import com.google.common.collect.ImmutableList;
import org.ogu.lang.parser.ast.Node;
import org.ogu.lang.parser.ast.IdentifierNode;
import org.ogu.lang.parser.ast.TypeIdentifierNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ediaz on 28-01-16.
 */
public class EnumDeclarationNode extends AlgebraicDataTypeDeclarationNode {

    private List<IdentifierNode> values;

    public EnumDeclarationNode(TypeIdentifierNode name, List<IdentifierNode> values, List<TypeIdentifierNode> deriving, List<DecoratorNode> decoratorNodes) {
        super(name, deriving, decoratorNodes);
        this.values = new ArrayList<>();
        this.values.addAll(values);
    }


    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder()
                .add(name)
                .addAll(values)
                .addAll(deriving)
                .addAll(decoratorNodes).build();
    }

    @Override
    public String toString() {
        return "Enum{" +
                "name='" + name + '\''+
                ", values=" + values +
                ", deriving=" + deriving +
                ", decorators=" + decoratorNodes +
                '}';
    }

}