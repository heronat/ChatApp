--
-- PostgreSQL database dump
--

-- Dumped from database version 15.4 (Debian 15.4-2.pgdg120+1)
-- Dumped by pg_dump version 15.4 (Debian 15.4-2.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: canals; Type: TABLE; Schema: public; Owner: sr03_postgres
--

CREATE TABLE public.canals (
    cid integer NOT NULL,
    creator_id integer,
    canal_title character varying(50) NOT NULL,
    canal_description text,
    creation_date date NOT NULL,
    valid_until date NOT NULL
);


ALTER TABLE public.canals OWNER TO sr03_postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: sr03_postgres
--

CREATE TABLE public.users (
    uid integer NOT NULL,
    last_name character varying(36) NOT NULL,
    first_name character varying(36) NOT NULL,
    is_admin boolean DEFAULT false NOT NULL,
    email character varying(320) NOT NULL,
    password_hash text NOT NULL,
    is_users_enabled boolean DEFAULT true
);


ALTER TABLE public.users OWNER TO sr03_postgres;

--
-- Name: users_canals; Type: TABLE; Schema: public; Owner: sr03_postgres
--

CREATE TABLE public.users_canals (
    users_id integer NOT NULL,
    canal_id integer NOT NULL
);


ALTER TABLE public.users_canals OWNER TO sr03_postgres;

--
-- Name: users_invitations; Type: TABLE; Schema: public; Owner: sr03_postgres
--

CREATE TABLE public.users_invitations (
    users_id integer NOT NULL,
    admin_id integer NOT NULL,
    canal_id integer NOT NULL
);


ALTER TABLE public.users_invitations OWNER TO sr03_postgres;

--
-- Data for Name: canals; Type: TABLE DATA; Schema: public; Owner: sr03_postgres
--

COPY public.canals (cid, creator_id, canal_title, canal_description, creation_date, valid_until) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: sr03_postgres
--

COPY public.users (uid, last_name, first_name, is_admin, email, password_hash, is_users_enabled) FROM stdin;
\.


--
-- Data for Name: users_canals; Type: TABLE DATA; Schema: public; Owner: sr03_postgres
--

COPY public.users_canals (users_id, canal_id) FROM stdin;
\.


--
-- Data for Name: users_invitations; Type: TABLE DATA; Schema: public; Owner: sr03_postgres
--

COPY public.users_invitations (users_id, admin_id, canal_id) FROM stdin;
\.


--
-- Name: canals canals_pk; Type: CONSTRAINT; Schema: public; Owner: sr03_postgres
--

ALTER TABLE ONLY public.canals
    ADD CONSTRAINT canals_pk PRIMARY KEY (cid);


--
-- Name: users_canals users_canals_pk; Type: CONSTRAINT; Schema: public; Owner: sr03_postgres
--

ALTER TABLE ONLY public.users_canals
    ADD CONSTRAINT users_canals_pk PRIMARY KEY (canal_id, users_id);


--
-- Name: users_invitations users_invitations_pk; Type: CONSTRAINT; Schema: public; Owner: sr03_postgres
--

ALTER TABLE ONLY public.users_invitations
    ADD CONSTRAINT users_invitations_pk PRIMARY KEY (canal_id, users_id, admin_id);


--
-- Name: users users_pk; Type: CONSTRAINT; Schema: public; Owner: sr03_postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pk PRIMARY KEY (uid);


--
-- Name: users_invitations admin_invitations_users_uid_fk; Type: FK CONSTRAINT; Schema: public; Owner: sr03_postgres
--

ALTER TABLE ONLY public.users_invitations
    ADD CONSTRAINT admin_invitations_users_uid_fk FOREIGN KEY (admin_id) REFERENCES public.users(uid);


--
-- Name: users_invitations canal_invitations___fk; Type: FK CONSTRAINT; Schema: public; Owner: sr03_postgres
--

ALTER TABLE ONLY public.users_invitations
    ADD CONSTRAINT canal_invitations___fk FOREIGN KEY (canal_id) REFERENCES public.canals(cid);


--
-- Name: users_canals canals_users__fk; Type: FK CONSTRAINT; Schema: public; Owner: sr03_postgres
--

ALTER TABLE ONLY public.users_canals
    ADD CONSTRAINT canals_users__fk FOREIGN KEY (canal_id) REFERENCES public.canals(cid);


--
-- Name: canals canals_users_uid_fk; Type: FK CONSTRAINT; Schema: public; Owner: sr03_postgres
--

ALTER TABLE ONLY public.canals
    ADD CONSTRAINT canals_users_uid_fk FOREIGN KEY (creator_id) REFERENCES public.users(uid);


--
-- Name: users_canals users_canals_users_uid_fk; Type: FK CONSTRAINT; Schema: public; Owner: sr03_postgres
--

ALTER TABLE ONLY public.users_canals
    ADD CONSTRAINT users_canals_users_uid_fk FOREIGN KEY (users_id) REFERENCES public.users(uid);


--
-- Name: users_invitations users_invitations_users_uid_fk; Type: FK CONSTRAINT; Schema: public; Owner: sr03_postgres
--

ALTER TABLE ONLY public.users_invitations
    ADD CONSTRAINT users_invitations_users_uid_fk FOREIGN KEY (users_id) REFERENCES public.users(uid);


--
-- PostgreSQL database dump complete
--

