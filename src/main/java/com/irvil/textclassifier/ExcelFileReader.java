package com.irvil.textclassifier;

import com.irvil.textclassifier.model.Characteristic;
import com.irvil.textclassifier.model.CharacteristicValue;
import com.irvil.textclassifier.model.ClassifiableText;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ExcelFileReader {

    List<ClassifiableText> xlsxToClassifiableTexts(File xlsxFile, int sheetNumber) throws IOException, EmptySheetException {
        if (xlsxFile == null ||
            sheetNumber < 1) {
            throw new IllegalArgumentException();
        }

        try (XSSFWorkbook excelFile = new XSSFWorkbook(new FileInputStream(xlsxFile))) {
            XSSFSheet sheet = excelFile.getSheetAt(sheetNumber - 1);

            // at least two rows
            if (sheet.getLastRowNum() > 0) {
                return getClassifiableTexts(sheet);
            } else {
                throw new EmptySheetException("Excel sheet (#" + sheetNumber + ") is empty");
            }
        } catch (IllegalArgumentException e) {
            throw new EmptySheetException("Excel sheet (#" + sheetNumber + ") is not found");
        }
    }

    private List<ClassifiableText> getClassifiableTexts(XSSFSheet sheet) {
        List<Characteristic> characteristics = getCharacteristics(sheet);
        List<ClassifiableText> classifiableTexts = new ArrayList<>();

        // start from second row
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            final var row = sheet.getRow(i);
            Map<Characteristic, CharacteristicValue> characteristicsValues = getCharacteristicsValues(row, characteristics);

            // exclude empty rows
            final var stringCellValue = row.getCell(0).getStringCellValue();
            if (!stringCellValue.equals("")) {
                classifiableTexts.add(new ClassifiableText(stringCellValue, characteristicsValues));
            }
        }

        return classifiableTexts;
    }

    private Map<Characteristic, CharacteristicValue> getCharacteristicsValues(Row row, List<Characteristic> characteristics) {
        Map<Characteristic, CharacteristicValue> characteristicsValues = new HashMap<>();

        for (int i = 1; i < row.getLastCellNum(); i++) {
            if (characteristics.size() > i-1) {
                final var characteristic = characteristics.get(i - 1);
                characteristicsValues.put(characteristic, new CharacteristicValue(row.getCell(i).getStringCellValue()));
            }
        }

        return characteristicsValues;
    }

    private List<Characteristic> getCharacteristics(XSSFSheet sheet) {
        final List<Characteristic> characteristics = new ArrayList<>();

        // first row from second to last columns contains Characteristics names
        final var headerRow = sheet.getRow(0);
        for (int i = 1; i < headerRow.getLastCellNum(); i++) {
            final var stringCellValue = headerRow.getCell(i).getStringCellValue();
            if (!stringCellValue.isEmpty()) {
                characteristics.add(new Characteristic(stringCellValue));
            }
        }

        return characteristics;
    }
}