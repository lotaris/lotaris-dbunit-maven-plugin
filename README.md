# lotaris-dbunit-maven-plugin

> A plugin to create/drop domains, deploy application and manage resources.

## Usage

1. Clone the repository.

2. Run the following command

```bash
cd <projectFolder>
mvn clean install
```

3. Put the following dependency in your pom.xml

```xml
<plugin>
	<groupId>com.lotaris.maven.plugins</groupId>
	<artifactId>lotaris-dbunit-maven-plugin</artifactId>
	<configuration>
		<url>jdbc:mysql://localhost:3306/oneDb/?sessionVariables=FOREIGN_KEY_CHECKS=0&amp;useUnicode=true&amp;characterEncoding=utf-8</url>
		<username>oneDbUser</username>
		<password>oneDbPassword</password>
		<src>src/main/resources/sample-data.xml</src>
		<clearAllTables>true</clearAllTables>
		<driver>com.mysql.jdbc.Driver</driver>
		<type>CLEAN_INSERT</type>
		<dataTypeFactoryName>org.dbunit.ext.mysql.MySqlDataTypeFactory</dataTypeFactoryName>
		<excludeEmptyTables>true</excludeEmptyTables>
	</configuration>
	
	<executions>
		<execution>
			<id>dbunit</id>
			<phase>integration-test</phase>
			<goals>
				<goal>operation</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

### Requirements

* Java 6+

## Contributing

* [Fork](https://help.github.com/articles/fork-a-repo)
* Create a topic branch - `git checkout -b feature`
* Push to your branch - `git push origin feature`
* Create a [pull request](http://help.github.com/pull-requests/) from your branch

Please add a changelog entry with your name for new features and bug fixes.

## License

**lotaris-dbunit-maven-plugin** is licensed under the [MIT License](http://opensource.org/licenses/MIT).
See [LICENSE.txt](LICENSE.txt) for the full text.
