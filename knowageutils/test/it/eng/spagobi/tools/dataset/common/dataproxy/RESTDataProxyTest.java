package it.eng.spagobi.tools.dataset.common.dataproxy;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.parameter;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.withHeader;
import static com.xebialabs.restito.semantics.Condition.withPostBodyContaining;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.semantics.Predicate;
import com.xebialabs.restito.server.StubServer;

public class RESTDataProxyTest extends TestCase {

	private static final String RESPONSE_VALID = "{\n" + 
			"    \"a\": [\n" + 
			"        {\n" + 
			"            \"b\": \"b1\",\n" + 
			"            \"c\": \"c1\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"b\": \"b2\",\n" + 
			"            \"c\": \"c2\"\n" + 
			"        }\n" + 
			"    ]\n" + 
			"}";
	private StubServer server;
	private JSONPathDataReader reader;
	private RESTDataProxy rdp;

	@Override
	protected void setUp() throws Exception {
		server = new StubServer(8090).run();
		List<JSONPathAttribute> jsonPathAttributes = getJsonPathAttributes();
		reader = new JSONPathDataReader("$.a", jsonPathAttributes,false,false);
		Map<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put("d", "e");
		rdp = new RESTDataProxy("http://localhost:8090/c?q=p", HttpMethod.Post, "{\n" + 
				"    \"id\": \"z\"\n" + 
				"}", requestHeaders,"myOffset","myFetchSize","myMaxResults",false);
	}
	
	public void testNGSI() {
		rdp = new RESTDataProxy("http://localhost:8090/v1/queryContext", HttpMethod.Post, "{\n" + 
				"    \"entities\": [\n" + 
				"        {\n" + 
				"            \"isPattern\": \"true\",\n" + 
				"            \"id\": \".*\"\n" + 
				"        }\n" + 
				"    ]\n" + 
				"}", new HashMap<String, String>(),null,null,null,true);
		
		whenHttp(server).match(post("/v1/queryContext"), withPostBodyContaining("{\n" + 
				"    \"entities\": [\n" + 
				"        {\n" + 
				"            \"isPattern\": \"true\",\n" + 
				"            \"id\": \".*\"\n" + 
				"        }\n" + 
				"    ]\n" + 
				"}"),withHeader( "Accept", "application/json"),withHeader( "Content-Type", "application/json")).then(ok(),
				stringContent(RESPONSE_VALID));
		IDataStore load = rdp.load(reader);
		assertReader(load);
	}

	private static List<JSONPathAttribute> getJsonPathAttributes() {
		List<JSONPathAttribute> res = new ArrayList<JSONPathDataReader.JSONPathAttribute>();
		JSONPathAttribute jpa = new JSONPathAttribute("b", "$.b", "string");
		res.add(jpa);

		JSONPathAttribute jpa2 = new JSONPathAttribute("c", "$.c", "string");
		res.add(jpa2);
		return res;
	}
	
	public void testFailResponse() throws URISyntaxException {
		whenHttp(server).match(post("/c"), parameter("q", "p"), withPostBodyContaining("{\n" + 
				"    \"id\": \"z\"\n" + 
				"}"),withHeader("d", "e")).then(ok(),
				stringContent("not valid"));
		
		boolean done=false;
		try {
			rdp.load(reader);
		} catch (Exception e) {
			done =true;
		}
		assertTrue(done);
		
	}

	static class PrintCondition extends Condition {

		public PrintCondition(Predicate<Call> predicate) {
			super(predicate);
			
		}
	
		
		@Override
		public Predicate<Call> getPredicate() {
			return new Predicate<Call>() {

				public boolean apply(Call c) {
					System.out.println(c.getUri());
					for (String param : c.getParameters().keySet()) {
						System.out.println(param+" "+Arrays.deepToString(c.getParameters().get(param)));
					}
					System.out.println(c.getPostBody());
					return true;
				}
			};
		}
		
		
		
	}
	public void testLoad() throws URISyntaxException {
		
		whenHttp(server).match(new PrintCondition(null),post("/c"), parameter("q", "p"), withPostBodyContaining("{\n" + 
				"    \"id\": \"z\"\n" + 
				"}"),parameter("myOffset", "10"),parameter("myFetchSize", "15"),parameter("myMaxResults", "30"),withHeader("d", "e")).then(ok(),
				stringContent(RESPONSE_VALID));
		
		
		rdp.setFetchSize(15);
		rdp.setMaxResults(30);
		rdp.setOffset(10);
		
		IDataStore load = rdp.load(reader);
		assertReader(load);
	}

	private void assertReader(IDataStore load) {
		assertEquals(2,load.getRecordsCount());
		boolean[] done=new boolean[2];
		for (int i = 0; i < load.getRecordsCount(); i++) {
			IRecord rec = load.getRecordAt(i);
			assertEquals(2, rec.getFields().size());
			for (IField field: rec.getFields()) {
				if ("c2".equals(field.getValue())) {
					done[0]=true;
					continue;
				}
				if ("b2".equals(field.getValue())) {
					done[1]=true;
					continue;
				}
			}
			//both must be true or false
			assert !(done[0] ^ done[1]);
		}
		for (int i = 0; i < done.length; i++) {
			assertTrue(Integer.toString(i), done[i]);
		}
	}
	
	

	@Override
	protected void tearDown() throws Exception {
		server.stop();
	}

}
