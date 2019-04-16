package io.mosip.kernel.fsadapter.ceph;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The Class FilesystemCephAdapterApplication.
 */
@SpringBootApplication
public class FilesystemCephAdapterApplication {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(FilesystemCephAdapterApplication.class, args);
	}

}
