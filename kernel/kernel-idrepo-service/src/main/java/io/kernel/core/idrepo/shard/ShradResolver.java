package io.kernel.core.idrepo.shard;

import javax.sql.DataSource;

/**
 * @author Manoj SP
 *
 */
public interface ShradResolver {
	
	DataSource getShrad(String id);
}
