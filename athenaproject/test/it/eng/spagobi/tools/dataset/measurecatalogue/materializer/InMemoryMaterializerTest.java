package it.eng.spagobi.tools.dataset.measurecatalogue.materializer;


import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogue;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogueMeasure;
import it.eng.spagobi.tools.dataset.measurecatalogue.MeasureCatalogueSingleton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
@Path("/test/inmemorymaterializer")
public class InMemoryMaterializerTest  {

	
	@GET
	@Path("/join_full_noday")
	@Produces(MediaType.APPLICATION_JSON)
	public String joinFull_FullNoDay() {
		return  join("validation_geo_time_full_m1", "validation_geo_time_noday_m1");
	}
	
	@GET
	@Path("/join_full_geo")
	@Produces(MediaType.APPLICATION_JSON)
	public String joinFull_geo() {
		return  join("validation_geo_time_full_m1", "validation_geo_full_m1");
	}
	
	@GET
	@Path("/join_full_time")
	@Produces(MediaType.APPLICATION_JSON)
	public String joinFull_time() {
		return  join("validation_geo_time_full_m1", "validation_time_full_m1");
	}
	
	@GET
	@Path("/join_geo_time")
	@Produces(MediaType.APPLICATION_JSON)
	public String joingeo_time() {
		return  join("validation_geo_full_m1", "validation_time_full_m1");
	}
	
	@GET
	@Path("/join_nocomune_noday")
	@Produces(MediaType.APPLICATION_JSON)
	public String joinFull_NoComuneNoDay() {
		return  join("validation_full_nocomune", "validation_geo_time_noday_m1");
	}
	
	
	
	private String join(String dsLabel1, String dsLabel2) {
		
		String s="";
		MeasureCatalogueMeasure measure1=null, measure2=null;		
		MeasureCatalogue mc = MeasureCatalogueSingleton.getMeasureCatologue();
		
		Iterator<MeasureCatalogueMeasure> it = mc.getMeasures().iterator();

		
		while(it.hasNext()){
			measure1 = it.next();
			s = measure1.getDataSet().getLabel();
			if(measure1.getDataSet().getLabel().equals(dsLabel1)){
				break;
			}
		}
		
		 it = mc.getMeasures().iterator();
		
		while(it.hasNext()){
			measure2 = it.next();
			s = measure2.getDataSet().getLabel();
			if(measure2.getDataSet().getLabel().equals(dsLabel2)){
				break;
			}
		}
		

		
		List<MeasureCatalogueMeasure> measures= new ArrayList<MeasureCatalogueMeasure>();
		measures.add(measure1);
		measures.add(measure2);
		
		InMemoryMaterializer imm = new InMemoryMaterializer();
		IDataStore dataStore= imm.joinMeasures(measures);
		
		
		JSONDataWriter dataSetWriter = new JSONDataWriter();
		JSONObject dataStroreJSON =  (JSONObject) dataSetWriter.write(dataStore);
		
		return  dataStroreJSON.toString();

	}
}