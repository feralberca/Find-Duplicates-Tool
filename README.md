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

