# ldap-viewer

Sample LDAP Viewer 

## To build  

`mvn clean dependency:copy-dependencies compile package install`

## To Run on port 8091

`mvn spring-boot:run -D"spring-boot.run.jvmArguments='-Dserver.port=8091'"`

# Alternate Instructions

## To build with Maven Wrapper - assuming basic authentiction is required
Set the environment variables MVNW_USERNAME and MVNW_PASSWORD (if Necessary)

`mvn wrapper:wrapper`
`.\mvnw.cmd clean install package`

## To Run on port 8085 default 8080

`java '-Dserver.port=8085' -jar .\target\ldap-viewer-0.0.1-SNAPSHOT.jar`

## Run without the command line and also port 8085 default 8080

`javaw '-Dserver.port=8085' -jar .\target\ldap-viewer-0.0.1-SNAPSHOT.jar`

## Example Connection String

`ldap://<user-dn>:<password>@<hostname>:389`

Example
`ldap://cn=read-only-admin,dc=example,dc=com:password@ldap.forumsys.com:389`

Queries
`ou=mathematicians,dc=example,dc=com`
`ou=scientists,dc=example,dc=com`
`dc=example,dc=com`
