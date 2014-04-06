//package cz.muni.fi.civ.newohybat.bpmn;
//
//import static org.mockito.Mockito.atLeastOnce;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import javax.persistence.Persistence;
//import javax.transaction.UserTransaction;
//
//import org.drools.KnowledgeBase;
//import org.drools.KnowledgeBaseFactory;
//import org.drools.base.RuleNameMatchesAgendaFilter;
//import org.drools.builder.KnowledgeBuilder;
//import org.drools.builder.KnowledgeBuilderConfiguration;
//import org.drools.builder.KnowledgeBuilderFactory;
//import org.drools.builder.ResourceType;
//import org.drools.builder.conf.PropertySpecificOption;
//import org.drools.event.DebugProcessEventListener;
//import org.drools.event.rule.AfterActivationFiredEvent;
//import org.drools.event.rule.AgendaEventListener;
//import org.drools.event.rule.DebugAgendaEventListener;
//import org.drools.event.rule.DebugWorkingMemoryEventListener;
//import org.drools.io.ResourceFactory;
//import org.drools.persistence.jpa.JPAKnowledgeService;
//import org.drools.runtime.Environment;
//import org.drools.runtime.EnvironmentName;
//import org.drools.runtime.process.ProcessInstance;
//import org.drools.runtime.rule.FactHandle;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.mockito.ArgumentCaptor;
//
//import bitronix.tm.TransactionManagerServices;
//import cz.muni.fi.civ.newohybat.drools.events.UnitEvent;
//import cz.muni.fi.civ.newohybat.jbpm.itemhandler.UnitWorkItemHandler;
//import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
//import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityImprovementDTO;
//import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;
//import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;
//
//
//public class CopyOfBuildUnitRulesJUnitTest extends BaseJUnitTest {
// 
//	private static KnowledgeBase kbase;
//	public CopyOfBuildUnitRulesJUnitTest() throws Exception{
//	}
//	@Override
//	protected void setSessions(KnowledgeBase kbase) {
//		Environment env = KnowledgeBaseFactory.newEnvironment();
//
//		env.set( EnvironmentName.ENTITY_MANAGER_FACTORY, Persistence.createEntityManagerFactory( "org.jbpm.persistence.jpa.test" ) );
//
//		env.set( EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager() );
//
//		          
//
//		// KnowledgeSessionConfiguration may be null, and a default will be used
//
//		ksession = JPAKnowledgeService.newStatefulKnowledgeSession( kbase, null, env );
//		
//		appendListeners();
//	}
//	@BeforeClass
//	public static void setKnowledgeBase() throws Exception {
//		System.out.println("Loading knowledge base.");
//		KnowledgeBuilderConfiguration config = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
//        config.setOption(PropertySpecificOption.ALWAYS);
//		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(config);
//		kbuilder.add(ResourceFactory.newClassPathResource("buildUnitRules.drl"), ResourceType.DRL);
//		kbuilder.add(ResourceFactory.newClassPathResource("buildUnit.bpmn"), ResourceType.BPMN2);
//		
//		kbase = kbuilder.newKnowledgeBase();
//		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
//	}
//	@Before
//	public void before(){
//		setSessions(kbase);
//        ksession.getWorkItemManager().registerWorkItemHandler("Unit", new UnitWorkItemHandler());
//	}
//	@After
//	public void after(){
//		ksession.dispose();
//	}
//	private static CityDTO getCity(Long id, String name){
//		CityDTO city = new CityDTO();
//    	city.setId(id);
//    	city.setName(name);
//    	city.setResourcesConsumption(0);
//    	city.setResourcesProduction(0);
//    	city.setUnitsSupport(0);
//    	city.setFoodConsumption(0);
//    	city.setFoodProduction(0);
//    	city.setFoodStock(0);
//    	city.setSize(0);
//    	city.setTradeProduction(0);
//    	city.setPeopleEntertainers(0);
//		city.setPeopleScientists(0);
//		city.setPeopleTaxmen(0);
//		city.setWeLoveDay(false);
//		city.setDisorder(false);
//		city.setSize(1);
//		city.setPeopleHappy(0);
//		city.setPeopleContent(0);
//		city.setPeopleUnhappy(0);
//		city.setImprovements(new HashSet<String>());
//		city.setHomeUnits(new HashSet<Long>());
//    	return city;
//	}
//	
//    private static PlayerDTO getPlayer(Long id, String name){
//		PlayerDTO player = new PlayerDTO();
//		player.setId(id);
//		player.setName(name);
//		player.setLuxuriesRatio(0);
//		player.setTaxesRatio(0);
//		player.setResearchRatio(0);
//		player.setResearch(0);
//		return player;
//	}
//    private static CityImprovementDTO getImprovement(String ident, Integer constructionCost){
//    	CityImprovementDTO imp = new CityImprovementDTO();
//    	imp.setConstructionCost(constructionCost);
//    	imp.setIdent(ident);
//    	return imp;
//    }
//    private static UnitTypeDTO getUnitType(Long id, String ident, Integer cost){
//    	UnitTypeDTO unitType = new UnitTypeDTO();
//    	unitType.setId(id);
//    	unitType.setIdent(ident);
//    	unitType.setCost(cost);
//    	return unitType;
//    }
//    
//    @Test
//    public void testWaitForNewTurnToComplete() throws Exception{
//    	UserTransaction ut = (UserTransaction) (new InitialContext()).lookup( "java:comp/UserTransaction" );
//    	ut.begin();
//    	AgendaEventListener ael = mock( AgendaEventListener.class );
//    	ksession.addEventListener( ael );
//    	// prepare test data
//    	CityDTO city = getCity(1L, "marefy");
//    	Set<String> improvements = new HashSet<String>();
//    	city.setImprovements(improvements);
//    	city.setCurrentUnit("phalanx");
//    	city.setResourcesSurplus(300);
//    	UnitTypeDTO unitType = getUnitType(1L,"phalanx",150);
//		// insert test data as facts
//		ksession.insert(unitType);
//		ksession.insert(city);
//		
//		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("City Start Build Unit"));
//		List<ProcessInstance> processes = (List<ProcessInstance>)ksession.getProcessInstances();
//		
//		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
//		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
//		List<String> firedRules = getFiredRules(aafe.getAllValues());
//		// rule "Build Irrigation" didn't fire
//		Assert.assertTrue(firedRules.contains("City Start Build Unit"));
//		
//		Assert.assertTrue("One Process Should Be Active",processes.size()==1);
//		Long pId = processes.get(0).getId();
//		
//		assertProcessInstanceActive(pId, ksession);
//		
//		ksession.signalEvent("turn-new", null,pId);
//		
//		assertProcessInstanceCompleted(pId, ksession);
//		
//		ut.commit();
//		
//		Assert.assertTrue("City Has New Unit.",city.getHomeUnits().size()==1);
//		
//		Assert.assertTrue("No Pending Processes Are There",ksession.getProcessInstances().size()==0);
//		
//		Assert.assertTrue("City Current Unit Null", city.getCurrentUnit()==null);
//	}
//    
//    @Test
//    public void testCancelUnit() throws Exception{
////    	ksession.addEventListener(new DebugWorkingMemoryEventListener());
////    	ksession.addEventListener(new DebugAgendaEventListener());
//    	// Add mock eventlistener
//    	UserTransaction ut = (UserTransaction) (new InitialContext()).lookup( "java:comp/UserTransaction" );
//    	ut.begin();
//    	AgendaEventListener ael = mock( AgendaEventListener.class );
//    	ksession.addEventListener( ael );
//    	// prepare test data
//    	CityDTO city = getCity(1L, "marefy");
//    	Set<String> improvements = new HashSet<String>();
//    	city.setImprovements(improvements);
//    	city.setCurrentUnit("phalanx");
//    	city.setResourcesSurplus(300);
//    	UnitTypeDTO unitType = getUnitType(1L,"phalanx",150);
//		// insert test data as facts
//		ksession.insert(unitType);
//		FactHandle cityHandle = ksession.insert(city);
//		
//		ksession.fireAllRules(new RuleNameMatchesAgendaFilter("City Start Build Unit"));
//		List<ProcessInstance> processes = (List<ProcessInstance>)ksession.getProcessInstances();
//		
//		
//		
//		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
//		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
//		List<String> firedRules = getFiredRules(aafe.getAllValues());
//		// rule "Build Irrigation" didn't fire
//		Assert.assertTrue(firedRules.contains("City Start Build Unit"));
//		
//		Assert.assertTrue("One Process Should Be Active",processes.size()==1);
//		ProcessInstance p = processes.get(0);
//		
//		assertProcessInstanceActive(p.getId(), ksession);
//		
//		
//		ksession.getWorkingMemoryEntryPoint("ActionCanceledStream").insert(new UnitEvent(city.getId()));
////		city.setCurrentUnit(null);
////		ksession.update(cityHandle, city);
//		ksession.fireAllRules();
//		
//		assertProcessInstanceAborted(p.getId(), ksession);
//		
//		ut.commit();
//		
//		Assert.assertFalse("City Has A Unit.",city.getHomeUnits().size()>0);
//		
//		Assert.assertTrue("No Pending Processes Are There",ksession.getProcessInstances().size()==0);
//	}
//    @Test
//    public void testCompleteAfterThreeTurns() throws Exception{
//    	ksession.addEventListener(new DebugWorkingMemoryEventListener());
//    	ksession.addEventListener(new DebugAgendaEventListener());
//    	ksession.addEventListener(new DebugProcessEventListener());
//    	
//    	UserTransaction ut = (UserTransaction) (new InitialContext()).lookup( "java:comp/UserTransaction" );
//    	ut.begin();
//    	
//    	// Add mock eventlistener
//    	AgendaEventListener ael = mock( AgendaEventListener.class );
//    	ksession.addEventListener( ael );
//    	// prepare test data
//    	CityDTO city = getCity(1L, "marefy");
//    	Set<String> improvements = new HashSet<String>();
//    	city.setImprovements(improvements);
//    	city.setCurrentUnit("phalanx");
//    	city.setResourcesSurplus(60);
//    	UnitTypeDTO unitType = getUnitType(1L,"phalanx",150);
//		// insert test data as facts
//		ksession.insert(unitType);
//		FactHandle cityHandle = ksession.insert(city);
//	
//		ksession.fireAllRules();
//		List<ProcessInstance> processes = (List<ProcessInstance>)ksession.getProcessInstances();
//		
//		
//		
//		ArgumentCaptor<AfterActivationFiredEvent> aafe = ArgumentCaptor.forClass( AfterActivationFiredEvent.class );
//		verify( ael ,atLeastOnce()).afterActivationFired( aafe.capture() );
//		List<String> firedRules = getFiredRules(aafe.getAllValues());
//		
//		Assert.assertTrue(firedRules.contains("City Start Build Unit"));
//		
////		Assert.assertTrue("One Process Should Be Active",processes.size()==1);
////		WorkflowProcessInstance p = (WorkflowProcessInstance)processes.get(0);
//		
//		ksession.signalEvent("turn-new", null);
////		assertProcessInstanceActive(p.getId(), ksession);
//		
//		city.setResourcesSurplus(60);
//		ksession.update(cityHandle, city);
//		ksession.signalEvent("turn-new", null);
////		assertProcessInstanceActive(p.getId(), ksession);
//		city.setResourcesSurplus(60);
//		ksession.update(cityHandle, city);
//		ksession.signalEvent("turn-new", null);
//
//		ut.commit();
////		assertProcessInstanceCompleted(p.getId(), ksession);
////		UnitDTO unit = (UnitDTO)p.getVariable("unit");
//		
//		Assert.assertTrue("No Pending Processes Are There",ksession.getProcessInstances().size()==0);
//		
//		Assert.assertTrue("City Contains Courthouse Improvement.",city.getHomeUnits().size()==1);
//		
////		Assert.assertTrue("Created Unit Is Of Given Type.",unit.getType()==unitType.getIdent());
//		
//	}
//}