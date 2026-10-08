package POKEDEX.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private int number; // Pokedex Number

    @Column(unique = true)
    private String name;
    private String type;
    private Integer level;
    private String abilities;
    private String imageUrl;
    private String artworkUrl;
    private String category; // e.g. "Butterfly Pokémon"
    private String height;   // e.g. "3'7\""
    private String weight;   // e.g. "70.5 lbs."

    // Stats
    private int hp;
    private int attack;
    private int defense;
    private int specialAttack;
    private int specialDefense;
    private int speed;

    private String weakness;
    private String strength;
    private String eggGroup;
    private String evolution; // e.g. "Bulbasaur -> Ivysaur -> Venusaur"
    private int bst;          // Base Stat Total
    private boolean favorite; // Favorites system

    @Column(length = 1000)
    private String description;

    public Pokemon() {}

    public Pokemon(int number, String name, String type, Integer level, String abilities, String imageUrl, String artworkUrl, 
                   int hp, int attack, int defense, int specialAttack, int specialDefense, int speed, 
                   String description, String category, String height, String weight,
                   String weakness, String strength, String eggGroup, String evolution, int bst) {
        this.number = number;
        this.name = name;
        this.type = type;
        this.level = level;
        this.abilities = abilities;
        this.imageUrl = imageUrl;
        this.artworkUrl = artworkUrl;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.description = description;
        this.category = category;
        this.height = height;
        this.weight = weight;
        this.weakness = weakness;
        this.strength = strength;
        this.eggGroup = eggGroup;
        this.evolution = evolution;
        this.bst = bst;
        this.favorite = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getAbilities() {
        return abilities;
    }

    public void setAbilities(String abilities) {
        this.abilities = abilities;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getSpecialAttack() {
        return specialAttack;
    }

    public void setSpecialAttack(int specialAttack) {
        this.specialAttack = specialAttack;
    }

    public int getSpecialDefense() {
        return specialDefense;
    }

    public void setSpecialDefense(int specialDefense) {
        this.specialDefense = specialDefense;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getWeakness() {
        return weakness;
    }

    public void setWeakness(String weakness) {
        this.weakness = weakness;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getEggGroup() {
        return eggGroup;
    }

    public void setEggGroup(String eggGroup) {
        this.eggGroup = eggGroup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getEvolution() {
        return evolution;
    }

    public void setEvolution(String evolution) {
        this.evolution = evolution;
    }

    public int getBst() {
        return bst;
    }

    public void setBst(int bst) {
        this.bst = bst;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
