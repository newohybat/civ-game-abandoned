package cz.muni.fi.civ.newohybat.bpmn;

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
import org.drools.io.ResourceFactory;
import org.drools.runtime.rule.QueryResults;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;


public class FightRulesJUnitTest extends BaseJUnitTest {

	private static KnowledgeBase kbase;
	public FightRulesJUnitTest() throws Exception{
	}
	@BeforeClass
	public static void setKnowledgeBase() throws Exception {
		System.out.println("Loading knowledge base.");
		KnowledgeBuilderConfiguration config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        config.setOption(PropertySpecificOption.ALWAYS);
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(config);
		kbuilder.add(ResourceFactory.newClassPathResource("rules/fightRules.drl"), ResourceType.DRL);
		
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

    private static UnitDTO getUnit(Long id,String type, Long pos, Long owner){
    	UnitDTO unit = new UnitDTO();
    	unit.setId(id);
    	unit.setType(type);
    	unit.setAttackStrength(0);
    	unit.setDefenseStrength(0);
    	unit.setTile(pos);
    	unit.setOwner(owner);
    	return unit;
    }
    private static TileDTO getTile(Long id, String terrain){
    	TileDTO tile = new TileDTO();
    	tile.setId(id);
    	tile.setImprovements(new HashSet<String>());
    	tile.setTerrain(terrain);
    	//tile.setDefenseBonus(50);
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
		type.setIdent("phalanx");
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
    public void testUnitsOnSameFileFight(){
//    	ksession.addEventListener(new DebugAgendaEventListener());
//    	ksession.addEventListener(new DebugWorkingMemoryEventListener());
    	TileDTO tile = getTile(3L, "forest");
		tile.setDefenseBonus(50);
		
		PlayerDTO player1 = getPlayer(1L, "Looser");
    	UnitDTO defender = getUnit(1L,"phalanx",tile.getId(), player1.getId());
    	defender.setHealthPoints(10);
    	defender.setDefenseStrength(10);
		
		PlayerDTO player2 = getPlayer(2L, "Conqueror");
		UnitDTO attacker = getUnit(2L,"phalanx", tile.getId(), player2.getId());
		attacker.setHealthPoints(10);
		attacker.setAttackStrength(15);
		
		ksession.setGlobal("timeout", "1s");
		
		ksession.insert(tile);
		ksession.insert(player1);
		ksession.insert(player2);
		ksession.insert(defender);
		ksession.insert(attacker);
		
		
		ksession.insert(getUnitType("phalanx"));
		
		
		ksession.fireAllRules();
		
		QueryResults results = ksession.getQueryResults("Get Unit Dead", new Object[]{defender});
		Assert.assertTrue("Defender died.",results.size()>0);
		Assert.assertTrue("Dead Unit Still In Memory", ksession.getFactHandle(defender)!=null);
		// wait for the timeout set to exceed
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		QueryResults resultsAfter = ksession.getQueryResults("Get Unit Dead", new Object[]{defender});
		Assert.assertTrue("Reference on dead unit removed.",resultsAfter.size()==0);
		System.out.println(ksession.getFactHandle(defender));
		Assert.assertTrue("Dead Unit Removed From Memory", ksession.getFactHandle(defender)==null);
		
    }
}