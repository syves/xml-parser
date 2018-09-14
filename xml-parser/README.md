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
