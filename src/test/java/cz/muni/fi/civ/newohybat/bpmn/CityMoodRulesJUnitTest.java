package cz.muni.fi.civ.newohybat.bpmn;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.PropertySpecificOption;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.process.ProcessInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;


public class CityMoodRulesJUnitTest extends BaseJUnitTest {

	private static KnowledgeBase kbase;
	public CityMoodRulesJUnitTest() throws Exception{
	}
	@BeforeClass
	public static void setKnowledgeBase() throws Exception {
		System.out.println("Loading knowledge base.");
		KnowledgeBuilderConfiguration config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        config.setOption(PropertySpecificOption.ALWAYS);
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(config);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/common.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/turn-city.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("processes/cityTurnProcess.bpmn"), ResourceType.BPMN2);
		
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
    	city.setSize(0);
    	city.setTradeProduction(0);
    	return city;
	}
    
	@Test
    public void testCityNeitherWeLoveDayNorDisorder(){
    	
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
		city.setPeopleHappy(5);
		city.setPeopleContent(6);
		city.setPeopleUnhappy(1);
		city.setPeopleEntertainers(0);
		city.setPeopleScientists(0);
		city.setPeopleTaxmen(0);
		city.setWeLoveDay(false);
		city.setDisorder(false);
		city.setSize(4);
		// insert facts
		ksession.insert(city);
		ProcessInstance processInstance = ksession.startProcess("cz.muni.fi.civ.newohybat.bpmn.cityturnprocess");
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertFalse("City Is Not In Disorder.",city.getDisorder());
		Assert.assertFalse("City Is Not Celebrating We Love Day.", city.getWeLoveDay());
    }
	@Test
    public void testCityWeLoveDay(){
    	
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
		city.setPeopleHappy(5);
		city.setPeopleContent(3);
		city.setPeopleUnhappy(0);
		city.setPeopleEntertainers(0);
		city.setPeopleScientists(0);
		city.setPeopleTaxmen(0);
		city.setWeLoveDay(false);
		city.setDisorder(false);
		city.setSize(4);
		// insert facts
		ksession.insert(city);
		ProcessInstance processInstance = ksession.startProcess("cz.muni.fi.civ.newohybat.bpmn.cityturnprocess");
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertFalse("City Is Not In Disorder.",city.getDisorder());
		Assert.assertTrue("City Is Not Celebrating We Love Day.", city.getWeLoveDay());
    }
	@Test
    public void testCityDisorder(){
    	
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
		city.setPeopleHappy(5);
		city.setPeopleContent(3);
		city.setPeopleUnhappy(6);
		city.setPeopleEntertainers(0);
		city.setPeopleScientists(0);
		city.setPeopleTaxmen(0);
		city.setWeLoveDay(false);
		city.setDisorder(false);
		city.setSize(4);
		// insert facts
		ksession.insert(city);
		ProcessInstance processInstance = ksession.startProcess("cz.muni.fi.civ.newohybat.bpmn.cityturnprocess");
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("City Is Not In Disorder.",city.getDisorder());
		Assert.assertFalse("City Is  Not Celebrating We Love Day.", city.getWeLoveDay());
    }
}