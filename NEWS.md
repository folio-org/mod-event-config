## 2018-11-02 v0.0.1
 * Add implementation `/eventConfig` which is accessible through OKAPI
 * Add endpoint GET: /eventConfig  
 * Update rest tests
 
  API: 
  
  | METHOD |  URL                          | DESCRIPTION                                                       |
  |--------|-------------------------------|-------------------------------------------------------------------|
  | POST   | /eventConfig                  | Create new event config in storage                                |
  | GET    | /eventConfig                  | Get all event configs or accepted by query                        |
  | GET    | /eventConfig/id               | Get event config from storage                                     |
  | PUT    | /eventConfig/id               | Update event config in storage                                    |
  | DELETE | /eventConfig/id               | Delete event config from storage                                  |

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
