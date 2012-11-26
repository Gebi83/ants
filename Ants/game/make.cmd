@echo off
rem clean
del *.class
del GebiBot.jar
rem compile
javac *.java
rem package
jar cvfm GebiBot.jar Manifest.txt *.class 
rem clean
del *.class
