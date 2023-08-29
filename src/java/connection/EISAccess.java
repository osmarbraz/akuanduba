package connection;
import java.util.Collection;
import java.util.Map;

import cartago.Artifact;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;
import eis.AgentListener;
import eis.EnvironmentInterfaceStandard;
import eis.EnvironmentListener;
import eis.PerceptUpdate;
import eis.exceptions.AgentException;
import eis.exceptions.ManagementException;
import eis.exceptions.RelationException;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.Percept;
import jason.asSyntax.Literal;
import massim.eismassim.EnvironmentInterface;

/* **
 * Classe que faz a interface entre o IESMassim e o Cartago.
 * Converte as percepções em propriedades observáveis.
 * É necessário um artefato por agente.
 * 
 */

public class EISAccess extends Artifact implements AgentListener {
    
    private EnvironmentInterfaceStandard ei;
    String agtName;
    Translator translator = new Translator();
    
    void init(String conf) {
        this.ei = new EnvironmentInterface(conf);

        try {
            ei.start();
        } catch (ManagementException e) {
            e.printStackTrace();
        }

        ei.attachEnvironmentListener(new EnvironmentListener() {
            public void handleNewEntity(String entity) {
            }

            public void handleStateChange(EnvironmentState s) {
            }

            public void handleDeletedEntity(String arg0, Collection<String> arg1) {
            }

            public void handleFreeEntity(String arg0, Collection<String> arg1) {
            }
        });

        this.agtName=(String) ei.getEntities().toArray()[0];

        try {
            ei.registerAgent( this.agtName);
        } catch (AgentException e1) {
            e1.printStackTrace();
        }
        ei.attachAgentListener( this.agtName, this);

        try {
            ei.associateEntity( this.agtName,  this.agtName);
        } catch (RelationException e1) {
            e1.printStackTrace();
        }
        execInternalOp("getPercepts");
    }
    /* **
     * Operação interna do artefato, 
     * a cada 50 milissegundos é feita uma atualização das percepeções
     */
    @INTERNAL_OPERATION void getPercepts() {         
        while(true) {
            await_time(50);
            Map<String, PerceptUpdate> perMap;
            if (ei != null) {       
                try {
                    perMap = ei.getPercepts(this.agtName);                    
                    String k=perMap.keySet().stream().findFirst().get();
                    for (Percept p:perMap.get(k).getDeleteList()) {
                        Literal percept = translator.perceptToLiteral(p);
                        //Remove propriedades observaveis que não são mais perceptiveis
                        removeObsPropertyByTemplate(percept.getFunctor(), percept.getTermsArray()); 
                    }
                    for (Percept p:perMap.get(k).getAddList()) {
                        Literal percept = translator.perceptToLiteral(p);
                        //Adiciona propriedades observaveis percebidas
                        defineObsProperty(percept.getFunctor(), percept.getTermsArray());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }                      
            }            
        }        
    }  
    /* **
     * Operação do artefato, 
     * a ação recebida é traduzida e enviada ao EISMassim
     */
    @OPERATION void action(String action) {
        Literal literal = Literal.parseLiteral(action);
        Action a = null;
        try {
            if (ei != null) {
                a = translator.literalToAction(literal);
                ei.performAction(this.agtName, a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void handlePercept(String arg0, Percept arg1) {}
}
