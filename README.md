# Text Classifier

Application for text classification using neural networks.

## Requirements

- Java SE Development Kit 8 (`jdk-1.8`)

## Dependencies

- Encog Machine Learning Framework (`org.encog:encog-core:3.3.0`)
- Apache POI (`org.apache.poi:poi-ooxml:3.16`)
- SQLiteJDBC (`org.xerial:sqlite-jdbc:3.19.3`)
- JUnit 4.12 (`junit:junit:4.12`)

## Config.ini file description

Parameter | Description | Possible values
------------ | ------------- | -------------
db_path | Path for database files and trained classifiers | Example: ./db
dao_type | Method of data storage and access | jdbc
dbms_type | Database management system | sqlite
sqlite_db_filename | SQLite database name | Example: TextClassifier.db
ngram_strategy | Text splitting algorithm | unigram, filtered_unigram, bigram, filtered_bigram

## Quick start guide

1. When you launch application first time, it will ask you for XLSX-file with data for training. The file can include one or two sheets. First sheet should contain data for training, second sheet should contain data for testing of accurancy. File structure:
<p align="center">
  <img src="https://github.com/RusZ/TextClassifier/raw/master/images/xlsx_example.png" />
</p>
2. After that application will build vocabulary, will create and train neural network for each Characteristic.

3. Restart application and use it for text classification.

## Author

- [Ruslan Zakaryaev](https://github.com/RusZ)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.