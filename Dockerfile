FROM ubuntu:16.04

MAINTAINER Trubnikov Dmitriy

RUN apt-get -y update

ENV PGVER 9.5

RUN apt-get install -y postgresql-$PGVER
RUN apt-get install -y openjdk-8-jdk-headless
RUN apt-get install -y maven

ENV WORK /opt/DataBase
ADD src/ $WORK/src/
ADD / $WORK/
WORKDIR $WORK/

USER postgres

RUN /etc/init.d/postgresql start &&\
    psql --command "CREATE USER trubnikov WITH SUPERUSER PASSWORD 'pass';" &&\
    psql --command "CREATE DATABASE db_tpark OWNER trubnikov ENCODING 'UTF-8' LC_COLLATE 'C.UTF-8' LC_CTYPE 'C.UTF-8' TEMPLATE template0;" &&\
    psql db_tpark < /opt/DataBase/src/main/resources/db/migration/V4__init.sql &&\
    /etc/init.d/postgresql stop


RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/$PGVER/main/pg_hba.conf

RUN echo "listen_addresses='*'" >> /etc/postgresql/$PGVER/main/postgresql.conf

RUN echo "synchronous_commit = off" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "fsync = off" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "full_page_writes = off" >> /etc/postgresql/$PGVER/main/postgresql.conf
RUN echo "wal_buffers = 3MB" >> /etc/postgresql/$PGVER/main/postgresql.conf

EXPOSE 5432

VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

USER root

EXPOSE 5000

RUN mvn install

CMD service postgresql start && java -Xms300M -Xmx600M -jar $WORK/target/tech-db-1.0-SNAPSHOT.jar
