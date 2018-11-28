# mod-event-config

Copyright (C) 2018 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0.
See the file "[LICENSE](LICENSE)" for more information.


<!-- ../../okapi/doc/md2toc -l 2 -h 4 README.md -->
* [Introduction](#introduction)
* [Compiling](#compiling)
* [Docker](#docker)
* [Installing the module](#installing-the-module)
* [Deploying the module](#deploying-the-module)

## Introduction

The module provides information describing the notification events. 
The event configuration describes the event name, delivery channels, and contain references to templates for each delivery channel.

## API

Module provides next API:

  | METHOD |  URL                          | DESCRIPTION                                                       |
  |--------|-------------------------------|-------------------------------------------------------------------|
  | POST   | /eventConfig                  | Create new event config in storage                                |
  | GET    | /eventConfig                  | Get all event configs or accepted by query                        |
  | GET    | /eventConfig/{id}             | Get event config from storage                                     |
  | PUT    | /eventConfig/{id}             | Update event config in storage                                    |
  | DELETE | /eventConfig/{id}             | Delete event config from storage                                  |

## Pre-populated configuration

| Config name            | Description                                | Required context properites                        | Optional context properties |
|------------------------|--------------------------------------------|----------------------------------------------------|-----------------------------|
| CREATE_PASSWORD_EVENT  | Email with link to activate account        | "user.personal.firstName", "user.username", "link" | "institution.name"          |
| RESET_PASSWORD_EVENT   | Email with link to reset password          | "user.personal.firstName", "link"                  | "institution.name"          |
| PASSWORD_CREATED_EVENT | Email notification about activated account | "user.personal.firstName"                          | "institution.name"          |
| PASSWORD_CHANGED_EVENT | Email notification about changed password  | "user.personal.firstName", "dateTime"              | -                           |
| USERNAME_LOCATED_EVENT | Email with username                        | "user.personal.firstName", "user.username"         | "institution.name"          |

## Compiling

```
   mvn install
```

See that it says "BUILD SUCCESS" near the end.

## Docker
Build the docker container with:

  * docker build -t mod-event-config .
   
Test that it runs with:
  
  * docker run -t -i -p 8081:8081 mod-event-config

## Installing the module

Follow the guide of
[Deploying Modules](https://github.com/folio-org/okapi/blob/master/doc/guide.md#example-1-deploying-and-using-a-simple-module)
sections of the Okapi Guide and Reference, which describe the process in detail.

First of all you need a running Okapi instance.
(Note that [specifying](../README.md#setting-things-up) an explicit 'okapiurl' might be needed.)

```
   cd .../okapi
   java -jar okapi-core/target/okapi-core-fat.jar dev
```

We need to declare the module to Okapi:

```
curl -w '\n' -X POST -D -   \
   -H "Content-type: application/json"   \
   -d @target/ModuleDescriptor.json \
   http://localhost:9130/_/proxy/modules
```

That ModuleDescriptor tells Okapi what the module is called, what services it
provides, and how to deploy it.

## Deploying the module

Next we need to deploy the module. There is a deployment descriptor in
`target/DeploymentDescriptor.json`. It tells Okapi to start the module on 'localhost'.

Deploy it via Okapi discovery:

```
curl -w '\n' -D - -s \
  -X POST \
  -H "Content-type: application/json" \
  -d @target/DeploymentDescriptor.json  \
  http://localhost:9130/_/discovery/modules
```

Then we need to enable the module for the tenant:

```
curl -w '\n' -X POST -D -   \
    -H "Content-type: application/json"   \
    -d @target/TenantModuleDescriptor.json \
    http://localhost:9130/_/proxy/tenants/<tenant_name>/modules
```

### Issue tracker

See project [MODLOGIN](https://issues.folio.org/browse/MODLOGIN)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).
