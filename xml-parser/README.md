# xml-parser

A Clojure library designed to ... well, that part is up to you.

## Set up a POSTGRES database at the top level

#intialize and start the server
$ initdb pg
$ postgres -D pg &

$ createdb testdb

$psql testedb
testdb=# create role hans with superuser;

#dump sql data into db
psql testdb --file="/Users/syves/github.com/syves/largeFiles/person.sql"

#check number of inserts
testdb=# select count(`*`) from person;

## License
Copyright © 2018 FIXME
Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
