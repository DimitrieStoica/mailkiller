angular.module('MailKillerDashboard', ['mailkillerservices', 'info', 'status', 'ngRoute', 'ui.directives', 'ui.bootstrap', 'angular-confirm', 'ngMaterial', 'ngMessages', 'material.svgAssetsCache', 'nvd3']).
    config(function ($locationProvider, $routeProvider) {
        // $locationProvider.html5Mode(true);
        $routeProvider.when('/vmdetails/:serviceInstanceId', {
            controller: VmServiceDetailsController,
            templateUrl: 'assets/templates/vmdetails.html'
        });
        $routeProvider.when('/vmservices/:spaceGuid', {
            controller: VmServicesController,
            templateUrl: 'assets/templates/vmservices.html'
        });
        $routeProvider.when('/lbservices/:spaceGuid', {
            controller: LbServicesController,
            templateUrl: 'assets/templates/lbservices.html'
        });
        $routeProvider.when('/lbdetails/:serviceInstanceId', {
            controller: LbServiceDetailsController,
            templateUrl: 'assets/templates/lbdetails.html'
        });
        $routeProvider.when('/inviteresponse/:inviteGuid', {
            controller: InviteResponseController,
            templateUrl: 'assets/templates/inviteresponse.html'
        });
        $routeProvider.when('/admin/monitoringinfo/:target', {
            controller: MonitoringController,
            templateUrl: 'assets/templates/vm/monitoring.html'
        });

        $routeProvider.when('/notfound', {
            templateUrl: 'assets/templates/notfound.html'
        });
        $routeProvider.otherwise({redirectTo: '/notfound'});
    }
);
