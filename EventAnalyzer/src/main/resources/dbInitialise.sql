
CREATE SCHEMA PUBLIC AUTHORIZATION DBA;
CREATE MEMORY TABLE PUBLIC.ALERTS(ID VARCHAR(255) NOT NULL PRIMARY KEY,ALERT BOOLEAN,DURATION INTEGER NOT NULL,HOST VARCHAR(255),TYPE INTEGER);

GRANT DBA TO SA;
SET SCHEMA SYSTEM_LOBS;
