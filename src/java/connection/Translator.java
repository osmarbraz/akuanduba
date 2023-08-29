package connection;

import eis.iilang.Action;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import jason.JasonException;
import jason.NoValueException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Term;

/* **
 * Classe que faz o casting entre percepções do IESMassim e o Literal do Jason
 */

public class Translator {

    public  Literal perceptToLiteral(Percept per) throws JasonException {
        Literal l=null;
        String perceptETL = per.toProlog().toLowerCase();
        perceptETL=  perceptETL.replace(",)", ",null)");
        perceptETL=  perceptETL.replace("()", "(null)");
        try {
            l = ASSyntax.parseLiteral(perceptETL);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return l;
    }

    public  Percept literalToPercept(jason.asSyntax.Literal l) throws NoValueException {
        Parameter tmp[] = new Parameter[l.getTerms().size()];
        for (int i=0; i<l.getTerms().size();i++)
            tmp[i]=termToParameter(l.getTerm(i));
        Percept p = new Percept(l.getFunctor(),tmp);
        return p;
    }

    public  Action literalToAction(Literal action) throws NoValueException {
        Parameter[] pars = new Parameter[action.getArity()];
        for (int i = 0; i < action.getArity(); i++)
            pars[i] = termToParameter(action.getTerm(i));
        return new Action(action.getFunctor(), pars);
    }


    public  Parameter termToParameter(Term t) throws NoValueException {
        if (t.isNumeric()) {
            double d = ((NumberTerm) t).solve();
            if((d == Math.floor(d)) && !Double.isInfinite(d)) return new Numeral((int)d);
            return new Numeral(d);
        } else if (t.isList()) {
            ListTerm lt=(ListTerm) t;
            Parameter[] terms = new Parameter[lt.size()];
            for (int i=0;i<lt.size();i++)
                terms[i]=termToParameter(lt.get(i));
            return new ParameterList(terms);
        } else if (t.isString()) {
            return new Identifier(((StringTerm) t).getString());
        } else if (t.isLiteral()) {
            Literal l = (Literal) t;
            if (!l.hasTerm()) {
                return new Identifier(l.getFunctor());
            } else {
                Parameter[] terms = new Parameter[l.getArity()];
                for (int i = 0; i < l.getArity(); i++)
                    terms[i] = termToParameter(l.getTerm(i));
                return new Function(l.getFunctor(), terms);
            }
        }
        return new Identifier(t.toString());
    }
}