@echo off
echo 运行测试...
cd %~dp0
call mvn test > test_output.txt 2>&1
echo 测试完成，结果已保存到test_output.txt
type test_output.txt
