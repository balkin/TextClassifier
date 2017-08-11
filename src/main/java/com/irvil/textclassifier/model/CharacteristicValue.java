package com.irvil.textclassifier.model;

import javax.persistence.*;

@Entity
@Table(name = "CHARACTERISTICSVALUES")
public class CharacteristicValue {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private int id;

  @Column(name = "OrderNumber")
  private int orderNumber;

  @Column(name = "VALUE")
  private String value;

  @ManyToOne
  @JoinColumn(name = "CHARACTERISTICSNAMEID", nullable = false)
  private Characteristic characteristic;

  public CharacteristicValue() {
  }

  public CharacteristicValue(int orderNumber, String value) {
    this.orderNumber = orderNumber;
    this.value = value;
  }

  public CharacteristicValue(String value) {
    this(0, value);
  }

  public void setCharacteristic(Characteristic characteristic) {
    this.characteristic = characteristic;
  }

  public int getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(int orderNumber) {
    this.orderNumber = orderNumber;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    return ((o instanceof CharacteristicValue) && (this.value.equals(((CharacteristicValue) o).getValue())));
  }

  @Override
  public int hashCode() {
    return this.value.hashCode();
  }
}