package cz.muni.fi.civ.newohybat.bpmn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;

import org.drools.KnowledgeBase;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessEventListener;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.event.process.ProcessVariableChangedEvent;
import org.drools.event.rule.ActivationCreatedEvent;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.event.rule.DefaultWorkingMemoryEventListener;
import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.RuleFlowGroupActivatedEvent;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.Assert;


public abstract class BaseJUnitTest {
	protected static Logger logger = Logger.getAnonymousLogger();
	protected StatefulKnowledgeSession ksession;

    protected EntityManagerFactory emf;
    protected KnowledgeBase kbase;
	
    public BaseJUnitTest() {
        prepareTestEnv();
    }
    public void prepareTestEnv(){
//    	emf = Persistence.createEntityManagerFactory("org.jbpm.task");
//        taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
//        taskSession = taskService.createSession();
//        
//        addUsersAndGroups(taskSession);
//
//        disableUserGroupCallback();
        		
    }
    public void closeTestEnv(){
//    	localTaskService.dispose();
//    	taskSession.dispose();
    	ksession.dispose();
//    	emf.close();
    }
    
    protected void setSessions(KnowledgeBase kbase){
    	ksession = kbase.newStatefulKnowledgeSession();
        
//        CustomProcessEventListener customProcessEventListener = new CustomProcessEventListener();
//		ksession.addEventListener(customProcessEventListener);
//        
//        LocalHTWorkItemHandler handler = new LocalHTWorkItemHandler(ksession);
//        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
//        ksession.addEventListener(new DebugAgendaEventListener());
//        ksession.addEventListener(new DebugWorkingMemoryEventListener());
        
        //ksession.getWorkItemManager().registerWorkItemHandler("Unit", new UnitWorkItemHandler());
//        localTaskService = new LocalTaskService(taskService);
//        handler.setClient(localTaskService);
    	appendListeners();
    }
    protected void appendListeners(){
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
            	FactHandle fh = event.getKnowledgeRuntime().getFactHandle(event.getProcessInstance());
            	if(fh!=null){
            		event.getKnowledgeRuntime().retract(fh);
            	}
	            	
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

    protected List<String> getFiredRules(List<AfterActivationFiredEvent> events){
    	List<String> runRules = new ArrayList<String>();
		for(AfterActivationFiredEvent e:events){
			runRules.add(e.getActivation().getRule().getName());
		}
		return runRules;
    }
    
    public void assertProcessInstanceCompleted(long processInstanceId, StatefulKnowledgeSession ksession) {
    		Assert.assertNull(ksession.getProcessInstance(processInstanceId));
    	}
    	
    	public void assertProcessInstanceAborted(long processInstanceId, StatefulKnowledgeSession ksession) {
    		Assert.assertNull(ksession.getProcessInstance(processInstanceId));
    	}
    	
    	public void assertProcessInstanceActive(long processInstanceId, StatefulKnowledgeSession ksession) {
    			Assert.assertNotNull(ksession.getProcessInstance(processInstanceId));
    	}
	
	protected static class CustomProcessEventListener implements ProcessEventListener {

		public void afterNodeLeft(ProcessNodeLeftEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void afterNodeTriggered(ProcessNodeTriggeredEvent arg0) {
		}

		public void afterProcessCompleted(ProcessCompletedEvent event) {
			// TODO Auto-generated method stub
//			logger.log(Level.INFO, "Process has completed.");
//			logger.log(Level.INFO,"hej");
		}

		public void afterProcessStarted(ProcessStartedEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void afterVariableChanged(ProcessVariableChangedEvent arg0) {
			// TODO Auto-generated method stub
			logger.log(Level.INFO,arg0.toString());
		}

		public void beforeNodeLeft(ProcessNodeLeftEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void beforeNodeTriggered(ProcessNodeTriggeredEvent arg0) {
			logger.log(Level.INFO,arg0.toString());
		}

		public void beforeProcessCompleted(ProcessCompletedEvent arg0) {
//			// TODO Auto-generated method stub
//			logger.log(Level.INFO, "Process is going to be completed.");
		}

		public void beforeProcessStarted(ProcessStartedEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void beforeVariableChanged(ProcessVariableChangedEvent arg0) {
			// TODO Auto-generated method stub
			//logger.log(Level.INFO,arg0.toString());
		}
		
	}
	
}