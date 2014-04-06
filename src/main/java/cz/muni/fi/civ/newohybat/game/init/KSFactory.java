package cz.muni.fi.civ.newohybat.game.init;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.TransactionManager;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.RuleBaseConfiguration;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.drools.event.rule.ActivationCreatedEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.event.rule.DefaultWorkingMemoryEventListener;
import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.RuleFlowGroupActivatedEvent;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.jbpm.persistence.JpaProcessPersistenceContextManager;
import org.jbpm.persistence.jta.ContainerManagedTransactionManager;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;

@ApplicationScoped
public class KSFactory {
	@PersistenceUnit(unitName="org.jbpm.persistence.jpa")
	private EntityManagerFactory emf;
//	@Inject
//	TransactionManager tm;
	@Produces @ApplicationScoped
	public StatefulKnowledgeSession getKnowledgeSession(KnowledgeBase kbase){
//		Environment env = KnowledgeBaseFactory.newEnvironment();
//		
//		env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,emf);
//		env.set(EnvironmentName.TRANSACTION_MANAGER, new ContainerManagedTransactionManager());
//        env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new JpaProcessPersistenceContextManager(env));
//		// KnowledgeSessionConfiguration may be null, and a default will be used
//
//		StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession( kbase, null, env );
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession(); 
		appendListeners(ksession);
		
		return ksession;
	}
	
	protected void appendListeners(StatefulKnowledgeSession ksession){
    	ksession.addEventListener(
                new DefaultAgendaEventListener()
                {
                    @Override
                    public void activationCreated(ActivationCreatedEvent event) {
                        ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                    }
                    @Override
                    public void afterRuleFlowGroupActivated(
                    		RuleFlowGroupActivatedEvent event) {
//                    	System.out.println(event);
//                    	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                    }
                    
                }
                );
        ksession.addEventListener(new DefaultProcessEventListener() 
        {
            @Override
            public void afterProcessStarted(ProcessStartedEvent event) {
//            	System.out.println(event);
                event.getKnowledgeRuntime().insert(((WorkflowProcessInstance)event.getProcessInstance()));
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event){
            	if(event.getProcessInstance().getState()==WorkflowProcessInstance.STATE_COMPLETED){
	            	WorkflowProcessInstance wpi = (WorkflowProcessInstance)event.getProcessInstance();
	            	VariableScopeInstance variableScope = (VariableScopeInstance) wpi.getContextInstance(VariableScope.VARIABLE_SCOPE);
	            	Map<String,Object> variables = variableScope.getVariables();
	            	for(Object o:variables.values()){
	            		FactHandle h = event.getKnowledgeRuntime().getFactHandle(o);
	            		if(h!=null){
	            			event.getKnowledgeRuntime().update(h, o);
	            		}
	            	}
            	}
            	event.getKnowledgeRuntime().retract(event.getKnowledgeRuntime().getFactHandle(event.getProcessInstance()));
	            	
//            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            	
            }
            @Override
            public void beforeVariableChanged(ProcessVariableChangedEvent event) {
//            	System.out.println(event);
            }
            @Override
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
            }
        }
        );
        ksession.addEventListener(new DefaultWorkingMemoryEventListener() 
        {
            @Override
            public void objectUpdated(ObjectUpdatedEvent event) {
            	//System.out.println(">>> Object updated! " + event);
//            	System.out.println(event);
            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
            @Override
            public void objectInserted(ObjectInsertedEvent event) {
//            	System.out.println(event);
//            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
            @Override
            public void objectRetracted(ObjectRetractedEvent event) {
            	// TODO Auto-generated method stub
//            	System.out.println(event);
//            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
//            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).getAgenda().clear();
            }
        }
        );
    }
}
