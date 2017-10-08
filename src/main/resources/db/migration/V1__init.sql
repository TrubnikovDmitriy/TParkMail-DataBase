CREATE TABLE users (
    user_id serial primary key NOT NULL,
    nickname text unique NOT NULL
);


CREATE TABLE users_extra (
    user_id integer primary key NOT NULL references users(user_id),
    fullname text NOT NULL,
    email text NOT NULL,
    about text
);


CREATE TABLE forums (
    forum_id serial primary key,
    admin_id integer NOT NULL references users(user_id),
    title text NOT NULL,
    slug text unique NOT NULL
);

CREATE TABLE posts (
    post_id serial primary key,
    thread_id integer NOT NULL,
    author_id integer NOT NULL,
    parent_id integer
);


CREATE TABLE posts_extra (
    post_id integer primary key NOT NULL references posts(post_id),
    created timestamp with time zone DEFAULT now() NOT NULL,
    message text NOT NULL,
    isedited boolean DEFAULT false NOT NULL
);

CREATE TABLE threads (
    thread_id serial primary key NOT NULL,
    forum_id integer NOT NULL,
    author_id integer NOT NULL
);


CREATE TABLE threads_extra (
    thread_id integer primary key NOT NULL references threads(thread_id),
    created timestamp with time zone DEFAULT now() NOT NULL,
    message text NOT NULL,
    slug text,
    title text NOT NULL
);

CREATE TABLE votes (
    user_id integer NOT NULL,
    thread_id integer NOT NULL,
    voice smallint NOT NULL,
    primary key(user_id, thread_id)
);


ALTER TABLE ONLY posts_extra
    ADD CONSTRAINT posts_extra_posts_post_id_fk FOREIGN KEY (post_id) REFERENCES posts(post_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_posts_post_id_fk FOREIGN KEY (parent_id) REFERENCES posts(post_id);

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_threads_thread_id_fk FOREIGN KEY (thread_id) REFERENCES threads(thread_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY posts
    ADD CONSTRAINT posts_users_user_id_fk FOREIGN KEY (author_id) REFERENCES users(user_id);


ALTER TABLE ONLY threads_extra
    ADD CONSTRAINT threads_extra_threads_thread_id_fk FOREIGN KEY (thread_id) REFERENCES threads(thread_id) ON UPDATE CASCADE ON DELETE CASCADE;


ALTER TABLE ONLY threads
    ADD CONSTRAINT threads_forums_forum_id_fk FOREIGN KEY (forum_id) REFERENCES forums(forum_id);

ALTER TABLE ONLY threads
    ADD CONSTRAINT threads_users_user_id_fk FOREIGN KEY (author_id) REFERENCES users(user_id);


ALTER TABLE ONLY users_extra
    ADD CONSTRAINT users_extra_users_user_id_fk FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY votes
    ADD CONSTRAINT votes_threads_thread_id_fk FOREIGN KEY (thread_id) REFERENCES threads(thread_id);

ALTER TABLE ONLY votes
    ADD CONSTRAINT votes_users_user_id_fk FOREIGN KEY (user_id) REFERENCES users(user_id);

