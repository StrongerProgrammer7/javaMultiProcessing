<sub>** This work is being done as part of the Course Distributed tasks and algorithms (author Abduykov Z.M.) **</sub> 

# javaMultiProcessing

## Content
- <a href="https://github.com/StrongerProgrammer7/javaMultiProcessing#which-develop-instruments-used">Which develop instruments used </a>
-  <a href="https://github.com/StrongerProgrammer7/javaMultiProcessing#how-connect-mpj-and-include-in-project">How connect MPJ and include in project</a>
- <a href="https://github.com/StrongerProgrammer7/javaMultiProcessing#work-in-project-description-for-intelij-idea-2023">Briefly description tasks</a>
  - Introduction with MPJ
  - Using Async
  - Using Probe
  - Sort Array
  - Mult Vector x Matrix
  - Graph - Floyd

## Which develop instruments used

- InteliJ IDEA Community 2023
- JDK - 16 version
- MPJ Express Version 0.44for win10 (I installed MPJ in the MPJ folder) 

## How connect MPJ and include in project

- Download and setup JDK
- Download and setup MPJ Express and remember where you put
- Download and install any InteliJ IDEA (recommended) or other IDEA
- Added system variable
  - User variable -> create variables
    - name: JAVA_HOME | value: path\Java\jdk-yourversion
    - name: MPJ_HOME | value: path\MPJ
  - Change variable path:
    - Added: 1) path\Java\jdk-yourversion 
  - System variable -> create -> name: CLASSPATH | value: %MPJ_HOME%\lib\mpj.jar
  - Change system variable path:
    - Added: 1) %MPJ_HOME%\bin | 2) %JAVA_HOME%\bin

If you have problem: maybe you don't have user varibale for InteliJ IDEA 

(name: InteliJ IDEA value: path\InteliJIDEA..\bin ; 

name: IntelliJ IDEA (Community or PRO) .. value: path\InteliJ IDEA..\bin)

### Work in project (description for InteliJ IDEA 2023)

- Create project
- Open File -> Project Structure -> Libraries -> + -> Java -> path/MPJ/lib/mpi.jar (& mpj.jar & starter.jar)
- Create your class
- Edit configuration -> + -> Application ->
  - Set name application
  - Modify options -> Add VM options -> put -jar ${MPJ_HOME}/lib/starter.jar -np (set count rank)
  - set name main class (if not set)
  - Environment variables: MPJ_HOME=path\MPJ

## Briefly description tasks

### Introduction with MPJ

Simple introduction with MPJ ( if you run and get succes then you've installed it correctly. )

### Using Async

Simple introduction with MPJ using Async (Isend, Irecv)
For taks using block and non block
The overall objective, is as follows:
1) Each processor puts its rank into the integer variable buf.
2) Each processor forwards the buf variable to its neighbor on the right.
3) Each processor sums the received value into a variable s, and then passes the calculated value to its neighbor on the right. 
4) The ring transfers stop when the zero processor sums up the ranks of all processors.

### Using Probe

A simple example of using Probe in asynchronous operation

### Sort Array

Simple array sorting
1) 9 processes are used ( I have 2 cores, so 7 virtual threads)
2) 1-6 processes generate random numbers in the array (i.e. each process has 1 number)
3) 1-3 send 8 processes with tag 0
4) the rest of you send 7 processes with tag 1.
5) 7 and 8 sort and send 0 to the process with tags 0 and 1
6) 0 sorts the rest

### Mult Vector x Matrix

Algorithm for calculating the product of a matrix over a vector. 
Consider the presence of a tail.
1) Execute in blocking variant
2) Execute in a non-blocking variant
3) Execute in a collective versio

### Graph - Floyd

Algorithm for computing the diameter of an undirected graph.
- Graph - for matrix with any values
- GraphWithSetMatrix - using ready matrix for check work algorithm 
