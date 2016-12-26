# betleopard

A simple distributed sportsbook example, to demonstrate Hazlecast IMDG and Apache
Spark, by showing how an example based on Java 8 collections can be generalized
and scaled up to multiple JVMs.

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
