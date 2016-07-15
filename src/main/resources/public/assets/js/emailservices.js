angular.module('emailServices', ['toaster', 'ngAnimate', 'ngResource', 'ngTable'])
    .factory('globalStats', function ($resource) {
        return $resource('mailstat/:type/globalstats');
    })
    .factory('getStatsFor', function ($resource) {
        return $resource('mailstat/:type/:account/stats');
    })
    .factory('getEmailsFor', function ($resource) {
        return $resource('emailsfor/:account/checkerstatus/:status');
    })
    .factory('getEmailInfo', function ($resource) {
        return $resource('/email/:emailid/checkerresult');
    })
    .factory('actOnEmails', function ($resource) {
        return $resource('actonemails/:action');
    })

function homeController($scope, 
		$interval, toaster, $timeout, $location,
		$routeParams, NgTableParams, 
		globalStats, getEmailsFor, getStatsFor, actOnEmails, getEmailInfo) {
	
	function getType() {
		console.log("location is", $location.path())
		type = "nospam"
		if($location.path().endsWith("spam")) {
			type = "spam"
		}
		console.log("type is", type, " for", $location.path())
		return type;
	}
	
	currentTabScopes = [];
	currentTabScope = 0
	
	$scope.newDate = function(d) {
		var date = new Date(d)
		return date.format()
	}
	
	$scope.getInfoFor = function(email) {
		console.log("getting info for", email);
		getEmailInfo.get({emailid:email.internalMessageId})
			.$promise.then(function (result) {
				console.log(result)
				toaster.pop('success', '', result.diagnostic)
			}, function(error) {
				console.log(error)
				toaster.pop('error', '', error.toString())
			});
	}
	
	$scope.selectTab = function(account) {
		currentTabScope = account;
		if(currentTabScopes[account] == null) {
			currentTabScopes[account] = [];
		}
		$scope.context = currentTabScopes[account];
		$scope.context.currentAccount = $scope.stats.statsPerAccounts[account]
		console.log("Selected tab", account, " context is now", $scope.context)

	}
	
	function reloadStats(account) {
		getStatsFor.query({type:getType(), account:account})
			.$promise.then(function (result) {
				//console.log(result)
				for(var index = 0; index < $scope.stats.statsPerAccounts.length; index++) {
					if($scope.stats.statsPerAccounts[index].accountName == account) {
						$scope.stats.statsPerAccounts[index].stats = result;
					}
				}
			}, function(error) {
    			console.log("saved failed", error)
    		}
			);
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
    
//    $scope.setContext = function(index) {
//    	console.log("init for", index)
//    	currentIndex = index;
//    }
    
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
    
    $scope.getType = function() {
    	getType()
    }

    $scope.init = function() {
    	$scope.loadingStats = true;
    	console.log("init called", $location.path())
 		globalStats.get({ type : getType() }).$promise.then(function (result) {
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
    		return s.substring(0, 60) + ' ...';
    	}
    	return s;
    }
    
    var refresh = new Array();
    refresh[0] = $interval(refreshStats, 10000);
    
    $scope.$on("$destroy", function(event) {
    	for(var index = 0; index < refresh.length; index++) {
    		$interval.cancel(refresh[index]);
    	}
    });
    
    function refreshStats() {
    	console.log("refreshing")
    	reloadStats($scope.context.currentAccount.accountName)
    }
    
    $scope.emailsAction = false;
    $scope.buttonAction = "<no action>"
    $scope.actOnEmails = function(action) {
    	console.log("acting on email", action);
    	var data = []
        for(var index = 0; index < $scope.context.summaries.length; index++) {
        	if($scope.context.summaries[index].selected == true
        		&& $scope.context.summaries[index].display == true) {
        		data.push($scope.context.summaries[index].internalMessageId)
        	}
        }
    	if(data.length == 0) {
			toaster.pop('warning', "", 'You must select at least 1 email');
    		return;
    	}
    	var currentContext = $scope.context
    	var currentSummary = $scope.context.summaries
    	var currentTable = $scope.context.tableParams
    	console.log("Saving ", data)
    	actOnEmails.save({action:action}, data)
    		.$promise.then(function (result) {
    			//console.log("saved ok", context)
    			//$scope.init()
    			for(var index = 0; index < currentSummary.length; index++) {
    	        	if(currentSummary[index].selected == true) {
    	        		currentSummary[index].display = false
    	        	}
    	        }
    			currentTable.reload()
    			reloadStats($scope.context.currentAccount.accountName)
    			toaster.pop('success', "", data.length + " email(s) saved.");
    			$scope.context.totalReceived -= data.length
    		}, function(error) {
    			console.log("saved failed", error)
    		}
    	);
    }
    
    $scope.loadEmailsFor = function(account, status) {
    	$scope.context.currentStatus = status
    	$scope.context.currentAccount = account;
    	$scope.context.loadingEmails = true
    	console.log("Loading for ", status)
    	getEmailsFor.query({status:status, account:account.accountName})
    		.$promise.then(function (result) {
    	    	//console.log("loaded data:", result)
    			var self = this;
    			$scope.context.tableParams = new NgTableParams({filter: {display: 'true'}}, { dataset: result});
    	        $scope.context.summaries = result;
    	        for(var index = 0; index < result.length; index++) {
    	        	//console.log(result[index]);
    	        	result[index]["selected"] = false;
       	        	result[index]["display"] = true;
    	        }
    	        $scope.context.totalReceived = result.length
    	    	$scope.context.loadingEmails = false
    	    }, function(error) {
    	    	console.log(error)
    	    	$scope.context.loadingEmails = false
    	    });
    	$scope.context.loadingEmails = false
    }
}
