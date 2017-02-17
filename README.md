# IRCT-API

## What is the IRCT?
As part of the NIH BD2K PIC-SURE Center of Excellence http://www.pic-sure.org the HMS Department of Biomedical informatics https://dbmi.hms.harvard.edu is developing an open-source infrastructure that will foster the incorporation of multiple heterogeneous patient level clinical, omics and environment datasets. This system embraces the idea of decentralized datasets of varying types, and the protocols used to access them while still providing a simple communication layer that can handle querying, joining, and computing on. The BD2K PIC-SURE RESTful API implementation is called Inter Resource Communication Tool (IRCT).

## What is the IRCT-API?
The IRCT Application Programming Interface (IRCT-API) is the core of the IRCT project. It manages the queries for data, and processing across the different resources.

## Version Information

### 1.3.2
Updated the Result field to include information about what part of the execution process created this result (i.e. EXECUTION/ACTION)

### 1.3.1
This release includes improved documentation and minor code cleanup.

### 1.3
The 1.3 release of the IRCT allows for subqueries, and for queries to be contained as values in fields. It also adds support for arrays to be values in fields. Queries can also include sort clauses, and select operations can now have aggregates.

### 1.2.2
Fixes a bug which caused the IRCT to keep open some files, eventually causing the application to crash.

### 1.2.1
Fixes minor bugs and improves the DB connection to prevent dropped DB connections.

### 1.2
This release added new search for term functionality, so that it can support more advanced searches. Improvements in the JPA, and minor bug fixes were also included.

### 1.1
This release adds event listener functionality so that some features can be added and removed by each implementation, and does not require any changes to the core code. The result data converters were moved to the RI repo, from the API repo.

### 1.0
This is the first release of the IRCT-API.

### 0.3
This release fixes bugs in the IRCT-API 0.2 release

### 0.2
This is the initial prerelease of the IRCT-API it provides a proof of concept of the IRCT.
