# Resource Store Module

This module is responsible for storing User Account data resources in a Mongo database, and exposing a REST API to allow
other components
to interact with the store.

## Sub-modules

### datamodel

This contains the public API data-model classes (DTOs) which are used to map the test facility bank user account data requests.

### repo

This module is responsible for MongoDB CRUD operations on user account data resources.

It contains Resource Entity definitions which are the internal representation of a user account data and services which interact
with the underlying data repositories.

### api

This module is the public REST API for the test facility bank user account data Store.

It is responsible for implementing the REST Controllers for interacting with the test facility bank user account data store. These controllers then call
services in the repo module to do the desired operations.