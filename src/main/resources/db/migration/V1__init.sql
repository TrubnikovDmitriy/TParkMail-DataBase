--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.9
-- Dumped by pg_dump version 9.5.9


SET client_encoding = 'UTF8';
--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;
COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';

CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;
COMMENT ON EXTENSION citext IS 'data type for case-insensitive character strings';


SET search_path = public, pg_catalog;

--
-- Name: trigger_for_create_path(); Type: FUNCTION; Schema: public; Owner: trubnikov
--

CREATE FUNCTION trigger_for_create_path() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
  NEW.path =
  ((SELECT path FROM posts WHERE post_id=NEW.parent_id) || NEW.post_id);
  RETURN NEW;
END$$;



SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: forums; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE forums (
    forum_id integer NOT NULL,
    admin_id integer NOT NULL,
    title text NOT NULL,
    slug citext NOT NULL
);



--
-- Name: forums_forum_id_seq; Type: SEQUENCE; Schema: public; Owner: trubnikov
--

CREATE SEQUENCE forums_forum_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: forums_forum_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: trubnikov
--

ALTER SEQUENCE forums_forum_id_seq OWNED BY forums.forum_id;


--
-- Name: posts; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE posts (
    post_id integer NOT NULL,
    thread_id integer NOT NULL,
    path integer[] NOT NULL,
    parent_id integer,
    author citext NOT NULL
);



--
-- Name: posts_extra; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE posts_extra (
    post_id integer NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    message text NOT NULL,
    isedited boolean DEFAULT false NOT NULL
);


--
-- Name: posts_post_id_seq; Type: SEQUENCE; Schema: public; Owner: trubnikov
--

CREATE SEQUENCE posts_post_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: posts_post_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: trubnikov
--

ALTER SEQUENCE posts_post_id_seq OWNED BY posts.post_id;


--
-- Name: threads; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE threads (
    thread_id integer NOT NULL,
    forum_id integer NOT NULL,
    author_id integer NOT NULL
);


--
-- Name: threads_extra; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE threads_extra (
    thread_id integer NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    message text NOT NULL,
    slug citext,
    title text NOT NULL
);


--
-- Name: threads_thread_id_seq; Type: SEQUENCE; Schema: public; Owner: trubnikov
--

CREATE SEQUENCE threads_thread_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: threads_thread_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: trubnikov
--

ALTER SEQUENCE threads_thread_id_seq OWNED BY threads.thread_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE users (
    user_id integer NOT NULL,
    nickname citext NOT NULL
);


--
-- Name: users_extra; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE users_extra (
    user_id integer NOT NULL,
    fullname text NOT NULL,
    email citext NOT NULL,
    about text
);


--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: trubnikov
--

CREATE SEQUENCE users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: trubnikov
--

ALTER SEQUENCE users_user_id_seq OWNED BY users.user_id;


--
-- Name: votes; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE votes (
    user_id integer NOT NULL,
    thread_id integer NOT NULL,
    voice smallint NOT NULL
);


--
-- Name: forum_id; Type: DEFAULT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY forums ALTER COLUMN forum_id SET DEFAULT nextval('forums_forum_id_seq'::regclass);


--
-- Name: post_id; Type: DEFAULT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts ALTER COLUMN post_id SET DEFAULT nextval('posts_post_id_seq'::regclass);


--
-- Name: thread_id; Type: DEFAULT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY threads ALTER COLUMN thread_id SET DEFAULT nextval('threads_thread_id_seq'::regclass);


--
-- Name: user_id; Type: DEFAULT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY users ALTER COLUMN user_id SET DEFAULT nextval('users_user_id_seq'::regclass);


--
-- Name: forums_pkey; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY forums
    ADD CONSTRAINT forums_pkey PRIMARY KEY (forum_id);


--
-- Name: posts_extra_pkey; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts_extra
    ADD CONSTRAINT posts_extra_pkey PRIMARY KEY (post_id);


--
-- Name: posts_pkey; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_pkey PRIMARY KEY (post_id);


--
-- Name: threads_extra_pkey; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY threads_extra
    ADD CONSTRAINT threads_extra_pkey PRIMARY KEY (thread_id);


--
-- Name: threads_pkey; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY threads
    ADD CONSTRAINT threads_pkey PRIMARY KEY (thread_id);


--
-- Name: users_extra_user_id_pk; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY users_extra
    ADD CONSTRAINT users_extra_user_id_pk PRIMARY KEY (user_id);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: votes_user_id_thread_id_pk; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY votes
    ADD CONSTRAINT votes_user_id_thread_id_pk PRIMARY KEY (user_id, thread_id);


--
-- Name: forums_slug_uindex; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE UNIQUE INDEX forums_slug_uindex ON forums USING btree (slug);


--
-- Name: posts_path_uindex; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE UNIQUE INDEX posts_path_uindex ON posts USING btree (path);


--
-- Name: threads_extra_slug_uindex; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE UNIQUE INDEX threads_extra_slug_uindex ON threads_extra USING btree (slug);


--
-- Name: users_extra_email_uindex; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE UNIQUE INDEX users_extra_email_uindex ON users_extra USING btree (email);


--
-- Name: users_nickname_uindex; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE UNIQUE INDEX users_nickname_uindex ON users USING btree (nickname);


--
-- Name: generate_path; Type: TRIGGER; Schema: public; Owner: trubnikov
--

CREATE TRIGGER generate_path BEFORE INSERT ON posts FOR EACH ROW EXECUTE PROCEDURE trigger_for_create_path();


--
-- Name: forums_users_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY forums
    ADD CONSTRAINT forums_users_user_id_fk FOREIGN KEY (admin_id) REFERENCES users(user_id);


--
-- Name: posts_extra_posts_post_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts_extra
    ADD CONSTRAINT posts_extra_posts_post_id_fk FOREIGN KEY (post_id) REFERENCES posts(post_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: posts_posts_post_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_posts_post_id_fk FOREIGN KEY (parent_id) REFERENCES posts(post_id) ON UPDATE CASCADE;


--
-- Name: posts_threads_thread_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_threads_thread_id_fk FOREIGN KEY (thread_id) REFERENCES threads(thread_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: posts_users_nickname_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_users_nickname_fk FOREIGN KEY (author) REFERENCES users(nickname) ON UPDATE CASCADE;


--
-- Name: threads_extra_threads_thread_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY threads_extra
    ADD CONSTRAINT threads_extra_threads_thread_id_fk FOREIGN KEY (thread_id) REFERENCES threads(thread_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: threads_forums_forum_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY threads
    ADD CONSTRAINT threads_forums_forum_id_fk FOREIGN KEY (forum_id) REFERENCES forums(forum_id);


--
-- Name: threads_users_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY threads
    ADD CONSTRAINT threads_users_user_id_fk FOREIGN KEY (author_id) REFERENCES users(user_id);


--
-- Name: users_extra_users_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY users_extra
    ADD CONSTRAINT users_extra_users_user_id_fk FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: votes_threads_thread_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY votes
    ADD CONSTRAINT votes_threads_thread_id_fk FOREIGN KEY (thread_id) REFERENCES threads(thread_id);


--
-- Name: votes_users_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY votes
    ADD CONSTRAINT votes_users_user_id_fk FOREIGN KEY (user_id) REFERENCES users(user_id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

