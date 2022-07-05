# nphc

NPHC Software Engineer Assignment (Backend)

### Selvaraj Ramesh Assignment

- Github : https://github.com/srameshfriends/nphc

#### Overview

The web service maintains an employee list with the following information:

- id - unique alphanumeric ID assigned by the company.
- login - unique alphanumeric login assigned by the company.
- name - possibly non-unique name. May not be in English, so please use UTF-8 encoding.
- salary - decimal.
- startDate - date of start of employment.

#### Upload Employees

- CSV format should be in UTF-8 encoding to allow for non-English characters.
- 1st row is the header row with id, login, name, salary, startDate columns.
- Date of start of employment in one of two formats “yyyy-mm-dd” or “dd-mmm-yy”.
- All columns must be filled, Salary should be more than zero, login should be unique.

#### Fetch list of Employees

- Pagination added to get list of employees details.
- Filter employees based on salary range.
- Ordered by employee ID, also login, name salary and start date can be ordered with ascending or
  descending.

#### Create, Retrieve, Update and Delete Operations.

-Get employee by id the api url : /users/{$id} , Method GET.
-Create employee api url : /users , Method : POST, Request Format : json.
-Update employee api url : /users , Method : PUT/PATCH, Request Format : json.
-Delete employee by id the api url : /users/{$id} , Method DELETE.

#### Dependency

- Java 1.8
- Spring Boot Framework, Web, Jdbc 2.7.1
- H2 Database 2.1.214
- Commons-csv 1.9.0
- slf4j logging 2.0.0-alpha7
- HikariCP 4.0.3

#### Testing

- MockWeb testing library is used.
- Html pages help to test backend service. (Included)

#### Others

- Jdbc template is used to communicate with database to application service.
- Data transfer object model helps to validate and parse & format the data type.
- maven build library, github version management, docker container service. 