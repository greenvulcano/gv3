﻿CREATE TABLE SERVICE_INFORMATIONS (
    SYSTEM          VARCHAR2(100) NOT NULL,
    SERVICE         VARCHAR2(100) NOT NULL,
    ID              VARCHAR2(24) NOT NULL,
    START_TIME      NUMBER NOT NULL,
    STOP_TIME       NUMBER NOT NULL,
    START_DATE      DATE NOT NULL,
    STOP_DATE       DATE NOT NULL,
    STATE           NUMBER NULL,
    ERROR_CODE      NUMBER NULL,
    PACKAGE_NAME    VARCHAR2(250) NOT NULL,
    PROCESS_NAME    VARCHAR2(250) NOT NULL,
    PROCESS_TIME    NUMBER NOT NULL
    )
/
