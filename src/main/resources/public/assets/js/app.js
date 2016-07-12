angular.module('emailKiller',  ['emailServices', 'ngTable', 'ngRoute', 'ui.bootstrap'])
.config(function ($routeProvider) {
    $routeProvider.when('/home', {
        controller: homeController,
        templateUrl: 'assets/templates/home.html'
    });
    $routeProvider.when('/', {
        controller: homeController,
        templateUrl: 'assets/templates/home.html'
    });
    $routeProvider.when('/notfound', {
        templateUrl: 'assets/templates/notfound.html'
    });
    $routeProvider.otherwise({redirectTo: '/notfound'});
});
