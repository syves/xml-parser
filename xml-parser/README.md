# xml-parser

## Personal notes

I attended ClojureBridge in 2014 in San Francisco. The community
is creative and supportive. I found Clojure to be fun and easy to
understand.

Last fall I got to be a coach and help first time users to understand
the concepts. Before this challenge I had never built anything
substantial with Clojure.

I had never dealt with XML data processing, managing memory, working
with readers, and very little experience with database interactions.
I'm a huge fan of functional programming and have enjoyed working on
this challenge. I have learned soo much!

That being said there are quite a few things I would do if I had more
time. I have included a list below. (I spent 50+ hours on this project.)

Now let's run the project!

1.  Initialize server:

    ```console
    $ initdb pg
    ```

1.  Start server:

    ```console
    $ postgres -D pg &
    ```

1.  Create database:

    ```console
    $ createdb testdb2
    ```

1.  Create role that appears in SQL dump file:

    ```console
    $ psql testdb2 --command='CREATE ROLE hans WITH superuser;'
    ```

1.  Dump SQL data into database:

    ```console
    $ psql testdb2 --file=person.sql
    ```

1.  Check number of inserts:

    ```sql
    SELECT COUNT(*) FROM person;
    ```

1.  Run tests:

    ```console
    $ lein test
    ```

1.  Stop postgres:

    ```console
    $ pkill postgres
    ```

## TODO

1.  Put database setup in a script or host database on Heroku.

1.  Figure out how to improve speed so that I can process the entire
    update file. I can process gzipped files on the fly, parse and
    update database records, but the process is very slow.

1.  Perhaps figure out how to use jdbc `getUpdateCount` for logging.

1.  Try benchmarks with index on person (fname, lname, dob). Creating
    an index took four minutes and is not recommended for database
    with frequent inserts and updates!

1.  Add lock on updates if not using atomic or with-transactions.

1.  Use upsert once ON CONFLICT constraint actually supports WHERE
    clauses with multiple columns (since we do not have a unique
    constraint or primary key).

1.  Replace simple string interpolation with SQL format library
    Honey SQL. I got 90% of the way but could not fix compilation
    errors.

    ```clojure
    ; This will not work because there is no support for WHERE clause
    ; on constraint.
    (jdbc/query db-spec
      (-> (insert-into :person)
          (values [{:fname "shakrah"
                    :lname "yves"
                    :phone "1234567891"}])
          (upsert (-> (on-conflict [:fname :lname :dob]
                      (do-update-set :phone))))
          (returning :*)
          sql/format))
    ```

    ```clojure
    ; I'm not sure how to do an update-and-insert with this library.
    (jdbc/query db-spec
      (-> (insert-into :person)
          (values [{:fname "shakrah"
                    :lname "yves"
                    :phone "1234567891"}])
          ; If update fails then insert.
          (helpers/update :person)
          (sset {:phone "1112225554"})
          (where [:and
                    [:= :fname "shakrah"]
                    [:= :lname "yves"]
                    [:<> :phone "1234567899"]])
          (lock :mode :update)
          sql/format))
    ```

    ```clojure
    ; Would need to cast incoming date-of-birth which is a varying
    ; character as a date to conform to db type DATE.
    (jdbc/query db-spec
      (-> (select :fname :lname :dob :phone)
          (from :person)
          (where [:and
                    [:= :fname "JIARA"]
                    [:= :lname "HERTZEL"]
                    ; This does not work because of the cast cannot be inserted here?
                    [:= :dob (sql/raw ["CAST ('str-date AS DATE)'"])]
                    [:<> :phone "5859012188"]])
          sql/format))

    ; Helper functions
    (defn format-date [varchar] (str/join (str/split ;varchar #"-")))
    (def str-date (format-date "1935-06-05"))
    (sql/raw ["CAST ('str-date AS DATE)'"])
    ```

1.  Troubleshoot transactions.

    ```clojure
    ; I don't think this will ever work because jdbc throws an exception
    ; on successful updates with error "caught exception: No results were
    ; returned by the query".
    (deftest trans-query-large
      (testing "transaction query with updates/inserts followed by select shows success."
        (is (=
               (trans-query test-list-map sql-upsert-builder sql-select-contraint-builder)
               ""))))
    ```
