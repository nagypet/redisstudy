/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE SCHEMA IF NOT EXISTS bookstore AUTHORIZATION postgres;

SET default_tablespace = 'pg_default';


CREATE SEQUENCE bookstore.author_author_id_seq;

ALTER SEQUENCE bookstore.author_author_id_seq
    OWNER TO postgres;


CREATE SEQUENCE bookstore.book_book_id_seq;

ALTER SEQUENCE bookstore.book_book_id_seq
    OWNER TO postgres;


CREATE SEQUENCE bookstore.bookxauthor_author_id_seq;

ALTER SEQUENCE bookstore.bookxauthor_author_id_seq
    OWNER TO postgres;


CREATE SEQUENCE bookstore.bookxauthor_book_id_seq;

ALTER SEQUENCE bookstore.bookxauthor_book_id_seq
    OWNER TO postgres;



CREATE TABLE IF NOT EXISTS bookstore.book
(
    book_id bigint NOT NULL DEFAULT nextval('bookstore.book_book_id_seq'::regclass),
    date_issued date,
    pages integer,
    title character varying(255) COLLATE pg_catalog."default",
	createdby character varying(255) COLLATE pg_catalog."default",
	createdat timestamp,
	updatedby character varying(255) COLLATE pg_catalog."default",
	updatedat timestamp,
	rec_version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT book_pkey PRIMARY KEY (book_id)
);


CREATE TABLE IF NOT EXISTS bookstore.author
(
    author_id bigint NOT NULL DEFAULT nextval('bookstore.author_author_id_seq'::regclass),
    name character varying(255) COLLATE pg_catalog."default",
	createdby character varying(255) COLLATE pg_catalog."default",
	createdat timestamp,
	updatedby character varying(255) COLLATE pg_catalog."default",
	updatedat timestamp,
    rec_version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT author_pkey PRIMARY KEY (author_id),
    CONSTRAINT author_ix_name UNIQUE (name)
);


CREATE TABLE IF NOT EXISTS bookstore.bookxauthor
(
    book_id bigint NOT NULL,
    author_id bigint NOT NULL,
    CONSTRAINT bookxauthor_pkey PRIMARY KEY (book_id, author_id),
    CONSTRAINT bookxauthor_fk1 FOREIGN KEY (book_id)
        REFERENCES bookstore.book (book_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT bookxauthor_fk2 FOREIGN KEY (author_id)
        REFERENCES bookstore.author (author_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
