# xml-parser



##notes
upsert with a where clause is not yet supported. Pr is still open.
update or insert may have a race condition. Perhaps a table lock could be used.

## Set up a POSTGRES database at the top level

#intialize server
$ initdb pg

#start server
$ postgres -D pg &

$ createdb testdb2

#start repl and seed database
$psql testedb2
testdb=# create role hans with superuser;

#exit repl
$ \q

#dump sql data into db
psql testdb2 --file="/Users/syves/github.com/syves/largeFiles/person.sql"

#check number of inserts
testdb=# select count(`*`) from person;

TODO:
1. Put db setup in a script or host.

2. Perhaps figure out how to use jdbc getUpdateCount for logging.

1. Try benchmarks with index on person (fname, lname, dob);

* creating an index took 4 mins.
* and is not reccomended for db with frequent inserts and updates!
testdb=# create index person_idx on person (fname, lname, dob);

2. Use upsert once ON CONFLICT constraint actually supports where clauses with multiple collumns(since we have not unique constraint, or primary key).

3. Replace simple string interpolation with sql format library honeySql. I got 90% of the way but could not fix compilation errors.

## this will not work because there is no support for where clause on constraint.
;(jdbc/query db-spec
;  (-> (insert-into :person)
;      (values [{:fname "shakrah"
;                :lname "yves"
;                :phone "1234567891"}])
;      (upsert (-> (on-conflict [:fname :lname :dob]
;                  (do-update-set :phone))))
;      (returning :`*`)
;      sql/format))

## I'm not sure how to do an udate-Or-insert with this library
;(jdbc/query db-spec
  ;these operations do npt happen in order?
  ;(-> (insert-into :person)
  ;    (values [{:fname "shakrah"
  ;              :lname "yves"
  ;              :phone "1234567891"}])
  ;    ;if update fails then insert
  ;    (helpers/update :person)
  ;    (sset {:phone "1112225554"})
  ;    (where [:and
  ;              [:= :fname "shakrah"]
  ;              [:= :lname "yves"]
  ;              [:<> :phone "1234567899"]])
      ;(lock :mode :update)
  ;    sql/format))

## this does not work because of the cast cannot be inserted here?
* would need to cast incoming date-of-birth which is a varchar as a date to conform to db type DATE.

;(jdbc/query db-spec
;  (-> (select :fname :lname :dob :phone)
;      (from :person)
;      (where [:and
;                [:= :fname "JIARA"]
;                [:= :lname "HERTZEL"]
;                ;syntax error at or near ")"
;                [:= :dob (sql/raw ["CAST ('str-date AS DATE)'"])]
;                [:<> :phone "5859012188"]])
;      sql/format))
;'5859012134'
;
;##helper functions
;(defn format-date [varrchar] (str/join (str/split ;varrchar #"-")))
;(def str-date (format-date "1935-06-05"))
;(sql/raw ["CAST ('str-date AS DATE)'"])

## License
Copyright © 2018 FIXME
Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
