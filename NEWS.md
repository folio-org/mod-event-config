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
