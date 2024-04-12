# General Information
This program verifies if input in AWS IAM Role JSON format contains 
the field `Resources` with a single asterisk inside of it.

# How to run
Run using gradle build system cli and provided scripts. 
If the path parameter is given, attempts to read from the file.
Otherwise, reads from stdin ('-q' suppresses build system logs).
## Linux / macOS
`./gradlew run -q [--args=<path>]`  
Please note that Java Standard Library (and hence this program) does not support '~' 
as an alias 
for the user home directory. Use `$HOME` instead.
## Windows
`.\gradlew.bat run -q [--args=<path>]`

## Run only unit tests.
### Linux/macOS
`./gradlew test`
### Windows
`.\gradlew.bat test`

# Project Structure
Code is located in `src/main/kotlin/Main.kt`  
Unit test are in `src/test/kotlin/MainKtTest.kt`