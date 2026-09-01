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
* `ou=mathematicians,dc=example,dc=com`
* `ou=scientists,dc=example,dc=com`
* `dc=example,dc=com`
## Search Arguments
 
The search arguments comprise of three parts:

1. The base distinguished name (or part thereof) 
2. The filter - follows **LDAP** filter syntax
3. The tree depth (Object, One Level, Subtree)
 
### Search Filter 
 
The search filter syntax structure (compound syntax):
 
> **AND (**`**&**`**)**
> 
> All conditions inside the parentheses must be true. 
> 
> * **Syntax:** `(&(condition1)(condition2)(condition3))`
> * **Example:** `(&(objectClass=user)(department=IT))`\n*(Finds entries that are users **AND** belong to the IT department)*
> 
> **OR (**`**|**`**)**
> 
> At least one condition inside the parentheses must be true. 
> 
> * **Syntax:** `(|(condition1)(condition2)(condition3))`
> * **Example:** `(|(department=Sales)(department=Marketing))`\n*(Finds entries in either the Sales **OR** Marketing departments)*
> 
> **NOT (**`**!**`**)**
> 
> The condition inside the parenthesis must not be true.
> 
> * **Syntax:** `(!condition)`
> * **Example:** `(!(UserAccountControl:1.2.840.113556.1.4.803:=2))`\n*(Finds accounts that are **NOT** disabled)* 
 
 
### **Special Character Escaping**
 
If your search value contains characters that have special meaning in LDAP, you must escape them using a backslash (`\`) followed by the two-digit ASCII hex value:
 
* `*` → `\2a`
* `(` → `\28`
* `)` → `\29`
* `\` → `\5c`
* **Example:** To search for a common name of `Domain Users (built-in)`, the filter looks like: `(cn=Domain Users \28built-in\29)`
 
**LDAP search filter** (which include the object class) is a string expression used to query a directory service. It follows **Prefix Notation** (also known as Polish Notation), meaning logical operators (`&`, `|`, `!`) are always placed *before* their arguments. 
 
| **Search Example** | **Description** |
|----------------|-------------|
| `(&(objectclass=*)(mail=Brian.*))` | Search an email address (includes a wild card) in all object classes.  Attribute name is *mail.*  |
| `(objectclass=inetOrgPerson)` | Return all directory entries that belong to the **inetOrgPerson** |
| `(&(objectclass=inerOrgPerson)(cn=NFB4*))` | Search the object class - **inetOrgPerson** - for common name that with NFB4. |
