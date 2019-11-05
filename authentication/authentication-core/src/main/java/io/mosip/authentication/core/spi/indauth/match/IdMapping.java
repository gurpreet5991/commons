package io.mosip.authentication.core.spi.indauth.match;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * The Interface IdMapping.
 *
 * @author Dinesh Karuppiah.T
 */

public interface IdMapping {

	/**
	 * Method to get ID name.
	 *
	 * @return the idname
	 */
	public String getIdname();

	/**
	 * Method to get Mapping Function.
	 *
	 * @return the mapping function
	 */
	public BiFunction<MappingConfig, MatchType, List<String>> getMappingFunction();

	/**
	 * Method to get ID Mapping.
	 *
	 * @param name the name
	 * @param values the values
	 * @return the id mapping
	 */
	public static Optional<IdMapping> getIdMapping(String name, IdMapping[] values) {
		return Stream.of(values).filter(m -> m.getIdname().equals(name)).findAny();
	}
	
	public Set<IdMapping> getSubIdMappings();
	
	public String getType();
	
	public String getSubType();
	

}
