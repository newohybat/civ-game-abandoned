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
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.ResourceFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import cz.muni.fi.civ.newohybat.drools.events.TurnEvent;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileDTO;


public class CityProductionRulesJUnitTest extends BaseJUnitTest {

	private static KnowledgeBase kbase;
	public CityProductionRulesJUnitTest() throws Exception{
	}
	@BeforeClass
	public static void setKnowledgeBase() throws Exception {
		System.out.println("Loading knowledge base.");
		KnowledgeBuilderConfiguration config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        config.setOption(PropertySpecificOption.ALWAYS);
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(config);

		kbuilder.add(ResourceFactory.newClassPathResource("rules/common.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/turn-city.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/governmentRules.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/cityImprovementRules.drl"), ResourceType.DRL);
		//kbuilder.add(ResourceFactory.newClassPathResource("cityTurnProcess.bpmn"), ResourceType.BPMN2);
		
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
	private static CityDTO getCity(Long id, String name, Set<Long> managedTiles){
		CityDTO city = getCity(id,name);
		city.setManagedTiles(managedTiles);
		return city;
	}
	
	private static TileDTO getTile(Long id, Integer foodProduction, Integer resourcesProduction, Integer tradeProduction){
    	TileDTO tile = new TileDTO();
    	tile.setId(id);
    	tile.setFoodProduction(foodProduction);
    	tile.setResourcesProduction(resourcesProduction);
    	tile.setTradeProduction(tradeProduction);
    	return tile;
    }
	private static PlayerDTO getPlayer(Long id, String name, String government){
		PlayerDTO player = new PlayerDTO();
		player.setId(id);
		player.setName(name);
		player.setGovernment(government);
		player.setLuxuriesRatio(0);
		player.setTaxesRatio(0);
		player.setResearchRatio(0);
		player.setResearch(0);
		return player;
	}
    
	@Test
    public void testCityBasicProduction(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	TileDTO tile = getTile(1L, 1,1,1);
    	TileDTO tile2 = getTile(2L, 2,2,2);
    	TileDTO tile3 = getTile(3L, 3,3,3);
    	Set<Long> managedTiles = new HashSet<Long>();
    	managedTiles.add(tile.getId());
    	managedTiles.add(tile2.getId());
    	managedTiles.add(tile3.getId());
    	
    	
    	CityDTO city = getCity(5L,"marefy", managedTiles);
		
		// insert facts
		ksession.insert(city);
		ksession.insert(tile);
		ksession.insert(tile2);
		ksession.insert(tile3);
//		ProcessInstance processInstance = ksession.startProcess("cz.muni.fi.civ.newohybat.bpmn.cityturnprocess");
		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Basic City Tiles Production"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("Basic City Tiles Production Fired",firedRules.contains("Basic City Tiles Production"));
		Assert.assertTrue("City Food Production Is Sum Of Tiles Productions.",city.getFoodProduction()==6);
		Assert.assertTrue("City Resources Production Is Sum Of Tiles Productions.",city.getResourcesProduction()==6);
		Assert.assertTrue("City Trade Production Is Sum Of Tiles Productions.",city.getTradeProduction()==6);
    }
	@Test
    public void testCityDespotismProduction(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	TileDTO tile = getTile(1L, 1,1,1);
    	TileDTO tile2 = getTile(2L, 2,2,2);
    	TileDTO tile3 = getTile(3L, 3,3,3);
    	Set<Long> managedTiles = new HashSet<Long>();
    	managedTiles.add(tile.getId());
    	managedTiles.add(tile2.getId());
    	managedTiles.add(tile3.getId());
    	PlayerDTO owner = getPlayer(1L,"newohybat","despotism");
    	
    	CityDTO city = getCity(5L,"marefy", managedTiles);
		city.setOwner(owner.getId());
		// insert facts
		ksession.insert(city);
		ksession.insert(tile);
		ksession.insert(tile2);
		ksession.insert(tile3);

		ksession.insert(owner);
//		ProcessInstance processInstance = ksession.startProcess("cz.muni.fi.civ.newohybat.bpmn.cityturnprocess");
		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Despotism Restricted City Tiles Production"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("Despotism Restricted City Tiles Production Fired",firedRules.contains("Despotism Restricted City Tiles Production"));
		Assert.assertTrue("City Food Production Is Sum Of Tiles Productions.",city.getFoodProduction()==5);
		Assert.assertTrue("City Resources Production Is Sum Of Tiles Productions.",city.getResourcesProduction()==5);
		Assert.assertTrue("City Trade Production Is Sum Of Tiles Productions.",city.getTradeProduction()==5);
    }
	@Test
    public void testCityMonarchyProduction(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	TileDTO tile = getTile(1L, 1,1,1);
    	TileDTO tile2 = getTile(2L, 2,2,2);
    	TileDTO tile3 = getTile(3L, 3,3,3);
    	Set<Long> managedTiles = new HashSet<Long>();
    	managedTiles.add(tile.getId());
    	managedTiles.add(tile2.getId());
    	managedTiles.add(tile3.getId());
    	PlayerDTO owner = getPlayer(1L,"newohybat","monarchy");
    	
    	CityDTO city = getCity(5L,"marefy", managedTiles);
		city.setOwner(owner.getId());
		// insert facts
		ksession.insert(city);
		ksession.insert(tile);
		ksession.insert(tile2);
		ksession.insert(tile3);

		ksession.insert(owner);
		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		
//		ProcessInstance processInstance = ksession.startProcess("cz.muni.fi.civ.newohybat.bpmn.cityturnprocess");
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Basic City Tiles Production"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("Basic City Tiles Production Fired",firedRules.contains("Basic City Tiles Production"));
		Assert.assertTrue("City Food Production Is Sum Of Tiles Productions.",city.getFoodProduction()==6);
		Assert.assertTrue("City Resources Production Is Sum Of Tiles Productions.",city.getResourcesProduction()==6);
		Assert.assertTrue("City Trade Production Is Sum Of Tiles Productions.",city.getTradeProduction()==6);
    }
	
	@Test
    public void testCityCommunismProduction(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	TileDTO tile = getTile(1L, 1,1,1);
    	TileDTO tile2 = getTile(2L, 2,2,2);
    	TileDTO tile3 = getTile(3L, 3,3,3);
    	Set<Long> managedTiles = new HashSet<Long>();
    	managedTiles.add(tile.getId());
    	managedTiles.add(tile2.getId());
    	managedTiles.add(tile3.getId());
    	PlayerDTO owner = getPlayer(1L,"newohybat","communism");
    	
    	CityDTO city = getCity(5L,"marefy", managedTiles);
		city.setOwner(owner.getId());
		// insert facts
		ksession.insert(city);
		ksession.insert(tile);
		ksession.insert(tile2);
		ksession.insert(tile3);

		ksession.insert(owner);
		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		
//		ProcessInstance processInstance = ksession.startProcess("cz.muni.fi.civ.newohybat.bpmn.cityturnprocess");
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Basic City Tiles Production"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("Basic City Tiles Production Fired",firedRules.contains("Basic City Tiles Production"));
		Assert.assertTrue("City Food Production Is Sum Of Tiles Productions.",city.getFoodProduction()==6);
		Assert.assertTrue("City Resources Production Is Sum Of Tiles Productions.",city.getResourcesProduction()==6);
		Assert.assertTrue("City Trade Production Is Sum Of Tiles Productions.",city.getTradeProduction()==6);
    }
	@Test
    public void testCityTheRepublicProduction(){
    	
		ksession.addEventListener(new DebugAgendaEventListener());
		AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	TileDTO tile = getTile(1L, 1,1,1);
    	TileDTO tile2 = getTile(2L, 2,2,2);
    	TileDTO tile3 = getTile(3L, 3,3,3);
    	Set<Long> managedTiles = new HashSet<Long>();
    	managedTiles.add(tile.getId());
    	managedTiles.add(tile2.getId());
    	managedTiles.add(tile3.getId());
    	PlayerDTO owner = getPlayer(1L,"newohybat","theRepublic");
    	
    	CityDTO city = getCity(5L,"marefy", managedTiles);
		city.setOwner(owner.getId());
		// insert facts
		ksession.insert(city);
		ksession.insert(tile);
		ksession.insert(tile2);
		ksession.insert(tile3);

		ksession.insert(owner);
		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		
		//ProcessInstance processInstance = ksession.startProcess("cz.muni.fi.civ.newohybat.bpmn.cityturnprocess");
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("The Republic City Tiles Production"));
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		//assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("The Republic City Tiles Production Fired",firedRules.contains("The Republic City Tiles Production"));
		Assert.assertTrue("City Food Production Is Sum Of Tiles Productions.",city.getFoodProduction()==6);
		Assert.assertTrue("City Resources Production Is Sum Of Tiles Productions.",city.getResourcesProduction()==6);
		Assert.assertTrue("City Trade Production Is Sum Of Tiles Productions.",city.getTradeProduction()==9);
    }
	
	@Test
    public void testCityDemocracyProduction(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	TileDTO tile = getTile(1L, 1,1,1);
    	TileDTO tile2 = getTile(2L, 2,2,2);
    	TileDTO tile3 = getTile(3L, 3,3,3);
    	Set<Long> managedTiles = new HashSet<Long>();
    	managedTiles.add(tile.getId());
    	managedTiles.add(tile2.getId());
    	managedTiles.add(tile3.getId());
    	PlayerDTO owner = getPlayer(1L,"newohybat","democracy");
    	
    	CityDTO city = getCity(5L,"marefy", managedTiles);
		city.setOwner(owner.getId());
		// insert facts
		ksession.insert(city);
		ksession.insert(tile);
		ksession.insert(tile2);
		ksession.insert(tile3);

		ksession.insert(owner);
		
		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Democracy City Tiles Production"));
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Democracy City Tiles Production Fired",firedRules.contains("Democracy City Tiles Production"));
		Assert.assertTrue("City Food Production Is Sum Of Tiles Productions.",city.getFoodProduction()==6);
		Assert.assertTrue("City Resources Production Is Sum Of Tiles Productions.",city.getResourcesProduction()==6);
		Assert.assertTrue("City Trade Production Is Sum Of Tiles Productions.",city.getTradeProduction()==9);
    }
	
	@Test
    public void testCityWithFactory(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
    	city.setFoodProduction(5);
    	city.setResourcesProduction(5);
    	city.setTradeProduction(5);
    	city.getImprovements().add("factory");
    	
    	
    	ksession.insert(city);
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Factory"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("Factory",firedRules.contains("Factory"));
		Assert.assertTrue("City Food Production Is Not Affected By Factory.",city.getFoodProduction()==5);
		Assert.assertTrue("City Resources Production Is Affected By Factory.",city.getResourcesProduction()==7);
		Assert.assertTrue("City Trade Production Is Not Affected By Factory.",city.getTradeProduction()==5);
    }
	@Test
    public void testCityWithFactoryAndPowerPlant(){
		AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
    	city.setFoodProduction(5);
    	city.setResourcesProduction(5);
    	city.setTradeProduction(5);
    	city.getImprovements().add("factory");
    	city.getImprovements().add("hydroPlant");
    	
    	
    	ksession.insert(city);
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Factory And Some Power Plant"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
//		Assert.assertTrue("Hydro Plant fired",firedRules.contains("Hydro Plant"));
		Assert.assertTrue("Factory And Some Power Plant Fired",firedRules.contains("Factory And Some Power Plant"));
		Assert.assertTrue("City Food Production Is Not Affected By Factory.",city.getFoodProduction()==5);
		Assert.assertTrue("City Resources Production Is Affected By Factory.",city.getResourcesProduction()==10);
		Assert.assertTrue("City Trade Production Is Not Affected By Factory.",city.getTradeProduction()==5);
    }
	@Test
    public void testCityWithHydroPlant(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
    	city.setFoodProduction(5);
    	city.setResourcesProduction(5);
    	city.setTradeProduction(5);
    	city.getImprovements().add("hydroPlant");
    	
    	
    	ksession.insert(city);
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Hydro Plant"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("Hydro Plant Fired",firedRules.contains("Hydro Plant"));
		Assert.assertTrue("City Food Production Is Not Affected By Factory.",city.getFoodProduction()==5);
		Assert.assertTrue("City Resources Production Is Affected By Factory.",city.getResourcesProduction()==7);
		Assert.assertTrue("City Trade Production Is Not Affected By Factory.",city.getTradeProduction()==5);
    }
	@Test
    public void testCityManufacturingPlantObsoletesFactory(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
    	city.setFoodProduction(5);
    	city.setResourcesProduction(5);
    	city.setTradeProduction(5);
    	city.getImprovements().add("manufacturingPlant");
    	city.getImprovements().add("factory");
    	
    	
    	ksession.insert(city);
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules();
		
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("Manufacturing Plant Obsoletes Factory Fired",firedRules.contains("Manufacturing Plant"));
		Assert.assertFalse("Factory Didn't Fire",firedRules.contains("Factory"));
		
    }
	@Test
    public void testCityManufacturingPlant(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
    	city.setFoodProduction(5);
    	city.setResourcesProduction(5);
    	city.setTradeProduction(5);
    	city.getImprovements().add("manufacturingPlant");
    	
    	
    	ksession.insert(city);
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Manufacturing Plant"));
		
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("Manufacturing Plant Fired",firedRules.contains("Manufacturing Plant"));
		Assert.assertTrue("City Food Production Is Not Affected By Factory.",city.getFoodProduction()==5);
		Assert.assertTrue("City Resources Production Is Affected By Factory.",city.getResourcesProduction()==10);
		Assert.assertTrue("City Trade Production Is Not Affected By Factory.",city.getTradeProduction()==5);
    }
	@Test
    public void testCityManufacturingPlantWithPowerPlant(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
    	city.setFoodProduction(5);
    	city.setResourcesProduction(5);
    	city.setTradeProduction(5);
    	city.getImprovements().add("manufacturingPlant");
    	city.getImprovements().add("hydroPlant");
    	
    	
    	ksession.insert(city);
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Manufacturing Plant With A Power Plant"));
		
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
//		Assert.assertTrue("Hydro Plant Fired",firedRules.contains("Hydro Plant"));
		Assert.assertTrue("Manufacturing Plant With A Power Plant Fired",firedRules.contains("Manufacturing Plant With A Power Plant"));
		Assert.assertTrue("City Food Production Is Not Affected By Factory.",city.getFoodProduction()==5);
		Assert.assertTrue("City Resources Production Is Affected By Factory.",city.getResourcesProduction()==15);
		Assert.assertTrue("City Trade Production Is Not Affected By Factory.",city.getTradeProduction()==5);
    }

	@Test
    public void testCityWithNuclearPlant(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
    	city.setFoodProduction(5);
    	city.setResourcesProduction(5);
    	city.setTradeProduction(5);
    	city.getImprovements().add("nuclearPlant");
    	
    	
    	ksession.insert(city);
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Nuclear Plant"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("Nuclear Plant Fired",firedRules.contains("Nuclear Plant"));
		Assert.assertTrue("City Food Production Is Not Affected By Factory.",city.getFoodProduction()==5);
		Assert.assertTrue("City Resources Production Is Affected By Factory.",city.getResourcesProduction()==7);
		Assert.assertTrue("City Trade Production Is Not Affected By Factory.",city.getTradeProduction()==5);
    }
	@Test
    public void testCityWithPowerPlant(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
    	city.setFoodProduction(5);
    	city.setResourcesProduction(5);
    	city.setTradeProduction(5);
    	city.getImprovements().add("powerPlant");
    	
    	
    	ksession.insert(city);
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageProductions");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Power Plant"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
//		assertProcessInstanceCompleted(processInstance.getId(), ksession);
		Assert.assertTrue("Power Plant Fired",firedRules.contains("Power Plant"));
		Assert.assertTrue("City Food Production Is Not Affected By Factory.",city.getFoodProduction()==5);
		Assert.assertTrue("City Resources Production Is Affected By Factory.",city.getResourcesProduction()==7);
		Assert.assertTrue("City Trade Production Is Not Affected By Factory.",city.getTradeProduction()==5);
    }
	
	
	
	
	
	
	
	
}