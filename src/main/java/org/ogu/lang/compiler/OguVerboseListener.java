package org.ogu.lang.compiler;

import java.util.*;
import org.antlr.v4.runtime.*;
import org.ogu.lang.util.Feedback;

public  class OguVerboseListener extends BaseErrorListener {

	private int errores = 0;

	public int getErrores() { return errores; }

	@Override
	public void syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
		List<String> stack = ((org.antlr.v4.runtime.Parser) recognizer).getRuleInvocationStack();
		Collections.reverse(stack);
		Feedback.error(ERROR_BEGIN);
		Feedback.error("rule stack: "+stack);
		Feedback.error("linea "+line+":"+charPositionInLine+" posición "+offendingSymbol+": "+msg+"\n");
		Feedback.error(ERROR_END);
		++errores;
	}

	private static final String ERROR_BEGIN = "Mi tanto nojao! (tienes errores):\n";
	private static final String ERROR_END = "Te boya sakar la ñoña kabro chiko!\n";
}