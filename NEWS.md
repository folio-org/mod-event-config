## 2018-10-22 v0.0.1
 * Initial module setup
 * Add stub endpoints `/eventConfig` which is accessible through OKAPI
 * Add rest tests
 
 API: 
 
 | METHOD |  URL                          | DESCRIPTION                                                       |
 |--------|-------------------------------|-------------------------------------------------------------------|
 | POST   | /eventConfig                  | Create new event config in storage                                |
 | GET    | /eventConfig/id               | Get event config from storage                                     |
 | PUT    | /eventConfig/id               | Update event config in storage                                    |
 | DELETE | /eventConfig/id               | Delete event config from storage                                  |
