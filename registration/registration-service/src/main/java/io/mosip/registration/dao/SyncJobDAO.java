package io.mosip.registration.dao;

import java.util.List;

import io.mosip.registration.entity.SyncControl;

/**
 * DAO class for SyncJobDAO.
 *
 * @author Chukka Sreekar
 * @since 1.0.0
 */
public interface SyncJobDAO {

	/**
	 * Gets the values for sync status.
	 *
	 * @return the syncInfo
	 */
	public SyncJobInfo getSyncStatus();

	/**
	 * Gets the sync count.
	 *
	 * @return the sync count
	 */
	/**
	 * Instantiates a new sync job info.
	 *
	 * @param comparableList
	 *            the comparable list
	 * @param syncCount
	 *            the sync count
	 */
	public class SyncJobInfo {

		/** The comparable list. */
		private List<SyncControl> syncControlList;

		/** The sync count. */
		private double yetToExportCount;

		public SyncJobInfo(List<SyncControl> syncControlList, double yetToExportCount) {
			super();
			this.syncControlList = syncControlList;
			this.yetToExportCount = yetToExportCount;
		}

		/**
		 * @return the syncControlList
		 */
		public List<SyncControl> getSyncControlList() {
			return syncControlList;
		}

		/**
		 * @param syncControlList
		 *            the syncControlList to set
		 */
		public void setSyncControlList(List<SyncControl> syncControlList) {
			this.syncControlList = syncControlList;
		}

		/**
		 * @return the yetToExportCount
		 */
		public double getYetToExportCount() {
			return yetToExportCount;
		}

		/**
		 * @param yetToExportCount
		 *            the yetToExportCount to set
		 */
		public void setYetToExportCount(double yetToExportCount) {
			this.yetToExportCount = yetToExportCount;
		}

	}

}
