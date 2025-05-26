# Backend Template for SE PR Group Phase

## How to run it

### Start the backed

`mvn spring-boot:run`

### Start the backed with test data

If the database is not clean, the test data won't be inserted

`mvn spring-boot:run -Dspring-boot.run.profiles=generateData`

## Default Credentials

The default E-Mail of the admin user is `admin@shyft.local`. The password can be set using the environment
variable `ADMIN_USER_PASSWORD`, however it is `password` by default.