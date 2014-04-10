package cz.muni.fi.civ.newohybat.game.service;

import java.util.Collection;

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

public interface GameService {
	/**
	 * Method used as input to working memory. When the object is present within the memory it gets replace, 
	 * otherwise it is inserted fresh new. Decision is based upon equals and hashCode methods of object.
	 * @param o object to be inserted
	 */
	void insert(Object o);
	/**
	 * 
	 * @param objects
	 */
	void insertAll(Collection<? extends Object> objects);
	
	void startGame();
	
	void stopGame();
	/**
	 * Method getAdvance returns AdvanceDTO object for given ident from working memory.
	 * @param ident Identificator of desired AdvanceDTO 
	 * @return AdvanceDTO with given ident
	 */
	AdvanceDTO getAdvance(String ident);
	/**
	 * 
	 * @param idents
	 * @return
	 */
	Collection<AdvanceDTO> getAdvances(Collection<String> idents);
	/**
	 * 
	 * @return
	 */
	Collection<AdvanceDTO> getAdvances();
	/**
	 * Method getCity serves to retreive a CityDTO object based on its id.
	 * @param id Long id of CityDTO
	 * @return	CityDTO with given id
	 */
	CityDTO getCity(Long id);
	/**
	 * 
	 * @param ids
	 * @return
	 */
	Collection<CityDTO> getCities(Collection<Long> ids);
	/**
	 * 
	 * @return
	 */
	Collection<CityDTO> getCities();
	/**
	 * Method getCityImprovement retreives CityImprovementDTO object with given ident.
	 * @param ident Ident of the CityImprovementDTO
	 * @return CityImprovementDTO with given ident
	 */
	CityImprovementDTO getCityImprovement(String ident);
	/**
	 * 
	 * @param idents
	 * @return
	 */
	Collection<CityImprovementDTO> getCityImprovements(Collection<String> idents);
	/**
	 * 
	 * @return
	 */
	Collection<CityImprovementDTO> getCityImprovements();
	/**
	 * 
	 * @param ident
	 * @return
	 */
	GovernmentDTO getGovernment(String ident);
	/**
	 * 
	 * @param idents
	 * @return
	 */
	Collection<GovernmentDTO> getGovernments(Collection<String> idents);
	/**
	 * 
	 * @return
	 */
	Collection<GovernmentDTO> getGovernments();
	/**
	 * 
	 * @param id
	 * @return
	 */
	PlayerDTO getPlayer(Long id);
	/**
	 * 
	 * @param ids
	 * @return
	 */
	Collection<PlayerDTO> getPlayers(Collection<Long> ids);
	/**
	 * 
	 * @return
	 */
	Collection<PlayerDTO> getPlayers();
	/**
	 * 
	 * @param ident
	 * @return
	 */
	SpecialDTO getSpecial(String ident);
	/**
	 * 
	 * @param idents
	 * @return
	 */
	Collection<SpecialDTO> getSpecials(Collection<String> idents);
	/**
	 * 
	 * @return
	 */
	Collection<SpecialDTO> getSpecials();
	/**
	 * 
	 * @param ident
	 * @return
	 */
	TerrainDTO getTerrain(String ident);
	/**
	 * 
	 * @param idents
	 * @return
	 */
	Collection<TerrainDTO> getTerrains(Collection<String> idents);
	/**
	 * 
	 * @return
	 */
	Collection<TerrainDTO> getTerrains();
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	TileDTO getTile(Long id);
	/**
	 * 
	 * @param ids
	 * @return
	 */
	Collection<TileDTO> getTiles(Collection<Long> ids);
	/**
	 * 
	 * @return
	 */
	Collection<TileDTO> getTiles();
	/**
	 * 
	 * @param coord
	 * @return
	 */
	TileDTO getTileByCoordinate(Coordinate coord);
	/**
	 * 
	 * @param coords
	 * @return
	 */
	Collection<TileDTO> getTilesByCoordinates(Collection<Coordinate> coords);
	/**
	 * 
	 * @param ident
	 * @return
	 */
	TileImprovementDTO getTileImprovement(String ident);
	/**
	 * 
	 * @param ident
	 * @return
	 */
	Collection<TileImprovementDTO> getTileImprovements(Collection<String> ident);
	/**
	 * 
	 * @return
	 */
	Collection<TileImprovementDTO> getTileImprovements();
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	UnitDTO getUnit(Long id);
	/**
	 * 
	 * @param ids
	 * @return
	 */
	Collection<UnitDTO> getUnits(Collection<Long> ids);
	/**
	 * 
	 * @return
	 */
	Collection<UnitDTO> getUnits();
	/**
	 * 
	 * @param ident
	 * @return
	 */
	UnitTypeDTO getUnitType(String ident);
	/**
	 * 
	 * @param idents
	 * @return
	 */
	Collection<UnitTypeDTO> getUnitTypes(Collection<String>idents);
	/**
	 * 
	 * @return
	 */
	Collection<UnitTypeDTO> getUnitTypes();
}
