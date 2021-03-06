-- MODULE DML038

-- SQL Test Suite, V6.0, Interactive SQL, dml038.sql
-- 59-byte ID
-- TEd Version #

-- AUTHORIZATION HU
   set schema HU;

--O   SELECT USER FROM HU.ECCO;
  VALUES USER;
-- RERUN if USER value does not match preceding AUTHORIZATION comment

-- date_time print

-- TEST:0205 Cartesian product is produced without WHERE clause!

     SELECT GRADE, HOURS, BUDGET
          FROM STAFF, WORKS, PROJ order by GRADE, HOURS, BUDGET;
-- PASS:0205 If 360 rows are selected ?

-- END TEST >>> 0205 <<< END TEST
-- *************************************************////END-OF-MODULE
