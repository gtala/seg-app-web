myApp.controller('fichaController', function($scope, $http, $stateParams, $rootScope) {

  $scope.traerFicha = function(tipo) {
    $http.get(settings.apiUrl + tipo + '/' + $stateParams.fichaId, {
      headers: {
        'token': $rootScope.sesionActual.idSesion
      }
    }).then(function(response) {
      $scope.item = response.data;
    });
    // $http.get(settings.apiUrl + 'user/movieLists', {
    //   headers: {
    //     'token': $rootScope.sesionActual.idSesion
    //   }
    // }).then(function(response) {
    //   $scope.listas = response.data;
    // })
  };

  // self.marcarActorFavorito = function(actor) {
  //   return $http.put(settings.apiUrl + 'user/favoriteactor/', actor, {
  //     headers: {
  //       'token': $rootScope.sesionActual.idSesion
  //     }
  //   });
  //   alert('Actor agregado.');
  // };
  //
  // self.agregarALista = function(pelicula, lista) {
  //   return $http.post(settings.apiUrl + 'list/' + lista.id + '/', pelicula, {
  //     headers: {
  //       'token': $rootScope.sesionActual.idSesion
  //     }
  //   });
  //   alert('Movie Agregada.');
  // };

});
