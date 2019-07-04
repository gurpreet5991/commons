package io.mosip.preregistration.util;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.testng.Assert;
import org.testng.annotations.AfterClass;

import io.mosip.kernel.util.CommonLibrary;

public class PreRegistartionDataBaseAccess {
	public SessionFactory factory;
	Session session;
	private static Logger logger = Logger.getLogger(PreRegistartionDataBaseAccess.class);

	public String env = System.getProperty("env.user");

	public Session getDataBaseConnection(String dbName) {

		String dbConfigXml = dbName+env+".cfg.xml";
		try {

		factory = new Configuration().configure(dbConfigXml).buildSessionFactory();
		} catch (HibernateException e) {
			e.printStackTrace();
			logger.info(e.getMessage());
		}
		session = factory.getCurrentSession();
		session.beginTransaction();
		return session;
	}

	@SuppressWarnings("unchecked")
	public List<String> getDbData(String queryString, String dbName) {
		try {
			return (List<String>) getDataBaseConnection(dbName.toLowerCase()).createSQLQuery(queryString).list();

		} catch (IndexOutOfBoundsException e) {
			Assert.assertTrue(false, "error while getting data from db :"+dbName);
			return null;
		}

	}
	@SuppressWarnings("unchecked")
	public void updateDbData(String queryString, String dbName) {
		Query query = getDataBaseConnection(dbName.toLowerCase()).createSQLQuery(queryString);
		int res = query.executeUpdate();
		session.getTransaction().commit();	
	}
	@SuppressWarnings("unchecked")
	public List<String> getConsumedStatus(String queryString, String dbName) {
		return (List<String>) getDataBaseConnection(dbName.toLowerCase()).createSQLQuery(queryString).list();
	}
	@SuppressWarnings("unchecked")
	public Date getHoliday(String queryString, String dbName) {
		return  (Date) getDataBaseConnection(dbName.toLowerCase()).createSQLQuery(queryString).list().get(0);
	}
	@SuppressWarnings("unchecked")
	public void delete(String queryString, String dbName) {
		Query query = getDataBaseConnection(dbName.toLowerCase()).createSQLQuery(queryString);
		int res = query.executeUpdate();
		session.getTransaction().commit();	
	}
	
	
	@AfterClass(alwaysRun = true)
	public void closingSession() {
		if (session != null)
			session.getTransaction().commit();
		session.close();
		factory.close();
	}

	
	
}
