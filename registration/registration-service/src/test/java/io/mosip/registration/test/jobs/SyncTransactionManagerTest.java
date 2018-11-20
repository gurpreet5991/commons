package io.mosip.registration.test.jobs;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.dao.SyncJobTransactionDAO;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dao.SyncJobDAO;
import io.mosip.registration.entity.SyncControl;
import io.mosip.registration.entity.SyncJob;
import io.mosip.registration.entity.SyncTransaction;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.manager.impl.SyncTransactionManagerImpl;
import io.mosip.registration.repositories.SyncTransactionRepository;
import io.mosip.registration.service.impl.JobConfigurationServiceImpl;
import io.mosip.registration.util.healthcheck.RegistrationSystemPropertiesChecker;

public class SyncTransactionManagerTest {

	@Mock
	private Logger logger;

	@Mock
	private SyncTransactionRepository syncTranscRepository;

	@Mock
	private JobExecutionContext jobExecutionContext;

	@Mock
	private JobDetail jobDetail;

	@Mock
	private Trigger trigger;

	@Mock
	private ApplicationContext applicationContext;

	@Mock
	JobDataMap jobDataMap = new JobDataMap();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@InjectMocks
	private SyncTransactionManagerImpl syncTransactionManagerImpl;

	SyncJob syncJob = new SyncJob();

	List<SyncJob> syncJobList;

	HashMap<String, SyncJob> jobMap = new HashMap<>();

	@Mock
	SyncJobTransactionDAO jobTransactionDAO;

	@Mock
	SyncJobDAO syncJobDAO;

	@Before
	public void initializeSyncJob() {
		syncJob.setId("1");
		syncJob.setName("Name");
		syncJob.setApiName("API");
		syncJob.setCrBy("Yaswanth");
		syncJob.setCrDtime(new Timestamp(System.currentTimeMillis()));
		syncJob.setDeletedDateTime(new Timestamp(System.currentTimeMillis()));
		syncJob.setIsActive(true);
		syncJob.setIsDeleted(false);
		syncJob.setLangCode("EN");
		syncJob.setLockDuration("20");
		syncJob.setParentSyncJobId("ParentSyncJobId");
		syncJob.setSyncFrequency("25");
		syncJob.setUpdBy("Yaswanth");
		syncJob.setUpdDtimes(new Timestamp(System.currentTimeMillis()));

		syncJobList = new LinkedList<>();
		syncJobList.add(syncJob);

		syncJobList.forEach(job -> {
			jobMap.put(job.getId(), job);
		});
		JobConfigurationServiceImpl.SYNC_JOB_MAP = jobMap;
	}

	@Test
	public void getJobUsingJobContextTest() {
		Mockito.when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);

		JobKey jobKey = new JobKey("1");

		// Mockito.doReturn(syncJob).when(transactionManagerImplSpy).getJob(jobDetail);
		Mockito.when(jobDetail.getKey()).thenReturn(jobKey);

		// SyncTransactionManagerImpl transactionManagerImplSpy = Mockito.spy(new
		// SyncTransactionManagerImpl());

		Assert.assertSame(syncTransactionManagerImpl.getJob(jobExecutionContext), syncJob);

	}

	/*
	 * @Test public void getJobusingJobDetailTest() { JobKey jobKey = new
	 * JobKey("1");
	 * 
	 * //
	 * Mockito.doReturn(syncJob).when(transactionManagerImplSpy).getJob(jobDetail);
	 * Mockito.when(jobDetail.getKey()).thenReturn(jobKey);
	 * 
	 * Assert.assertSame(syncTransactionManagerImpl.getJob(jobDetail).getId(), "1");
	 * 
	 * }
	 */

	@Test
	public void getJobUsingTriggerTest() {
		Mockito.when(trigger.getJobDataMap()).thenReturn(jobDataMap);
		Mockito.when(jobDataMap.get(Mockito.any())).thenReturn(jobDetail);
		JobKey jobKey = new JobKey("1");

		// Mockito.doReturn(syncJob).when(transactionManagerImplSpy).getJob(jobDetail);
		Mockito.when(jobDetail.getKey()).thenReturn(jobKey);

		Assert.assertSame(syncTransactionManagerImpl.getJob(trigger).getId(), "1");

	}

	@Test
	public void getJobUsingID() {
		Assert.assertSame(syncTransactionManagerImpl.getJob("1").getId(), "1");

	}

	
	
	private SyncTransaction prepareSyncTransaction() {
		SyncTransaction syncTransaction=new SyncTransaction();

		String transactionId = Integer.toString(new Random().nextInt(10000));
		syncTransaction.setId(transactionId);

		syncTransaction.setSyncJobId(syncJob.getId());

		syncTransaction.setSyncDateTime(new Timestamp(System.currentTimeMillis()));
		syncTransaction.setStatusCode("Completed");
		syncTransaction.setStatusComment("Completed");

		// TODO
		syncTransaction.setTriggerPoint("User");

		syncTransaction.setSyncFrom(RegistrationSystemPropertiesChecker.getMachineId());

		// TODO
		syncTransaction.setSyncTo("SERVER???");

		syncTransaction.setMachmId(RegistrationSystemPropertiesChecker.getMachineId());
		// syncTransaction.setCntrId(SessionContext.getInstance().getUserContext().getRegistrationCenterDetailDTO()
		// .getRegistrationCenterId());

		// TODO
		/*
		 * syncTransaction.setRefId("REFID"); syncTransaction.setRefType("REFTYPE");
		 * syncTransaction.setSyncParam("SyncParam");
		 */

		// TODO
		syncTransaction.setLangCode("EN");

		syncTransaction.setActive(true);

		syncTransaction.setCrBy(SessionContext.getInstance().getUserContext().getUserId());

		syncTransaction.setCrDtime(new Timestamp(System.currentTimeMillis()));
		return syncTransaction;

	}
	@Test
	public void createSyncTest() {
		SyncTransaction syncTransaction = prepareSyncTransaction();
		SyncControl syncControl=null;
		Mockito.when(syncJobDAO.findBySyncJobId(Mockito.anyString())).thenReturn(syncControl);
		
		Mockito.when(jobTransactionDAO.save(Mockito.any(SyncTransaction.class))).thenReturn(syncTransaction);
		
		syncTransactionManagerImpl.createSyncTransaction("Completed", "Completed", "USER", syncJob);
	}
	
	@Test
	public void createSyncControlUpdateTest() {
		SyncTransaction syncTransaction = prepareSyncTransaction();
		SyncControl syncControl=new SyncControl();
		Mockito.when(syncJobDAO.findBySyncJobId(Mockito.anyString())).thenReturn(syncControl);
		Mockito.when(syncJobDAO.update(Mockito.any(SyncControl.class))).thenReturn(syncControl);
		syncTransactionManagerImpl.createSyncControlTransaction(syncTransaction);
		
		
	}
	
	@Test(expected = RegBaseUncheckedException.class)
	public void createSyncTransactionExceptionTest() {
		SyncTransaction syncTransaction = null;
//		SyncControl syncControl=new SyncControl();
		Mockito.when(syncJobDAO.findBySyncJobId(Mockito.anyString())).thenThrow(NullPointerException.class);
//		Mockito.when(syncJobDAO.update(Mockito.any(SyncControl.class))).thenReturn(syncControl);
		syncTransactionManagerImpl.createSyncTransaction("Completed", "Completed", "USER", syncJob);
		
		
	}

}
