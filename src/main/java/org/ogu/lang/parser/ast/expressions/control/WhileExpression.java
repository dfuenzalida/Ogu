package org.ogu.lang.parser.ast.expressions.control;

import com.google.common.collect.ImmutableList;
import org.ogu.lang.parser.ast.Node;
import org.ogu.lang.parser.ast.expressions.Expression;
import org.ogu.lang.typesystem.TypeUsage;

/**
 * when expr do ...
 * Created by ediaz on 31-01-16.
 */
public class WhileExpression extends Expression {

    private Expression cond;
    private DoExpression doExpr;

    public WhileExpression(Expression cond, DoExpression doExpr) {
        super();
        this.cond = cond;
        this.cond.setParent(this);
        this.doExpr = doExpr;
        this.doExpr.setParent(this);
    }



    @Override
    public String toString() {
        return "While { cond="+cond+", do=" + doExpr + '}';
    }

    @Override
    public TypeUsage calcType() {
        return null;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.<Node>builder().add(cond).add(doExpr).build();
    }
}