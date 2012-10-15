@echo off
set CLASSPATH=!CLASSPATH!;./bin
rem clean
del "./bin/*"
del MyBot.jar
rem compile
javac ./src/ant/*.java
rem package
jar cvfm MyBot.jar Manifest.txt "./bin/*" 
rem clean
del "./bin/*"
