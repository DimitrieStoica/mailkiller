angular.module('emailServices', ['ngResource', 'ngTable'])
    .factory('globalStats', function ($resource) {
        return $resource('mailstat/globalstats');
    })
    .factory('getEmailsFor', function ($resource) {
        return $resource('emailsfor/:account/status/:status');
    })



function homeController($scope, $routeParams, NgTableParams, globalStats, getEmailsFor) {
	$scope.newDate = function(d) {
		var date = new Date(d)
		return date.format()
	}
    $scope.getIconClassFor = function(status) {
    	switch(status) {
    	case "CLEAR":
    		return 'fa fa-hand-peace-o'
    	case "CERTAINLY_SPAM":
    		return 'fa fa-thumbs-o-down'
    	case "PROBABLY_SPAM":
    		return 'fa fa-hand-pointer-o'
    	case "COULD_BE_SPAM":
    		return 'fa fa-question-circle-o'
    	case "UNKNOWN":
    		return 'fa fa-question-circle-o'
    	}
		return 'fa fa-question-circle-o'
    }
    
    $scope.getShortStatusFor = function(status) {
    	switch(status) {
    	case "CLEAR":
    		return 'Clear'
    	case "CERTAINLY_SPAM":
    		return 'Certainly spam'
    	case "PROBABLY_SPAM":
    		return 'Probably spam'
    	case "UNKNOWN":
    	default:
    		return 'Unknown'
    	}
    }
    
    $scope.getLongStatusFor = function(status) {
    	switch(status) {
    	case "CLEAR":
    		return 'Email cleared by the spam checker pipeline'
    	case "CERTAINLY_SPAM":
    		return 'Email checked or forced as spam'
    	case "PROBABLY_SPAM":
    		return 'Email checked as probably spam'
    	case "UNKNOWN":
    		return 'The spam status of the email could not be set'
    	}
    	return '??'
    }

    $scope.init = function() {
		$scope.loadingStats = true;
		//var globalStatsService = globalStats.get();
		globalStats.get().$promise.then(function (result) {
	    	console.log("loaded data:", result)
	        $scope.stats = result;
	    	$scope.loadingStats = false;
	    }, function(error) {
	    	console.log(error)
	    	$scope.loadingStats = false;
	    });
	}
    
    $scope.display = function(s) {
    	if(s.length > 60) {
    		//console.log("truncatenating ", s);
    		return s.substring(0, 60) + '...';
    	}
    	return s;
    }
    
    $scope.loadEmailsFor = function(account, status) {
    	$scope.currentStatus = status
    	$scope.loadingEmails = true
    	console.log("Loading for ", status)
    	getEmailsFor.query({status:status, account:account.accountName})
    		.$promise.then(function (result) {
    	    	//console.log("loaded data:", result)
    			var self = this;
    			$scope.tableParams = new NgTableParams({}, { dataset: result});
    	        $scope.summaries = result;
    	        $scope.totalReceived = result.length
    	    	$scope.loadingEmails = false
    	    }, function(error) {
    	    	console.log(error)
    	    	$scope.loadingEmails = false
    	    });
    	$scope.loadingEmails = false
    }
    
}
