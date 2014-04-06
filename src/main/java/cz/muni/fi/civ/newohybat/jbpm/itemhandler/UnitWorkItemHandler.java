package cz.muni.fi.civ.newohybat.jbpm.itemhandler;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;

public class UnitWorkItemHandler implements WorkItemHandler {

	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
	
	}

	public void executeWorkItem(WorkItem item, WorkItemManager manager) {
		UnitTypeDTO unitType = (UnitTypeDTO)item.getParameter("unitType");
		CityDTO city = (CityDTO)item.getParameter("city");
		UnitDTO unit = new UnitDTO();
		unit.setType(unitType.getIdent());
		unit.setCurrentAction(null);
		unit.setDistanceHome(0);
		unit.setOwner(city.getOwner());
		unit.setTile(city.getCityCentre());
		// unitService.create(unit);
		unit.setId((long)Math.ceil(Math.random()));	// after preceding line uncomented, delete this one
		city.getHomeUnits().add(unit.getId());
		Map<String,Object> results = new HashMap<String, Object>();
		results.put("unit", unit);
		manager.completeWorkItem(item.getId(), results);
	}

}
