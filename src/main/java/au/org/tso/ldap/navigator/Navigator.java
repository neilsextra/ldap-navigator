package au.org.tso.ldap.navigator;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.LoggerFactory;

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

import org.springframework.beans.factory.annotation.Autowired;

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
	DirectoryExplorer directoryExplorer;

	@Autowired
	DirectoryExporter directoryExporter;

	@GetMapping("/connect")
	HashMap<String, String> connect(@RequestParam("url") String url) throws Exception {
		HashMap<String, String> parts = connectionManager.parse(url);

		connectionManager.reconnect(url);

		return parts;

	}

	@GetMapping("/status")
		String status(@RequestParam("url") String url) throws Exception {
		
		connectionManager.parse(url);

		return Integer.toString(connectionManager.status(url));

	}

	@GetMapping("/search")
	SearchResponse search(@RequestParam("url") String url, @RequestParam("argument") String argument) throws Exception {
		var logger = LoggerFactory.getLogger(Navigator.class);

		logger.info("Search Started: '" + argument + "'");

		LdapConnection connection = connectionManager.connect(url);

		SearchResponse response = directoryExplorer.search(connection, argument);

		logger.info("Search Successful: " + response.getResults().size());

		return response;

	}

	@GetMapping("/next")
	SearchResponse next(@RequestParam("url") String url, @RequestParam("argument") String argument,
			@RequestParam("cursorPosition") String cursorPosition) throws Exception {
		var logger = LoggerFactory.getLogger(Navigator.class);

		logger.info("Next Started: '" + argument + "' - '" + cursorPosition +"'");

		LdapConnection connection = connectionManager.connect(url);

		SearchResponse response = directoryExplorer.next(connection, argument, cursorPosition);

		logger.info("Search Successful: " + response.getResults().size());

		return response;

	}

	@GetMapping("/retrieve")
	Vector<Map<String, String>> retrieve(@RequestParam("url") String url, @RequestParam("argument") String argument)
			throws Exception {
		var logger = LoggerFactory.getLogger(Navigator.class);

		logger.info("Retrieve Started");

		LdapConnection connection = connectionManager.connect(url);

		Vector<Map<String, String>> attributes = directoryExplorer.retrieve(connection, argument);

		logger.info("Retrieve Successful: " + attributes.size());

		return attributes;

	}

	@GetMapping("/export")
	byte[] export(@RequestParam("url") String url, @RequestParam("dn") String dn) throws Exception {
		var logger = LoggerFactory.getLogger(Navigator.class);

		logger.info("Export Started");

		LdapConnection connection = connectionManager.connect(url);

		var exportValue = directoryExporter.export(connection, dn);

		return exportValue;

	}

	public static void main(String[] args) {

		SpringApplication.run(Navigator.class, args);

	}

}
