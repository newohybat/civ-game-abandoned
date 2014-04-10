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
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;


public class CitySurplusRulesJUnitTest extends BaseJUnitTest {

	private static KnowledgeBase kbase;
	public CitySurplusRulesJUnitTest() throws Exception{
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
    private static CityImprovementDTO getImprovement(String ident, Integer upkeepCost){
    	CityImprovementDTO imp = new CityImprovementDTO();
    	imp.setUpkeepCost(upkeepCost);
    	imp.setIdent(ident);
    	return imp;
    }
    private static UnitDTO getUnit(Long id,String type, Long owner){
    	UnitDTO unit = new UnitDTO();
    	unit.setId(id);
    	unit.setType(type);
    	unit.setAttackStrength(0);
    	unit.setDefenseStrength(0);
    	unit.setOwner(owner);
    	return unit;
    }
    private static UnitTypeDTO getUnitType(String ident){
    	UnitTypeDTO type = new UnitTypeDTO();
		type.setIdent(ident);
		Set<String> actions = new HashSet<String>();
		type.setActions(actions);
		return type;
    }
    @Test
    public void testCityImprovementsUpkeep(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	PlayerDTO player = getPlayer(1L, "honza", "despotism");
    	player.setLuxuriesRatio(30);
    	player.setTaxesRatio(30);
    	player.setResearchRatio(40);
    	CityDTO city = getCity(5L,"marefy");
		city.setTradeProduction(10);
		city.setOwner(player.getId());
		
    	
    	
		// insert facts
		ksession.insert(city);
		ksession.insert(player);

		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageSurpluses");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Trade To Luxuries/Taxes/Research Resolution"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Trade To Luxuries/Taxes/Research Resolution Fired",firedRules.contains("Trade To Luxuries/Taxes/Research Resolution"));
		Assert.assertTrue("City Luxuries According to Luxuries Ratio",city.getLuxuriesAmount()==3);
		Assert.assertTrue("City Taxes According to Taxes Ratio",city.getTaxesAmount()==3);
		Assert.assertTrue("City Research According to Research Ratio",city.getResearchAmount()==4);
    }
    @Test
    public void testCityTaxesToTreasury(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	PlayerDTO player = getPlayer(1L, "honza", "despotism");
    	player.setTreasury(0);
    	CityDTO city = getCity(5L,"marefy");
		city.setTaxesAmount(150);
		city.setImprovementsUpkeep(0);
		city.setOwner(player.getId());
		CityDTO city2 = getCity(6L,"brno");
		city2.setTaxesAmount(100);
		city2.setImprovementsUpkeep(0);
		city2.setOwner(player.getId());
		
    	
		// insert facts
		ksession.insert(city);
		ksession.insert(city2); 
		ksession.insert(player);

		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageSurpluses");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Taxes Surplus To Treasury"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Taxes Surplus To Treasury Fired",firedRules.contains("Taxes Surplus To Treasury"));
		Assert.assertTrue("City Taxes Go To Treasury",player.getTreasury()==250);
    }
    @Test
    public void testCityTaxesShortageFromTreasury(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	PlayerDTO player = getPlayer(1L, "honza", "despotism");
    	player.setTreasury(60);
    	CityDTO city = getCity(5L,"marefy");
		city.setTaxesAmount(0);
		city.setImprovementsUpkeep(50);
		city.setOwner(player.getId());
		
    	
		// insert facts
		ksession.insert(city);
		ksession.insert(player);

		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
				
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageSurpluses");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Taxes Shortage Covered From Treasury"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Taxes Shortage Covered From Treasury Fired",firedRules.contains("Taxes Shortage Covered From Treasury"));
		Assert.assertTrue("City Taxes Shortage Get From Treasury",player.getTreasury()==10);
    }
    @Test
    public void testCityTaxesShortageNotCoveredFromTreasury(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	Set<String> improvements = new HashSet<String>();
    	improvements.add("bank");
    	improvements.add("factory");
    	PlayerDTO player = getPlayer(1L, "honza", "despotism");
    	player.setTreasury(40);
    	CityDTO city = getCity(5L,"marefy");
		city.setTaxesAmount(0);
		city.setImprovementsUpkeep(50);
		city.setOwner(player.getId());
		city.setImprovements(improvements);
		
    	
		// insert facts
		ksession.insert(city);
		ksession.insert(player);

		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageSurpluses");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Taxes Shortage Not Covered From Treasury"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Taxes Shortage Not Covered From Treasury Fired",firedRules.contains("Taxes Shortage Not Covered From Treasury"));
		Assert.assertTrue("City Taxes Shortage Zero Treasury",player.getTreasury()==0);
		Assert.assertTrue("City Improvement Sold.",city.getImprovements().size()<2);
    }
    @Test
    public void testCityResearchContribute(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	PlayerDTO player = getPlayer(1L, "honza", "despotism");
    	player.setResearch(60);
    	CityDTO city = getCity(5L,"marefy");
		city.setResearchAmount(10);
		city.setOwner(player.getId());
		
    	
		// insert facts
		ksession.insert(city);
		ksession.insert(player);

		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageSurpluses");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Research Of City To Global"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Research Of City To Global Fired",firedRules.contains("Research Of City To Global"));
		Assert.assertTrue("Research Of City To Global",player.getResearch()==70);
    }
    @Test
    public void testCityFoodSurplusToStock(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	PlayerDTO player = getPlayer(1L, "honza", "despotism");
    	player.setResearch(60);
    	CityDTO city = getCity(5L,"marefy");
		city.setResearchAmount(10);
		city.setOwner(player.getId());
		city.setFoodProduction(250);
		city.setFoodConsumption(120);
		city.setFoodStock(10);
		
    	
		// insert facts
		ksession.insert(city);
		ksession.insert(player);

		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("manageSurpluses");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Food Surplus To Stock"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Food Surplus To Stock Fired",firedRules.contains("Food Surplus To Stock"));
		Assert.assertTrue("Food Surplus To Stock Check",city.getFoodStock()==140);
    }
    @Test
    public void testCityFoodShortageNotFromStock(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	PlayerDTO player = getPlayer(1L, "honza", "despotism");
    	CityDTO city = getCity(5L,"marefy");
		city.setOwner(player.getId());
		city.setFoodProduction(250);
		city.setFoodConsumption(300);
		city.setSize(3);
		city.setFoodStock(10);
		
    	
		// insert facts
		ksession.insert(city);
		ksession.insert(player);

		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("affectPopulation");
		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("Food Shortage Not Covered From Stock"));
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Food Shortage Not Covered From Stock Fired",firedRules.contains("Food Shortage Not Covered From Stock"));
		Assert.assertTrue("Food Stock Empty",city.getFoodStock()==0);
		Assert.assertTrue("City Size Decreased",city.getSize()==2);
    }
    
    
    
}