--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.10
-- Dumped by pg_dump version 9.5.10

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: citext; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;


--
-- Name: EXTENSION citext; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION citext IS 'data type for case-insensitive character strings';


SET search_path = public, pg_catalog;

--
-- Name: trigger_add_to_forumusers_from_threads(); Type: FUNCTION; Schema: public; Owner: trubnikov
--

CREATE FUNCTION trigger_add_to_forumusers_from_threads() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
    INSERT INTO forum_users(forum_id, user_id)
            VALUES (NEW.forum_id, NEW.author_id)
            ON CONFLICT DO NOTHING;
    RETURN NEW;
  END$$;


ALTER FUNCTION public.trigger_add_to_forumusers_from_threads() OWNER TO trubnikov;

--
-- Name: trigger_post_create_path(); Type: FUNCTION; Schema: public; Owner: trubnikov
--

CREATE FUNCTION trigger_post_create_path() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
    IF (NEW.parent_id!=0)
      THEN NEW.path=array_append((SELECT path FROM posts WHERE post_id=NEW.parent_id), NEW.post_id);
      ELSE NEW.path=ARRAY[NEW.post_id];
    END IF;
    RETURN NEW;
  END$$;


ALTER FUNCTION public.trigger_post_create_path() OWNER TO trubnikov;

--
-- Name: trigger_post_isedited(); Type: FUNCTION; Schema: public; Owner: trubnikov
--

CREATE FUNCTION trigger_post_isedited() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
  NEW.isedited=TRUE;
  RETURN NEW;
  END$$;


ALTER FUNCTION public.trigger_post_isedited() OWNER TO trubnikov;

--
-- Name: trigger_thread_increment(); Type: FUNCTION; Schema: public; Owner: trubnikov
--

CREATE FUNCTION trigger_thread_increment() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
    UPDATE forums SET thread_count = thread_count + 1
    WHERE forum_id = NEW.forum_id;
    RETURN NEW;
  END$$;


ALTER FUNCTION public.trigger_thread_increment() OWNER TO trubnikov;

--
-- Name: trigger_votes_after_insert(); Type: FUNCTION; Schema: public; Owner: trubnikov
--

CREATE FUNCTION trigger_votes_after_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
    IF (NEW.voice)
      THEN UPDATE threads SET votes=votes+1 WHERE thread_id=NEW.thread_id;
      ELSE UPDATE threads SET votes=votes-1 WHERE thread_id=NEW.thread_id;
    END IF;
  RETURN NEW;
  END$$;


ALTER FUNCTION public.trigger_votes_after_insert() OWNER TO trubnikov;

--
-- Name: trigger_votes_after_update(); Type: FUNCTION; Schema: public; Owner: trubnikov
--

CREATE FUNCTION trigger_votes_after_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
    IF (NEW.voice!=OLD.voice)
      THEN IF (NEW.voice)
        THEN UPDATE threads SET votes=votes+2 WHERE thread_id=NEW.thread_id;
        ELSE UPDATE threads SET votes=votes-2 WHERE thread_id=NEW.thread_id;
      END IF;
    END IF;
  RETURN NEW;
  END$$;


ALTER FUNCTION public.trigger_votes_after_update() OWNER TO trubnikov;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: forum_users; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE forum_users (
    forum_id integer NOT NULL,
    user_id integer NOT NULL
);


ALTER TABLE forum_users OWNER TO trubnikov;

--
-- Name: forums; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE forums (
    forum_id integer NOT NULL,
    slug citext NOT NULL,
    title text NOT NULL,
    author_id integer NOT NULL,
    thread_count bigint DEFAULT 0 NOT NULL,
    post_count bigint DEFAULT 0 NOT NULL
);


ALTER TABLE forums OWNER TO trubnikov;

--
-- Name: forums_forum_id_seq; Type: SEQUENCE; Schema: public; Owner: trubnikov
--

CREATE SEQUENCE forums_forum_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE forums_forum_id_seq OWNER TO trubnikov;

--
-- Name: forums_forum_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: trubnikov
--

ALTER SEQUENCE forums_forum_id_seq OWNED BY forums.forum_id;


--
-- Name: posts; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE posts (
    post_id integer NOT NULL,
    thread_id integer,
    author_nickname citext,
    parent_id integer,
    path integer[],
    mess text,
    created timestamp with time zone DEFAULT now() NOT NULL,
    isedited boolean DEFAULT false NOT NULL,
    forum_slug citext NOT NULL
);


ALTER TABLE posts OWNER TO trubnikov;

--
-- Name: posts_post_id_seq; Type: SEQUENCE; Schema: public; Owner: trubnikov
--

CREATE SEQUENCE posts_post_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE posts_post_id_seq OWNER TO trubnikov;

--
-- Name: posts_post_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: trubnikov
--

ALTER SEQUENCE posts_post_id_seq OWNED BY posts.post_id;


--
-- Name: threads; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE threads (
    thread_id integer NOT NULL,
    forum_id integer,
    author_id integer,
    title text NOT NULL,
    mess text,
    slug citext,
    created timestamp with time zone DEFAULT now() NOT NULL,
    votes integer DEFAULT 0 NOT NULL,
    author_nickname citext
);


ALTER TABLE threads OWNER TO trubnikov;

--
-- Name: threads_thread_id_seq; Type: SEQUENCE; Schema: public; Owner: trubnikov
--

CREATE SEQUENCE threads_thread_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE threads_thread_id_seq OWNER TO trubnikov;

--
-- Name: threads_thread_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: trubnikov
--

ALTER SEQUENCE threads_thread_id_seq OWNED BY threads.thread_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE users (
    user_id integer NOT NULL,
    nickname citext NOT NULL,
    fullname text,
    email citext NOT NULL,
    about text
);


ALTER TABLE users OWNER TO trubnikov;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: trubnikov
--

CREATE SEQUENCE users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE users_user_id_seq OWNER TO trubnikov;

--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: trubnikov
--

ALTER SEQUENCE users_user_id_seq OWNED BY users.user_id;


--
-- Name: votes; Type: TABLE; Schema: public; Owner: trubnikov
--

CREATE TABLE votes (
    thread_id integer NOT NULL,
    user_nickname citext NOT NULL,
    voice boolean NOT NULL
);


ALTER TABLE votes OWNER TO trubnikov;

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
-- Name: forum_users_forum_id_user_id_pk; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY forum_users
    ADD CONSTRAINT forum_users_forum_id_user_id_pk PRIMARY KEY (forum_id, user_id);


--
-- Name: forums_pkey; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY forums
    ADD CONSTRAINT forums_pkey PRIMARY KEY (forum_id);


--
-- Name: posts_pkey; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_pkey PRIMARY KEY (post_id);


--
-- Name: threads_pkey; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY threads
    ADD CONSTRAINT threads_pkey PRIMARY KEY (thread_id);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: votes_user_id_thread_id_pk; Type: CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY votes
    ADD CONSTRAINT votes_user_id_thread_id_pk PRIMARY KEY (user_nickname, thread_id);


--
-- Name: forums_slug_uindex; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE UNIQUE INDEX forums_slug_uindex ON forums USING btree (slug);


--
-- Name: posts_author_id_index; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE INDEX posts_author_id_index ON posts USING btree (author_nickname);


--
-- Name: posts_post_id_thread_id_uindex; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE UNIQUE INDEX posts_post_id_thread_id_uindex ON posts USING btree (post_id, thread_id);


--
-- Name: posts_thread_id_parent_id_path_index; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE INDEX posts_thread_id_parent_id_path_index ON posts USING btree (thread_id, parent_id, path);


--
-- Name: threads_author_id_index; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE INDEX threads_author_id_index ON threads USING btree (author_id);


--
-- Name: threads_forum_id_created_index; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE INDEX threads_forum_id_created_index ON threads USING btree (forum_id, created);


--
-- Name: threads_slug_uindex; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE UNIQUE INDEX threads_slug_uindex ON threads USING btree (slug);


--
-- Name: users_email_uindex; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE UNIQUE INDEX users_email_uindex ON users USING btree (email);


--
-- Name: users_nickname_uindex; Type: INDEX; Schema: public; Owner: trubnikov
--

CREATE UNIQUE INDEX users_nickname_uindex ON users USING btree (nickname);


--
-- Name: change_vote_after_insert; Type: TRIGGER; Schema: public; Owner: trubnikov
--

CREATE TRIGGER change_vote_after_insert AFTER INSERT ON votes FOR EACH ROW EXECUTE PROCEDURE trigger_votes_after_insert();


--
-- Name: change_vote_after_update; Type: TRIGGER; Schema: public; Owner: trubnikov
--

CREATE TRIGGER change_vote_after_update AFTER UPDATE ON votes FOR EACH ROW EXECUTE PROCEDURE trigger_votes_after_update();


--
-- Name: post_create_path_before_insert; Type: TRIGGER; Schema: public; Owner: trubnikov
--

CREATE TRIGGER post_create_path_before_insert BEFORE INSERT ON posts FOR EACH ROW EXECUTE PROCEDURE trigger_post_create_path();


--
-- Name: post_isedited_before_update; Type: TRIGGER; Schema: public; Owner: trubnikov
--

CREATE TRIGGER post_isedited_before_update BEFORE UPDATE ON posts FOR EACH ROW EXECUTE PROCEDURE trigger_post_isedited();


--
-- Name: thread_increment_after_insert; Type: TRIGGER; Schema: public; Owner: trubnikov
--

CREATE TRIGGER thread_increment_after_insert AFTER INSERT ON threads FOR EACH ROW EXECUTE PROCEDURE trigger_thread_increment();


--
-- Name: forum_users_forums_forum_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY forum_users
    ADD CONSTRAINT forum_users_forums_forum_id_fk FOREIGN KEY (forum_id) REFERENCES forums(forum_id);


--
-- Name: forum_users_users_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY forum_users
    ADD CONSTRAINT forum_users_users_user_id_fk FOREIGN KEY (user_id) REFERENCES users(user_id);


--
-- Name: forums_users_user_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY forums
    ADD CONSTRAINT forums_users_user_id_fk FOREIGN KEY (author_id) REFERENCES users(user_id);


--
-- Name: posts_forums_slug_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_forums_slug_fk FOREIGN KEY (forum_slug) REFERENCES forums(slug);


--
-- Name: posts_threads_thread_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_threads_thread_id_fk FOREIGN KEY (thread_id) REFERENCES threads(thread_id);


--
-- Name: posts_users_nickname_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_users_nickname_fk FOREIGN KEY (author_nickname) REFERENCES users(nickname);


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
-- Name: votes_threads_thread_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY votes
    ADD CONSTRAINT votes_threads_thread_id_fk FOREIGN KEY (thread_id) REFERENCES threads(thread_id);


--
-- Name: votes_users_nickname_fk; Type: FK CONSTRAINT; Schema: public; Owner: trubnikov
--

ALTER TABLE ONLY votes
    ADD CONSTRAINT votes_users_nickname_fk FOREIGN KEY (user_nickname) REFERENCES users(nickname);


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

