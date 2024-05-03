## 2024-05-03 v2.7.1
* Release 2.7.1 with Vert.x 4.5 fixing HttpPostRequestDecoder buffer overflow (MODEVENTC-54)

## 2024-03-19 v2.7.0
* Upgrade RAML and Vertx Version (MODEVENTC-53)

## 2023-10-11 v2.6.0
* Logging improvement (MODEVENTC-39)
* Update copyright year (FOLIO-1021)
* Use GitHub Workflows api-lint, api-schema-lint and api-doc (FOLIO-3678)
* Update to Java 17 (MODEVENTC-50)

## 2023-02-23 v2.5.0
* Add log4j configuration (MODEVENTC-47)

## 2022-10-18 v2.4.0
* Upgrade to RMB v35.0.0, Vertx v4.3.3 (MODEVENTC-43)

## 2022-06-27 v2.3.0
* Upgrade to RMB v34.0.0 (MODEVENTC-41)

## 2022-02-23 v2.2.0
* Add new summary id trait for the transaction API (MODFISTO-268)
* Add totalRecords calculation options: exact, estimated, none, auto (RMB-724, RMB-718)
* Remove lang query parameter and language.raml trait (FOLIO-3351)
* Use string, not enum, for easy calling of PgUtil methods
* Move totalRecords before offset

## 2021-06-11 v2.1.0
 * Upgrade to RMB 33.0.0 and Vert.x 4.1.0 (MODEVENTC-30)

## 2021-03-09 v1.7.0
 * Upgrade to RMB 32.1.0 and Vert.x 4.0.0 (MODEVENTC-27)
 * Upgrade to RMB 31.1.5 and Vert.x 3.9.4 (MODEVENTC-25)
 * Document jar options in README.md (MODEVENTC-24)

## 2020-10-09 v1.6.0
 * Upgrade to RMB v31 and JDK11 (MODEVENTC-22)
 
## 2020-06-11 v1.5.0
 * Update RMB version to 30.0.2 and Vertx to 3.9.1 (MODEVENTC-19)
 * Update "CREATE_PASSWORD_EVENT" email template (MODTEMPENG-44)
 
## 2019-04-12 v1.4.1
 * Update RMB version (MODEVENTC-17)
 * Use JVM features to manage container memory (MODEVENTC-16)
 * Fix security vulnerabilities reported in jackson-databind (MODEVENTC-15)
 * Tenant with "X-Okapi-Request-Id: 1" fails with 500 Internal Server Error (MODEVENTC-14)
 * Remove old ModuleDescriptor "metadata" section (FOLIO-2321)
 * Reset password email: Entire Reset password URL is not hyperlinked | Revised reset password template (MODTEMPENG-27)
 * Add LaunchDescriptor settings  (FOLIO-2234)
 * Enable kube-deploy pipeline for platform-core modules (FOLIO-2256)

## 2019-09-10 v1.3.2
 * Fix security vulnerabilities reported in jackson-databind (MODEVENTC-11)

## 2019-07-23 v1.3.1
 * Fix security vulnerabilities reported in jackson-databind

## 2019-06-11 v1.3.0
 * Fix security vulnerabilities reported in jackson-databind
 * Add links to README additional info (FOLIO-473)
 * Initial module metadata (FOLIO-2003)

## 2019-05-07 v1.2.0
 * Refactor EventConfigAPIs to use PgUtil (MODEVENTC-6)

## 2019-03-14 v1.1.0
 * Fix security vulnerabilities reported in jackson-databind (MODEVENTC-4)
 
## 2018-11-02 v1.0.0
 * Add endpoint GET: /eventConfig  
 
  API: 
  
  | METHOD |  URL                          | DESCRIPTION                                                       |
  |--------|-------------------------------|-------------------------------------------------------------------|
  | POST   | /eventConfig                  | Create new event config in storage                                |
  | GET    | /eventConfig                  | Get all event configs or accepted by query                        |
  | GET    | /eventConfig/{id}             | Get event config from storage                                     |
  | PUT    | /eventConfig/{id}             | Update event config in storage                                    |
  | DELETE | /eventConfig/{id}             | Delete event config from storage                                  |
