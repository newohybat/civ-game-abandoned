package cz.muni.fi.civ.newohybat.game.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;

import cz.muni.fi.civ.newohybat.drools.events.TurnEvent;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.AdvanceDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.GovernmentDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.SpecialDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TerrainDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.iface.CivBackend;

@RequestScoped
public class GameServiceImpl implements GameService{
	@Inject @ApplicationScoped
	StatefulKnowledgeSession ksession;
	@Inject
	CivBackend cb;
	
	public void insert(Object o){
		FactHandle handle = ksession.getFactHandle(o);
		if(handle==null){
			ksession.insert(o);
		}else{
			ksession.update(handle, o);
		}
		ksession.fireAllRules();
	}
	public List<TileDTO> get(Long x){
		List<TileDTO> tiles= new ArrayList<TileDTO>();
		QueryResults results = ksession.getQueryResults("getTile", new Object[]{x,x});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TileDTO tile = (TileDTO)row.get("$tile"); 
            tiles.add(tile);
         } 
		return tiles;
	}
	public void insertAll(Collection<? extends Object> objects) {
		for(Object o:objects){
			this.insert(o);
		}
	}
	public AdvanceDTO getAdvance(String ident) {
		AdvanceDTO advance= new AdvanceDTO();
		QueryResults results = ksession.getQueryResults("getAdvance", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            advance = (AdvanceDTO)row.get("$advance"); 
        } 
		return advance;
	}
	public Collection<AdvanceDTO> getAdvances(Collection<String> idents) {
		Collection<AdvanceDTO> advances= new ArrayList<AdvanceDTO>();
		QueryResults results = ksession.getQueryResults("getAdvances", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            AdvanceDTO advance = (AdvanceDTO)row.get("$advance");
            advances.add(advance);
        } 
		return advances;
	}
	public Collection<AdvanceDTO> getAdvances() {
		Collection<AdvanceDTO> advances= new ArrayList<AdvanceDTO>();
		QueryResults results = ksession.getQueryResults("getAllAdvances", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            AdvanceDTO advance = (AdvanceDTO)row.get("$advance");
            advances.add(advance);
        } 
		return advances;
	}
	public CityDTO getCity(Long id) {
		CityDTO city= new CityDTO();
		QueryResults results = ksession.getQueryResults("getCity", new Object[]{id});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            city = (CityDTO)row.get("$city"); 
        } 
		return city;
	}
	public Collection<CityDTO> getCities(Collection<Long> ids) {
		Collection<CityDTO> cities= new ArrayList<CityDTO>();
		QueryResults results = ksession.getQueryResults("getCities", new Object[]{ids});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            CityDTO city = (CityDTO)row.get("$city");
            cities.add(city);
        } 
		return cities;
	}
	public Collection<CityDTO> getCities() {
		Collection<CityDTO> cities= new ArrayList<CityDTO>();
		QueryResults results = ksession.getQueryResults("getAllCities", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            CityDTO city = (CityDTO)row.get("$city");
            cities.add(city);
        } 
		return cities;
	}
	public CityImprovementDTO getCityImprovement(String ident) {
		CityImprovementDTO cityImprovement= new CityImprovementDTO();
		QueryResults results = ksession.getQueryResults("getCityImprovement", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            cityImprovement = (CityImprovementDTO)row.get("$cityImprovement"); 
        } 
		return cityImprovement;
	}
	public Collection<CityImprovementDTO> getCityImprovements(
			Collection<String> idents) {
		Collection<CityImprovementDTO> cityImprovements= new ArrayList<CityImprovementDTO>();
		QueryResults results = ksession.getQueryResults("getCityImprovements", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            CityImprovementDTO cityImprovement = (CityImprovementDTO)row.get("$cityImprovement"); 
            cityImprovements.add(cityImprovement);
        } 
		return cityImprovements;
	}
	public Collection<CityImprovementDTO> getCityImprovements() {
		Collection<CityImprovementDTO> cityImprovements= new ArrayList<CityImprovementDTO>();
		QueryResults results = ksession.getQueryResults("getAllCityImprovements", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            CityImprovementDTO cityImprovement = (CityImprovementDTO)row.get("$cityImprovement"); 
            cityImprovements.add(cityImprovement);
        } 
		return cityImprovements;
	}
	public GovernmentDTO getGovernment(String ident) {
		GovernmentDTO government= new GovernmentDTO();
		QueryResults results = ksession.getQueryResults("getGovernment", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            government = (GovernmentDTO)row.get("$government"); 
        } 
		return government;
	}
	public Collection<GovernmentDTO> getGovernments(Collection<String> idents) {
		Collection<GovernmentDTO> governments= new ArrayList<GovernmentDTO>();
		QueryResults results = ksession.getQueryResults("getGovernments", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            GovernmentDTO government = (GovernmentDTO)row.get("$government");
            governments.add(government);
        } 
		return governments;
	}
	public Collection<GovernmentDTO> getGovernments() {
		Collection<GovernmentDTO> governments= new ArrayList<GovernmentDTO>();
		QueryResults results = ksession.getQueryResults("getAllGovernments", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            GovernmentDTO government = (GovernmentDTO)row.get("$government");
            governments.add(government);
        } 
		return governments;
	}
	public PlayerDTO getPlayer(Long id) {
		PlayerDTO player= new PlayerDTO();
		QueryResults results = ksession.getQueryResults("getPlayer", new Object[]{id});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            player = (PlayerDTO)row.get("$player"); 
        } 
		return player;
	}
	public Collection<PlayerDTO> getPlayers(Collection<Long> ids) {
		Collection<PlayerDTO> players= new ArrayList<PlayerDTO>();
		QueryResults results = ksession.getQueryResults("getPlayers", new Object[]{ids});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            PlayerDTO player = (PlayerDTO)row.get("$player");
            players.add(player);
        } 
		return players;
	}
	public Collection<PlayerDTO> getPlayers() {
		Collection<PlayerDTO> players= new ArrayList<PlayerDTO>();
		QueryResults results = ksession.getQueryResults("getAllPlayers", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            PlayerDTO player = (PlayerDTO)row.get("$player");
            players.add(player);
        } 
		return players;
	}
	public SpecialDTO getSpecial(String ident) {
		SpecialDTO special= new SpecialDTO();
		QueryResults results = ksession.getQueryResults("getSpecial", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            special = (SpecialDTO)row.get("$special"); 
        } 
		return special;
	}
	public Collection<SpecialDTO> getSpecials(Collection<String> idents) {
		Collection<SpecialDTO> specials= new ArrayList<SpecialDTO>();
		QueryResults results = ksession.getQueryResults("getSpecials", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            SpecialDTO special = (SpecialDTO)row.get("$special");
            specials.add(special);
        } 
		return specials;
	}
	public Collection<SpecialDTO> getSpecials() {
		Collection<SpecialDTO> specials= new ArrayList<SpecialDTO>();
		QueryResults results = ksession.getQueryResults("getAllSpecials", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            SpecialDTO special = (SpecialDTO)row.get("$special");
            specials.add(special);
        } 
		return specials;
	}
	public TerrainDTO getTerrain(String ident) {
		TerrainDTO terrain= new TerrainDTO();
		QueryResults results = ksession.getQueryResults("getTerrain", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            terrain = (TerrainDTO)row.get("$terrain"); 
        } 
		return terrain;
	}
	public Collection<TerrainDTO> getTerrains(Collection<String> idents) {
		Collection<TerrainDTO> terrains= new ArrayList<TerrainDTO>();
		QueryResults results = ksession.getQueryResults("getTerrains", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TerrainDTO terrain = (TerrainDTO)row.get("$terrain");
            terrains.add(terrain);
        } 
		return terrains;
	}
	public Collection<TerrainDTO> getTerrains() {
		Collection<TerrainDTO> terrains= new ArrayList<TerrainDTO>();
		QueryResults results = ksession.getQueryResults("getAllTerrains", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TerrainDTO terrain = (TerrainDTO)row.get("$terrain");
            terrains.add(terrain);
        } 
		return terrains;
	}
	public TileDTO getTile(Long id) {
		TileDTO tile= new TileDTO();
		QueryResults results = ksession.getQueryResults("getTileById", new Object[]{id});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            tile = (TileDTO)row.get("$tile"); 
        } 
		return tile;
	}
	public Collection<TileDTO> getTiles(Collection<Long> ids) {
		QueryResults results = ksession.getQueryResults("getTilesByIds", new Object[]{ids});
		Collection<TileDTO> tiles=  new ArrayList<TileDTO>();
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TileDTO tile = (TileDTO)row.get("$tile");
            tiles.add(tile);
        } 
		return tiles;
	}
	public Collection<TileDTO> getTiles() {
		QueryResults results = ksession.getQueryResults("getAllTiles", new Object[]{});
		Collection<TileDTO> tiles=  new ArrayList<TileDTO>();
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TileDTO tile = (TileDTO)row.get("$tile");
            tiles.add(tile);
        } 
		return tiles;
	}
	public TileDTO getTileByCoordinate(Coordinate coord) {
		TileDTO tile= new TileDTO();
		QueryResults results = ksession.getQueryResults("getTileByPosition", new Object[]{coord.getX(),coord.getY()});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            tile = (TileDTO)row.get("$tile"); 
        } 
		return tile;
	}
	public Collection<TileDTO> getTilesByCoordinates(Collection<Coordinate> coords) {
		Collection<TileDTO> tiles = new ArrayList<TileDTO>();
		for(Coordinate c:coords){
			tiles.add(this.getTileByCoordinate(c));
		}
		return tiles;
	}
	public TileImprovementDTO getTileImprovement(String ident) {
		TileImprovementDTO tileImp= new TileImprovementDTO();
		QueryResults results = ksession.getQueryResults("getTileImprovement", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            tileImp = (TileImprovementDTO)row.get("$tileImprovement"); 
        } 
		return tileImp;
	}
	public Collection<TileImprovementDTO> getTileImprovements(Collection<String> ident) {
		Collection<TileImprovementDTO> tileImps= new ArrayList<TileImprovementDTO>();
		QueryResults results = ksession.getQueryResults("getTileImprovements", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TileImprovementDTO tileImp = (TileImprovementDTO)row.get("$tileImprovement"); 
            tileImps.add(tileImp);
        } 
		return tileImps;
	}
	public Collection<TileImprovementDTO> getTileImprovements() {
		Collection<TileImprovementDTO> tileImps= new ArrayList<TileImprovementDTO>();
		QueryResults results = ksession.getQueryResults("getAllTileImprovements", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TileImprovementDTO tileImp = (TileImprovementDTO)row.get("$tileImprovement"); 
            tileImps.add(tileImp);
        } 
		return tileImps;
	}
	public UnitDTO getUnit(Long id) {
		UnitDTO unit= new UnitDTO();
		QueryResults results = ksession.getQueryResults("getUnit", new Object[]{id});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            unit = (UnitDTO)row.get("$unit"); 
        } 
		return unit;
	}
	public Collection<UnitDTO> getUnits(Collection<Long> ids) {
		Collection<UnitDTO> units= new ArrayList<UnitDTO>();
		QueryResults results = ksession.getQueryResults("getUnits", new Object[]{ids});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            UnitDTO unit = (UnitDTO)row.get("$unit");
            units.add(unit);
        } 
		return units;
	}
	public Collection<UnitDTO> getUnits() {
		Collection<UnitDTO> units= new ArrayList<UnitDTO>();
		QueryResults results = ksession.getQueryResults("getAllUnits", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            UnitDTO unit = (UnitDTO)row.get("$unit");
            units.add(unit);
        } 
		return units;
	}
	public UnitTypeDTO getUnitType(String ident) {
		UnitTypeDTO unitType= new UnitTypeDTO();
		QueryResults results = ksession.getQueryResults("getUnitType", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            unitType = (UnitTypeDTO)row.get("$unitType"); 
        } 
		return unitType;
	}
	public Collection<UnitTypeDTO> getUnitTypes(Collection<String> idents) {
		Collection<UnitTypeDTO> unitTypes= new ArrayList<UnitTypeDTO>();
		QueryResults results = ksession.getQueryResults("getUnitTypes", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            UnitTypeDTO unitType = (UnitTypeDTO)row.get("$unitType");
            unitTypes.add(unitType);
        } 
		return unitTypes;
	}
	public Collection<UnitTypeDTO> getUnitTypes() {
		Collection<UnitTypeDTO> unitTypes= new ArrayList<UnitTypeDTO>();
		QueryResults results = ksession.getQueryResults("getAllUnitTypes", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            UnitTypeDTO unitType = (UnitTypeDTO)row.get("$unitType");
            unitTypes.add(unitType);
        } 
		return unitTypes;
	}
	public void startGame() {
		ksession.getWorkingMemoryEntryPoint("GameControlStream").insert(new TurnEvent());
		HashMap<String,Object>params = new HashMap<String, Object>();
		params.put("timer-delay", "10s");
		ProcessInstance pi = ksession.createProcessInstance("cz.muni.fi.civ.newohybat.bpmn.turn", params);
		ksession.startProcessInstance(pi.getId());
		
		ksession.fireAllRules();
		
	}
	public void stopGame() {
		QueryResults results = ksession.getQueryResults("getTurnProcess", new Object[]{});
		Iterator<QueryResultsRow> i = results.iterator();
		ProcessInstance pi = null;
		if(i.hasNext()){
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            pi = (ProcessInstance)row.get("$process");
            ksession.signalEvent("cancel", null, pi.getId()); 
        }
		
	}
}
