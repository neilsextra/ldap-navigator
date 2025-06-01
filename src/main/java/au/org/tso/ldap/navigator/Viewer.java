package au.org.tso.ldap.navigator;

import java.util.HashMap;
import java.util.Vector;

import javax.management.monitor.Monitor;

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
@ComponentScan("au.org.tso.ldap.viewer")
@RequestMapping("viewer")
public class Viewer {

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
			var logger = LoggerFactory.getLogger(Monitor.class);
			logger.error(ex.getMessage());

			return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Autowired
	DirectoryConnector directoryConnector;

	@Autowired
	DirectorySearcher directorySearcher;

	@Autowired
	DirectoryRetriever directoryRetriever;

	@Autowired
	DirectoryExporter directoryExporter;

	@GetMapping("/connect")
	HashMap<String, String> connect(@RequestParam("url") String url) throws Exception {
		var logger = LoggerFactory.getLogger(Viewer.class);

		HashMap<String, String> parts = directoryConnector.parse(url);

		LDAPConnection connection = directoryConnector.getLdapConnection(parts);

		connection.disconnect();

		logger.info("Login Successful");

		return parts;

	}

	@GetMapping("/search")
	Vector<String> search(@RequestParam("url") String url, @RequestParam("argument") String argument) throws Exception {
		var logger = LoggerFactory.getLogger(Viewer.class);

		logger.info("Search Started");

		LDAPConnection connection = directoryConnector.getLdapConnection(directoryConnector.parse(url));
		var rows = new Vector<String>();

		rows = directorySearcher.search(connection, argument);

		connection.disconnect();

		logger.info("Search Successful: " + rows.size());

		return rows;

	}

	@GetMapping("/retrieve")
	Vector<Vector<String>> retrieve(@RequestParam("url") String url, @RequestParam("dn") String dn) throws Exception {
		var logger = LoggerFactory.getLogger(Monitor.class);

		logger.info("Retrieve Started");

		LDAPConnection connection = directoryConnector.getLdapConnection(directoryConnector.parse(url));

		var attributes = directoryRetriever.retrieve(connection, dn);

		connection.disconnect();

		return attributes;

	}

	@GetMapping("/export")
	byte[] export(@RequestParam("url") String url, @RequestParam("dn") String dn) throws Exception {
		var logger = LoggerFactory.getLogger(Viewer.class);

		logger.info("Export Started");

		LDAPConnection connection = directoryConnector.getLdapConnection(directoryConnector.parse(url));

		var exportValue = directoryExporter.export(connection, dn);

		connection.disconnect();

		return exportValue;

	}

	public static void main(String[] args) {

		SpringApplication.run(Viewer.class, args);

	}

}