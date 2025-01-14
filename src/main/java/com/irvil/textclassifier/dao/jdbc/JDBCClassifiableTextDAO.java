package com.irvil.textclassifier.dao.jdbc;

import com.irvil.textclassifier.dao.ClassifiableTextDAO;
import com.irvil.textclassifier.dao.NotExistsException;
import com.irvil.textclassifier.dao.jdbc.connectors.JDBCConnector;
import com.irvil.textclassifier.model.Characteristic;
import com.irvil.textclassifier.model.CharacteristicValue;
import com.irvil.textclassifier.model.ClassifiableText;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JDBCClassifiableTextDAO implements ClassifiableTextDAO {

    private JDBCConnector connector;

    public JDBCClassifiableTextDAO(JDBCConnector connector) {
        if (connector == null) {
            throw new IllegalArgumentException();
        }

        this.connector = connector;
    }

    @Override
    public List<ClassifiableText> getAll() {
        final List<ClassifiableText> classifiableTexts = new ArrayList<>();

        try (Connection con = connector.getConnection()) {
            final String sqlSelect = "SELECT Id, Text FROM ClassifiableTexts";
            final ResultSet rs = con.createStatement().executeQuery(sqlSelect);
            while (rs.next()) {
                final var classifableText = rs.getString("Text");
                final var classifableId = rs.getInt("Id");
                final var chv = getCharacteristicsValues(con, classifableId);
                classifiableTexts.add(new ClassifiableText(classifableText, chv));
            }
        } catch (SQLException ignored) {
        }
        return classifiableTexts;
    }

    @Override
    public void addAll(List<ClassifiableText> classifiableTexts) throws NotExistsException {
        if (classifiableTexts == null ||
            classifiableTexts.size() == 0) {
            return;
        }

        try (Connection con = connector.getConnection()) {
            con.setAutoCommit(false);

            // prepare sql query
            //

            String sqlInsert = "INSERT INTO ClassifiableTexts (Text) VALUES (?)";
            PreparedStatement statement = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS);

            //

            for (ClassifiableText classifiableText : classifiableTexts) {
                if (classifiableText != null &&
                    !classifiableText.getText().equals("") &&
                    classifiableText.getCharacteristics() != null &&
                    classifiableText.getCharacteristics().size() != 0) {

                    if (!fillCharacteristicNamesAndValuesIDs(con, classifiableText)) {
                        throw new NotExistsException("Characteristic value not exists");
                    }

                    // insert
                    //

                    statement.setString(1, classifiableText.getText());
                    statement.executeUpdate();
                    ResultSet generatedKeys = statement.getGeneratedKeys();

                    if (generatedKeys.next()) {
                        // save all characteristics to DB
                        //

                        for (Map.Entry<Characteristic, CharacteristicValue> entry : classifiableText.getCharacteristics().entrySet()) {
                            insertToClassifiableTextsCharacteristicsTable(con, generatedKeys.getInt(1), entry.getKey(), entry.getValue());
                        }
                    }
                }
            }

            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Map<Characteristic, CharacteristicValue> getCharacteristicsValues(Connection con, int classifiableTextId) throws SQLException {
        final Map<Characteristic, CharacteristicValue> characteristics = new HashMap<>();
        final String sqlSelect = "SELECT CharacteristicsNames.Id AS CharacteristicId, " +
            "CharacteristicsNames.Name AS CharacteristicName, " +
            "CharacteristicsValues.Id AS CharacteristicValueId, " +
            "CharacteristicsValues.OrderNumber AS CharacteristicValueOrderNumber, " +
            "CharacteristicsValues.Value AS CharacteristicValue " +
            "FROM ClassifiableTextsCharacteristics " +
            "LEFT JOIN CharacteristicsNames " +
            "ON ClassifiableTextsCharacteristics.CharacteristicsNameId = CharacteristicsNames.Id " +
            "LEFT JOIN CharacteristicsValues " +
            "ON ClassifiableTextsCharacteristics.CharacteristicsValueId = CharacteristicsValues.Id " +
            "AND ClassifiableTextsCharacteristics.CharacteristicsNameId = CharacteristicsValues.CharacteristicsNameId " +
            "WHERE ClassifiableTextsCharacteristics.ClassifiableTextId = ?";
        PreparedStatement statement = con.prepareStatement(sqlSelect);
        statement.setInt(1, classifiableTextId);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            Characteristic characteristic = new Characteristic(rs.getInt("CharacteristicId"), rs.getString("CharacteristicName"));
            CharacteristicValue characteristicValue = new CharacteristicValue(rs.getInt("CharacteristicValueOrderNumber"), rs.getString("CharacteristicValue"));
            characteristicValue.setId(rs.getInt("CharacteristicValueId"));
            characteristics.put(characteristic, characteristicValue);
        }

        return characteristics;
    }

    private boolean fillCharacteristicNamesAndValuesIDs(Connection con, ClassifiableText classifiableText) throws SQLException {
        final String sqlSelect = "SELECT CharacteristicsNames.Id AS CharacteristicId, " +
            "CharacteristicsValues.Id AS CharacteristicValueId " +
            "FROM CharacteristicsValues JOIN CharacteristicsNames " +
            "ON CharacteristicsValues.CharacteristicsNameId = CharacteristicsNames.Id " +
            "WHERE CharacteristicsNames.Name = ? AND CharacteristicsValues.Value = ?";
        final PreparedStatement statement = con.prepareStatement(sqlSelect);

        for (Map.Entry<Characteristic, CharacteristicValue> entry : classifiableText.getCharacteristics().entrySet()) {
            final var key = entry.getKey();
            final var value = entry.getValue();
            if (key.getName().isEmpty() || value.getValue().isEmpty()) {
                log.warn("Weird classifable text: {} (empty cat)", classifiableText.getText());
                continue;
            }
            statement.setString(1, key.getName());
            statement.setString(2, value.getValue());
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                key.setId(rs.getInt("CharacteristicId"));
                value.setId(rs.getInt("CharacteristicValueId"));
            } else {
                return false;
            }
        }

        return true;
    }

    private void insertToClassifiableTextsCharacteristicsTable(Connection con, int classifiableTextId, Characteristic characteristic, CharacteristicValue characteristicValue) throws SQLException {
        String sqlInsert = "INSERT INTO ClassifiableTextsCharacteristics (ClassifiableTextId, CharacteristicsNameId, CharacteristicsValueId) VALUES (?, ?, ?)";
        PreparedStatement statement = con.prepareStatement(sqlInsert);
        statement.setInt(1, classifiableTextId);
        statement.setInt(2, characteristic.getId());
        statement.setInt(3, characteristicValue.getId());
        statement.executeUpdate();
    }
}