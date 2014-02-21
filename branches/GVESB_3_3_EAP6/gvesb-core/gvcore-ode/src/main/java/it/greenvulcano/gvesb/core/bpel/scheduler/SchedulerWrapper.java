package it.greenvulcano.gvesb.core.bpel.scheduler;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.transaction.TransactionManager;

import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.il.MockScheduler;
import org.apache.ode.utils.GUID;


public  class SchedulerWrapper implements Scheduler {

		MockScheduler scheduler;
		@SuppressWarnings("unused")
		long _nextSchedule;

		public SchedulerWrapper(BpelServerImpl server, TransactionManager txManager) {
			ExecutorService executorService = Executors.newCachedThreadPool();
			scheduler = new MockScheduler(txManager);
			scheduler.setExecutorSvc(executorService);
			scheduler.setJobProcessor(server);
		}

		public String schedulePersistedJob(JobDetails jobDetail, Date when)
		throws ContextException {
			String jobId = scheduler.schedulePersistedJob(jobDetail, when);
			// Invocation checks get scheduled much later, we don't want (or
			// need) to wait for them
			if (jobDetail.getType() != JobType.INVOKE_CHECK)
				_nextSchedule = when == null ? System.currentTimeMillis()
						: when.getTime();
				return jobId;
		}

		public String scheduleMapSerializableRunnable(
				MapSerializableRunnable runnable, Date when)
		throws ContextException {
			runnable.run();
			return new GUID().toString();
		}

		public String scheduleVolatileJob(boolean transacted,
				JobDetails jobDetail) throws ContextException {
			return scheduleVolatileJob(transacted, jobDetail, null);
		}

		public String scheduleVolatileJob(boolean transacted,
				JobDetails jobDetail, Date when) throws ContextException {
			String jobId = scheduler.scheduleVolatileJob(transacted,
					jobDetail, when);
			_nextSchedule = System.currentTimeMillis();
			return jobId;
		}

		public void cancelJob(String jobId) throws ContextException {
			scheduler.cancelJob(jobId);
		}

		public <T> T execTransaction(Callable<T> transaction) throws Exception,
		ContextException {
			return scheduler.execTransaction(transaction, 0);
		}

		public <T> T execTransaction(Callable<T> transaction, int timeout)
		throws Exception, ContextException {
			return scheduler.execTransaction(transaction, timeout);
		}

		@SuppressWarnings("unused")
		public void beginTransaction() throws Exception {
			scheduler.beginTransaction();
		}

		@SuppressWarnings("unused")
		public void commitTransaction() throws Exception {
			scheduler.commitTransaction();
		}

		@SuppressWarnings("unused")
		public void rollbackTransaction() throws Exception {
			scheduler.rollbackTransaction();
		}

		public void setRollbackOnly() throws Exception {
			scheduler.setRollbackOnly();
		}

		public <T> Future<T> execIsolatedTransaction(Callable<T> transaction)
		throws Exception, ContextException {
			return scheduler.execIsolatedTransaction(transaction);
		}

		public boolean isTransacted() {
			return scheduler.isTransacted();
		}

		public void start() {
			scheduler.start();
		}

		public void stop() {
			scheduler.stop();
		}

		public void shutdown() {
			scheduler.shutdown();
		}

		public void registerSynchronizer(Synchronizer synch)
		throws ContextException {
			scheduler.registerSynchronizer(synch);
		}

		public void setJobProcessor(JobProcessor processor)
		throws ContextException {
			scheduler.setJobProcessor(processor);

		}

		public void setPolledRunnableProcesser(
				JobProcessor delegatedRunnableProcessor) {
		}

		@SuppressWarnings("unused")
		public boolean amICoordinator() {
			return true;
		}
	}