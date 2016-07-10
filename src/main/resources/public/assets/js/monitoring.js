angular.module('monitoring', ['ngResource', 'ui.bootstrap', 'status', 'darthwade.loading', 'nvd3']).
    factory('dump', function ($resource) {
        return $resource('dump');
    })

function MonitoringController($scope, $routeParams, $loading, $interval, $http, $uibModal) {
	
	$scope.target = $routeParams.target
	console.log('target is ' + $scope.target)
	$scope.baseurl = $routeParams.target + '/';
	console.log('baseurl is [' + $scope.baseurl + ']')

	$scope.loadingMonitor = true
	$scope.threadStats = new Object(); //.NEW = 0
	$scope.threadStats["RUNNABLE"] = 0
	$scope.threadStats["BLOCKED"] = 0
	$scope.threadStats["WAITING"] = 0
	$scope.threadStats["TIMED_WAITING"] = 0
	$scope.threadStats["TERMINATED"] = 0
	$scope.threadStats["total"] = 0
	
	
	$scope.currentActive = 0
	$scope.maxActive = 0

	$scope.heapFree = 0
	$scope.heapCommitted = 0
	$scope.heapTotal = 0

	var refConnection = $interval(refreshConnection, 1000);
	var refAllData = $interval(refreshAllData, 30000);
	
    $scope.$on("$destroy", function(event) {
		$interval.cancel(refConnection);
		$interval.cancel(refAllData);
    });
    
    refreshConnection();

	function refreshConnection() {
        $scope.dataCnx = [
                       {
                           key: "Total connection(s)",
                           y: $scope.maxActive,
                           color : 'green'
                       },
                       {
                           key: "Active connection(s)",
                           y: $scope.currentActive,
                           color : 'tomato'
                       }
                   ];
        
        $scope.dataHeap = [
                          {
                              key: "Total Heap (bytes)",
                              y: $scope.heapTotal,
                              color : 'green'
                          },
                          {
                              key: "Committed Heap (bytes)",
                              y: $scope.heapCommitted,
                              color : 'blue'
                          },
                          {
                              key: "Used Heap (bytes)",
                              y: $scope.heapUsed,
                              color : 'tomato'
                          }
                      ];

	}
	
	$scope.optionsHeap = {
            chart: {
                type: 'pieChart',
                height: 200,
                width: 200,
                x: function(d){return d.key;},
                y: function(d){return d.y;},
                color : function(d){return d.color;},
                showLabels: false,
                duration: 500,
                labelThreshold: 0.01,
                labelSunbeamLayout: true,
                tooltip: {
                	valueFormatter: function(num, i) {
                		    return num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,")
                	}
                },
                legend: {
                    margin: {
                        top: 5,
                        right: 35,
                        bottom: 5,
                        left: 0
                    }
                }
            }
        };

	
	$scope.optionsCnx = {
            chart: {
                type: 'pieChart',
                height: 200,
                width: 200,
                x: function(d){return d.key;},
                y: function(d){return d.y;},
                color : function(d){return d.color;},
                showLabels: false,
                duration: 500,
                labelThreshold: 0.01,
                labelSunbeamLayout: true,
                tooltip: {
                	valueFormatter: function(d, i) {return parseInt(d)}
                },
                legend: {
                    margin: {
                        top: 5,
                        right: 35,
                        bottom: 5,
                        left: 0
                    }
                }
            }
        };

	$scope.loaddump = true
	$scope.loadlogs = true

	function refreshAllData() {
		getAll('env');
		getAll('metrics');
	}
	
	$scope.roundIt = function (n) {
		return parseFloat(Math.floor(n*10)/10).toFixed(1)
	}
	
	function getAll(url) {
		$http.get($scope.baseurl + url)
			.success(function (data) {
				$scope.error = null
				$scope[url] = data
				for(var p in data) {
					//console.log("data :" + p)
					if(p == "datasource.primary.active") {
						$scope.currentActive = data[p]
					} else if(p == "datasource.primary.usage") {
						$scope.percentageActive = parseFloat(Math.round(data[p] * 10000) / 100).toFixed(2)
					} else if(p == "heap") {
						$scope.heapTotal = data[p]
					} else if(p == "heap.committed") {
						$scope.heapCommitted = data[p]
					} else if(p == "heap.used") {
						$scope.heapUsed = data[p]
					}
					for(var pp in data[p]) {
						//console.log("sub level is :" + p + "/" + pp)
						if(pp == "spring.datasource.max-active") {
							$scope.maxActive = data[p][pp]
							//console.log(">>>> $scope.maxActive =" + $scope.maxActive)
						}
					}
				}
			})
			.error(function(data) {
				$scope.currentActive = 0
				$scope.percentageActive = 0
				$scope.maxActive = 0
				$scope.loadingMonitor = false
				$scope.loaddump = false
				$scope.error = data
				console.log(data)
			});
	}

	$scope.getThreadDump = function () {
		$scope.loaddump = true
		$scope.threadStats["RUNNABLE"] = 0
		$scope.threadStats["BLOCKED"] = 0
		$scope.threadStats["WAITING"] = 0
		$scope.threadStats["TIMED_WAITING"] = 0
		$scope.threadStats["TERMINATED"] = 0
		$scope.threadStats["total"] = 0

		$http.get($scope.baseurl + 'dump')
			.success(function (data) {
				for(var index = 0; index < data.length; index++) {
					$scope.threadStats.total++;
					$scope.threadStats[data[index].threadState]++;
				}
				$scope.loadingMonitor = false
				$scope.loaddump = false
				$scope.dump = data
				$scope.error = null

			})
			.error(function(data) {
				$scope.loadingMonitor = false
				$scope.loaddump = false
				$scope.error = data
				console.log(data)
			});
	}
	$scope.getDate = function(d) {
		return new Date(d)
	}
	$scope.getLRULogs = function () {
		$scope.loadlogs = true
		$http.get($scope.baseurl + 'lrulogs')
		.success(function (data) {
			$scope.loadingMonitor = false
			$scope.loadlogs = false
			$scope.logs = data
			$scope.error = null

		})
		.error(function(data) {
			$scope.loadingMonitor = false
			$scope.loadlogs = false
			$scope.error = data
			console.log(data)
		});
	}

	refreshAllData()
	$scope.getThreadDump();
	$scope.getLRULogs();
	

}
