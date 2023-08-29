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

    /**
     * Converte uma percepção do IESMassim em um Literal do Jason
     */
    public Literal perceptToLiteral(Percept per) throws JasonException {
        Literal l = null;
        String perceptETL = per.toProlog().toLowerCase();
        perceptETL = perceptETL.replace(",)", ",null)");
        perceptETL = perceptETL.replace("()", "(null)");
        try {
            l = ASSyntax.parseLiteral(perceptETL);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return l;
    }

    /**
     * Converte um Literal do Jason em uma percepção do IESMassim
     */
    public  Percept literalToPercept(jason.asSyntax.Literal l) throws NoValueException {
        // Inicializa a variável tmp com o tamanho do literal
        Parameter tmp[] = new Parameter[l.getTerms().size()];
        // Percorre os termos do literal e converte para parâmetros
        for (int i=0; i<l.getTerms().size();i++) {
            tmp[i] = termToParameter(l.getTerm(i));
        }
        // Instancia a percepção
        Percept p = new Percept(l.getFunctor(),tmp);
        return p;
    }

    /**
     * Converte uma ação do IESMassim em um Literal do Jason
     */
    public Action literalToAction(Literal action) throws NoValueException {
        // Inicializa a variável pars com o tamanho da ação
        Parameter[] pars = new Parameter[action.getArity()];
        // Percorre os termos da ação e converte para parâmetros
        for (int i = 0; i < action.getArity(); i++) {
            pars[i] = termToParameter(action.getTerm(i));
        }
        // Retorna a ação
        return new Action(action.getFunctor(), pars);
    }

    /**
     * Converte um Literal do Jason em uma ação do IESMassim
     */
    public  Parameter termToParameter(Term t) throws NoValueException {
        // Verifica o tipo do termo
        if (t.isNumeric()) {
            double d = ((NumberTerm) t).solve();
            // Verifica se é um inteiro
            if((d == Math.floor(d)) && !Double.isInfinite(d)) {
                return new Numeral((int)d);
            }
            return new Numeral(d);
        } else { 
            // Verifica se é uma lista
            if (t.isList()) {
                ListTerm lt = (ListTerm) t;
                Parameter[] terms = new Parameter[lt.size()];
                // Percorre os termos da lista e converte para parâmetros
                for (int i=0; i < lt.size(); i++) {
                    terms[i] = termToParameter(lt.get(i));
                }
                return new ParameterList(terms);
            } else {
                // Verifica se é uma string
                if (t.isString()) {
                    return new Identifier(((StringTerm) t).getString());
                } else { 
                    // Verifica se é um literal
                    if (t.isLiteral()) {
                        Literal l = (Literal) t;
                        // Verifica se é um literal sem termos
                        if (!l.hasTerm()) {
                            return new Identifier(l.getFunctor());
                        } else {
                            Parameter[] terms = new Parameter[l.getArity()];
                            for (int i = 0; i < l.getArity(); i++) {
                                terms[i] = termToParameter(l.getTerm(i));
                            }
                            return new Function(l.getFunctor(), terms);
                        }
                    }
                }
            }
        }
        return new Identifier(t.toString());
    }
}