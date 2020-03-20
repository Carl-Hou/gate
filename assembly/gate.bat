@echo off

rem   Licensed to the Apache Software Foundation (ASF) under one or more
rem   contributor license agreements.  See the NOTICE file distributed with
rem   this work for additional information regarding copyright ownership.
rem   The ASF licenses this file to You under the Apache License, Version 2.0
rem   (the "License"); you may not use this file except in compliance with
rem   the License.  You may obtain a copy of the License at
rem 
rem       http://www.apache.org/licenses/LICENSE-2.0
rem 
rem   Unless required by applicable law or agreed to in writing, software
rem   distributed under the License is distributed on an "AS IS" BASIS,
rem   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem   See the License for the specific language governing permissions and
rem   limitations under the License.

rem   =====================================================

rem Minimal version to run Gate
set MINIMAL_VERSION=1.8.0


rem --add-opens if JAVA 9
set JAVA9_OPTS=


for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    rem @echo Debug Output: %%g
    set JAVAVER=%%g
)
if not defined JAVAVER (
    @echo Not able to find Java executable or version. Please check your Java installation.
    set ERRORLEVEL=2
    goto pause
)



rem Check if version is from OpenJDK or Oracle Hotspot JVM prior to 9 containing 1.${version}.x
rem JAVAVER will be equal to "9.0.4" (quotes are part of the value) for Oracle Java 9
rem JAVAVER will be equal to "1.8.0_161" (quotes are part of the value) for Oracle Java 8
rem so we extract 2 chars starting from index 1
IF "%JAVAVER:~1,2%"=="1." (
    set JAVAVER=%JAVAVER:"=%
    for /f "delims=. tokens=1-3" %%v in ("%JAVAVER%") do (
        set current_minor=%%w
)
) else (
    rem Java 9 at least
    set current_minor=9
    set JAVA9_OPTS=--add-opens java.desktop/sun.awt=ALL-UNNAMED --add-opens java.desktop/sun.swing=ALL-UNNAMED --add-opens java.desktop/javax.swing.text.html=ALL-UNNAMED --add-opens java.desktop/java.awt=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED
)


for /f "delims=. tokens=1-3" %%v in ("%MINIMAL_VERSION%") do (
    set minimal_minor=%%w
)

if not defined current_minor (
    @echo Not able to find Java executable or version. Please check your Java installation.
    set ERRORLEVEL=2
    goto pause
)
rem @echo Debug: CURRENT=%current_minor% - MINIMAL=%minimal_minor%
if %current_minor% LSS %minimal_minor% (
    @echo Error: Java version -- %JAVAVER% -- is too low to run Gate. Needs a Java version greater than or equal to %MINIMAL_VERSION%
    set ERRORLEVEL=3
    goto pause
)


:winNT1
rem On NT/2K grab all arguments at once
set GATE_CMD_LINE_ARGS=%*

rem The following link describes the -XX options:
rem http://docs.oracle.com/javase/8/docs/technotes/tools/unix/java.html

if not defined HEAP (
    rem See the unix startup file for the rationale of the following parameters,
    rem including some tuning recommendations
    set HEAP=-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m
)

rem Uncomment this to generate GC verbose file with Java prior to 9 
rem set VERBOSE_GC=-verbose:gc -Xloggc:gc_gate_%%p.log -XX:+PrintGCDetails -XX:+PrintGCCause -XX:+PrintTenuringDistribution -XX:+PrintHeapAtGC -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDateStamps -XX:+PrintAdaptiveSizePolicy

rem Uncomment this to generate GC verbose file with Java 9 and above
rem set VERBOSE_GC=-Xlog:gc*,gc+age=trace,gc+heap=debug:file=gc_gate_%%p.log
rem You may want to add those settings
rem -XX:+ParallelRefProcEnabled -XX:+PerfDisableSharedMem
if not defined GC_ALGO (
    set GC_ALGO=-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:G1ReservePercent=20
)

set SYSTEM_PROPS=-Djava.security.egd=file:/dev/urandom

rem Always dump on OOM (does not cost anything unless triggered)
set DUMP=-XX:+HeapDumpOnOutOfMemoryError

rem Uncomment this if you run Gate in DOCKER (need Java SE 8u131 or JDK 9)
rem see https://blogs.oracle.com/java-platform-group/java-se-support-for-docker-cpu-and-memory-limits
rem set RUN_IN_DOCKER=-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap

rem Additional settings that might help improve GUI performance on some platforms
rem See: http://www.oracle.com/technetwork/java/perf-graphics-135933.html

if not defined DDRAW (
    set DDRAW=
    rem  Setting this flag to true turns off DirectDraw usage, which sometimes helps to get rid of a lot of rendering problems on Win32.
    rem set DDRAW=%DDRAW% -Dsun.java2d.noddraw=true

    rem  Setting this flag to false turns off DirectDraw offscreen surfaces acceleration by forcing all createVolatileImage calls to become createImage calls, and disables hidden acceleration performed on surfaces created with createImage .
    rem set DDRAW=%DDRAW% -Dsun.java2d.ddoffscreen=false

    rem Setting this flag to true enables hardware-accelerated scaling.
    rem set DDRAW=%DDRAW% -Dsun.java2d.ddscale=true
)



rem Collect the settings defined above
set ARGS=%JAVA9_OPTS% %DUMP% %HEAP% %VERBOSE_GC% %GC_ALGO% %DDRAW% %SYSTEM_PROPS% %RUN_IN_DOCKER%


java %ARGS% -Duser.language=en -Duser.region=EN -Dfile.encoding=UTF-8 -cp .;%CLASSPATH%;%~dp0/../lib/* org.gate.GateMain %GATE_CMD_LINE_ARGS%