function HomeController($scope, $http) {

    $scope.fetchTracks = function (playlistId, offset, callback) {
        $http.get("api/playlists/" + playlistId + "/" + offset)
            .then(function (response) {
                callback(response.data);
            });
    };
    
    $scope.tryLoadMoreTracks = function () {
        if (!$scope.currentPlaylist || $scope.currentPlaylist.isFull) return;
        $scope.fetchTracks($scope.currentPlaylist.id, $scope.currentPlaylist.tracks.length, function (playlist) {
            playlist.tracks.forEach(function (track) {
                $scope.currentPlaylist.tracks.push(track);
            });
            if (playlist.tracks.length < 150) {
                $scope.currentPlaylist.isFull = true;
            }
        });
    };

    $scope.selectPlaylist = function (playlistId) {
        $scope.fetchTracks(playlistId, 0, function (playlist) {
            $scope.currentPlaylist = playlist;
            $('html, body').animate({scrollTop: 0}, 'fast');
            if (playlist.tracks.length < 150) {
                $scope.currentPlaylist.isFull = true;
            }
        });
    };

    $scope.playTrack = function(track) {
        $scope.currentTrack = track;
        var trackId = track.id;
        if($scope.soundFile) {
            $scope.soundFile.pause();
        }
        $scope.soundFile = document.createElement("audio");
        $scope.soundFile.preload = "auto";
        $scope.soundFile.loop = "true";

        var src = document.createElement("source");
        src.src = 'api/tracks/' + trackId;
        $scope.soundFile.appendChild(src);
        $scope.soundFile.load();
        $scope.soundFile.volume = 1;
        $scope.soundFile.play();
        $scope.soundFile.playing = true;
    };

    $scope.playPause = function() {
        $scope.soundFile.playing ? $scope.soundFile.pause() : $scope.soundFile.play();
        $scope.soundFile.playing = !$scope.soundFile.playing;
    };

    $http.get("api/playlists")
        .then(function (response) {
            $scope.playlists = response.data;
        });
}