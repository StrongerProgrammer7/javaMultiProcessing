<sub>** This work is being done as part of the Course Distributed tasks and algorithms (author Abduykov Z.M.) **</sub> 

# javaMultiProcessing

## Content
- Which develop instruments used
- How connect MPJ and include in project
- Briefly description tasks
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

(name: InteliJ IDEA value: path\InteliJIDEA..\bin ; name: IntelliJ IDEA (Community or PRO) .. value: path\InteliJ IDEA..\bin)

### Work in project (description for InteliJ IDEA 2023)

- Create project
- Open File -> Project Structure -> Libraries -> + -> Java -> path/MPJ/lib/mpi.jar (& mpj.jar & starter.jar)
- Create your class
- Edit configuration -> + -> Application ->
  - Set name application
  - Modify options -> Add VM options -> put -jar ${MPJ_HOME}/lib/starter.jar -np 2
  - set name main class (if not set)
  - Environment variables: MPJ_HOME=path\MPJ

## Briefly description tasks
