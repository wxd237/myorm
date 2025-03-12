@echo off

REM 创建目标目录
mkdir target\classes

REM 设置类路径
set CLASSPATH=.;target\classes;%USERPROFILE%\.m2\repository\javax\persistence\javax.persistence-api\2.2\javax.persistence-api-2.2.jar;%USERPROFILE%\.m2\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;%USERPROFILE%\.m2\repository\ch\qos\logback\logback-classic\1.2.11\logback-classic-1.2.11.jar;%USERPROFILE%\.m2\repository\ch\qos\logback\logback-core\1.2.11\logback-core-1.2.11.jar;%USERPROFILE%\.m2\repository\com\h2database\h2\2.1.214\h2-2.1.214.jar

REM 编译源代码
javac -d target\classes -cp %CLASSPATH% src\main\java\com\myorm\annotation\*.java src\main\java\com\myorm\exception\*.java src\main\java\com\myorm\util\*.java src\main\java\com\myorm\core\*.java src\main\java\com\myorm\Main.java

REM 复制资源文件
xcopy /Y src\main\resources\* target\classes\

REM 运行Main类
java -cp %CLASSPATH% com.myorm.Main
