# xml-parser

A Clojure library designed to ... well, that part is up to you.

##notes
upsert with a where clause is not yet supported. Pr is still open.
update or insert may have a race condition. Perhaps a table lock could be used.

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

#create index took 4 mins and is not reccomended for db with frequent inserts and updates!
testdb=# create index person_idx on person (fname, lname, dob);

## License
Copyright © 2018 FIXME
Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
