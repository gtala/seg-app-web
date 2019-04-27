package app.service;

import app.model.dto.RespuestaDto;
import app.model.odb.*;
import app.repositories.RepositorioDeActores;
import app.repositories.RepositorioDeListas;
import app.repositories.RepositorioDeUsuarios;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Rodrigo on 08/04/2017.
 */
@Service
public class UserService {

    @Autowired
    SesionesService sesionesService;

    @Autowired
    RepositorioDeUsuarios repositorioDeUsuarios;

    @Autowired
    RepositorioDeListas repositorioDeListas;

    @Autowired
    RepositorioDeActores repositorioDeActores;


    public void crearNuevoUsuario(Credencial userAndPassword) throws ExceptionInInitializerError {
        User usuarioNuevo = User.create(userAndPassword, false);
        repositorioDeUsuarios.insert(usuarioNuevo);
    }

    public List<User> obtenerUsuarios() {
        return repositorioDeUsuarios.findAll();
    }

    public RespuestaDto marcarActorFavorito(String token, Actor actor) throws JSONException, IOException {
        try {
            User usuario = sesionesService.obtenerUsuarioPorToken(token);
            Optional<Actor> optActor = usuario.getFavoriteActors().stream()
                    .filter(actorFavorito -> actorFavorito.getId().equals(actor.getId())).findFirst();
            RespuestaDto rta = new RespuestaDto();
            if (optActor.isPresent()) {
                rta.setMessage("Ya lo tiene como favorito al actor  " + actor.getName());

            } else {
                repositorioDeActores.save(actor);
                usuario.getFavoriteActors().add(actor);
                repositorioDeUsuarios.save(usuario);
                rta.setMessage("Actor favorito agregado: " + actor.getName());
            }

            return rta;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new RuntimeException("El id de actor posee un formato inválido.");
        }
    }

    public void desmarcarActorFavorito(String token, String id_actor) {
        User usuario = sesionesService.obtenerUsuarioPorToken(token);
        usuario.getFavoriteActors().stream()
                .filter(actorFavorito -> actorFavorito.getId().equals(id_actor)).findFirst().ifPresent(actor -> usuario.getFavoriteActors().remove(actor));
        repositorioDeUsuarios.save(usuario);
    }

    public List<Actor> verActoresFavoritos(String token) throws JSONException, IOException {
        User usuario = sesionesService.obtenerUsuarioPorToken(token);
        return usuario.getFavoriteActors();
    }

    public List<Actor> verRankingActoresFavoritos(String token) throws JSONException, IOException {

        ArrayList<Actor> rankingActores = new ArrayList<Actor>(0);
        List<User> usuarios = obtenerUsuarios();
        usuarios
                .forEach(usuario -> usuario.getFavoriteActors()
                        .forEach(actor -> {
                            Optional<Actor> optActRank = rankingActores.stream()
                                    .filter(actorRank -> actorRank.getId().equals(actor.getId())).findFirst();
                            if (optActRank.isPresent())
                                optActRank.get().incScoreRank();
                            else {
                                actor.resetScoreRak();
                                rankingActores.add(actor);
                            }
                        })
                );

        return rankingActores.stream().sorted((actor1, actor2) -> Integer.compare(actor2.getScoreRank(), actor1.getScoreRank()))
                .collect(Collectors.toList());
    }

    public List<Movie> verPeliculasConMasDeUnActorFavorito(String token) throws JSONException, IOException {

        User usuario = sesionesService.obtenerUsuarioPorToken(token);
        ArrayList<Movie> listPelConMasDeUnActorFav = new ArrayList<Movie>(0);

        int cantActores = 0;

        List<Actor> actoresFav = usuario.getFavoriteActors();

        //Si solo tengo un actor o ninguno me devuelve vacio.
        if (actoresFav.size() <= 1)
            return listPelConMasDeUnActorFav;

        List<Movie> peliculas = new ArrayList<>();

        for (MovieList list : usuario.getLists()) {
            MovieList listaDePeliculas = repositorioDeListas.findOne(list.getId());
            peliculas.addAll(listaDePeliculas.getMovies());
        }

        for (Movie movie : peliculas) {

            for (Actor actorFav : actoresFav) {
                if (movie.getCast().stream().anyMatch(x -> x.getActorId().equals(actorFav.getId()))) {
                    cantActores++;
                }
            }

            if (cantActores > 1) {
                listPelConMasDeUnActorFav.add(movie);
            }

            cantActores = 0;
        }

        return listPelConMasDeUnActorFav;
    }

    public List<MovieList> verListas(String token) {
        User usuario = sesionesService.obtenerUsuarioPorToken(token);
        //esto lo hago para que me devuelva el contenido de cada lista .
        List<MovieList> listasADevolver = getListasCompletasDelUsuario(usuario);
        return listasADevolver;
    }

    private List<MovieList> getListasCompletasDelUsuario(User usuario) {
        return usuario.getLists().stream().map(movieList -> repositorioDeListas.findOne(movieList.getId())).collect(Collectors.toList());
    }

    public List<String> rankingDeActoresPorMayorRepeticion(String token, String idlistaDePeliculas) {
        User usuario = sesionesService.obtenerUsuarioPorToken(token);

        MovieList listaDePeliculas = getListasCompletasDelUsuario(usuario).stream()
                .filter(movieList -> movieList.getId().equals(idlistaDePeliculas)).findFirst()
                .orElseThrow(() -> new RuntimeException("No existe la lista de peliculas que intenta rankear."));

        List<ActorEnPelicula> actoresEnPeliculas = obtenerTodosLosActoresEnPeliculas(listaDePeliculas.getMovies());

        Map<String, Integer> aparicionDeActores = mapearPorRepeticionesLosActoresEnPeliculas(actoresEnPeliculas);

        List<String> actoresOrdenados = aparicionDeActores.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return actoresOrdenados;
    }

    private Map<String, Integer> mapearPorRepeticionesLosActoresEnPeliculas(List<ActorEnPelicula> actoresEnPeliculas) {

        Map<String, Integer> aparicionDeActores = new HashMap<>();

        actoresEnPeliculas.forEach(actorEnPelicula -> evaluarApariciones(actorEnPelicula, aparicionDeActores));

        return aparicionDeActores;
    }

    private List<ActorEnPelicula> obtenerTodosLosActoresEnPeliculas(List<Movie> peliculas) {
        List<ActorEnPelicula> actoresEnPeliculas = new ArrayList<ActorEnPelicula>();
        peliculas.forEach(pelicula -> actoresEnPeliculas.addAll(pelicula.getCast()));
        return actoresEnPeliculas;
    }

    private void evaluarApariciones(ActorEnPelicula actor, Map<String, Integer> aparicionDeActores) {

        try {
//            Actor actor = new Actor(idActor.toString());
            if (aparicionDeActores.containsKey(actor.getName())) {
                Integer valor = aparicionDeActores.get(actor.getName());
                aparicionDeActores.replace(actor.getName(), ++valor);
            } else
                aparicionDeActores.put(actor.getName(), 1);
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
    }

    public void borrarTodo() {
        repositorioDeUsuarios.deleteAll();
    }
}
