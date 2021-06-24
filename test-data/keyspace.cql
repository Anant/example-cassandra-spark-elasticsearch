DROP KEYSPACE IF EXISTS test;
CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} AND durable_writes = true;

CREATE TABLE test.users
(
    id         text,
    username   text,
    PRIMARY KEY (id, username)
);
