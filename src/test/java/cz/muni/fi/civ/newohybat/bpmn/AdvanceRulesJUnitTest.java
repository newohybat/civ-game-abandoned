package cz.muni.fi.civ.newohybat.bpmn;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.PropertySpecificOption;
import org.drools.conf.EventProcessingOption;
import org.drools.event.DebugProcessEventListener;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import cz.muni.fi.civ.newohybat.drools.events.AdvanceEvent;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.AdvanceDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.GovernmentDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;


public class AdvanceRulesJUnitTest extends BaseJUnitTest {

	private static KnowledgeBase kbase;
	public AdvanceRulesJUnitTest() throws Exception{
	}
	@BeforeClass
	public static void setKnowledgeBase() throws Exception {
		System.out.println("Loading knowledge base.");
		KnowledgeBuilderConfiguration config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        config.setOption(PropertySpecificOption.ALWAYS);
        
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(config);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/common.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/advanceRules.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("processes/discoverAdvance.bpmn"), ResourceType.BPMN2);
		KnowledgeBaseConfiguration baseConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
		baseConfig.setOption( EventProcessingOption.STREAM );
		kbase = KnowledgeBaseFactory.newKnowledgeBase(baseConfig);
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
	}
	@Before
	public void before(){
		setSessions(kbase);
	}
	@After
	public void after(){
		ksession.halt();
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
		city.setHomeUnits(new HashSet<Long>());
		city.setEnabledUnitTypes(new HashSet<String>());
		city.setEnabledImprovements(new HashSet<String>());
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
		player.setResearchSpent(0);
		player.setAdvances(new HashSet<String>());
		player.setCities(new HashSet<Long>());
		player.setEnabledAdvances(new HashSet<String>());
		player.setEnabledGovernments(new HashSet<String>());
		return player;
	}
    
    private static AdvanceDTO getAdvance(String ident, Integer cost){
    	AdvanceDTO advance = new AdvanceDTO();
    	advance.setIdent(ident);
    	advance.setEnabledAdvances(new HashSet<String>());
    	advance.setEnabledCityImprovements(new HashSet<String>());
    	advance.setEnabledGovernments(new HashSet<String>());
    	advance.setEnabledUnitTypes(new HashSet<String>());
    	advance.setCost(cost);
    	return advance;
    }
    
    private static GovernmentDTO getGovernment(String ident){
    	GovernmentDTO gov = new GovernmentDTO();
    	gov.setIdent(ident);
    	return gov;
    }
    @Test
    public void testWaitForNewTurnToComplete(){
    	ksession.addEventListener(new DebugAgendaEventListener());
    	ksession.addEventListener(new DebugWorkingMemoryEventListener());
    	ksession.addEventListener(new DebugProcessEventListener());
//    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	PlayerDTO player = getPlayer(1L, "honza");
    	player.setResearch(205);
    	player.getAdvances().add("basicOne");
    	AdvanceDTO basicOne = getAdvance("basicOne", 100);
    	player.getEnabledAdvances().add("consecutiveOne");
    	basicOne.getEnabledAdvances().add("consecutiveOne");
    	AdvanceDTO consecutiveOne = getAdvance("consecutiveOne", 100);
    	consecutiveOne.getEnabledAdvances().add("consecutiveTwo");
    	consecutiveOne.getEnabledCityImprovements().add("bank");
    	consecutiveOne.getEnabledGovernments().add("mercantilism");
    	consecutiveOne.getEnabledUnitTypes().add("warClerk");
    	GovernmentDTO mercantilism = getGovernment("mercantilism");
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	city.setImprovements(improvements);
    	city.setOwner(player.getId());
    	player.getCities().add(city.getId());
		// insert test data as facts
    	ksession.insert(basicOne);
		ksession.insert(consecutiveOne);
		ksession.insert(getAdvance("consecutiveTwo", 10));
		ksession.insert(mercantilism);
		FactHandle pH = ksession.insert(player);
		ksession.insert(city);
		
//		WorkingMemoryEntryPoint ep = ksession.getWorkingMemoryEntryPoint("InitStream");
//		Assert.assertNotNull( ep );
//		ep.insert(new AdvanceEvent(player.getId()));
////    	
		ksession.fireAllRules();
		player.setCurrentAdvance("consecutiveOne");
		ksession.update(pH, player);
		
//		ksession.fireAllRules();
		List<ProcessInstance> processes = (List<ProcessInstance>)ksession.getProcessInstances();
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		System.out.println(firedRules);
		// rule "Build Irrigation" didn't fire
		Assert.assertTrue(firedRules.contains("Discover Advance"));
		
		Assert.assertTrue("One Process Should Be Active",processes.size()==1);
		Long pId = processes.get(0).getId();
		
		assertProcessInstanceActive(pId, ksession);
		
		ksession.signalEvent("turn-new", null, pId);
		
		assertProcessInstanceCompleted(pId, ksession);
		
		ksession.fireAllRules();
		
		Assert.assertTrue("Player has reached advance.",player.getAdvances().contains(consecutiveOne.getIdent()));
		System.out.println(player.getEnabledAdvances());
		Assert.assertTrue("Player can discover advance.",player.getEnabledAdvances().containsAll(consecutiveOne.getEnabledAdvances()));

		Assert.assertTrue("Player can build bank.",city.getEnabledImprovements().contains("bank"));
		System.out.println(city.getEnabledUnitTypes());
		Assert.assertTrue("Player can make warClerk.",city.getEnabledUnitTypes().contains("warClerk"));
		Assert.assertTrue("Player can convert to mercantilism.",player.getEnabledGovernments().contains("mercantilism"));
		
	}
}