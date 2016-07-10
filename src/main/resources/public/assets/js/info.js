angular.module('info', ['ngResource']).
    factory('Info', function ($resource) {
        return $resource('info');
    })
    .controller('InfoController' , InfoController);

function InfoController($scope, Info) {
    $scope.info = Info.get();
}
