package com.irvil.textclassifier.dao.jpa;

import com.irvil.textclassifier.dao.AlreadyExistsException;
import com.irvil.textclassifier.dao.CharacteristicDAO;
import com.irvil.textclassifier.dao.EmptyRecordException;
import com.irvil.textclassifier.model.Characteristic;
import com.irvil.textclassifier.model.CharacteristicValue;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HibernateCharacteristicDAO implements CharacteristicDAO {
  //todo: move to other place
  private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
      .createEntityManagerFactory("TextClassifier");

  @Override
  public List<Characteristic> getAllCharacteristics() {
    Set<Characteristic> characteristics = new LinkedHashSet<>();
    EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
    EntityTransaction transaction = null;

    try {
      transaction = manager.getTransaction();
      transaction.begin();

      characteristics.addAll(manager.createQuery("SELECT c FROM Characteristic c JOIN c.possibleValues v", Characteristic.class).getResultList());

      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }

      e.printStackTrace();
    } finally {
      manager.close();
    }

    List<Characteristic> characteristicsList = new ArrayList<>();
    characteristicsList.addAll(characteristics);
    return characteristicsList;
  }

  @Override
  public Characteristic addCharacteristic(Characteristic characteristic) throws AlreadyExistsException {
    if (characteristic == null ||
        characteristic.getName().equals("") ||
        characteristic.getPossibleValues() == null ||
        characteristic.getPossibleValues().size() == 0) {
      return null;
    }

    if (findCharacteristicByName(characteristic.getName()) != null) {
      throw new AlreadyExistsException("Characteristic already exists");
    }

    int i = 1;

    characteristic.getPossibleValues().remove(null);
    characteristic.getPossibleValues().remove(new CharacteristicValue(""));

    for (CharacteristicValue characteristicValue : characteristic.getPossibleValues()) {
      if (characteristicValue != null) {
        characteristicValue.setCharacteristic(characteristic);
        characteristicValue.setOrderNumber(i++);
      }
    }

    EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
    EntityTransaction transaction = null;

    try {
      transaction = manager.getTransaction();
      transaction.begin();

      manager.persist(characteristic);
      manager.flush();

      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }

      e.printStackTrace();
    } finally {
      manager.close();
    }

    return characteristic;
  }

  Characteristic findCharacteristicByName(String characteristicName) {
    Characteristic characteristic = null;

    EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
    EntityTransaction transaction = null;

    try {
      transaction = manager.getTransaction();
      transaction.begin();

      TypedQuery<Characteristic> query = manager.createNamedQuery("Characteristic.findByName", Characteristic.class);
      query.setParameter("characteristicName", characteristicName);

      try {
        characteristic = query.getSingleResult();
      } catch (NoResultException ignored) {

      }

      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }

      e.printStackTrace();
    } finally {
      manager.close();
    }

    return characteristic;
  }
}