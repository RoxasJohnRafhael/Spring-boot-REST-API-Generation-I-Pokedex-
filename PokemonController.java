package POKEDEX.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import POKEDEX.demo.model.Pokemon;
import POKEDEX.demo.repository.PokemonRepository;

@RestController
@RequestMapping("/api/pokemon")
@CrossOrigin(origins = "*")
public class PokemonController {

    @Autowired
    private PokemonRepository pokemonRepository;

    @GetMapping
    public List<Pokemon> getAllPokemon() {
        return pokemonRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pokemon> getPokemonById(@PathVariable Long id) {
        return pokemonRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<Pokemon> getPokemonByNumber(@PathVariable int number) {
        Pokemon pokemon = pokemonRepository.findByNumber(number);
        return pokemon != null ? ResponseEntity.ok(pokemon) : ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public List<Pokemon> searchPokemon(@RequestParam String name) {
        return pokemonRepository.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/type/{type}")
    public List<Pokemon> getPokemonByType(@PathVariable String type) {
        return pokemonRepository.findByTypeIgnoreCase(type);
    }

    @PostMapping
    public ResponseEntity<?> addPokemon(@RequestBody Pokemon pokemon) {
        if (pokemonRepository.findByNumber(pokemon.getNumber()) != null) {
            return ResponseEntity.badRequest().body("Pokemon with number " + pokemon.getNumber() + " already exists.");
        }
        if (pokemonRepository.findByNameIgnoreCase(pokemon.getName()) != null) {
            return ResponseEntity.badRequest().body("Pokemon with name " + pokemon.getName() + " already exists.");
        }
        return ResponseEntity.ok(pokemonRepository.save(pokemon));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pokemon> updatePokemon(@PathVariable Long id, @RequestBody Pokemon pokemonDetails) {
        return pokemonRepository.findById(id)
                .map(pokemon -> {
                    pokemon.setNumber(pokemonDetails.getNumber());
                    pokemon.setName(pokemonDetails.getName());
                    pokemon.setType(pokemonDetails.getType());
                    pokemon.setLevel(pokemonDetails.getLevel());
                    pokemon.setAbilities(pokemonDetails.getAbilities());
                    pokemon.setImageUrl(pokemonDetails.getImageUrl());
                    pokemon.setArtworkUrl(pokemonDetails.getArtworkUrl());
                    pokemon.setHp(pokemonDetails.getHp());
                    pokemon.setAttack(pokemonDetails.getAttack());
                    pokemon.setDefense(pokemonDetails.getDefense());
                    pokemon.setSpecialAttack(pokemonDetails.getSpecialAttack());
                    pokemon.setSpecialDefense(pokemonDetails.getSpecialDefense());
                    pokemon.setSpeed(pokemonDetails.getSpeed());
                    pokemon.setBst(pokemonDetails.getBst());
                    pokemon.setDescription(pokemonDetails.getDescription());
                    pokemon.setCategory(pokemonDetails.getCategory());
                    pokemon.setHeight(pokemonDetails.getHeight());
                    pokemon.setWeight(pokemonDetails.getWeight());
                    pokemon.setWeakness(pokemonDetails.getWeakness());
                    pokemon.setStrength(pokemonDetails.getStrength());
                    pokemon.setEggGroup(pokemonDetails.getEggGroup());
                    pokemon.setEvolution(pokemonDetails.getEvolution());
                    pokemon.setFavorite(pokemonDetails.isFavorite());
                    return ResponseEntity.ok(pokemonRepository.save(pokemon));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/favorite")
    public ResponseEntity<Pokemon> toggleFavorite(@PathVariable Long id) {
        return pokemonRepository.findById(id)
                .map(pokemon -> {
                    pokemon.setFavorite(!pokemon.isFavorite());
                    return ResponseEntity.ok(pokemonRepository.save(pokemon));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePokemon(@PathVariable Long id) {
        return pokemonRepository.findById(id)
                .map(pokemon -> {
                    pokemonRepository.delete(pokemon);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
