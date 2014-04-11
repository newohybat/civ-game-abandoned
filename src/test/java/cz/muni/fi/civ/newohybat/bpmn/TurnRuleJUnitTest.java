package cz.muni.fi.civ.newohybat.bpmn;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.drools.event.rule.ActivationCreatedEvent;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.event.rule.RuleFlowGroupActivatedEvent;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
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


public class TurnRuleJUnitTest extends BaseJUnitTest {

	private static KnowledgeBase kbase;
	public TurnRuleJUnitTest() throws Exception{
	}
	@BeforeClass
	public static void setKnowledgeBase() throws Exception {
		System.out.println("Loading knowledge base.");
		KnowledgeBuilderConfiguration config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        config.setOption(PropertySpecificOption.ALWAYS);
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(config);
		kbuilder.add(ResourceFactory.newClassPathResource("allRules.changeset"), ResourceType.CHANGE_SET);
		
		kbase = kbuilder.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
	}
	@Before
	public void before(){
		setSessions(kbase);
		ksession.addEventListener(
                new DefaultAgendaEventListener()
                {
                    @Override
                    public void activationCreated(ActivationCreatedEvent event) {
//                        ((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                    }
                    @Override
                    public void afterRuleFlowGroupActivated(
                    		RuleFlowGroupActivatedEvent event) {
//                    	System.out.println(event);
                    	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
                    }
                    
                }
                );
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
	
	private static TileDTO getTile(Long id, String terrain,String special){
    	TileDTO tile = new TileDTO();
    	tile.setId(id);
    	tile.setTerrain(terrain);
    	tile.setSpecial(special);
    	tile.setFoodProduction(0);
    	tile.setResourcesProduction(0);
    	tile.setTradeProduction(0);
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
    public void testTurn(){
    	ksession.addEventListener(new DebugAgendaEventListener());
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	PlayerDTO player1 = getPlayer(1L, "honza", "despotism");
    	PlayerDTO player2 = getPlayer(2L, "jirka", "democracy");
    	CityDTO city1 = getCity(5L,"marefy");
    	city1.setOwner(player1.getId());
    	CityDTO city2 = getCity(6L, "bucovice");
    	city2.setOwner(player2.getId());
		
		TileDTO tile1 = getTile(1L, "plains",null);
		TileDTO tile2 = getTile(2L, "rivers",null);
		TileDTO tile3 = getTile(3L, "swamp","oil");
		TileDTO tile4 = getTile(4L, "hills","coal");
		
		Set<Long> managedTiles = new HashSet<Long>();
		managedTiles.add(tile1.getId());
		managedTiles.add(tile2.getId());
		city1.setManagedTiles(managedTiles);
		
		Set<Long> managedTiles2 = new HashSet<Long>();
		managedTiles2.add(tile3.getId());
		managedTiles2.add(tile4.getId());
		city2.setManagedTiles(managedTiles2);
		
    	
    	ksession.insert(tile1);
		ksession.insert(tile2);
//		ksession.insert(tile3);
//		ksession.insert(tile4);
		// insert facts
		ksession.insert(player1);
//		ksession.insert(player2);
		ksession.insert(city1);
//		ksession.insert(city2);
		
		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("timer-delay", "5s");
		ProcessInstance pi = ksession.startProcess("cz.muni.fi.civ.newohybat.bpmn.turn", params);
		
		ksession.fireAllRules();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		
		System.out.println(city1.getFoodProduction());
		Assert.assertTrue("City1 has foodproduction 3", city1.getFoodProduction()==3);
		Assert.assertTrue("City1 has foodconsumption 2", city1.getFoodConsumption()==2);
		Assert.assertTrue("City1 has foodStock 1", city1.getFoodStock()==1);
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		ksession.fireAllRules();
		System.out.println(city1.getFoodStock());
		
		Assert.assertTrue("City1 has foodproduction 3", city1.getFoodProduction()==3);
		Assert.assertTrue("City1 has foodconsumption 2", city1.getFoodConsumption()==2);
		Assert.assertTrue("City1 has foodStock 2", city1.getFoodStock()==2);
		
		System.out.println(firedRules);
		
		ksession.abortProcessInstance(pi.getId());
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    
    
}