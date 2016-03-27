var baseApp = angular.module('baseApp', ['ngRoute']);

baseApp.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.when('/', {
            templateUrl: 'home/home.html',
            controller: HomeController
        }).when('/about', {
            templateUrl: 'about/about.html',
            controller: AboutController
        }).when('/contact', {
            templateUrl: 'contact/contact.html',
            controller: ContactController
        }).otherwise({
            redirectTo: '/'
        });
    }]);