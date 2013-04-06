
package com.neodem.pop.data.util;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * connect us to the pop2 db
 * 
 * @author Vince
 * 
 */
public class Pop2Support {

	private static Log log = LogFactory.getLog(Pop2Support.class.getName());

	private static final String DRIVERNAME = "com.mysql.jdbc.Driver";

	private static final String URL_WITHPW = "jdbc:mysql://orion:3306/pop2?user=popUser&password=pop&autoRecoonect=true";

	private static final String URL = "jdbc:mysql://localhost/pop2";

	private static final String USERNAME = "popUser";

	private static final String PW = "pop";

	private static DataSource ds = null;

	protected void registerDriver() {
		//
		// First we load the underlying JDBC driver.
		// You need this if you don't use the jdbc.drivers
		// system property.
		//
		log.debug("Loading underlying JDBC driver.");
		try {
			Class.forName(DRIVERNAME);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		log.debug("Done.");
	}

	public Pop2Support() {
		registerDriver();
		ds = setupPoolingDataSource();
	}

	public DataSource setupNonPoolingDataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(DRIVERNAME);
		ds.setUsername(USERNAME);
		ds.setPassword(PW);
		ds.setUrl(URL);
		return ds;
	}

	public DataSource setupPoolingDataSource() {
		//
		// First, we'll need a ObjectPool that serves as the
		// actual pool of connections.
		//
		// We'll use a GenericObjectPool instance, although
		// any ObjectPool implementation will suffice.
		//
		ObjectPool connectionPool = new GenericObjectPool(null);

		//
		// Next, we'll create a ConnectionFactory that the
		// pool will use to create Connections.
		// We'll use the DriverManagerConnectionFactory,
		// using the connect string passed in the command line
		// arguments.
		//
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(URL_WITHPW, null);

		//
		// Now we'll create the PoolableConnectionFactory, which wraps
		// the "real" Connections created by the ConnectionFactory with
		// the classes that implement the pooling functionality.
		//
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,
				connectionPool, null, null, false, true);

		//
		// Finally, we create the PoolingDriver itself,
		// passing in the object pool we created.
		//
		PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

		return dataSource;
	}

	public static DataSource getDataSource() {
		if (ds == null) {
			new Pop2Support();
		}
		return ds;
	}
}
