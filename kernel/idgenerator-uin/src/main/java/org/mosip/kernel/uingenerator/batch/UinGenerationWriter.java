package org.mosip.kernel.uingenerator.batch;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.mosip.kernel.uingenerator.entity.UinEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class have functionality to persists the list of uins in database
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Component
public class UinGenerationWriter implements ItemWriter<List<UinEntity>> {

	/**
	 * The Logger instance
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(UinGenerationWriter.class);

	/**
	 * Interface used to interact with the persistence context.
	 */
	@Autowired
	private EntityManager entityManager;

	/**
	 * The main runtime interface between a Java application and Hibernate.
	 */
	private Session session;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
	 */
	@Override
	public void write(List<? extends List<UinEntity>> uins) {
		setSession();
		LOGGER.info("Persisting generated uins in database");

		uins.stream().flatMap(List::stream).forEach(this::persistUin);

		LOGGER.info("Persisted generated uins in database");
	}

	/**
	 * Persist a uin in database. If that uin already exists than rollback
	 * 
	 * @param item
	 */
	private void persistUin(UinEntity item) {
		Session currentSession = getSession();
		if (!currentSession.getTransaction().isActive()) {
			currentSession.getTransaction().begin();
		}
		currentSession.save(item);
		try {
			currentSession.flush();
			currentSession.getTransaction().commit();
		} catch (PersistenceException e) {
			currentSession.getTransaction().rollback();
		} finally {
			currentSession.clear();
		}
	}

	/**
	 * Function to set {@link #session} from {@link #entityManager}
	 */
	private void setSession() {
		entityManager = entityManager.getEntityManagerFactory().createEntityManager();
		this.session = entityManager.unwrap(Session.class);
	}

	/**
	 * Function to get session
	 * 
	 * @return {@link #session}
	 */
	private Session getSession() {
		return session;
	}
}