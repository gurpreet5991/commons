package io.mosip.registration.repositories;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.registration.entity.MachineMaster;
import io.mosip.registration.entity.id.RegMachineSpecId;

/**
 * The repository interface for {@link MachineMaster} entity
 * @author Yaswanth S
 * @since 1.0.0
 *
 */
public interface MachineMasterRepository extends BaseRepository<MachineMaster, RegMachineSpecId>{
	
	/**
	 * Find the station id based on macAddress.
	 *
	 * @param macAddress macAddress to get {@link MachineMaster}
	 * @param langCode the lang code
	 * @return the machine master
	 */
	MachineMaster findByIsActiveTrueAndMacAddressAndRegMachineSpecIdLangCode(String macAddress, String langCode);
	
}
