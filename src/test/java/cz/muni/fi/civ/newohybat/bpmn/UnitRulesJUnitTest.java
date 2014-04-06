package cz.muni.fi.civ.newohybat.bpmn;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.PropertySpecificOption;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.DefaultWorkingMemoryEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.rule.FactHandle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;


public class UnitRulesJUnitTest extends BaseJUnitTest {

	private static KnowledgeBase kbase;
	public UnitRulesJUnitTest() throws Exception{
	}
	@BeforeClass
	public static void setKnowledgeBase() throws Exception {
		System.out.println("Loading knowledge base.");
		KnowledgeBuilderConfiguration config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        config.setOption(PropertySpecificOption.ALWAYS);
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(config);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/unitRules.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("processes/buildTileImprovement.bpmn"), ResourceType.BPMN2);
		kbuilder.add(ResourceFactory.newClassPathResource("processes/setAvailableActions.bpmn"), ResourceType.BPMN2);
		
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

    private static UnitDTO getUnit(String type, Long pos){
    	UnitDTO unit = new UnitDTO();
    	unit.setId(3L);
    	unit.setType(type);
    	unit.setAttackStrength(0);
    	unit.setDefenseStrength(0);
    	unit.setTile(pos);
    	return unit;
    }
    private static TileDTO getTile(Long id, String terrain, Set<String> imps){
    	TileDTO tile = new TileDTO();
    	tile.setId(id);
    	tile.setImprovements(imps);
    	tile.setTerrain(terrain);
    	return tile;
    }
    private static PlayerDTO getPlayer(Long id, String name){
    	PlayerDTO player = new PlayerDTO();
    	player.setId(id);
    	player.setName(name);
    	return player;
    }
    private static UnitTypeDTO getUnitType(String ident){
    	UnitTypeDTO type = new UnitTypeDTO();
		type.setIdent(ident);
		Set<String> actions = new HashSet<String>();
		actions.add("buildIrrigation");
		type.setActions(actions);
		return type;
    }
    private static TileImprovementDTO getTileImp(String ident, Integer cost){
    	TileImprovementDTO imp = new TileImprovementDTO();
    	imp.setIdent(ident);
    	imp.setCost(cost);
    	return imp;
    }
    @Test
    public void testPhalanx(){
		
		UnitDTO unit = getUnit("phalanx",3L);
		UnitTypeDTO unitType = getUnitType("phalanx"); 
		ksession.insert(unitType);
		
		TileDTO tile = getTile(3L, "plains", new HashSet<String>());
		tile.setDefenseBonus(0);
		ksession.insert(tile);	
		ksession.insert(unit);
		//ksession.fireAllRules();		
		
		Assert.assertTrue("Phalanx type defense strength", unitType.getDefenseStrength().equals(2));
		Assert.assertTrue("Phalanx without bonus.",unit.getDefenseStrength().equals(2));
	}
    @Test
    public void testPhalanxInForest(){
    	UnitDTO unit = getUnit("phalanx",3L);
		TileDTO tile = getTile(3L, "forest", new HashSet<String>());
		tile.setDefenseBonus(50);
		
		ksession.insert(tile);
		ksession.insert(getUnitType("phalanx"));
		ksession.insert(unit);
		
        //ksession.fireAllRules();
		
		Assert.assertTrue("Phalanx with forest defense bonus.",unit.getDefenseStrength().equals(3));
	}
    @Test
    public void testPhalanxChangeTile(){
    	UnitDTO unit = getUnit("phalanx",3L);
		TileDTO tile = getTile(3L, "forest", new HashSet<String>());
		tile.setDefenseBonus(50);
		ksession.insert(tile);
		TileDTO tile2 = getTile(4L, "plains", new HashSet<String>());
		tile2.setDefenseBonus(0);
		ksession.insert(tile2);
		
		ksession.insert(getUnitType("phalanx"));
		
		FactHandle unitHandle = ksession.insert(unit);
		ksession.addEventListener(new DefaultWorkingMemoryEventListener());
		
       // ksession.fireAllRules();
		
		Assert.assertTrue("Phalanx with forest defense bonus.",unit.getDefenseStrength().equals(3));
		//logger.log(Level.INFO,unit.toString());
		unit.setTile(tile2.getId());
		ksession.update(unitHandle, unit);
		//ksession.fireAllRules();
		Assert.assertTrue("Phalanx plains no defense bonus.",unit.getDefenseStrength().equals(2));
    }
    @Test
    public void testCantIrrigateWhenCloseTilesAreNot(){
    	
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	PlayerDTO player = getPlayer(11L, "honza");
    	UnitDTO unit = getUnit("phalanx",5L);
		unit.setOwner(player.getId());
		
		TileDTO tile = getTile(5L, "forest", new HashSet<String>());
		tile.setDefenseBonus(50); 
		tile.setPosX(45L);
		tile.setPosY(56L);
		TileDTO tile2 = getTile(6L, "forest", new HashSet<String>());
		tile2.setDefenseBonus(50); 
		tile2.setPosX(45L);
		tile2.setPosY(57L);
		
		// insert test data as facts
		ksession.insert(player);
		ksession.insert(getTileImp("irrigation",1));
		ksession.insert(getUnitType("phalanx"));
		ksession.insert(tile);
		ksession.insert(tile2);
		FactHandle unitHandle = ksession.insert(unit);
		
		//ksession.fireAllRules();
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		// rule "Build Irrigation" didn't fire
		Assert.assertFalse(firedRules.contains("Build Irrigation"));
		
	}
    @Test
    public void testCanIrrigateWhenOneCloseTileIs(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	PlayerDTO player = getPlayer(11L, "honza");
    	TileDTO tile = getTile(3L, "forest", new HashSet<String>());
		tile.setDefenseBonus(50); 
		tile.setPosX(5L);
		tile.setPosY(6L);
		
		UnitDTO unit = getUnit("phalanx",tile.getId());
		unit.setOwner(player.getId());
		unit.setCurrentAction("buildIrrigation");
		TileDTO tile2 = getTile(4L, "plains", new HashSet<String>());
		tile2.getImprovements().add("irrigation");
		tile2.setDefenseBonus(0);
		tile2.setPosX(5L);
		tile2.setPosY(5L);
		TileImprovementDTO irrigation = getTileImp("irrigation",2);
		
		// insert facts
		ksession.insert(tile);
		ksession.insert(tile2);
		ksession.insert(irrigation);
		ksession.insert(getUnitType("phalanx"));
		
		ksession.insert(player);
		FactHandle unitHandle = ksession.insert(unit);
		ksession.fireAllRules();
		List<ProcessInstance> processes = (List<ProcessInstance>)ksession.getProcessInstances();
		System.out.println(processes);
		Assert.assertTrue("Only one process should be in session",processes.size()==1);
		ProcessInstance pi = processes.get(0);
		ksession.signalEvent("turn-new",null);
		assertProcessInstanceActive(pi.getId(), ksession);
		ksession.signalEvent("turn-new",null);
		assertProcessInstanceCompleted(pi.getId(), ksession);
		
		
		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		// rule "Build Irrigation" did fire
		Assert.assertTrue("Build Irrigation Rule fired.",firedRules.contains("Build Irrigation"));
		Assert.assertTrue("Process Build Irrigation completed.",tile.getImprovements().contains("irrigation"));
		Assert.assertNull("Current action should change to null", unit.getCurrentAction());
    }
}