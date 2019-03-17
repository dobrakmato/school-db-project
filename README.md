Mestská polícia
-------------

Project for school subject Databases (2).

### Table of contents
- [Assignment](https://github.com/dobrakmato/school-db-project/blob/master/docs/ASSIGNMENT.md)
- [Data Model #1](https://github.com/dobrakmato/school-db-project/blob/master/docs/DATA_MODEL1.pdf)
- [Data Model #2](https://github.com/dobrakmato/school-db-project/blob/master/docs/DATA_MODEL2.pdf)



### FAQ

#### Why Hikari and not connection pooling in JDBC driver.

Connection pooling should not be handled in JDBC driver and even `PGPoolingDataSource` is deprecated of that reason. We
instead use a dedicated connection pooling library HikariCP.