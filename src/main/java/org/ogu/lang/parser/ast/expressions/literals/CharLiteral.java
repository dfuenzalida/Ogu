package org.ogu.lang.parser.ast.expressions.literals;

import org.ogu.lang.parser.ast.Node;
import org.ogu.lang.parser.ast.expressions.Expression;
import org.ogu.lang.typesystem.TypeUsage;

import java.math.BigInteger;
import java.util.Collections;

/**
 * Char literal (TODO: parse escape sequcence)
 * Created by ediaz on 27-01-16.
 */
public class CharLiteral extends Expression {

    String value; //TODO parse and use char instead

    public CharLiteral(String value) {
        // TODO PARSE
        super();
        this.value = value;
    }


    @Override
    public TypeUsage calcType() {
        return null;
    }

    @Override
    public Iterable<Node> getChildren() {
        return Collections.emptyList();
    }
}
