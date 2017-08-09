package com.irvil.textclassifier.model;

import javax.persistence.*;
import java.util.Map;

@Entity
@Table(name = "CLASSIFIABLETEXTS")
public class ClassifiableText {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  private final int id;

  @Column(name = "TEXT")
  private final String text;

  @ManyToMany
  @JoinTable(name = "CLASSIFIABLETEXTSCHARACTERISTICS",
      joinColumns = @JoinColumn(name = "CLASSIFIABLETEXTID"),
      inverseJoinColumns = @JoinColumn(name = "CHARACTERISTICSVALUEID"))
  @MapKeyJoinColumn(name = "CHARACTERISTICSNAMEID")
  private final Map<Characteristic, CharacteristicValue> characteristics;

  public ClassifiableText(String text, Map<Characteristic, CharacteristicValue> characteristics) {
    this.id = 0;
    this.text = text;
    this.characteristics = characteristics;
  }

  public ClassifiableText(String text) {
    this(text, null);
  }

  public String getText() {
    return text;
  }

  public Map<Characteristic, CharacteristicValue> getCharacteristics() {
    return characteristics;
  }

  public CharacteristicValue getCharacteristicValue(Characteristic characteristic) {
    return characteristics.get(characteristic);
  }
}