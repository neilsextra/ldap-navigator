package au.org.tso.ldap.navigator;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@ComponentScan("au.org.tso.ldap.navigator")
@RequestMapping("navigator")
public class Navigator {

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public class ResourceNotFoundException extends RuntimeException {
		public ResourceNotFoundException(String message) {
			super(message);
		}
	}

	@ControllerAdvice
	public class ExceptionControllerAdvice {

		// this way you don't need to annotate on the exception directly
		@ExceptionHandler(ResourceNotFoundException.class)
		public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {


			return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
		}

		// default handler, in case the exception is not catch by any other catch method
		@ExceptionHandler(Exception.class)
		public ResponseEntity<String> handleGenericException(Exception ex) {
			var logger = LoggerFactory.getLogger(Navigator.class);
			logger.error(ex.getMessage());

			return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Autowired
	ConnectionManager connectionManager;

	@Autowired
	ConnectionManager conenctionManager;

	@Autowired
	DirectoryExplorer directoryExplorer;

	@Autowired
	DirectoryExporter directoryExporter;

	@GetMapping("/connect")
	HashMap<String, String> connect(@RequestParam("url") String url) throws Exception {
		HashMap<String, String> parts = connectionManager.parse(url);

		LdapConnection connection = connectionManager.connect(url);

		connection.close();

		return parts;

	}

	@GetMapping("/search")
	Vector<String> search(@RequestParam("url") String url, @RequestParam("argument") String argument) throws Exception {
		var logger = LoggerFactory.getLogger(Navigator.class);

		logger.info("Search Started");

		LdapConnection connection = conenctionManager.connect(url);

		Vector<String> entries = directoryExplorer.search(connection, argument);

		connection.close();

		logger.info("Search Successful: " + entries.size());

		return entries;

	}

	@GetMapping("/retrieve")
	Vector<Map<String, String>> retrieve(@RequestParam("url") String url, @RequestParam("argument") String argument) throws Exception {
		var logger = LoggerFactory.getLogger(Navigator.class);

		logger.info("Retrieve Started");

		LdapConnection connection = conenctionManager.connect(url);

		Vector<Map<String, String>>  attributes = directoryExplorer.retrieve(connection, argument);

		connection.close();

		logger.info("Retrieve Successful: " + attributes.size());

		return attributes;

	}

	@GetMapping("/export")
	byte[] export(@RequestParam("url") String url, @RequestParam("dn") String dn) throws Exception {
		var logger = LoggerFactory.getLogger(Navigator.class);

		logger.info("Export Started");

		LdapConnection connection = conenctionManager.connect(url);

		var exportValue = directoryExporter.export(connection, dn);

		connection.close();

		return exportValue;

	}

	public static void main(String[] args) {

		SpringApplication.run(Navigator.class, args);

	}

}
