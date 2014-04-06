package cz.muni.fi.civ.newohybat.game.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;

import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileDTO;

@RequestScoped
public class GameService {
	@Inject @ApplicationScoped
	StatefulKnowledgeSession ksession;
	
	public void insert(TileDTO o){
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
}
