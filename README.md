## sbt: The interactive tutorial
*(Covers Scala 2.11.8 and sbt 0.13.11)*


**Goal:** The main goal of this tutorial is to highlight the interactive fetures of sbt.
It will focus on how to use the sbt shell to inspect and modify tasks interactively. 
This tutorial targets anyone that wants to learn new (and often hidden) features in sbt. 


**Q:** Why sbt? 

1. It is interactive and used within a shell, 
2. its build tasks are written in Scala, and
3. it supports incremental compilation.  

The first makes it easier to experiment with new settings and execute/inspect your 
tasks instantly;  the second gives you the power of a programming language to define 
and customize all your build, compile, test and deploy tasks; finally, the third 
significantly improve the compile performance of large Scala projects. 

### A simple sbt project
*Note: If you are already familiar with .sbt files and task/settings you can skip this section.*

Let's start with an example project to explain the basics (assuming you have sbt installed). 
First create an empty directory, and then create the following files/directories: 

1) A build file
 
**build.sbt**

```sbt
name := "sbt-turorial"
organization := "org.sbt-tutorial"
version := "1.0"
scalaVersion := "2.11.8"
```

2) A very basic Scala class with a runnable `main()`

**src/main/scala/org/sbttutorial/main/Main.scala**

```Scala
package org.sbttutorial.main

object Main extends App {
  println("This is main()")
}
```

3) Optionally if you want to specify the exact version of sbt

**project/build.properties**

```sbt
sbt.version = 0.13.11
```


This is it. Just these 2-3 simple files make for a fully functional sbt project with a lot of
features to learn and experiment. 

First, type `sbt` from the root of your project (the folder containing `build.sbt`). 
This will get you to the interactive shell of sbt. 

Compile the project

```
> compile
```

Run the project 

```
> run
```

Clean the compiled artifacts (e.g., .class) files generated by the compile task

```
> clean
```

You now have a way to compile your project, execute any main() class you have, and clean your project. 


#### Tasks and Settings 

These are two of the most fundamental concepts in sbt. Understanding them is very important.  
You can think of sbt as a large collection of key-value pairs. Keys can be either tasks or settings. 

**Task:** A task is similar to a function in Scala. Like functions, 
tasks have return values and  every time you call a task some piece of code 
will be executed. Their main difference with functions is that tasks don't take arguments. 
(Sbt has a different kind of task called [InputTask](http://www.scala-sbt.org/0.13/docs/Input-Tasks.html) 
that takes arguments, but we will focus on regular tasks in this tutorial.) 
Tasks can depend on other tasks or settings (so task and setting form a DAG of inter-dependencies).
From what we have used so far `compile, run, clean` are example of tasks.  


**Settings**: Settings are like tasks in that they don't take arguments and they have a return value. 
However, unlike tasks that are similar to functions, settings are similar to `lazy vals` in Scala. 
The code in a setting will be executed just once and the return value will be returned the same every 
time the setting is referenced. The `name` of a project is an example of a setting. 
**Unlike tasks that may depend on other tasks and settings, settings can only depend on other settings.**


Without getting into too many details, we are going to cover some examples of creating custom settings and tasks:

**Example of a Setting**

Add the following in you `build.sbt` file:

```scala
lazy val confFileName = settingKey[java.io.File]("Path to configuration settings")

confFileName := baseDirectory.value / "conf" / "gen_file.txt"
```

In this example, the `confFileName` setting depends on the `baseDirectory` setting. 
To execute a task or get the value of a setting we use the `.value` method. 
Every setting has 
(i) a name (so you can reference it) 
(ii) a short description, and 
(iii) a return type (in the example the type is `java.io.File`).

To get the value of the setting from the shell simply type:

```
> confFileName
```

**Example of a Task**

```scala
lazy val genConfFile = taskKey[Unit]("Creates a dummy configuration file")

genConfFile := {
  val time: String = new java.util.Date().toString
  println(time)
  IO.write(confFileName.value, s"time=$time")
}
```

This example demonstrates one of the main benefits of sbt: you can use Scala to define your settings and tasks! 
`IO.write` is a convenient function offered by sbt to wrap Java's BufferedWriter. There is nothing preventing you
from using any Java or Scala IO libraries to achieve this. 
Again, to execute a task or get the value of a setting we use the `.value` method.
Finally, note that our example task depends on the `genConfFile` setting we defined earlier. 


To execute the task from from the shell simply type:

```
> genConfFile
```

**Example of modifying an existing Setting (or Task)**

Existing tasks and/or settings can be indirectly modified by changing the values
of other tasks/settings they depend on. We demonstrate this with an example. 
Our `genConfFile` task creates a new file every time it is called. 
It would be nice if sbt deleted that configuration file 
when we call `clean`. Here is how we can enhance the `clean` task to also 
clean the custom file we created:

```scala
cleanFiles := confFileName.value +: cleanFiles.value
```

The `clean` task depends on the `cleanFiles` to get the list of files that need to be deleted. 
By changing adding one more file ine `cleanFiles` setting we idirectly alter the behavior of `clean`.
Now when we call `clean` sbt will also delete the configuration files we generated. 

sbt has a lot of default settings and tasks. It is a good investment of your time to learn as many of them possible. 
Many of these settings and tasks are defined in [sbt.Keys](https://github.com/sbt/sbt/blob/0.13/main/src/main/scala/sbt/Keys.scala). 


Here is the content of out build.sbt file after the above additions:

```scala
name := "sbt-tutorial"

version := "1.0"

scalaVersion := "2.11.8"

organization := "org.sbt-tutorial"

lazy val confFileName = settingKey[java.io.File]("Path to configuration settings")
confFileName := baseDirectory.value / "conf" / "gen_file.txt"

lazy val genConfFile = taskKey[Unit]("Creates a dummy configuration file")
genConfFile := {
  val time: String = new java.util.Date().toString
  println(time)
  IO.write(confFileName.value, s"time=$time")
}

cleanFiles := confFileName.value +: cleanFiles.value
```


### Interactive mode

We now move to the main topic of this tutorial. 
The interactive nature of sbt is one its greater strengths. We are going
to spend some time to demonstrate how to effectively utilize this feature. 

Type `sbt` to get started


**show**

The show command will execute and print the return value of a task or setting in sbt. 

```scala
> show name
[info] sbt-tutorial
> show version
[info] 1.0
> show organization
[info] org.sbt-tutorial
> show scalaVersion
[info] 2.11.8
> show baseDirectory
[info] /Users/xyz/sbt-turorial
> show sources
[info] ArrayBuffer(/Users/xyz/sbt-turorial/src/main/scala/org/sbttutorial/main/Main.scala)
[success] Total time: 0 s, completed Aug 14, 2016 2:37:05 PM
> show confFileName
[info] /Users/xyz/sbt-turorial/conf/gen_file.txt
``` 

```scala
> clean
[success] Total time: 0 s, completed Aug 14, 2016 2:22:54 PM
> show compile
[info] Updating {file:/Users/xyz/sbt-turorial/}sbt-turorial...
[info] Resolving jline#jline;2.12.1 ...
[info] Done updating.
[info] Compiling 1 Scala source to /Users/xyz/sbt-turorial/target/scala-2.11/classes...
[info] Analysis: 1 Scala source, 3 classes, 2 binary dependencies
[success] Total time: 1 s, completed Aug 14, 2016 2:22:57 PM
```

**inspect**

Gives you a detailed description of a task or setting. 
Inspect is great for looking up commands and understanding the internals of sbt.


Here we use inspect to learn about the *name* setting we set in our `build.sbt` file.
 
 ```scala
 > inspect name
 1.  [info] Setting: java.lang.String = sbt-tutorial
 2.  [info] Description:
 3.  [info] 	Project name.
 4.  [info] Provided by:
 5.  [info] 	{file:/Users/xyz/sbt-turorial/}sbt-turorial/ *:name
 6.  [info] Defined at:
 7.  [info] 	/Users/xyz/sbt-turorial/build.sbt:1
 8.  [info] Reverse dependencies:
 9.  [info] 	*:projectInfo
 10. [info] 	*:onLoadMessage
 11. [info] 	*:description
 12. [info] 	*:normalizedName
 13. [info] Delegates:
 14. [info] 	*:name
 15. [info] 	{.}/*:name
 16. [info] 	*/*:name
 ```

Line 1 says that this a **setting** with a return type being String and the return value being "sbt-tutorial".
Lines 2-3 give the description of the setting. Similarly, lines 4-5 we will talk more later. We can see in 
Line 7 that this setting is defined in the first line of out build.sbt file. 
Lines 8-12 show which other settings/tasks depend on this setting (that is, they use this setting internally). 
Lines 13-16 we will cover later in more detail. 

```scala
> inspect sources
1.  [info] Task: scala.collection.Seq[java.io.File]
2.  [info] Description:
3.  [info] 	All sources, both managed and unmanaged.
4.  [info] Provided by:
6.  [info] 	{file:/Users/xyz/sbt-turorial/}sbt-turorial/compile:sources
6.  [info] Defined at:
7.  [info] 	(sbt.Defaults) Defaults.scala:191
8.  [info] Dependencies:
9.  [info] 	compile:unmanagedSources
10. [info] 	compile:managedSources
11. [info] Delegates:
12. [info] 	compile:sources
13. [info] 	*:sources
14. [info] 	{.}/compile:sources
15. [info] 	{.}/ *:sources
16. [info] 	*/compile:sources
17. [info] 	*/ *:sources
18. [info] Related:
19. [info] 	test:sources
```
Line 1 says that this is a **task** that returns a `Seq[java.io.File]`. 
Line 3 shows the description of the task. We now see in line 8-10 that this task depends on two other tasks/settings. 
So when we execute `sources` it will first run `unmanagedSources` and  `managedSources`. 
Finally, lines 18-19 give a list of similar tasks/settings (have same name) that are defined in other scopes (namespaces). 
We will discuss more about namespaces later.

Finally, here is the inspect on the custom task (genConfFile) we created before:

```scala
> inspect genConfFile
[info] Task: Unit
[info] Description:
[info] 	Creates a dummy configuration file
[info] Provided by:
[info] 	{file:/Users/xyz/sbt-turorial/}sbt-turorial/ *:genConfFile
[info] Defined at:
[info] 	/Users/xyz/sbt-turorial/build.sbt:16
[info] Dependencies:
[info] 	*:confFileName
[info] Delegates:
[info] 	*:genConfFile
[info] 	{.}/ *:genConfFile
[info] 	*/ *:genConfFile
```

`Unit` here means that our task does not have a return value. 


**set**

This is a great tool when you experiment with different settings in a project. This command allows you to
change the value of a setting or a task without having to modify any code. The changes are ephemeral 
and remain active only during the current sbt shell session. 

```scala
> set name := "new-name"
[info] Defining *:name
[info] The new value will be used by *:description, *:normalizedName and 5 others.
[info] 	Run `last` for details.
[info] Reapplying settings...
[info] Set current project to new-name (in build file:/Users/xyz/sbt-turorial/)
> show name
[info] new-name
```

By default, sbt will run tasks in parallel (in the example below ignore the `::` notation for now). It essentially says
"give me the value of key `parallelExecution` used by task `test`."  

```scala
> show test::parallelExecution
[info] true

> set parallelExecution in test := false
[info] Defining *:test::parallelExecution
[info] The new value will be used by test:test::testExecution
[info] Reapplying settings...
[info] Set current project to new-name (in build file:/Users/xyz/sbt-turorial/)
> show test::parallelExecution
[info] false
```

This is really powerful. We can go and change the individual keys used by different tasks and then execute them
in the shell to see the effect of our changes. To better understand task and setting in sbt we are going to take 
a look on how namespaces and scoping works. 


### Namespaces

An important concept in sbt is how keys (settings, tasks, etc.) are organized into namespaces. 
sbt has three scope axis (think of it as a 3D coordinate system) for all its variables (keys): 
Project, Configuration, and Task. Project scope will make sense when we talk about multi-project builds, 
but for now, let's focus on the latter two, Configuration and Task. 

You might have already noticed that some tasks, such as `compile`, can refer to different things depending 
on their context. For example, you might want to compile your test code, or compile everything but your test code. 
How does sbt knows what to do when you just say `compile`? The answer is that sbt does that by namespacing the 
compile task (and every other key) into different configurations. 

By default there are three main configurations: Compile, Test, and Runtime. Compile groups together everything that 
has to do with managing your production code (the code located under `src/main`). For example, if you type `compile:compile` 
sbt will execute the compile task in the Compile configuration and compile just your production code (not your tests). 
Similarly, the Test configuration is responsible for all your test code (the code located under `src/test`). 
If you type `test:compile` it will run the compile task under the Test configuration. For the compile task,  
`runtime:compile` is delicated to `compile:compile`, so nothing different happens in this case. You can see this
by yourself by inspecting the `runtime:compile` task and look under *Provided by*.

```scala
> inspect runtime:compile
[info] Task: sbt.inc.Analysis
[info] Description:
[info] 	Compiles sources.
[info] Provided by:
[info] 	{file:/Users/xyz/sbt-tutorial/root-only/}root-only/compile:compile
[info] Defined at:
[info] 	(sbt.Defaults) Defaults.scala:271
...
```

Here are some examples of Coniguration/Task keys:

```scala
> show runtime:sources
[info] ArrayBuffer(/Users/xyz/sbt-tutorial/root-only/src/main/scala/org/sbttutorial/main/Main.scala)
[success] Total time: 0 s, completed Aug 16, 2016 9:36:55 PM
> show compile:sources
[info] ArrayBuffer(/Users/xyz/sbt-tutorial/root-only/src/main/scala/org/sbttutorial/main/Main.scala)
[success] Total time: 0 s, completed Aug 16, 2016 9:37:02 PM
> show test:sources
[info] ArrayBuffer()
```

Runtime and Compile have the same sources, whereas for Test we don't have any test classes, so the `sources` returns empty. 


In the example below we tell sbt to create a new JVM when we execute `run`, but
use the same JVM as sbt when we execute tests.  

```scala
> set fork in Compile := true
[info] Defining compile:fork
[info] The new value will be used by compile:run::runner
> set fork in Test := false
[info] Defining test:fork
[info] The new value will be used by test:run::runner, test:testGrouping
```

The last example shows that `test:fork` is used by `test:run::runner` 
and `test:testGrouping`. What if we wanted to fork a new JVM each time we execute `test:run` 
but not every time we run our tests? sbt allows us to focus into the Configuration 
and the Task:

```scala
 set fork in testGrouping in (Test, run) := false
 > show test:testGrouping::fork
[info] false
...
set fork in runner in (Test, run) := true
> show test:runner::fork
[info] true
...
> show test:run::fork
[info] true
> show test:fork
[info] false
```

We have exactly what we wanted. We create a new JVM when we execute `test:run` 
(this will execute a Main function defined inside our test classes) but we
don't fork a JVM when we simple run our tests `test:test`.
You can see how powerful this is. We can manipulate different tasks by changing 
the values of the tasks or settings that it depends upon. The interactive shell will
be the users best friend while trying to figure these out.  


#### Understandin the sbt scope syntax

Here, is better to think of the scoe axis as a 3-tired namespace: **1) Project, 2) Configuration, and 3) Task**

Here is how you navigate to these dimensions from sbt interactive: 

```scala
<project-id>/config:intask::key
```

This essentially says: execute from project X (first dimension) and configuration Compile (second dimension) 
the run task (third dimension). This will execute the main function found in the compile 
(under main/src folder) code of project X. 

```scala
projectX/compile:run
```

Similarly you can say execute run in the Test configuration of project X. This will now execute the main method (say you have one defined) that is under the test/src folder. 

```scala
projectX/test:run
```
 
Now, tasks also have keys (internal configuration variables). For example: 

```scala
show projectX/compile:compile::sources
```

This will show the source files used by the compile task of Project-X's Compile configuration (when you compile just your code, not the tests). 
 
```scala
show projectX/test:compile::sources
```
 
This will show the source files used by the compile task of Project-X's Test configuration (when you compile just your test files).

#### Naming in SBT DSL

sbt has different convention for naming tasks and scopes while you are writing in its DSL 
(inside .sbt files) and while executing tasks interactively via the sbt cli. 

```scala
projectX/test:compile::sources
```

translates to 

```scala
sources in (Test,compile) in projectX
```
 
sbt has a lot of default scopes. For example, if you just enter sbt and type `compile`, 
it will actually execute the compile task under the Compile configuration of the Root project.  
