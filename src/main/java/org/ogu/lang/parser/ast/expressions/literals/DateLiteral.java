package org.ogu.lang.parser.ast.expressions.literals;

import com.google.common.collect.ImmutableList;
import org.ogu.lang.parser.ast.Node;
import org.ogu.lang.parser.ast.expressions.Expression;
import org.ogu.lang.typesystem.ReferenceTypeUsage;
import org.ogu.lang.typesystem.TypeUsage;

/**
 * An String Litearl
 * Created by ediaz on 22-01-16.
 */
public class DateLiteral extends Expression {

    private String value;

    public DateLiteral(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DateLiteral{" +
                "value='" + value + '\'' +
                '}';
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateLiteral that = (DateLiteral) o;

        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of();
    }

    @Override
    public TypeUsage calcType() {
        return ReferenceTypeUsage.STRING(symbolResolver());
    }
}