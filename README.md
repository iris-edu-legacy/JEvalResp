# JEvalResp
JEvalResp is a Java utility for evaluating and processing instrument response descriptions. It can make use of instrument responses available as RESP text files and is also capable of reading StationXML files (as of v1.80)

This is a publicly available repository of the JEvalResp utility initiated as of v1.80 and is intended for community use and improvement.  If you have questions about this utility or would like to become a contributing maintainer, please contact the staff at EarthScope Consortium:   https://www.earthscope.org/contact-us/

## TO BUILD
Clone a copy of this repo onto your local workstation.  Make sure you are running Java 8 and have installed the 'ant' makefile utility.  Building JEvalResp to run it is very simple.

```
$ cd JEvalResp
$ ant clean
$ ant
```

You may find some warnings appear relating to the JavaDoc generator.  This does not prevent the build from completing.

## DOCUMENTATION
JEvalResp builds its own HTML docs that can be referenced and explored by accessing the index.html file in your JEvalResp folder in your browser.

```
file:///my_directory_path/JEvalResp/index.html
```

## TO RUN JEVALRESP
Use your Java installation to run JEvalResp from the command line.  JEvalResp runs as a self-contained jar file.  This means that you can copy the JEvalResp.jar anywhere that you want to use it.  The JEvalResp jar file can be found under the jars directory after you complete the build.  An example is shown below:

```
$ cd JEvalResp
$ cp jars/JEvalResp.jar /home/my_user/.
$ cd /home/my_user
$ java -jar JEvalResp.jar
```

JEvalResp is a command line tool only.  In the above example, since no arguments are provided, you will be presented with a usage message.  Since JEvalResp is meant to read response files, an example run of JEvalResp is shown here for illustration:

```
$ java -jar JEvalResp.jar YY ZZZ 2000 1 0.001 20 1000 -f CMG-6TD_TL100_TP800-200-100-25-5_FR800.RESP -v
```

If you are wanting a GUI tool with plotting capabilties, please search in https://github.com/iris-edu-legacy for the JPlotResp utility.

## LICENSE
This source code is being released with approval by Instrumental Software Technologies, Inc.  (http://www.isti.com) using a standard Apache 2 License.  The LICENSE file can be found in this repository for more details on terms of use.

## CREDITS
JEvalResp was originally written and maintained by Instrumental Software Technologies, Inc.  (http://www.isti.com).  Many thanks to Sid Hellman, Paul Friberg, Eric Thomas, and Kevin Frechette for all of their hard work on this utility.  It is an implementation based on the original C program called 'evalresp' (https://github.com/EarthScope/evalresp/releases).
