--
-- PostgreSQL database dump
--

-- Dumped from database version 16.3 (Debian 16.3-1.pgdg120+1)
-- Dumped by pg_dump version 16.3 (Debian 16.3-1.pgdg120+1)

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
-- Name: canals; Type: TABLE; Schema: public; Owner: sr03
--

CREATE TABLE public.canals (
    cid integer NOT NULL,
    creator_id integer,
    canal_title character varying(50) NOT NULL,
    canal_description text,
    creation_date timestamp(6) without time zone NOT NULL,
    valid_until timestamp(6) without time zone NOT NULL,
    is_canal_enabled boolean DEFAULT true
);


ALTER TABLE public.canals OWNER TO sr03;

--
-- Name: canals_cid_seq; Type: SEQUENCE; Schema: public; Owner: sr03
--

ALTER TABLE public.canals ALTER COLUMN cid ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.canals_cid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: invitations; Type: TABLE; Schema: public; Owner: sr03
--

CREATE TABLE public.invitations (
    id bigint NOT NULL,
    status character varying(10),
    canal_id integer NOT NULL,
    invitee_id integer DEFAULT 0 NOT NULL,
    inviter_id integer NOT NULL,
    CONSTRAINT invitations_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'DECLINED'::character varying])::text[])))
);


ALTER TABLE public.invitations OWNER TO sr03;

--
-- Name: invitations_id_seq; Type: SEQUENCE; Schema: public; Owner: sr03
--

CREATE SEQUENCE public.invitations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.invitations_id_seq OWNER TO sr03;

--
-- Name: invitations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: sr03
--

ALTER SEQUENCE public.invitations_id_seq OWNED BY public.invitations.id;


--
-- Name: roles; Type: TABLE; Schema: public; Owner: sr03
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    name character varying(20),
    CONSTRAINT roles_name_check CHECK (((name)::text = ANY ((ARRAY['ROLE_USER'::character varying, 'ROLE_MODERATOR'::character varying, 'ROLE_ADMIN'::character varying])::text[])))
);


ALTER TABLE public.roles OWNER TO sr03;

--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: sr03
--

CREATE SEQUENCE public.roles_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.roles_id_seq OWNER TO sr03;

--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: sr03
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;


--
-- Name: tokens; Type: TABLE; Schema: public; Owner: sr03
--

CREATE TABLE public.tokens (
    id bigint NOT NULL,
    expiration_date timestamp(6) without time zone,
    generation_date timestamp(6) without time zone,
    is_revoked boolean,
    token character varying(255),
    username character varying(255),
    user_id integer NOT NULL
);


ALTER TABLE public.tokens OWNER TO sr03;

--
-- Name: tokens_seq; Type: SEQUENCE; Schema: public; Owner: sr03
--

CREATE SEQUENCE public.tokens_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tokens_seq OWNER TO sr03;

--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: sr03
--

CREATE TABLE public.user_roles (
    user_id integer NOT NULL,
    role_id integer NOT NULL
);


ALTER TABLE public.user_roles OWNER TO sr03;

--
-- Name: users; Type: TABLE; Schema: public; Owner: sr03
--

CREATE TABLE public.users (
    uid integer NOT NULL,
    last_name character varying(36) NOT NULL,
    first_name character varying(36) NOT NULL,
    is_admin boolean DEFAULT false NOT NULL,
    email character varying(320) NOT NULL,
    password_hash text NOT NULL,
    is_users_enabled boolean DEFAULT true,
    username character varying(255)
);


ALTER TABLE public.users OWNER TO sr03;

--
-- Name: users_canals; Type: TABLE; Schema: public; Owner: sr03
--

CREATE TABLE public.users_canals (
    users_id integer NOT NULL,
    canal_id integer NOT NULL
);


ALTER TABLE public.users_canals OWNER TO sr03;

--
-- Name: users_invitations; Type: TABLE; Schema: public; Owner: sr03
--

CREATE TABLE public.users_invitations (
    users_id integer NOT NULL,
    canal_id integer NOT NULL
);


ALTER TABLE public.users_invitations OWNER TO sr03;

--
-- Name: users_uid_seq; Type: SEQUENCE; Schema: public; Owner: sr03
--

ALTER TABLE public.users ALTER COLUMN uid ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.users_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: invitations id; Type: DEFAULT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.invitations ALTER COLUMN id SET DEFAULT nextval('public.invitations_id_seq'::regclass);


--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);


--
-- Data for Name: canals; Type: TABLE DATA; Schema: public; Owner: sr03
--

COPY public.canals (cid, creator_id, canal_title, canal_description, creation_date, valid_until, is_canal_enabled) FROM stdin;
\.


--
-- Data for Name: invitations; Type: TABLE DATA; Schema: public; Owner: sr03
--

COPY public.invitations (id, status, canal_id, invitee_id, inviter_id) FROM stdin;
\.


--
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: sr03
--

COPY public.roles (id, name) FROM stdin;
1	ROLE_USER
2	ROLE_MODERATOR
3	ROLE_ADMIN
\.


--
-- Data for Name: tokens; Type: TABLE DATA; Schema: public; Owner: sr03
--

COPY public.tokens (id, expiration_date, generation_date, is_revoked, token, username, user_id) FROM stdin;
\.


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: sr03
--

COPY public.user_roles (user_id, role_id) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: sr03
--

COPY public.users (uid, last_name, first_name, is_admin, email, password_hash, is_users_enabled, username) FROM stdin;
\.


--
-- Data for Name: users_canals; Type: TABLE DATA; Schema: public; Owner: sr03
--

COPY public.users_canals (users_id, canal_id) FROM stdin;
\.


--
-- Data for Name: users_invitations; Type: TABLE DATA; Schema: public; Owner: sr03
--

COPY public.users_invitations (users_id, canal_id) FROM stdin;
\.


--
-- Name: canals_cid_seq; Type: SEQUENCE SET; Schema: public; Owner: sr03
--

SELECT pg_catalog.setval('public.canals_cid_seq', 17, true);


--
-- Name: invitations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sr03
--

SELECT pg_catalog.setval('public.invitations_id_seq', 17, true);


--
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: sr03
--

SELECT pg_catalog.setval('public.roles_id_seq', 3, true);


--
-- Name: tokens_seq; Type: SEQUENCE SET; Schema: public; Owner: sr03
--

SELECT pg_catalog.setval('public.tokens_seq', 2051, true);


--
-- Name: users_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: sr03
--

SELECT pg_catalog.setval('public.users_uid_seq', 20, true);


--
-- Name: canals canals_pk; Type: CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.canals
    ADD CONSTRAINT canals_pk PRIMARY KEY (cid);


--
-- Name: invitations invitations_pkey; Type: CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.invitations
    ADD CONSTRAINT invitations_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: tokens tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.tokens
    ADD CONSTRAINT tokens_pkey PRIMARY KEY (id);


--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- Name: users_canals users_canals_pk; Type: CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.users_canals
    ADD CONSTRAINT users_canals_pk PRIMARY KEY (canal_id, users_id);


--
-- Name: users users_pk; Type: CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pk PRIMARY KEY (uid);


--
-- Name: users_canals canals_users__fk; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.users_canals
    ADD CONSTRAINT canals_users__fk FOREIGN KEY (canal_id) REFERENCES public.canals(cid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: canals canals_users_uid_fk; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.canals
    ADD CONSTRAINT canals_users_uid_fk FOREIGN KEY (creator_id) REFERENCES public.users(uid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: tokens fk2dylsfo39lgjyqml2tbe0b0ss; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.tokens
    ADD CONSTRAINT fk2dylsfo39lgjyqml2tbe0b0ss FOREIGN KEY (user_id) REFERENCES public.users(uid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: invitations fk88f0ea4w8fhwtsy1tkc0sl1xp; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.invitations
    ADD CONSTRAINT fk88f0ea4w8fhwtsy1tkc0sl1xp FOREIGN KEY (invitee_id) REFERENCES public.users(uid) ON UPDATE CASCADE ON DELETE SET DEFAULT;


--
-- Name: invitations fkc93ihvftpd11j547qgc9fobmc; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.invitations
    ADD CONSTRAINT fkc93ihvftpd11j547qgc9fobmc FOREIGN KEY (inviter_id) REFERENCES public.users(uid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: user_roles fkh8ciramu9cc9q3qcqiv4ue8a6; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: user_roles fkhfh9dx7w3ubf1co1vdev94g3f; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(uid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: users_invitations fkjwj4a3s3y08tbcq98m3xlngtx; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.users_invitations
    ADD CONSTRAINT fkjwj4a3s3y08tbcq98m3xlngtx FOREIGN KEY (canal_id) REFERENCES public.canals(cid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: users_invitations fklqupdh1j6u35dph67kh5f5mpt; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.users_invitations
    ADD CONSTRAINT fklqupdh1j6u35dph67kh5f5mpt FOREIGN KEY (users_id) REFERENCES public.users(uid);


--
-- Name: invitations fkn2v7dofguhgm8psv9450xgias; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.invitations
    ADD CONSTRAINT fkn2v7dofguhgm8psv9450xgias FOREIGN KEY (canal_id) REFERENCES public.canals(cid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: users_canals users_canals_users_uid_fk; Type: FK CONSTRAINT; Schema: public; Owner: sr03
--

ALTER TABLE ONLY public.users_canals
    ADD CONSTRAINT users_canals_users_uid_fk FOREIGN KEY (users_id) REFERENCES public.users(uid) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

