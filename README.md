# Duplicate Finder Tool

Java program tool for traversing directories searching for duplicates files.

### Import project in eclipse
* Can be imported using ./gradlew eclipse command and then importing the project in eclipse or directly importing the project as a gradle project

### Fat jar generation
 * ./gradlew jar
 
### Usage
```
java -jar duplicate-finder-tool-{version}.jar -rootDir [root directory for searching duplicates] -parallel [number of threads] [-skipLinks] [-skipEmpty]
```
Results are written to the console and a log file in the current folder.

# Duplication detection rules
For detecting duplicate files the following rules are applied:
 * Empty files are processed by name, meaning that if the file is empty but has the same name that other empty file it will treated as duplicate
 * Files are compared by content regardless of the file name, unless the file is empty
 * Hardlinks pointing to the same location will treated as duplicates
 * Invalid symlinks are skipped
 * The first file occurrence will be logged not all of them. Meaning that if the file is duplicated N times just the first duplication detected will be logged.

