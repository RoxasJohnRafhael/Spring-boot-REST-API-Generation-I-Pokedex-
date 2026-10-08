package POKEDEX.demo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import POKEDEX.demo.model.Pokemon;
import POKEDEX.demo.model.Role;
import POKEDEX.demo.repository.PokemonRepository;
import POKEDEX.demo.service.PokeApiService;
import POKEDEX.demo.service.UserService;

import java.util.Collections;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PokemonRepository pokemonRepository;
    private final PokeApiService pokeApiService;
    private final UserService userService;

    public DataInitializer(PokemonRepository pokemonRepository, PokeApiService pokeApiService, UserService userService) {
        this.pokemonRepository = pokemonRepository;
        this.pokeApiService = pokeApiService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        initializeUsers();
        System.out.println("Checking for duplicate Pokémon entries...");
        removeDuplicates();

        System.out.println("Starting high-speed Pokedex seeding (Gen 1-10)...");
        
        // Use a thread pool to fetch data in parallel (limit to 30 threads for faster seeding)
        ExecutorService executor = Executors.newFixedThreadPool(30);
        List<CompletableFuture<Pokemon>> futures = new java.util.ArrayList<>();

        int totalPokemon = 1025; // Gen 9 ends at 1025
        
        for (int i = 1; i <= totalPokemon; i++) {
            final int id = i;
            futures.add(CompletableFuture.supplyAsync(() -> fetchAndPreparePokemon(id), executor));
        }

        // Wait for all fetches to complete and filter out nulls
        List<Pokemon> toSaveOrUpdate = futures.stream()
                .map(CompletableFuture::join)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        // Batch save in chunks of 100 to reduce transaction overhead
        int batchSize = 100;
        for (int i = 0; i < toSaveOrUpdate.size(); i += batchSize) {
            int end = Math.min(i + batchSize, toSaveOrUpdate.size());
            List<Pokemon> batch = toSaveOrUpdate.subList(i, end);
            pokemonRepository.saveAll(batch);
            System.out.println("Progress: " + end + "/" + toSaveOrUpdate.size() + " Pokémon processed...");
        }

        executor.shutdown();
        System.out.println("Pokedex automated seeding complete!");
    }

    private void initializeUsers() {
        System.out.println("Initializing default users...");
        if (userService.findByUsername("admin") == null) {
            userService.registerUser("admin", "admin123", "admin@pokedex.com", new HashSet<>(java.util.Arrays.asList(Role.ADMIN, Role.USER)));
            System.out.println("Admin account created: admin/admin123");
        }
        if (userService.findByUsername("user") == null) {
            userService.registerUser("user", "user123", "user@pokedex.com", Collections.singleton(Role.USER));
            System.out.println("User account created: user/user123");
        }
    }

    private Pokemon fetchAndPreparePokemon(int i) {
        try {
            Pokemon existingByNumber = pokemonRepository.findByNumber(i);
            if (existingByNumber == null) {
                return pokeApiService.fetchPokemonData(i);
            } else {
                int gen = pokeApiService.getGeneration(i);
                String expectedSpriteUrl = pokeApiService.getSpriteUrl(existingByNumber.getName(), gen);
                
                boolean needsUpdate =
                    existingByNumber.getDescription() == null || existingByNumber.getDescription().isBlank() ||
                    existingByNumber.getCategory() == null || existingByNumber.getCategory().isBlank() ||
                    existingByNumber.getImageUrl() == null || !existingByNumber.getImageUrl().equals(expectedSpriteUrl) ||
                    existingByNumber.getArtworkUrl() == null || existingByNumber.getArtworkUrl().isBlank() ||
                    existingByNumber.getHp() == 0 || existingByNumber.getAttack() == 0 ||
                    existingByNumber.getDefense() == 0 || existingByNumber.getSpeed() == 0 ||
                    existingByNumber.getWeakness() == null || existingByNumber.getWeakness().equals("Unknown") ||
                    existingByNumber.getStrength() == null || existingByNumber.getStrength().equals("Unknown") ||
                    existingByNumber.getEvolution() == null || existingByNumber.getEvolution().equals("No evolution data") ||
                    existingByNumber.getBst() == 0;

                if (needsUpdate) {
                    Pokemon p = pokeApiService.fetchPokemonData(i);
                    if (p != null) {
                        existingByNumber.setName(p.getName());
                        existingByNumber.setType(p.getType());
                        if (existingByNumber.getLevel() == null) {
                            existingByNumber.setLevel(p.getLevel());
                        }
                        existingByNumber.setAbilities(p.getAbilities());
                        existingByNumber.setImageUrl(p.getImageUrl());
                        existingByNumber.setArtworkUrl(p.getArtworkUrl());
                        existingByNumber.setHp(p.getHp());
                        existingByNumber.setAttack(p.getAttack());
                        existingByNumber.setDefense(p.getDefense());
                        existingByNumber.setSpecialAttack(p.getSpecialAttack());
                        existingByNumber.setSpecialDefense(p.getSpecialDefense());
                        existingByNumber.setSpeed(p.getSpeed());
                        existingByNumber.setDescription(p.getDescription());
                        existingByNumber.setCategory(p.getCategory());
                        existingByNumber.setHeight(p.getHeight());
                        existingByNumber.setWeight(p.getWeight());
                        existingByNumber.setWeakness(p.getWeakness());
                        existingByNumber.setStrength(p.getStrength());
                        existingByNumber.setEggGroup(p.getEggGroup());
                        existingByNumber.setEvolution(p.getEvolution());
                        existingByNumber.setBst(p.getBst());
                        return existingByNumber;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error preparing Pokémon #" + i + ": " + e.getMessage());
        }
        return null;
    }

    private void removeDuplicates() {
        // Find all pokemon
        List<Pokemon> allPokemon = pokemonRepository.findAll();

        // Check for duplicate numbers
        Map<Integer, List<Pokemon>> groupedByNumber = allPokemon.stream()
                .collect(Collectors.groupingBy(Pokemon::getNumber));

        for (Map.Entry<Integer, List<Pokemon>> entry : groupedByNumber.entrySet()) {
            if (entry.getValue().size() > 1) {
                System.out.println("Removing " + (entry.getValue().size() - 1) + " duplicates for Pokémon #" + entry.getKey());
                for (int i = 1; i < entry.getValue().size(); i++) {
                    pokemonRepository.delete(entry.getValue().get(i));
                }
            }
        }

        // Re-fetch all remaining pokemon to check for duplicate names
        allPokemon = pokemonRepository.findAll();
        Map<String, List<Pokemon>> groupedByName = allPokemon.stream()
                .collect(Collectors.groupingBy(p -> p.getName().toLowerCase()));

        for (Map.Entry<String, List<Pokemon>> entry : groupedByName.entrySet()) {
            if (entry.getValue().size() > 1) {
                System.out.println("Removing " + (entry.getValue().size() - 1) + " duplicates for Pokémon name: " + entry.getKey());
                for (int i = 1; i < entry.getValue().size(); i++) {
                    pokemonRepository.delete(entry.getValue().get(i));
                }
            }
        }
    }
}
