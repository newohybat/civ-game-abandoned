package cz.muni.fi.civ.newohybat.bpmn;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drools.KnowledgeBase;
import org.drools.base.RuleNameMatchesAgendaFilter;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.PropertySpecificOption;
import org.drools.event.DebugProcessEventListener;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import cz.muni.fi.civ.newohybat.drools.events.CityImprovementEvent;
import cz.muni.fi.civ.newohybat.drools.events.TurnEvent;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;


public class CityImprovementRulesJUnitTest extends BaseJUnitTest {

	private static KnowledgeBase kbase;
	public CityImprovementRulesJUnitTest() throws Exception{
	}
	@BeforeClass
	public static void setKnowledgeBase() throws Exception {
		System.out.println("Loading knowledge base.");
		KnowledgeBuilderConfiguration config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        config.setOption(PropertySpecificOption.ALWAYS);
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(config);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/cityImprovementRules.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("processes/buildCityImprovement.bpmn"), ResourceType.BPMN2);
		
		kbase = kbuilder.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
	}
	@Before
	public void before(){
		setSessions(kbase);
		
	}
	@After
	public void after(){
		ksession.dispose();
	}
	private static CityDTO getCity(Long id, String name){
		CityDTO city = new CityDTO();
    	city.setId(id);
    	city.setName(name);
    	city.setResourcesConsumption(0);
    	city.setResourcesProduction(0);
    	city.setUnitsSupport(0);
    	city.setFoodConsumption(0);
    	city.setFoodProduction(0);
    	city.setFoodStock(0);
    	city.setSize(0);
    	city.setTradeProduction(0);
    	city.setPeopleEntertainers(0);
		city.setPeopleScientists(0);
		city.setPeopleTaxmen(0);
		city.setWeLoveDay(false);
		city.setDisorder(false);
		city.setSize(1);
		city.setPeopleHappy(0);
		city.setPeopleContent(0);
		city.setPeopleUnhappy(0);
		city.setImprovements(new HashSet<String>());
    	return city;
	}
	
    private static PlayerDTO getPlayer(Long id, String name){
		PlayerDTO player = new PlayerDTO();
		player.setId(id);
		player.setName(name);
		player.setLuxuriesRatio(0);
		player.setTaxesRatio(0);
		player.setResearchRatio(0);
		player.setResearch(0);
		return player;
	}
    private static CityImprovementDTO getImprovement(String ident, Integer constructionCost){
    	CityImprovementDTO imp = new CityImprovementDTO();
    	imp.setConstructionCost(constructionCost);
    	imp.setIdent(ident);
    	return imp;
    }
    
    @Test
    public void testWaitForNewTurnToComplete(){
    	ksession.addEventListener(new DebugAgendaEventListener());
    	ksession.addEventListener(new DebugProcessEventListener());
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	improvements.add("granary");
    	improvements.add("bank");
    	city.setImprovements(improvements);
    	city.setCurrentImprovement("courthouse");
    	city.setResourcesSurplus(300);
    	CityImprovementDTO courtHouse = getImprovement("courthouse",250);
		// insert test data as facts
		ksession.insert(courtHouse);
		ksession.insert(city);
		
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Build City Improvement"));
		List<ProcessInstance> processes = (List<ProcessInstance>)ksession.getProcessInstances();
		
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		// rule "Build Irrigation" didn't fire
		Assert.assertTrue(firedRules.contains("Build City Improvement"));
		
		Assert.assertTrue("One Process Should Be Active",processes.size()==1);
		Long pId = processes.get(0).getId();
		
		assertProcessInstanceActive(pId, ksession);
		
		ksession.signalEvent("turn-new", null);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertProcessInstanceCompleted(pId, ksession);
		
		Assert.assertTrue("City Contains Courthouse Improvement.",city.getImprovements().contains("courthouse"));
		
		Assert.assertTrue("No Pending Processes Are There",ksession.getProcessInstances().size()==0);
	}
    
    @Test
    public void testCancelImprovement(){
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	improvements.add("granary");
    	improvements.add("bank");
    	city.setImprovements(improvements);
    	city.setCurrentImprovement("courthouse");
    	city.setResourcesSurplus(300);
    	CityImprovementDTO courtHouse = getImprovement("courthouse",250);
		// insert test data as facts
		ksession.insert(courtHouse);
		FactHandle cityHandle = ksession.insert(city);
		
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Build City Improvement"));
		List<ProcessInstance> processes = (List<ProcessInstance>)ksession.getProcessInstances();
		
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		// rule "Build Irrigation" didn't fire
		Assert.assertTrue(firedRules.contains("Build City Improvement"));
		
		Assert.assertTrue("One Process Should Be Active",processes.size()==1);
		ProcessInstance p = processes.get(0);
		
		assertProcessInstanceActive(p.getId(), ksession);
		
		System.out.println("cancelling");
		ksession.getWorkingMemoryEntryPoint("ActionCanceledStream").insert(new CityImprovementEvent(city.getId()));
//		city.setCurrentImprovement(null);
//		ksession.update(cityHandle, city);
		ksession.fireAllRules();
		
		Assert.assertFalse("City Contains Courthouse Improvement.",city.getImprovements().contains("courthouse"));
		
		Assert.assertTrue("No Pending Processes Are There",ksession.getProcessInstances().size()==0);
	}
    @Test
    public void testCompleteAfterThreeTurns(){
//    	ksession.addEventListener(new DebugAgendaEventListener());
//    	ksession.addEventListener(new DebugProcessEventListener());
//    	ksession.addEventListener(new DebugWorkingMemoryEventListener());
//    	
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
//    	improvements.add("granary");
//    	improvements.add("bank");
    	city.setImprovements(improvements);
    	city.setCurrentImprovement("courthouse");
    	city.setResourcesSurplus(100);
    	CityImprovementDTO courtHouse = getImprovement("courthouse",251);
		// insert test data as facts
		ksession.insert(courtHouse);
		FactHandle cityHandle = ksession.insert(city);
		
		ksession.fireAllRules();
		List<ProcessInstance> processes = (List<ProcessInstance>)ksession.getProcessInstances();
		
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		// rule "Build Irrigation" didn't fire
		Assert.assertTrue(firedRules.contains("Build City Improvement"));
		
		Assert.assertTrue("One Process Should Be Active",processes.size()==1);
		WorkflowProcessInstance p = (WorkflowProcessInstance)processes.get(0);
		
		ksession.signalEvent("turn-new", null);
		assertProcessInstanceActive(p.getId(), ksession);
		((CityDTO)p.getVariable("city")).setResourcesSurplus(100);
		//ksession.update(cityHandle, city);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ksession.signalEvent("turn-new", null);
		assertProcessInstanceActive(p.getId(), ksession);
		((CityDTO)p.getVariable("city")).setResourcesSurplus(100);
		
		//ksession.update(cityHandle, city);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ksession.signalEvent("turn-new", null);
//		assertProcessInstanceActive(p.getId(), ksession);
		
		assertProcessInstanceCompleted(p.getId(), ksession);
		
		Assert.assertTrue("No Pending Processes Are There",ksession.getProcessInstances().size()==0);
		
		Assert.assertTrue("City Contains Courthouse Improvement.",city.getImprovements().contains("courthouse"));
		
	}
}