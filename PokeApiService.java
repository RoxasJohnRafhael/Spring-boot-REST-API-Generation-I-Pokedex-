package POKEDEX.demo.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import POKEDEX.demo.model.Pokemon;

@Service
public class PokeApiService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://pokeapi.co/api/v2/";
    
    // Cache for type relations to avoid redundant API calls
    private final Map<String, Map<String, Object>> typeCache = new ConcurrentHashMap<>();
    // Cache for evolution chains
    private final Map<String, Map<String, Object>> evolutionCache = new ConcurrentHashMap<>();
    // Cache for basic pokemon data
    private final Map<Integer, Map<String, Object>> pokemonCache = new ConcurrentHashMap<>();
    // Cache for species data
    private final Map<Integer, Map<String, Object>> speciesCache = new ConcurrentHashMap<>();

    public Pokemon fetchPokemonData(int id) {
        try {
            // Fetch basic data (with caching)
            Map<String, Object> basicData = pokemonCache.computeIfAbsent(id, 
                k -> restTemplate.getForObject(BASE_URL + "pokemon/" + k, Map.class));
            
            // Fetch species data (with caching)
            Map<String, Object> speciesData = speciesCache.computeIfAbsent(id, 
                k -> restTemplate.getForObject(BASE_URL + "pokemon-species/" + k, Map.class));

            if (basicData == null || speciesData == null) return null;

            String name = capitalize((String) basicData.get("name"));
            
            // Extract types
            List<Map<String, Object>> typesList = (List<Map<String, Object>>) basicData.get("types");
            List<String> rawTypes = typesList.stream()
                    .map(t -> (String) ((Map<String, Object>) t.get("type")).get("name"))
                    .collect(Collectors.toList());

            String type = rawTypes.stream()
                    .map(this::capitalize)
                    .collect(Collectors.joining("/"));

            // Fetch Type Relations (with caching)
            Set<String> weakTo = new HashSet<>();
            Set<String> strongAgainst = new HashSet<>();
            
            for (String tName : rawTypes) {
                Map<String, Object> typeData = typeCache.computeIfAbsent(tName, 
                    k -> restTemplate.getForObject(BASE_URL + "type/" + k, Map.class));
                
                if (typeData != null) {
                    Map<String, Object> damageRelations = (Map<String, Object>) typeData.get("damage_relations");
                    
                    // Weaknesses: double damage from
                    List<Map<String, String>> ddFrom = (List<Map<String, String>>) damageRelations.get("double_damage_from");
                    ddFrom.forEach(d -> weakTo.add(capitalize(d.get("name"))));
                    
                    // Strengths: double damage to
                    List<Map<String, String>> ddTo = (List<Map<String, String>>) damageRelations.get("double_damage_to");
                    ddTo.forEach(d -> strongAgainst.add(capitalize(d.get("name"))));
                }
            }

            String weaknessStr = weakTo.isEmpty() ? "None" : String.join(", ", weakTo);
            String strengthStr = strongAgainst.isEmpty() ? "None" : String.join(", ", strongAgainst);

            // Extract abilities
            List<Map<String, Object>> abilitiesList = (List<Map<String, Object>>) basicData.get("abilities");
            String abilities = abilitiesList.stream()
                    .map(a -> capitalize((String) ((Map<String, Object>) a.get("ability")).get("name")))
                    .collect(Collectors.joining(", "));

            // Extract stats
            List<Map<String, Object>> statsList = (List<Map<String, Object>>) basicData.get("stats");
            int hp = (int) statsList.get(0).get("base_stat");
            int attack = (int) statsList.get(1).get("base_stat");
            int defense = (int) statsList.get(2).get("base_stat");
            int specialAttack = (int) statsList.get(3).get("base_stat");
            int specialDefense = (int) statsList.get(4).get("base_stat");
            int speed = (int) statsList.get(5).get("base_stat");
            int bst = hp + attack + defense + specialAttack + specialDefense + speed;

            // Extract physical data
            String height = formatHeight((int) basicData.get("height"));
            String weight = formatWeight((int) basicData.get("weight"));

            // Extract description (first English flavor text)
            List<Map<String, Object>> flavorTexts = (List<Map<String, Object>>) speciesData.get("flavor_text_entries");
            String description = flavorTexts.stream()
                    .filter(f -> ((Map<String, Object>) f.get("language")).get("name").equals("en"))
                    .map(f -> (String) f.get("flavor_text"))
                    .findFirst()
                    .orElse("No data available.")
                    .replace("\n", " ")
                    .replace("\f", " ");

            // Extract category (genera)
            List<Map<String, Object>> genera = (List<Map<String, Object>>) speciesData.get("genera");
            String category = genera.stream()
                    .filter(g -> ((Map<String, Object>) g.get("language")).get("name").equals("en"))
                    .map(g -> (String) g.get("genus"))
                    .findFirst()
                    .orElse("Unknown Pokémon");

            // Extract egg groups
            List<Map<String, Object>> eggGroupsList = (List<Map<String, Object>>) speciesData.get("egg_groups");
            String eggGroup = eggGroupsList.stream()
                    .map(e -> capitalize((String) e.get("name")))
                    .collect(Collectors.joining("/"));

            // Extract Evolution Chain (with caching)
            String evolution = "No evolution data";
            try {
                Map<String, Object> evolutionChainRef = (Map<String, Object>) speciesData.get("evolution_chain");
                if (evolutionChainRef != null) {
                    String chainUrl = (String) evolutionChainRef.get("url");
                    Map<String, Object> chainData = evolutionCache.computeIfAbsent(chainUrl, 
                        url -> restTemplate.getForObject(url, Map.class));
                    
                    if (chainData != null) {
                        Map<String, Object> chain = (Map<String, Object>) chainData.get("chain");
                        List<String> evolutionNames = new ArrayList<>();
                        parseEvolutionChain(chain, evolutionNames);
                        evolution = String.join(" -> ", evolutionNames);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error fetching evolution for " + name + ": " + e.getMessage());
            }

            // Determine generation
            int gen = getGeneration(id);

            // Animated (battle feel) or Static sprite based on generation
            String animatedUrl = getSpriteUrl(name, gen);
            
            // High quality official art (for detail view)
            String artworkUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/" + id + ".png";

            return new Pokemon(id, name, type, 5, abilities, animatedUrl, artworkUrl, hp, attack, defense, 
                               specialAttack, specialDefense, speed, description, category, 
                               height, weight, weaknessStr, strengthStr, eggGroup, evolution, bst);

        } catch (Exception e) {
            System.err.println("Error fetching Pokémon " + id + ": " + e.getMessage());
            return null;
        }
    }

    private void parseEvolutionChain(Map<String, Object> chain, List<String> names) {
        if (chain == null) return;
        Map<String, Object> species = (Map<String, Object>) chain.get("species");
        names.add(capitalize((String) species.get("name")));
        
        List<Map<String, Object>> evolvesTo = (List<Map<String, Object>>) chain.get("evolves_to");
        if (evolvesTo != null && !evolvesTo.isEmpty()) {
            // For simplicity, we just take the first evolution path
            parseEvolutionChain(evolvesTo.get(0), names);
        }
    }

    public String getSpriteUrl(String name, int gen) {
        name = name.toLowerCase();

        return switch (gen) {
            case 1, 2, 3, 4, 5 -> "https://play.pokemonshowdown.com/sprites/gen5ani/" + name + ".gif";
            case 6, 7, 8, 9 -> "https://play.pokemonshowdown.com/sprites/ani/" + name + ".gif";
            default -> "https://play.pokemonshowdown.com/sprites/dex/" + name + ".png";
        };
    }

    public int getGeneration(int id) {
        if (id <= 151) return 1;
        if (id <= 251) return 2;
        if (id <= 386) return 3;
        if (id <= 493) return 4;
        if (id <= 649) return 5;
        if (id <= 721) return 6;
        if (id <= 809) return 7;
        if (id <= 905) return 8;
        if (id <= 1025) return 9;
        return 10;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).replace("-", " ");
    }

    private String formatHeight(int decimeters) {
        double meters = decimeters / 10.0;
        double feet = meters * 3.28084;
        int ft = (int) feet;
        int in = (int) Math.round((feet - ft) * 12);
        return String.format("%d'%d\"", ft, in);
    }

    private String formatWeight(int hectograms) {
        double lbs = hectograms * 0.220462;
        return String.format("%.1f lbs.", lbs);
    }
}
