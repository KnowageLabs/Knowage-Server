--
-- PostgreSQL database dump
--

-- Dumped from database version 9.4.1
-- Dumped by pg_dump version 9.5.1

-- Started on 2016-03-24 17:16:52

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 1 (class 3079 OID 12723)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2869 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- TOC entry 174 (class 1259 OID 12160424)
-- Name: cachePersistTest_colbigserial_seq; Type: SEQUENCE; Schema: public; Owner: knowagecache
--

CREATE SEQUENCE "cachePersistTest_colbigserial_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 175 (class 1259 OID 12160426)
-- Name: cachePersistTest_colserial_seq; Type: SEQUENCE; Schema: public; Owner: knowagecache
--

CREATE SEQUENCE "cachePersistTest_colserial_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 173 (class 1259 OID 12160418)
-- Name: cachepersisttest; Type: TABLE; Schema: public; Owner: knowagecache
--

CREATE TABLE cachepersisttest (
    colsmallint smallint,
    colint integer,
    colbigint bigint,
    colnumeric numeric,
    colreal real,
    coldoubleprecision double precision,
    colserial integer NOT NULL,
    colbigserial bigint NOT NULL,
    colchar "char",
    coltext text,
    colbytea bytea,
    coldate date,
    coltime time without time zone,
    coltimetz time with time zone,
    colboolean boolean,
    colbit bit(1),
    id integer NOT NULL,
    coltimestamp timestamp without time zone,
    coltimestamptz timestamp with time zone
);


--
-- TOC entry 2870 (class 0 OID 0)
-- Dependencies: 174
-- Name: cachePersistTest_colbigserial_seq; Type: SEQUENCE SET; Schema: public; Owner: knowagecache
--

SELECT pg_catalog.setval('"cachePersistTest_colbigserial_seq"', 1, false);


--
-- TOC entry 2871 (class 0 OID 0)
-- Dependencies: 175
-- Name: cachePersistTest_colserial_seq; Type: SEQUENCE SET; Schema: public; Owner: knowagecache
--

SELECT pg_catalog.setval('"cachePersistTest_colserial_seq"', 1, false);


--
-- TOC entry 2859 (class 0 OID 12160418)
-- Dependencies: 173
-- Data for Name: cachepersisttest; Type: TABLE DATA; Schema: public; Owner: knowagecache
--

COPY cachepersisttest (colsmallint, colint, colbigint, colnumeric, colreal, coldoubleprecision, colserial, colbigserial, colchar, coltext, colbytea, coldate, coltime, coltimetz, colboolean, colbit, id, coltimestamp, coltimestamptz) FROM stdin;
1	2	3	4	5	6	7	8	c	text	\\xdeadbeef	2016-03-22	10:24:49.015678	10:24:49.015678+01	t	1	1	2016-03-22 10:24:49.015678	2016-03-22 10:24:49.015678+01
\.


--
-- TOC entry 2868 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2016-03-24 17:16:54

--
-- PostgreSQL database dump complete
--

