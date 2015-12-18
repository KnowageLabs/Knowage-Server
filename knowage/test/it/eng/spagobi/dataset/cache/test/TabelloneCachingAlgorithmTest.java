package it.eng.spagobi.dataset.cache.test;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TabelloneCachingAlgorithmTest {

	private static Connection connection = null;
	private static Statement stmt = null;
	private static ExecutorService pool = null;

	ResultSet rs_d1 = null;
	ResultSet rs_d2 = null;
	ResultSet rs_d3 = null;
	String tableName = null;
	String queryJoin = null;
	String queryTabellone = null;
	String query_d1 = null; // TODO
	String query_d2 = null; // TODO
	String query_d3 = null; // TODO

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection("jdbc:mysql://localhost/ds_cache", "root", "lancer");
		assertNotNull("Connection correctly established", connection);
		stmt = connection.createStatement();
		assertNotNull("Statement correctly created", stmt);

		pool = Executors.newFixedThreadPool(3);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		connection.close();
	}

	@Before
	public void setUp() throws Exception {

		tableName = "sbicache" + UUID.randomUUID().toString().replace("-", "");
		queryJoin = "select d1.`sbicache_row_id` as `d1 - sbicache_row_id`, d1.`gender` as `d1 - gender`, d1.`education` as `d1 - education`, d1.`state_province` as `d1 - state_province`, d1.`total_children` as `d1 - total_children`, d2.`sbicache_row_id` as `d2 - sbicache_row_id`, d2.`gender` as `d2 - gender`, d2.`country` as `d2 - country`, d2.`num_children_at_home` as `d2 - num_children_at_home`, d3.`sbicache_row_id` as `d3 - sbicache_row_id`, d3.`education` as `d3 - education`, d3.`state_province` as `d3 - state_province`, d3.`num_cars_owned` as `d3 - num_cars_owned` from sbicache44bc27bf82f811e5b66cc9 d1, sbicache557ca35082f811e5b66cc9 d2, sbicache6b01f9f182f811e5b66cc9 d3 where d1.`state_province` = d3.`state_province` and d1.`gender` = d2.`gender` and d1.`education` = d3.`education`";
		queryTabellone = "CREATE TABLE " + tableName + " AS (" + queryJoin + ")";

		query_d1 = "SELECT D1.gender, D1.education, D1.state_province, D1.total_children FROM " + tableName + " WHERE ";
		query_d2 = "SELECT D2.gender, D2.country, D2.num_children_at_home FROM " + tableName + " WHERE ";
		query_d3 = "SELECT D3.education, D3.state, D3.num_cars_owned FROM " + tableName + " WHERE ";
	}

	@After
	public void tearDown() throws Exception {
		tableName = null;
		queryJoin = null;
		queryTabellone = null;
		query_d1 = null;
		query_d2 = null;
		query_d3 = null;
	}

	@Test
	public void test() throws Exception {
		stmt.execute(queryTabellone);

		// Collection<Handler> threads = new ArrayList<Handler>(3);
		// threads.add(new Handler(connection.createStatement(), query_d1));
		// threads.add(new Handler(connection.createStatement(), query_d2));
		// threads.add(new Handler(connection.createStatement(), query_d3));
		// pool.invokeAll(threads, 60, TimeUnit.SECONDS);
	}

	class Handler implements Callable<ResultSet> {
		private final Statement stmt;
		private final String query;

		Handler(Statement stmt, String query) {
			this.stmt = stmt;
			this.query = query;
		}

		@Override
		public ResultSet call() throws Exception {
			return stmt.executeQuery(query);
		}
	}
}