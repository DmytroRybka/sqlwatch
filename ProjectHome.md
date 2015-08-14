# SQL Watch 4.0 Preview: Speaking Truth #
I'm glad to announce preview version of sqlwatch jdbc monitoring tool.

Be aware, project is not yet completed and some panels is not implemented.

BUT! It is possible to use it alredy.

Use featured link or right panel or direct link to download preview distribution:
http://code.google.com/p/sqlwatch/downloads/detail?name=sqlwatch-4.0-preview-v1.zip

Usage hints:
  1. Configuration left same as in v3.0
  1. Just copy lib/sqlwatch-drivier-4.0.jar into your application classpath and change your JDBC url.
  1. Change JDBC driver to **`net.sf.log4jdbc.DriverSpy`** and url form something like this<br /> jdbc:mysql://localhost:3306/sakila<br>to<br>jdbc:<b>log4jdbc</b>:mysql://localhost:3306/sakila<br>
<ol><li>Note, local port 8666 is used to communicate between driver and client tool. Be shure that 8666 port is not used by another app or nothing will work.</li></ol>

Now demo application is shipped with sqlwatch tool.<br>
<ol><li>Upnack application distribution: <a href='http://code.google.com/p/sqlwatch/downloads/detail?name=sqlwatch-4.0-preview-v1.zip'>http://code.google.com/p/sqlwatch/downloads/detail?name=sqlwatch-4.0-preview-v1.zip</a>
</li><li>Execute start-sqlwatch-demo.cmd<br>
</li><li>Execute start-sqlwatch-ui.cmd<br>
</li><li>Watchout!<br>
</li><li>Note: do not foget to stop demo before starting to use sqlwatch4 in your app.</li></ol>

<h1>SQL Watch 3.0: Stealing from the poor giving to the rich</h1>
<h2>Maintenance note</h2>
Due to develpment of version 4 I switched project to<br>
mercurial vcs. Now only new version sources will remain versioned.<br>
<br>
All legacy source code kept public and still avalable here:<br>
<a href='http://code.google.com/p/sqlwatch/downloads/detail?name=svn_backup_v1_v2_v3.zip&can=2&q='>http://code.google.com/p/sqlwatch/downloads/detail?name=svn_backup_v1_v2_v3.zip&amp;can=2&amp;q=</a>

<h2>How to use it</h2>
<ol><li>Download <a href='http://code.google.com/p/sqlwatch/downloads/detail?name=sqlwatch3.0.0.jar&can=2&q='>tool jar</a> and put it into your application classpath.<br>
</li><li>Change JDBC driver to <b><code>net.sf.log4jdbc.DriverSpy</code></b> and url form something like this<br /> jdbc:mysql://localhost:3306/sakila<br>to<br>jdbc:<b>log4jdbc</b>:mysql://localhost:3306/sakila<br>
</li><li>That's it!</li></ol>

<h2>What you get as result</h2>
<pre><code>----------- &gt;&gt; begin &gt;&gt; queries profiling report &gt;&gt; -----------<br>
Table Name         Requests (of total %) /Unique Requests | Useless % |  Time  (of total %)|<br>
staff            =       88 (        9%) /             10 |       89% |         32ms ( 16%)|<br>
film             =       87 (        9%) /             10 |       89% |          0ms (  0%)|<br>
address          =       87 (        9%) /             10 |       89% |         31ms ( 15%)|<br>
rental           =       86 (        9%) /             10 |       88% |          0ms (  0%)|<br>
payment          =       85 (        9%) /             10 |       88% |         31ms ( 15%)|<br>
actor            =       76 (        8%) /             10 |       87% |         16ms (  8%)|<br>
language         =       73 (        7%) /             10 |       86% |         62ms ( 31%)|<br>
category         =       72 (        7%) /             10 |       86% |         15ms (  7%)|<br>
store            =       72 (        7%) /             10 |       86% |          0ms (  0%)|<br>
customer         =       72 (        7%) /             10 |       86% |         16ms (  8%)|<br>
inventory        =       72 (        7%) /             10 |       86% |          0ms (  0%)|<br>
country          =       69 (        7%) /             10 |       86% |          0ms (  0%)|<br>
city             =       61 (        6%) /             10 |       84% |          0ms (  0%)|<br>
TOTAL 468 ms &gt;&gt;&gt; =     1000 (      100%) /            130 |       87% |        203ms (100%)|<br>
----------- &gt;&gt; end &gt;&gt; [Memory    3M Free/    4M Total] &gt;&gt; -----------<br>
<br>
Legend:<br>
1. Table Name      - name of a table in FROM section of SQL query<br>
2. Requests        - count of querties with this table name in FROM section<br>
3. (of total %)    - percentage for such queries over total count of queries in reprot<br>
4. Unique Requests - count of unique request (requests with different arguments like `WHERE X=?` counted as different)<br>
5. Useless %       - percentage of queries which requests data which was already requested in this report<br>
6. Time            - total time spend to perform this queries<br>
7. (of total %)    - percentage of total time<br>
TOTAL XXXX ms -  while time elapsed from first to last query in report,<br>
                 this time includes not inly SQL execution time, but <br>
                 whole application logic either.<br>
</code></pre>

All this statistics will be printed automatically to <code>stdout</code> stream.<br>
<br>
<h2>From Author</h2>
Special tkanks to <a href='http://code.google.com/p/log4jdbc/'>http://code.google.com/p/log4jdbc/</a> project for it's<br>
good-enough codebase!