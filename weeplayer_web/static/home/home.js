function HomeController($scope, $http) {

    $scope.selectPlaylist = function(playlistId) {
        $http.get("api/playlists/" + playlistId)
            .then(function(response) {
                $scope.currentPlaylist = response.data;
                console.log($scope.currentPlaylist)
            });
    };

    $http.get("api/playlists")
        .then(function(response) {
            $scope.playlists = response.data;
        });
}