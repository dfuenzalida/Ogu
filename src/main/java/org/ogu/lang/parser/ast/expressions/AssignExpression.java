package org.ogu.lang.parser.ast.expressions;

import com.google.common.collect.ImmutableList;
import org.ogu.lang.parser.ast.Node;
import org.ogu.lang.parser.ast.OguIdentifier;
import org.ogu.lang.typesystem.TypeUsage;

/**
 * set id = value
 * id <- value
 * id v <- value
 * set id v <- value
 * Created by ediaz on 31-01-16.
 */
public class AssignExpression extends Expression {

    protected OguIdentifier id;
    protected Expression optArg;
    protected Expression value;


    public AssignExpression(OguIdentifier id, Expression value) {
        super();
        this.id = id;
        this.id.setParent(this);
        this.value = value;
        this.value.setParent(this);
    }

    public AssignExpression(OguIdentifier id, Expression optArg, Expression value) {
        super();
        this.id = id;
        this.id.setParent(this);
        this.optArg = optArg;
        this.optArg.setParent(this);
        this.value = value;
        this.value.setParent(this);
    }

    @Override
    public String toString() {
        return "Assign {" +
                "id = "+id +
                "opt_arg = "+optArg +
                "value = "+value +
                '}';
    }

    @Override
    public TypeUsage calcType() {
        return null;
    }


    @Override
    public Iterable<Node> getChildren() {
        if (optArg != null)
            return ImmutableList.<Node>builder().add(id).add(optArg).add(value).build();
        else
            return ImmutableList.<Node>builder().add(id).add(value).build();
    }

}