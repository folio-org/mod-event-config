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
