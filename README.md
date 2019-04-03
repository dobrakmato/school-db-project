Mestská polícia
-------------

Project for school subject Databases (2).

### Table of contents
- [Assignment](https://github.com/dobrakmato/school-db-project/blob/master/docs/ASSIGNMENT.md)
- [Data Model #1](https://github.com/dobrakmato/school-db-project/blob/master/docs/DATA_MODEL1.pdf)
- [Data Model #2](https://github.com/dobrakmato/school-db-project/blob/master/docs/DATA_MODEL2.pdf)

### Introduction

This is implementation of IS for police department written in Kotlin with PostgreSQL as database server. It contains
simple home-made ORM which is using Java Reflection API to instantiate and introspect classes.

It also has simple console UI to allow users to interact with database. 

### Database

```
docker run --rm -p 5432:5432 -e POSTGRES_PASSWORD=root postgres
```

### FAQ

#### Why Hikari and not connection pooling in JDBC driver.

Connection pooling should not be handled in JDBC driver and even `PGPoolingDataSource` is deprecated of that reason. We
instead use a dedicated connection pooling library HikariCP.