# LDAP Navigator

GTK Sample LDAP Viewer 

## To build  

`mvn clean dependency:copy-dependencies compile package install`

## To Run 

### Maven - on Port 8085 - Default 8080
`mvn spring-boot:run -D"spring-boot.run.jvmArguments='-Dserver.port=8085'"`

### Without Maven
`java -jar .\target\navigator-1.0-SNAPSHOT.jar`

### Without Maven on Port 8080
`java -D"server.port=8085" -jar .\target\navigator-1.0-SNAPSHOT.jar`

## Example Connection String

`ldap://<user-dn>:<password>@<hostname>:389`

Example
`ldap://cn=read-only-admin,dc=example,dc=com@ldap.forumsys.com:389`

### Password for the above example
`password`

Queries
`ou=mathematicians,dc=example,dc=com`
`ou=scientists,dc=example,dc=com`
`dc=example,dc=com`
