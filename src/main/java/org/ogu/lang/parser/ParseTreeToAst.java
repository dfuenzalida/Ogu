package org.ogu.lang.parser;

import com.google.common.collect.ImmutableList;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.ogu.lang.antlr.OguParser;
import org.ogu.lang.parser.ast.*;
import org.ogu.lang.parser.ast.decls.*;
import org.ogu.lang.parser.ast.decls.funcdef.*;
import org.ogu.lang.parser.ast.decls.typedef.ClassParam;
import org.ogu.lang.parser.ast.decls.typedef.TypeParam;
import org.ogu.lang.parser.ast.decls.typedef.TypeParamConstrained;
import org.ogu.lang.parser.ast.expressions.*;
import org.ogu.lang.parser.ast.expressions.control.*;
import org.ogu.lang.parser.ast.expressions.literals.*;
import org.ogu.lang.parser.ast.modules.*;
import org.ogu.lang.parser.ast.typeusage.*;
import org.ogu.lang.util.Logger;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.ogu.lang.util.Messages.message;

/**
 * Transforms from Antlr ParseTree to Ast.
 * Based on code from here: https://github.com/ftomassetti/turin-programming-language
 * Created by ediaz on 21-01-16.
 */
public class ParseTreeToAst {

    private org.ogu.lang.parser.ast.Position getPosition(ParserRuleContext ctx) {
        return new org.ogu.lang.parser.ast.Position(getStartPoint(ctx.start), getEndPoint(ctx.stop));
    }

    private void getPositionFrom(Node node, ParserRuleContext ctx) {
        node.setPosition(getPosition(ctx));
    }

    private Point getStartPoint(Token token) {
        return new Point(token.getLine(), token.getCharPositionInLine());
    }

    private Point getEndPoint(Token token) {
        return new Point(token.getLine(), token.getCharPositionInLine() + token.getText().length());
    }

    public OguModule toAst(File file, org.ogu.lang.antlr.OguParser.ModuleContext ctx) {
        OguModule module = new OguModule();
        getPositionFrom(module, ctx);
        module.setName(toAst(file, ctx.moduleHeader));

        OguParser.Module_bodyContext bodyCtx = ctx.module_body();
        for (OguParser.Module_declContext memberCtx : bodyCtx.module_decl()) {
            Node memberNode = toAst(memberCtx);
            if (memberNode instanceof Expression)
                module.add((Expression) memberNode);
            else if (memberNode instanceof AliasDeclaration)
                module.add((AliasDeclaration) memberNode);
            else if (memberNode instanceof ExportableDeclaration)
                module.add((ExportableDeclaration) memberNode);
        }

        for (OguParser.Module_usesContext usesDeclarationContext : ctx.module_uses()) {
            module.addUses(toAst(usesDeclarationContext, toAstDecorators(usesDeclarationContext.decs)));
        }
        for (OguParser.Module_exportsContext exportsDeclarationContext : ctx.module_exports()) {
            module.addExports(toAst(exportsDeclarationContext, toAstDecorators(exportsDeclarationContext.decs)));
        }
        return module;
    }

    private ModuleNameDefinition toAst(File file, OguParser.Module_headerContext ctx) {
        if (ctx == null)
            return new ModuleNameDefinition(buildModuleNameFromFileName(file.getName()));
        return new ModuleNameDefinition(toAst(ctx.name).qualifiedName());
    }

    private OguTypeIdentifier toAst(OguParser.Module_nameContext ctx) {
        OguTypeIdentifier type = OguTypeIdentifier.create(ctx.parts.stream().map(Token::getText).collect(Collectors.toList()));
        getPositionFrom(type, ctx);
        return type;
    }

    private OguTypeIdentifier toOguTypeIdentifier(OguParser.Alias_targetContext ctx) {
        OguTypeIdentifier tname = OguTypeIdentifier.create(idText(ctx.alias_tid));
        getPositionFrom(tname, ctx);
        return tname;
    }

    private OguTypeIdentifier toOguTypeIdentifier(OguParser.Alias_originContext ctx) {
        OguTypeIdentifier tname = OguTypeIdentifier.create(ctx.alias_origin_tid.stream().map(Token::getText).collect(Collectors.toList()));
        getPositionFrom(tname, ctx);
        return tname;
    }

    private OguIdentifier toOguIdentifier(OguParser.Alias_targetContext ctx) {
        OguIdentifier tname = OguIdentifier.create(idText(ctx.alias_id));
        getPositionFrom(tname, ctx);
        return tname;
    }


    private OguIdentifier toOguIdentifier(OguParser.Alias_originContext ctx) {
        OguIdentifier tname = OguIdentifier.create(ctx.alias_origin_tid.stream().map(Token::getText).collect(Collectors.toList()), idText(ctx.alias_origin_id));
        getPositionFrom(tname, ctx);
        return tname;
    }

    private ExportsDeclaration toAst(OguParser.Export_nameContext ctx, List<Decorator> decs) {
        ExportsDeclaration result;
        if (ctx.ID() != null)
            result = new ExportsFunctionDeclaration(new OguIdentifier(idText(ctx.ID().getSymbol())), decs);
        else if (ctx.TID() != null)
            result = new ExportsTypeDeclaration(OguTypeIdentifier.create(idText(ctx.TID().getSymbol())), decs);
        else {
            throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
        }
        getPositionFrom(result, ctx);
        return result;
    }

    private Node toAst(OguParser.Module_declContext ctx) {
        List<Decorator> decs = toAstDecorators(ctx.decs);

        if (ctx.alias_def() != null) {
            return toAst(ctx.alias_def(), decs);
        }
        if (ctx.val_def() != null) {
            return toAst(ctx.val_def(), decs);
        }

        if (ctx.var() != null) {
            return toAst(ctx.var(), decs);
        }

        if (ctx.func_decl() != null) {
            return toAst(ctx.func_decl(), decs);
        }
        if (ctx.func_def() != null) {
            return toAst(ctx.func_def(), decs);
        }

        if (ctx.type_def() != null) {
            return toAst(ctx.type_def(), decs);
        }

        if (ctx.trait_def() != null) {
            return toAst(ctx.trait_def(), decs);
        }

        if (ctx.instance_def() != null) {
            return toAst(ctx.instance_def(), decs);
        }

        if (ctx.data_def() != null) {
            return toAst(ctx.data_def(), decs);
        }

        if (ctx.enum_def() != null) {
            return toAst(ctx.enum_def(), decs);
        }

        if (ctx.class_def() != null) {
            return toAst(ctx.class_def(), decs);
        }

        if (ctx.expr() != null) {
            return toAst(ctx.expr());
        }


        Logger.debug(ctx.getText());

        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private InstanceDeclaration toAst(OguParser.Instance_defContext ctx, List<Decorator> decs) {
        OguTypeIdentifier name = new OguTypeIdentifier(idText(ctx.name));
        Map<String,OguType> constraints = new HashMap<>();
        if (ctx.constraints != null)
            loadTypeConstraints(ctx.constraints, constraints);
        List<TypeParam> params = getInstaceTypeParamList(ctx, ctx.params, constraints);
        List<FunctionalDeclaration> members = internalDeclToAst(ctx.internal_decl());
        InstanceDeclaration instance = new InstanceDeclaration(name, params, members, decs);
        getPositionFrom(instance, ctx);
        return instance;
    }

    private List<TypeParam> getInstaceTypeParamList(OguParser.Instance_defContext ctx, List<OguParser.TypeContext> types, Map<String, OguType> constraints) {
        List<TypeParam> params = new ArrayList<>();
        for (OguParser.TypeContext typeContext : types) {
            OguType type = toAst(typeContext);
            TypeParam p = new TypeParam(type);
            getPositionFrom(p, ctx);
            params.add(p);
        }
        return params;

    }

    private ClassDeclaration toAst(OguParser.Class_defContext ctx, List<Decorator> decs) {
        boolean isMutable = ctx.mut != null;
        OguTypeIdentifier name = new OguTypeIdentifier(idText(ctx.name));
        Map<String,OguType> constraints = new HashMap<>();
        if (ctx.constraints != null)
            loadTypeConstraints(ctx.constraints, constraints);
        List<TypeParam> genParams = getTypeParamList(ctx, ctx.params, constraints);
        List<FunctionalDeclaration> members = internalDeclToAst(ctx.internal_decl());
        List<ClassParam> params = new ArrayList<>();
        if (ctx.class_params() != null)
            params = toAst(ctx.class_params());
        ClassDeclaration clazz = new ClassDeclaration(name, isMutable, genParams, params, members, decs);
        getPositionFrom(clazz, ctx);
        return clazz;
    }

    private List<ClassParam> toAst(OguParser.Class_paramsContext ctx) {
        List<ClassParam> result = new ArrayList<>();
        for (OguParser.Class_paramContext cp:ctx.class_param()) {
            result.addAll(toAst(cp));
        }
        return result;
    }

    private List<ClassParam> toAst(OguParser.Class_paramContext ctx) {
        OguType type = toAst(ctx.type());
        List<ClassParam> result = new ArrayList<>();
        for (TerminalNode cid:ctx.ID()) {
            OguIdentifier id = OguIdentifier.create(cid.getText());
            result.add(new ClassParam(id, type));
        }
        return result;
    }


    private EnumDeclaration toAst(OguParser.Enum_defContext ctx, List<Decorator> decs) {
        OguTypeIdentifier name = new OguTypeIdentifier(ctx.en.getText());
        List<OguIdentifier> values = ctx.values.stream().map((t) -> new OguIdentifier(idText(t))).collect(Collectors.toList());
        List<OguTypeIdentifier> deriving = toDerivingAst(ctx.deriving());
        EnumDeclaration decl = new EnumDeclaration(name, values, deriving, decs);
        getPositionFrom(decl, ctx);
        return decl;
    }

    private List<OguTypeIdentifier> toDerivingAst(OguParser.DerivingContext ctx) {
        if (ctx == null)
            return Collections.emptyList();

        List<OguTypeIdentifier> deriving = ctx.deriving_types().dt.stream().map(this::toAst).collect(Collectors.toList());
        for (OguTypeIdentifier oguTypeIdentifier : deriving) {
           getPositionFrom(oguTypeIdentifier, ctx);
        }
        return deriving;
    }

    private DataDeclaration toAst(OguParser.Data_defContext ctx, List<Decorator> decs) {
        OguTypeIdentifier name = new OguTypeIdentifier(idText(ctx.name));
        Map<String, OguType> constraints = new HashMap<>();
        if (ctx.constraints != null)
            loadTypeConstraints(ctx.constraints, constraints);

        List<TypeParam> params;
        if (ctx.typedef_params() != null)
            params = getTypeParamList(ctx, ctx.typedef_params().params, constraints);
        else
            params = Collections.emptyList();
        List<OguTypeIdentifier> deriving = new ArrayList<>();
        List<OguType> values = toAst(ctx.data_type_decl(), deriving);
        DataDeclaration decl = new DataDeclaration(name, params, values, deriving, decs);
        getPositionFrom(decl, ctx);
        return decl;
    }

    private List<OguType> toAst(OguParser.Data_type_declContext ctx, List<OguTypeIdentifier> deriving) {
        if (ctx.deriving() != null)
            deriving.addAll(toDerivingAst(ctx.deriving()));
        List<OguType> types = new ArrayList<>();
        for (OguParser.TypeContext t : ctx.t) {
            types.add(toAst(t));
        }
        return types;
    }

    private TraitDeclaration toAst(OguParser.Trait_defContext ctx, List<Decorator> decs) {
        boolean isMutable = ctx.mut != null;
        OguTypeIdentifier name = new OguTypeIdentifier(idText(ctx.name));
        Map<String,OguType> constraints = new HashMap<>();
        if (ctx.constraints != null)
            loadTypeConstraints(ctx.constraints, constraints);
        List<TypeParam> params = getTypeParamList(ctx, ctx.params, constraints);
        List<FunctionalDeclaration> members = internalDeclToAst(ctx.internal_decl());
        TraitDeclaration trait = new TraitDeclaration(name, isMutable, params, members, decs);
        getPositionFrom(trait, ctx);
        return trait;
    }

    private List<FunctionalDeclaration> internalDeclToAst(List<OguParser.Internal_declContext> decls) {
        List<FunctionalDeclaration> members = new ArrayList<>();
        for (OguParser.Internal_declContext decl : decls) {
            members.add(toAst(decl));
        }
        return members;
    }

    private FunctionalDeclaration toAst(OguParser.Internal_declContext ctx) {
        List<Decorator> decs = toAstDecorators(ctx.decorators());

        if (ctx.func_decl() != null)
            return toAst(ctx.func_decl(), decs);
        if (ctx.val_def() != null)
            return toAst(ctx.val_def(), decs);
        if (ctx.func_def() != null)
            return toAst(ctx.func_def(), decs);
        if (ctx.var() != null)
            return toAst(ctx.var(), decs);
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private TypedefDeclaration toAst(OguParser.Type_defContext ctx, List<Decorator> decs) {
        Map<String,OguType> constraints = new HashMap<>();
        if (ctx.constraints != null)
            loadTypeConstraints(ctx.constraints, constraints);


        OguTypeIdentifier name = new OguTypeIdentifier(idText(ctx.t));
        OguType type = toAst(ctx.type());
        getPositionFrom(name, ctx);
        if (ctx.ta == null)  {
            SimpleTypeDeclaration tdecl = new SimpleTypeDeclaration(name, type, decs);
            getPositionFrom(tdecl, ctx);
            return tdecl;
        } else {
            // TODO Warning if are unused contraints
            List<TypeParam> params = getTypeParamList(ctx, ctx.typedef_params().params, constraints);
            GenericTypeDeclaration tdecl = new GenericTypeDeclaration(name, params, type, decs);
            getPositionFrom(tdecl, ctx);
            return tdecl;
        }
    }

    private List<TypeParam> getTypeParamList(ParserRuleContext ctx, List<Token> tokens, Map<String,OguType> constraints) {
        if (ctx == null)
            return Collections.emptyList();

        List<TypeParam> params = new ArrayList<>();
        for (Token token : tokens) {
            String id = idText(token);
            TypeParam p;
            if (constraints.containsKey(id))
                p = new TypeParamConstrained(id, constraints.get(id));
            else
                p = new TypeParam(id);
            getPositionFrom(p, ctx);
            params.add(p);
        }
        return params;
    }

    private void loadTypeConstraints(OguParser.Typedef_args_constraintsContext ctx, Map<String, OguType> cons) {
        for (OguParser.Typedef_arg_constraintContext tac:ctx.tac) {
            OguType type = toAst(tac.type());
            for (Token id : tac.ids) {
                cons.put(idText(id), type);
            }
        }
    }

    private FunctionalDeclaration toAst(OguParser.Func_defContext ctx, List<Decorator> decorators) {
        if (ctx.let_func_name != null) {
            if (ctx.let_func_name.lid_val_id != null) {
                OguIdentifier funcId = OguIdentifier.create(idText(ctx.let_func_name.lid_val_id));
                OguType type = toAst(ctx.let_func_name.t);
                if (ctx.let_func_args != null && !ctx.let_func_args.isEmpty()) {
                    return new ErrorFunctionalDeclaration("error.let_as_val.no_params", getPosition(ctx));
                }
                Expression expr = toAst(ctx.let_expr());
                ValDeclaration val = new ValDeclaration(funcId, type, expr, decorators);
                getPositionFrom(val, ctx);
                return val;
            }
            if (ctx.let_func_name.lid_fun_id != null) {
                OguIdentifier funcId = OguIdentifier.create(idText(ctx.let_func_name.lid_fun_id));
                List<FunctionPatternParam> params = funcArgsToAst(ctx.let_func_args);
                LetDefinition funcdef = new LetDefinition(funcId, params, decorators);
                getPositionFrom(funcdef, ctx);
                if (ctx.let_expr() != null) {
                    toAst(ctx.let_expr(), funcdef);

                }
                return funcdef;
            }
        }
        if (ctx.infix_op != null || ctx.prefix_op != null) {
            List<FunctionPatternParam> params = new ArrayList<>();
            params.add(toAst(ctx.left));
            params.add(toAst(ctx.right));
            OguOperator op = ctx.infix_op != null ? toAst(ctx.infix_op) : toAst(ctx.prefix_op);
            OpDefinition opDef = new OpDefinition(op, params, decorators);
            getPositionFrom(opDef, ctx);
            if (ctx.let_expr() != null) {
                toAst(ctx.let_expr(), opDef);
            }
            return opDef;
        }



        if (!ctx.tup.isEmpty()) {
            List<OguIdentifier> ids = new ArrayList<>();
            Map<OguIdentifier, OguType> types = new HashMap<>();
            lidsToAst(ctx.tup, ids, types);
            Expression value = toAst(ctx.expr());
            TupleValDeclaration decl = new TupleValDeclaration(ids, types, value, decorators);
            getPositionFrom(decl, ctx);
            return decl;
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private void lidsToAst(List<OguParser.LidContext> lids, List<OguIdentifier> ids, Map<OguIdentifier, OguType> types) {
        for(OguParser.LidContext ctx : lids) {
            if (ctx.lid_fun_id !=null) {
                ids.add(OguIdentifier.create(idText(ctx.lid_fun_id)));
            } else {
                OguIdentifier id = OguIdentifier.create(idText(ctx.lid_val_id));
                OguType type = toAst(ctx.type());
                ids.add(id);
                types.put(id, type);
            }
        }
    }

    private Expression toAst(OguParser.Let_exprContext ctx) {
        if (ctx.expr() != null) {
            return toAst(ctx.expr());
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private void toAst(OguParser.Let_exprContext ctx, LetDeclaration funcdef) {
        if (ctx.let_block() != null) {
            toAst(ctx.let_block(), funcdef);
            if (ctx.let_block().where() != null)
                parseWhere(ctx.let_block().where(), funcdef);
            return;
        }
        if (ctx.expr() != null) {
            funcdef.add(toAst(ctx.expr()));
            if (ctx.where() != null)
                parseWhere(ctx.where(), funcdef);
            return;
        }
        if (ctx.guards() != null) {
            for (OguParser.GuardContext guard:ctx.guards().guard()) {
                funcdef.add(toAst(guard));
            }
            if (ctx.where() != null)
                parseWhere(ctx.where(), funcdef);
            if (ctx.guards().where() != null)
                parseWhere(ctx.guards().where(), funcdef);
            return;
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private void parseWhere(OguParser.WhereContext ctx, LetDeclaration funcdef) {
        for (OguParser.Where_exprContext wc:ctx.wl) {
            parseWhereCtx(wc, funcdef);
        }
    }

    private void parseWhereCtx(OguParser.Where_exprContext ctx, LetDeclaration funcdef) {
        if (ctx.i != null) {
            OguIdentifier funcId = OguIdentifier.create(idText(ctx.i));
            List<FunctionPatternParam> params = funcArgsToAst(ctx.let_arg());
            LetDefinition letDef = new LetDefinition(funcId, params, Collections.emptyList());
            getPositionFrom(letDef, ctx);
            if (ctx.let_expr() != null) {
                toAst(ctx.let_expr(), letDef);
            }
            WhereDeclaration where = new WhereDeclaration(letDef);
            getPositionFrom(where, ctx);
            funcdef.add(where);
            return;
        }
        else {
            if (!ctx.tup.isEmpty()) {
                List<OguIdentifier> ids = new ArrayList<>();
                Map<OguIdentifier, OguType> types = new HashMap<>();
                lidsToAst(ctx.tup, ids, types);
                Expression value = toAst(ctx.expr());
                TupleValDeclaration decl = new TupleValDeclaration(ids, types, value, Collections.emptyList());
                getPositionFrom(decl, ctx);
                WhereDeclaration where = new WhereDeclaration(decl);
                getPositionFrom(where, ctx);
                funcdef.add(where);
                return;
            }

        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private GuardDeclaration toAst(OguParser.GuardContext ctx) {
        Expression base = toAst(ctx.be);
        List<Expression> args = new ArrayList<>();
        for (OguParser.ExprContext c : ctx.ae)
            args.add(toAst(c));
        List<Expression> exprs = new ArrayList<>();
        if (ctx.de != null)
            exprs.add(toAst(ctx.de));
        else {
            iterLetDecls(ctx.eb.let_decl(), exprs);
        }
        DoExpression doExpr = new DoExpression(exprs);
        getPositionFrom(doExpr, ctx);
        GuardDeclaration guard = new GuardDeclaration(base, args, doExpr);
        getPositionFrom(guard, ctx);
        return guard;
    }

    private void toAst(OguParser.Let_blockContext ctx, LetDeclaration funcdef) {
        for (OguParser.Let_declContext decl : ctx.ld) {
            if (decl.expr() != null)
                funcdef.add(toAst(decl.expr()));
            else if (decl.func_def() != null)
                funcdef.add(new FunctionNodeDecl(toAst(decl.func_def(),Collections.emptyList())));
            else { ///var
                funcdef.add(new FunctionNodeDecl(toAst(decl.var(), Collections.emptyList())));
            }
        }
    }


    private List<FunctionPatternParam> funcArgsToAst(List<OguParser.Let_argContext> let_func_args) {
        return let_func_args.stream().map(this::toAst).collect(Collectors.toList());
    }

    private FunctionPatternParam toAst(OguParser.Let_argContext ctx) {


        if (ctx.l_atom != null)
            return toAst(ctx.l_atom);

        if (ctx.let_arg_vector() != null)
            return toAst(ctx.let_arg_vector());


        if (ctx.let_arg_tuple_or_list() != null)
            return toAst(ctx.let_arg_tuple_or_list());

        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private FunctionPatternParam toAst(OguParser.Let_arg_tuple_or_listContext ctx) {
        if (ctx.la != null && !ctx.la.isEmpty()) {
            List<FunctionPatternParam> args = new ArrayList<>();
            for (OguParser.Let_arg_atomContext ac:ctx.la)
                args.add(toAst(ac));
            FuncListParam param = new FuncListParam(args);
            getPositionFrom(param, ctx);
            return param;
        }
        if (ctx.ta != null && !ctx.ta.isEmpty()) {
            List<FunctionPatternParam> args = new ArrayList<>();
            for (OguParser.Let_arg_atomContext ac:ctx.ta)
                args.add(toAst(ac));
            FuncTupleParam param = new FuncTupleParam(args);
            getPositionFrom(param, ctx);
            return param;
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private FunctionPatternParam toAst(OguParser.Let_arg_vectorContext ctx) {
        if (ctx.la == null || ctx.la.isEmpty()) {
            FuncEmptyVectorParam param = new FuncEmptyVectorParam();
            getPositionFrom(param, ctx);
            return param;
        }
        List<FunctionPatternParam> args = new ArrayList<>();
        for (OguParser.Let_arg_atomContext ac:ctx.la) {
             args.add(toAst(ac));
        }
        FuncVectorParam param = new FuncVectorParam(args);
        getPositionFrom(param, ctx);
        return param;
    }

    private FunctionPatternParam toAst(OguParser.Let_arg_atomContext ctx) {
        if (ctx.l_id != null) {
            if (ctx.l_id.lid_fun_id != null) {
                FuncIdParam id = toAst(ctx.l_id.lid_fun_id);
                getPositionFrom(id, ctx);
                return id;
            }
            else {
                OguType type = toAst(ctx.l_id.type());
                OguIdentifier id = OguIdentifier.create(idText(ctx.l_id.lid_val_id));
                FuncIdTypeParam param = new FuncIdTypeParam(id, type);
                getPositionFrom(param, ctx);
                return param;
            }
        }
        if (ctx.t_id != null)
        {
            OguTypeIdentifier tid =  OguTypeIdentifier.create(idText(ctx.t_id));
            if (ctx.la == null) {
                FuncTypeParam typeParam = new FuncTypeParam(tid);
                getPositionFrom(typeParam, ctx);
                return typeParam;
            } else {
                List<FunctionPatternParam> args = new ArrayList<>();
                for (OguParser.Let_argContext ac:ctx.la)
                    args.add(toAst(ac));
                FuncGenericTypeParam typeParam = new FuncGenericTypeParam(tid, args);
                getPositionFrom(typeParam, ctx);
                return typeParam;
            }
        }

        if (ctx.a != null) {
            FuncExprParam param = new FuncExprParam(toAst(ctx.atom()));
            getPositionFrom(param, ctx);
            return param;
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private FuncIdParam toAst(Token tok) {
        return new FuncIdParam(OguIdentifier.create(idText(tok)));
    }

    private List<Decorator> toAstDecorators(OguParser.DecoratorsContext decs) {
        if (decs == null)
            return Collections.emptyList();
        return decs.dec.stream().map(this::toAst).collect(Collectors.toList());
    }

    private Decorator toAst(OguParser.DecoratorContext ctx) {
        String decoratorId = idText(ctx.dec_id);
        if ("extern".equals(decoratorId)) {
            List<String> decoratorArgs = ctx.dec_args.stream().map(this::idText).collect(Collectors.toList());
            if (decoratorArgs.size() != 2)
                return new DecoratorError(message("error.decorator.wrong_size_of_arguments"), getPosition(ctx));
            Decorator decorator = new ExternDecorator(decoratorArgs.get(0), decoratorArgs.get(1));
            getPositionFrom(decorator, ctx);
            return decorator;
        }
        if ("primitive".equals(decoratorId)) {
            Decorator decorator = new PrimitiveDecorator();
            getPositionFrom(decorator, ctx);
            return decorator;
        }
        if ("entrypoint".equals(decoratorId)) {
            Decorator decorator = new EntryPointDecorator();
            getPositionFrom(decorator, ctx);
            return decorator;
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private FunctionalDeclaration toAst(OguParser.Func_declContext ctx, List<Decorator> decorators) {
        if (ctx.name.f_id != null) {
            List<TypeArg> params = ctx.func_decl_arg().stream().map(this::toAst).collect(Collectors.toList());
            FunctionDeclaration funcDecl = new FunctionDeclaration(toAst(ctx.name), params, decorators);
            getPositionFrom(funcDecl, ctx);
            return funcDecl;
        }
        if (ctx.name.f_op != null) {
            // op params...
            List<TypeArg> params = ctx.func_decl_arg().stream().map(this::toAst).collect(Collectors.toList());
            OpDeclaration opDecl = new OpDeclaration(toAst(ctx.name.f_op), params, decorators);
            getPositionFrom(opDecl, ctx);
            return opDecl;
        }
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private OguOperator toAst(OguParser.OpContext ctx) {
        OguOperator op = new OguOperator(ctx.getText());
        getPositionFrom(op, ctx);
        return op;
    }

    private TypeArg toAst(OguParser.Func_decl_argContext ctx) {
        if (ctx.unit() != null)
            return new UnitTypeArg();
        if (ctx.fda_id != null)
            return new QualifiedTypeArg(OguTypeIdentifier.create(idText(ctx.fda_id)));
        if (!ctx.fda_tid.isEmpty()) {
            OguTypeIdentifier id = OguTypeIdentifier.create(ctx.fda_tid.stream().map(this::idText).collect(Collectors.toList()));
            if (ctx.tid_or_id_arg == null || ctx.tid_or_id_arg.isEmpty()){
                return new QualifiedTypeArg(id);
            } else {
                List<OguName> args = new ArrayList<>();
                for (OguParser.Tid_or_idContext ti:ctx.tid_or_id_arg)
                    if (ti.i != null)
                        args.add(OguIdentifier.create(idText(ti.i)));
                    else
                        args.add(OguTypeIdentifier.create(idText(ti.t)));
                GenericTypeArg genType = new GenericTypeArg(id, args);
                getPositionFrom(genType, ctx);
                return genType;
            }
        }
        if (ctx.vector() != null) {
            TypeArg arg = toAst(ctx.vector().func_decl_arg());
            VectorTypeArg vec = new VectorTypeArg(arg);
            getPositionFrom(vec, ctx);
            return vec;
        }
        if (ctx.tuple() != null) {
            List<TypeArg> args = new ArrayList<>();
            for (OguParser.Func_decl_argContext ct:ctx.tuple().func_decl_arg())
                args.add(toAst(ct));
            TupleTypeArg tuple = new TupleTypeArg(args);
            getPositionFrom(tuple, ctx);
            return tuple;
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private OguIdentifier toAst(OguParser.Func_name_declContext ctx) {
        if (ctx.f_id != null)
            return new OguIdentifier(idText(ctx.f_id));
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private List<ExportsDeclaration> toAst(OguParser.Module_exportsContext ctx, List<Decorator> decorators) {
        if (ctx.export_name() != null)
            return ctx.exports.stream().map((e) -> toAst(e, decorators)).collect(Collectors.toList());
        throw new UnsupportedOperationException(ctx.toString());
    }


    private List<UsesDeclaration> toAst(OguParser.Module_usesContext ctx, List<Decorator> decs) {
        if (ctx.module_name() != null)
            return ctx.imports.stream().map((i) -> new UsesDeclaration(toAst(i), decs)).collect(Collectors.toList());
        throw new UnsupportedOperationException(ctx.toString());
    }

    private AliasDeclaration toAst(OguParser.Alias_defContext ctx, List<Decorator> decs) {
        OguParser.Alias_targetContext target = ctx.alias_target();
        OguParser.Alias_originContext origin = ctx.alias_origin();
        if (target.alias_tid != null) {
            if (origin.alias_origin_id != null) {
                return new ErrorAlias(message("error.alias.tid_no_tid"), getPosition(ctx));
            }
            TypeAliasDeclaration decl = new TypeAliasDeclaration(toOguTypeIdentifier(target), toOguTypeIdentifier(origin), decs);
            getPositionFrom(decl, ctx);
            return decl;
        } else {
            if (origin.alias_origin_id == null) {
                return new ErrorAlias(message("error.alias.id_no_id"), getPosition(ctx));
            }
            IdAliasDeclaration decl = new IdAliasDeclaration(toOguIdentifier(target), toOguIdentifier(origin), decs);
            getPositionFrom(decl, ctx);
            return decl;
        }
    }


    private VarDeclaration toAst(OguParser.VarContext ctx, List<Decorator> decs) {
        if (ctx.vid().i != null) {
            OguIdentifier id =  OguIdentifier.create(idText(ctx.vid().i));
            VarDeclaration var;
            if (ctx.type() == null) {
                var = new VarDeclaration(id, toAst(ctx.expr()), decs);
            } else {
                if (ctx.expr() != null)
                    var = new VarDeclaration(id, toAst(ctx.type()), toAst(ctx.expr()), decs);
                else
                    var = new VarDeclaration(id, toAst(ctx.type()), decs);
            }
            getPositionFrom(var, ctx);
            return var;
        }
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private ValDeclaration toAst(OguParser.Val_defContext ctx, List<Decorator> decorators) {
        if (ctx.val_id != null) {
            if (ctx.type() == null) {
                ValDeclaration val = new ValDeclaration(OguIdentifier.create(idText(ctx.val_id)), toAst(ctx.expr()), decorators);
                getPositionFrom(val, ctx);
                return val;
            }
            ValDeclaration val = new ValDeclaration(OguIdentifier.create(idText(ctx.val_id)), toAst(ctx.type()), toAst(ctx.expr()), decorators);
            getPositionFrom(val, ctx);
            return val;
        }
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private OguType toAst(OguParser.TypeContext ctx) {
        if (ctx.vector_type() != null) {
            OguType type = toAst(ctx.vector_type().type());
            VectorType vtype = new VectorType(type);
            getPositionFrom(vtype, ctx);
            return vtype;
        }
        if (ctx.unit() != null) {
            UnitType utype = new UnitType();
            getPositionFrom(utype, ctx);
            return utype;
        }
        if (ctx.tuple_type() != null) {
            List<OguType> types = new ArrayList<>();
            for (OguParser.TypeContext type : ctx.tuple_type().type()) {
                types.add(toAst(type));
            }
            TupleType ttype = new TupleType(types);
            getPositionFrom(ttype, ctx);
            return ttype;
        }
        if (ctx.map_type() != null) {
            OguType key = toAst(ctx.map_type().k);
            OguType val = toAst(ctx.map_type().v);
            MapType type = new MapType(key, val);
            getPositionFrom(type, ctx);
            return type;
        }

        if (ctx.anon_record_type() != null) {
            return toAst(ctx.anon_record_type());
        }

        if (ctx.record_type() != null) {
            return toAst(ctx.record_type());
        }

        if (ctx.tid() != null) {
            if (ctx.t_a.isEmpty()) {
                return new QualifiedTypeArg(toAst(ctx.tid()));
            } else {
                List<OguType> args = new ArrayList<>();
                for (OguParser.Tid_argsContext ac:ctx.t_a)
                    args.add(toAst(ac));
                OguTypeIdentifier tName = toAst(ctx.gt);
                GenericType type = new GenericType(tName, args);
                getPositionFrom(type, ctx);
                return type;
            }
        }
        if (ctx.nat != null) {
            OguNativeType type = new OguNativeType(idText(ctx.nat));
            getPositionFrom(type, ctx);
            return type;
        }
        if (ctx.i != null) {
            IdTypeArg type = new IdTypeArg(OguIdentifier.create(idText(ctx.i)));
            getPositionFrom(type, ctx);
            return type;
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }



    private OguType toAst(OguParser.Tid_argsContext ctx) {
        if (ctx.i != null) {
            IdTypeArg type = new IdTypeArg(OguIdentifier.create(idText(ctx.i)));
            getPositionFrom(type, ctx);
            return type;
        }
        if (ctx.type() != null) {
            return toAst(ctx.type());
        }
        if (ctx.tid() != null) {
            QualifiedType type = new QualifiedType(toAst(ctx.tid()));
            getPositionFrom(type, ctx);
            return type;
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private RecordType toAst(OguParser.Record_typeContext ctx) {
        List<RecordField> fields = new ArrayList<>();
        for (OguParser.FldDeclContext fc:ctx.fldDecl()) {
            fields.add(toAst(fc));
        }
        OguTypeIdentifier name = OguTypeIdentifier.create(idText(ctx.ti));
        RecordType record = new RecordType(name, fields);
        getPositionFrom(record, ctx);
        return record;
    }

    private AnonRecordType toAst(OguParser.Anon_record_typeContext ctx) {
        List<RecordField> fields = new ArrayList<>();
        for (OguParser.FldDeclContext fc:ctx.fldDecl()) {
            fields.add(toAst(fc));
        }
        AnonRecordType record = new AnonRecordType(fields);
        getPositionFrom(record, ctx);
        return record;
    }

    private RecordField toAst(OguParser.FldDeclContext ctx) {
        OguIdentifier id = OguIdentifier.create(idText(ctx.i));
        OguType type = toAst(ctx.t);
        RecordField fld = new RecordField(id, type);
        getPositionFrom(fld, ctx);
        return fld;
    }

    private OguTypeIdentifier toAst(OguParser.TidContext ctx) {
        return OguTypeIdentifier.create(ctx.t.stream().map(this::idText).collect(Collectors.toList()));
    }


    private Expression toAst(OguParser.ExprContext ctx) {

        if (ctx.if_expr() != null) {
            return toAst(ctx.if_expr());
        }

        if (ctx.for_expr() != null) {
            return toAst(ctx.for_expr());
        }

        if (ctx.case_expr() != null) {
            return toAst(ctx.case_expr());
        }

        if (ctx.when_expr() != null) {
            return toAst(ctx.when_expr());
        }

        if (ctx.while_expr() != null) {
            return toAst(ctx.while_expr());
        }

        if (ctx.let_in_expr() != null) {
            return toAst(ctx.let_in_expr());
        }

        if (ctx.lambda_expr() != null) {
            return toAst(ctx.lambda_expr());
        }

        if (ctx.yield_expr() != null) {
            return toAst(ctx.yield_expr());
        }

        if (ctx.recur_expr() != null) {
            return toAst(ctx.recur_expr());
        }


        if (ctx.assign_expr() != null) {
            return toAst(ctx.assign_expr());
        }

        if (ctx.paren_expr() != null) {
            return toAst(ctx.paren_expr());
        }

        if (ctx.vector_expr() != null) {
            return toAst(ctx.vector_expr());
        }

        if (ctx.constructor() != null) {
            return toAst(ctx.constructor());
        }

        if (ctx.infix_id != null) {
            Reference name = new Reference(OguIdentifier.create(idText(ctx.infix_id)));
            Expression l = toAst(ctx.l_infix);
            Expression r = toAst(ctx.r_infix);
            List<ActualParam> params = new ArrayList<>();
            params.add(new ActualParam(l));
            params.add(new ActualParam(r));
            FunctionCall call = new FunctionCall(l, params);
            getPositionFrom(call, ctx);
            return call;
        }

        if (ctx.self_id() != null) {
            return toAst(ctx.self_id());
        }

        if (ctx.function != null) {
            return toAstFunctionCall(ctx);
        }


        if (ctx.qual_function != null) {
            return toAstFunctionCall(ctx);
        }
        if (ctx.ref != null) {
            Reference ref = new Reference(OguIdentifier.create(idText(ctx.ref)));
            getPositionFrom(ref, ctx);
            return ref;
        }
        if (ctx.o != null) {
            Expression left = toAst(ctx.l);
            Expression right = toAst(ctx.r);
            BinaryOpExpression expr = new BinaryOpExpression(new OguOperator(ctx.o.getText()), left, right);
            getPositionFrom(expr, ctx);
            return expr;
        }
        if (ctx.primary() != null) {
            return toAst(ctx.primary());
        }

        if (ctx.dict_expr() != null) {
            return toAst(ctx.dict_expr());
        }

        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private SelfRefExpression toAst(OguParser.Self_idContext ctx) {
        SelfRefExpression expr = new SelfRefExpression(OguIdentifier.create(idText(ctx.i)));
        getPositionFrom(expr, ctx);
        return expr;
    }

    private DictExpression toAst(OguParser.Dict_exprContext ctx) {
        if (ctx.map_expr() == null) {
            EmptyMapExpression dic = new EmptyMapExpression();
            getPositionFrom(dic, ctx);
            return dic;
        }
        if (ctx.map_expr().m_arrow != null && !ctx.map_expr().ma.isEmpty()) {
            List<Expression> keys = new ArrayList<>();
            List<Expression> vals = new ArrayList<>();
            for (OguParser.M_arrowContext mc:ctx.map_expr().ma) {
                keys.add(toAst(mc.k));
                vals.add(toAst(mc.v));
            }
            MapExpression map = new MapExpression(keys, vals);
            getPositionFrom(map, ctx);
            return map;
        }
        if (ctx.map_expr().m_assign != null && !ctx.map_expr().mb.isEmpty()) {
            List<FieldExpression> fieldExprs = new ArrayList<>();
            for (OguParser.M_assignContext mc:ctx.map_expr().mb) {
                OguIdentifier id = OguIdentifier.create(idText(mc.i));
                Expression expr = toAst(mc.expr());
                FieldExpression fld = new FieldExpression(id, expr);
                getPositionFrom(fld, ctx.map_expr());
                fieldExprs.add(fld);
            }
            RecordExpression rec = new RecordExpression(fieldExprs);
            getPositionFrom(rec, ctx);
            return rec;
        }

        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private LetInExpression toAst(OguParser.Let_in_exprContext ctx) {
        Map<OguIdentifier, Expression> exprs = new HashMap<>();
        for (OguParser.Let_in_argContext lin:ctx.let_in_arg()) {
            OguIdentifier id = OguIdentifier.create(idText(lin.i));
            Expression expr = toAst(lin.e);
            exprs.put(id, expr);
        }
        LetInExpression expr;
        if (ctx.in_expr == null)
            expr = new LetInExpression(exprs);
        else
            expr = new LetInExpression(exprs, toAst(ctx.in_expr));
        getPositionFrom(expr, ctx);
        return expr;
    }

    private RecurExpression toAst(OguParser.Recur_exprContext ctx) {
        List<Expression> args = new ArrayList<>();
        for (OguParser.ExprContext ce:ctx.expr())
            args.add(toAst(ce));
        RecurExpression expr = new RecurExpression(args);
        getPositionFrom(expr, ctx);
        return expr;
    }

    private YieldExpression toAst(OguParser.Yield_exprContext ctx) {
        YieldExpression expr = new YieldExpression(toAst(ctx.expr()));
        getPositionFrom(expr, ctx);
        return expr;
    }

    private LambdaExpression toAst(OguParser.Lambda_exprContext ctx) {
        List<LambdaArg> args = new ArrayList<>();
        if (ctx.lambda_args() != null) {
            for (OguParser.Lambda_argContext cl:ctx.lambda_args().lambda_arg()) {
                args.addAll(toAst(cl));
            }
        }
        DoExpression doExpr;
        if (ctx.expr() == null)
            doExpr = toAst(ctx.block());
        else
            doExpr = new DoExpression(ImmutableList.of(toAst(ctx.expr())));
        LambdaExpression expr = new LambdaExpression(args, doExpr);
        getPositionFrom(expr, ctx);
        return expr;
    }

    private DoExpression toAst(OguParser.BlockContext ctx) {
        List<Expression> exprs = new ArrayList<>();
        iterLetDecls(ctx.let_decl(), exprs);
        DoExpression doExpr = new DoExpression(exprs);
        getPositionFrom(doExpr, ctx);
        return doExpr;
    }

    private List<LambdaArg> toAst(OguParser.Lambda_argContext ctx) {
        if (ctx.i != null) {
            LambdaArg arg;
            OguIdentifier id = OguIdentifier.create(idText(ctx.i));
            if (ctx.type() == null)
                arg = new LambdaArg(id);
            else
                arg = new LambdaArg(id, toAst(ctx.type()));
            getPositionFrom(arg, ctx);
            return ImmutableList.of(arg);
        }
        List<LambdaArg> args = new ArrayList<>();
        for (OguParser.Lambda_argContext lac:ctx.lambda_arg()) {
            args.addAll(toAst(lac));
        }
        return args;
    }

    private WhileExpression toAst(OguParser.While_exprContext ctx) {
        WhileExpression expr = new WhileExpression(toAst(ctx.expr()), toAst(ctx.do_expression()));
        getPositionFrom(expr, ctx);
        return expr;
    }

    private WhenExpression toAst(OguParser.When_exprContext ctx) {
        WhenExpression expr = new WhenExpression(toAst(ctx.expr()), toAst(ctx.do_expression()));
        getPositionFrom(expr, ctx);
        return expr;
    }

    private AssignExpression toAst(OguParser.Assign_exprContext ctx) {
        AssignExpression expr;
        if (ctx.si != null) {
            OguIdentifier id = OguIdentifier.create(idText(ctx.si));
            if (ctx.a == null)
                expr = new AssignSelfExpression(id, toAst(ctx.e));
            else
                expr = new AssignSelfExpression(id, toAst(ctx.a), toAst(ctx.e));
        } else {
            OguIdentifier id = OguIdentifier.create(idText(ctx.i));
            if (ctx.a == null)
                expr = new AssignExpression(id, toAst(ctx.e));
            else
                expr = new AssignExpression(id, toAst(ctx.a), toAst(ctx.e));
        }
        getPositionFrom(expr, ctx);
        return expr;
    }

    private Expression toAst(OguParser.For_exprContext ctx) {
        SetConstraint forCond = toAst(ctx.set_constraint_expr());
        DoExpression doExpr = toAst(ctx.do_expression());
        ForExpression forExpr = new ForExpression(forCond, doExpr);
        getPositionFrom(forExpr, ctx);
        return forExpr;
    }

    private DoExpression toAst(OguParser.Do_expressionContext ctx) {
        List<Expression> exprs = new ArrayList<>();
        if (ctx.block() == null) {
            exprs.add(toAst(ctx.expr()));
        } else {
            iterLetDecls(ctx.block().let_decl(), exprs);
        }
        DoExpression doExpr = new DoExpression(exprs);
        getPositionFrom(doExpr, ctx);
        return doExpr;
    }

    private VarDeclExpression toAst(OguParser.VarContext ctx) {
        VarDeclExpression expr = new VarDeclExpression(toAst(ctx, Collections.emptyList()));
        getPositionFrom(expr, ctx);
        return expr;
    }

    private ValDeclExpression toAst(OguParser.Val_defContext ctx) {
        ValDeclExpression expr = new ValDeclExpression(toAst(ctx, Collections.emptyList()));
        getPositionFrom(expr, ctx);
        return expr;
    }

    private FuncDeclExpression toAst(OguParser.Func_defContext ctx) {
        FuncDeclExpression expr = new FuncDeclExpression(toAst(ctx, Collections.emptyList()));
        getPositionFrom(expr, ctx);
        return expr;
    }

    private ListExpression toAst(OguParser.Vector_exprContext ctx) {
        if (ctx.list_expr() == null) {
            EmptyListExpression empty = new EmptyListExpression();
            getPositionFrom(empty, ctx);
            return empty;
        }
        return toAst(ctx.list_expr());
    }

    private ListExpression toAst(OguParser.List_exprContext ctx) {
        if (ctx.e == null) {
            List<RangeExpression> ranges = new ArrayList<>();
            for (OguParser.Range_exprContext r : ctx.le) {
                ranges.add(toAst(r));
            }
            ListByExtensionExpression lexpr = new ListByExtensionExpression(ranges);
            getPositionFrom(lexpr, ctx);
            return lexpr;
        } else {
            Expression value = toAst(ctx.e);
            List<SetConstraint> constraints = new ArrayList<>();
            for (OguParser.Set_constraint_exprContext sc:ctx.se) {
                constraints.add(toAst(sc));
            }
            ListByComprehensionExpression list = new ListByComprehensionExpression(value, constraints);
            getPositionFrom(list, ctx);
            return list;
        }
    }

    private SetConstraint toAst(OguParser.Set_constraint_exprContext ctx) {
        if (ctx.s_id != null) {
            SetConstraint cons = new SetConstraint(OguIdentifier.create(idText(ctx.s_id)), toAst(ctx.expr()));
            getPositionFrom(cons, ctx);
            return cons;
        } else {
            List<OguIdentifier> ids = ctx.l_id.stream().map((t) -> new OguIdentifier(idText(t))).collect(Collectors.toList());
            SetConstraint cons = new SetConstraint(ids, toAst(ctx.expr()));
            getPositionFrom(cons, ctx);
            return cons;
        }
    }

    private RangeExpression toAst(OguParser.Range_exprContext ctx) {
        if (ctx.end == null) {
            InfiniteRangeExpression range = new InfiniteRangeExpression(toAst(ctx.beg));
            getPositionFrom(range, ctx);
            return range;
        }
        RangeExpression range = new RangeExpression(toAst(ctx.beg), toAst(ctx.end));
        getPositionFrom(range, ctx);
        return range;
    }

    private CaseExpression toAst(OguParser.Case_exprContext ctx) {
        Expression selector = toAst(ctx.s);
        List<CaseGuard> guards = new ArrayList<>();
        for (OguParser.Case_guardContext cgctx : ctx.g.case_guard()) {
            Expression cond = toAst(cgctx.c);
            Expression result = toAst(cgctx.r);
            CaseGuard guard = new CaseGuard(cond, result);
            getPositionFrom(guard, ctx);
            guards.add(guard);
        }
        CaseExpression caseExpr = new CaseExpression(selector, guards);
        getPositionFrom(caseExpr, ctx);
        return caseExpr;
    }

    private IfExpression toAst(OguParser.If_exprContext ctx) {
        Expression cond = toAst(ctx.cond);
        List<Expression> rest = toAst(ctx.then_part());
        IfExpression ifExpr = new IfExpression(cond, rest.get(0), rest.get(1));
        getPositionFrom(ifExpr, ctx);
        return ifExpr;
    }

    private void iterLetDecls(List<OguParser.Let_declContext> decls, List<Expression> exprs) {
        for (OguParser.Let_declContext decl : decls) {
            if (decl.expr() != null)
                exprs.add(toAst(decl.expr()));
            else if (decl.func_def() != null)
                exprs.add(toAst(decl.func_def()));
            else if (decl.val_def() != null)
                exprs.add(toAst(decl.val_def()));
            else {  ///var
                exprs.add(toAst(decl.var()));
            }
        }
    }

    private List<Expression> toAst(OguParser.Then_partContext ctx) {
        List<Expression> ifElems = new ArrayList<>();
        if (ctx.tb != null) {
            List<Expression> exprs = new ArrayList<>();
            iterLetDecls(ctx.tb.let_decl(), exprs);
            DoExpression doExpr = new DoExpression(exprs);
            getPositionFrom(doExpr, ctx);
            ifElems.add(doExpr);
        }
        else { // tb == null
            ifElems.add(toAst(ctx.te));
        }
        ifElems.add(toAst(ctx.else_part()));
        return ifElems;
    }

    private Expression toAst(OguParser.Else_partContext ctx) {
        if (ctx.eb == null) {
            return toAst(ctx.e);
        }
        else {
            List<Expression> exprs = new ArrayList<>();
            iterLetDecls(ctx.eb.let_decl(), exprs);
            DoExpression doExpr = new DoExpression(exprs);
            getPositionFrom(doExpr, ctx);
            return doExpr;
        }
    }

    private Constructor toAst(OguParser.ConstructorContext ctx) {
        TypeReference type = new TypeReference(toAst(ctx.tid()));
        getPositionFrom(type, ctx);
        List<ActualParam> params;
        if (ctx.tuple_expr() == null)
            params = Collections.emptyList();
        else
            params = ctx.tuple_expr().expr().stream().map(this::toAstParam).collect(Collectors.toList());
        Constructor ctor = new Constructor(type, params);
        getPositionFrom(ctor, ctx);
        return ctor;
    }

    private ActualParam toAstParam(OguParser.ExprContext ctx) {
        ActualParam param = new ActualParam(toAst(ctx));
        getPositionFrom(param, ctx);
        return param;
    }

    private Expression toAst(OguParser.Paren_exprContext ctx) {
        if (ctx.tuple_expr() != null) {
            return toAst(ctx.tuple_expr());
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private Expression toAst(OguParser.Tuple_exprContext ctx) {
        if (ctx.e.size() == 1)
            return toAst(ctx.e.get(0));
        List<Expression> exprs = new ArrayList<>();
        for (OguParser.ExprContext ec : ctx.e) {
            exprs.add(toAst(ec));
        }
        TupleExpression tuple = new TupleExpression(exprs);
        getPositionFrom(tuple, ctx);
        return tuple;
    }

    private Expression toAst(OguParser.PrimaryContext ctx) {
        if (ctx.atom() != null) {
            return toAst(ctx.atom());
        }
        return toAst(ctx.neg_expr());
    }

    private NegExpression toAst(OguParser.Neg_exprContext ctx) {
        NegExpression ne;
        if (ctx.e != null)
            ne = new NegExpression(toAst(ctx.e));
        else {
            if (ctx.a.d != null || ctx.a.string_literal != null)
                return new NegExpressionError(message("error.expr.neg"), getPosition(ctx));
            ne = new NegExpression(toAst(ctx.a));
        }
        getPositionFrom(ne, ctx);
        return ne;
    }

    private Expression toAst(OguParser.Func_nameContext ctx) {
        Reference id = new Reference(new OguIdentifier(idText(ctx.name)));
        getPositionFrom(id, ctx);
        return id;
    }

    private Expression toAst(OguParser.Qual_func_nameContext ctx) {
        if (ctx.name == null) {
            OguTypeIdentifier tname = OguTypeIdentifier.create(ctx.qual.stream().map(Token::getText).collect(Collectors.toList()));
            TypeReference ref = new TypeReference(tname);
            getPositionFrom(ref, ctx);
            return ref;
        } else {
            OguIdentifier tname = OguIdentifier.create(ctx.qual.stream().map(Token::getText).collect(Collectors.toList()), idText(ctx.name));
            Reference id = new Reference(tname);
            getPositionFrom(id, ctx);
            return id;
        }
    }


    private Expression toAst(OguParser.AtomContext ctx) {
        if (ctx.string_literal != null) {
            StringLiteral lit = new StringLiteral(idText(ctx.STRING().getSymbol()));
            getPositionFrom(lit, ctx);
            return lit;
        }
        if (ctx.INT() != null) {
            String itxt = ctx.INT().getText().replace("_", "");
            BigInteger bi = new BigInteger(itxt);
            IntLiteral lit = new IntLiteral(bi);
            getPositionFrom(lit, ctx);
            return lit;
        }
        if (ctx.CHAR() != null) {
            String ctxt = ctx.CHAR().getText();
            CharLiteral cl = new CharLiteral(ctxt);
            getPositionFrom(cl, ctx);
            return cl;
        }

        if (ctx.FLOAT() != null) {
            String dtxt = ctx.FLOAT().getText().replace("_", "");
            BigDecimal bd = new BigDecimal(dtxt);
            FloatLiteral lit = new FloatLiteral(bd);
            getPositionFrom(lit, ctx);
            return lit;
        }
        if (ctx.DATE() != null) {
            String dtxt = ctx.DATE().getText();
            DateLiteral lit = new DateLiteral(dtxt);
            getPositionFrom(lit, ctx);
            return lit;
        }
        Logger.debug(ctx.getText());
        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }

    private FunctionCall toAstFunctionCall(OguParser.ExprContext ctx) {
        if (ctx.function != null) {
            Expression function = toAst(ctx.function);
            FunctionCall functionCall = new FunctionCall(function, ctx.expr().stream().map(this::toAstParam).collect(Collectors.toList()));
            getPositionFrom(functionCall, ctx);
            return functionCall;

        }
        if (ctx.qual_function != null) {
            Expression function = toAst(ctx.qual_function);
            FunctionCall functionCall = new FunctionCall(function, ctx.expr().stream().map(this::toAstParam).collect(Collectors.toList()));
            getPositionFrom(functionCall, ctx);
            return functionCall;
        }

        throw new UnsupportedOperationException(ctx.getClass().getCanonicalName());
    }


    private String idText(Token token) {
        return token.getText();
    }

    private String buildModuleNameFromFileName(String name) {
        int pos = name.indexOf('.');
        return Character.toUpperCase(name.charAt(0)) + name.substring(1, pos);
    }

}