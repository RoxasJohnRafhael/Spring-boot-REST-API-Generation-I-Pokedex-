package POKEDEX.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import POKEDEX.demo.model.Pokemon;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Long> {
    Pokemon findByNumber(int number);
    Pokemon findByNameIgnoreCase(String name);
    List<Pokemon> findAllByNumber(int number);
    List<Pokemon> findByNameContainingIgnoreCase(String name);
    List<Pokemon> findByTypeIgnoreCase(String type);
}
