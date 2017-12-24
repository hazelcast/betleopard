# betleopard

A simple distributed sportsbook example, to demonstrate Hazlecast IMDG and Apache
Spark, by showing how an example based on Java 8 collections can be generalized
and scaled up to multiple JVMs.

## The White Paper

Included in this distribution is a white paper deascribing the Betleopard application in
detail and how it should be understood.

Broadly, Betleopard tries to provide a simple "on-ramp" for using the Spark connector from
within Hazelcast. It achieves this by starting with some analysis of historical races, first
using Java 8 streams (in the class AnalysisSimple) and then with Spark.

After the basics of the Hazelcast / Spark API has been introduced and contrasted to the simple
Java 8 version, the reader should start to look at the class that provides a "live" example
of recalculating and working with a dataset that is changing in real time.

This is the LiveBetMain class and shows how to use a Hazelcast API to query a Spark dataset
to provide a real-time risk reclaculation - essentially the answer to the question: 

"In the worst case, how much potential loss is the sportsbook exposed to, and what combination
of race results would cause that worst case to occur?"

Full details are in the whitepaper, which is in the Asciidoc file betleopard-wp.adoc 

## Code layout

The package structure is very straightforward:

* com.betleopard - general factory and serialization classes.

* com.betleopard.domain - domain types for describing the betting functionality

* com.betleopard.hazelcast - the main driver classes for the Hazelcast and Spark examples

* com.betleopard.simple - a good starting point for the project. Contains implementations based only on Java 8


## To generate the docs

Documentation is included in the distribution, but to regenerate the docs, do 
the following. From the src/main/java directory, run this:

----
javadoc -D ../../../doc -subpackages com.betleopard com.betleopard
----
