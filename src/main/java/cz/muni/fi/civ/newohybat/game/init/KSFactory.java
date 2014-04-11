package cz.muni.fi.civ.newohybat.game.init;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.TransactionManager;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.RuleBaseConfiguration;
import org.drools.event.DebugProcessEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEventListener;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.drools.event.rule.ActivationCancelledEvent;
import org.drools.event.rule.ActivationCreatedEvent;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.AgendaGroupPoppedEvent;
import org.drools.event.rule.AgendaGroupPushedEvent;
import org.drools.event.rule.BeforeActivationFiredEvent;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.event.rule.DefaultWorkingMemoryEventListener;
import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.RuleFlowGroupActivatedEvent;
import org.drools.event.rule.RuleFlowGroupDeactivatedEvent;
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
		Environment env = KnowledgeBaseFactory.newEnvironment();
		InitialContext ic;
		TransactionManager man = null;
		try {
			ic = new InitialContext();
			man = ((TransactionManager)ic.lookup("java:jboss/TransactionManager"));
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
//		env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,emf);
//		env.set(EnvironmentName.TRANSACTION_MANAGER, man);
//        env.set(EnvironmentName.PERSISTENCE_CONTEXT_MANAGER, new JpaProcessPersistenceContextManager(env));
//		// KnowledgeSessionConfiguration may be null, and a default will be used
//
//		StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession( kbase, null, env );
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession(); 
		appendListeners(ksession);
		ksession.addEventListener(new DebugProcessEventListener());
		ksession.addEventListener(new DebugAgendaEventListener());
		ksession.addEventListener(new DebugWorkingMemoryEventListener());
		return ksession;
	}
	
	protected void appendListeners(StatefulKnowledgeSession ksession){
    	ksession.addEventListener(
                new AgendaEventListener() {
					
					public void beforeRuleFlowGroupDeactivated(
							RuleFlowGroupDeactivatedEvent event) {
						// TODO Auto-generated method stub
						
					}
					
					public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
						// TODO Auto-generated method stub
						
					}
					
					public void beforeActivationFired(BeforeActivationFiredEvent event) {
						// TODO Auto-generated method stub
						
					}
					
					public void agendaGroupPushed(AgendaGroupPushedEvent event) {
						// TODO Auto-generated method stub
						
					}
					
					public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
						// TODO Auto-generated method stub
						
					}
					
					public void afterRuleFlowGroupDeactivated(
							RuleFlowGroupDeactivatedEvent event) {
						// TODO Auto-generated method stub
						
					}
					
					public void afterRuleFlowGroupActivated(
                    		RuleFlowGroupActivatedEvent event) {
//                    	System.out.println(event);
                    	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                    }
					
					public void afterActivationFired(AfterActivationFiredEvent event) {
						// TODO Auto-generated method stub
						
					}
					
					public void activationCreated(ActivationCreatedEvent event) {
                        ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                    }
					
					public void activationCancelled(ActivationCancelledEvent event) {
						// TODO Auto-generated method stub
						
					}
				});
        ksession.addEventListener(new ProcessEventListener() {
			
			public void beforeVariableChanged(ProcessVariableChangedEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			public void beforeProcessStarted(ProcessStartedEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			public void beforeProcessCompleted(ProcessCompletedEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			public void beforeNodeLeft(ProcessNodeLeftEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			public void afterVariableChanged(ProcessVariableChangedEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			public void afterProcessStarted(ProcessStartedEvent event) {
//            	System.out.println(event);
                event.getKnowledgeRuntime().insert(((WorkflowProcessInstance)event.getProcessInstance()));
                ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            }
			
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
            	FactHandle fh = event.getKnowledgeRuntime().getFactHandle(event.getProcessInstance());
            	if(fh!=null){
            		event.getKnowledgeRuntime().retract(fh);
            	}	
//            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            	
            }
			
			public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
				// TODO Auto-generated method stub
				
			}
			
			public void afterNodeLeft(ProcessNodeLeftEvent event) {
				// TODO Auto-generated method stub
				
			}
		}); 
        
//        ksession.addEventListener(new DefaultWorkingMemoryEventListener() 
//        {
//            @Override
//            public void objectUpdated(ObjectUpdatedEvent event) {
//            	//System.out.println(">>> Object updated! " + event);
////            	System.out.println(event);
//            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
//            }
//            @Override
//            public void objectInserted(ObjectInsertedEvent event) {
////            	System.out.println(event);
////            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
//            }
//            @Override
//            public void objectRetracted(ObjectRetractedEvent event) {
//            	// TODO Auto-generated method stub
////            	System.out.println(event);
////            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
////            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).getAgenda().clear();
//            }
//        }
//        );
    }
}
