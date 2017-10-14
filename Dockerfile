FROM ubuntu:16.04

MAINTAINER Trubnikov Dmitriy

RUN apt-get -y update

ENV PGVER 9.5

RUN apt-get install -y postgresql-$PGVER

USER postgres


RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/$PGVER/main/pg_hba.conf

RUN echo "listen_addresses='*'" >> /etc/postgresql/$PGVER/main/postgresql.conf

EXPOSE 5432

VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

USER root

RUN apt-get install -y openjdk-8-jdk-headless
RUN apt-get install -y maven

ENV WORK /opt/DataBase
ADD target/ $WORK/target/
ADD src/ $WORK/src/
ADD / $WORK/

WORKDIR $WORK/

EXPOSE 5000

USER postgres
RUN /etc/init.d/postgresql start &&\
    psql --command "CREATE USER trubnikov WITH SUPERUSER PASSWORD 'pass';" &&\
    psql --command "CREATE DATABASE tech_park OWNER trubnikov;" &&\
    psql --command "\i /opt/DataBase/src/main/resources/db/migration/V4__init.sql"  &&\
    psql --command "\d"  &&\
    psql --command "\l"  &&\
    psql --command "\du"  &&\
    /etc/init.d/postgresql stop
USER root

CMD service postgresql start && java -Xms300M -Xmx300M -jar $WORK/target/tech-db-1.0-SNAPSHOT.jar
