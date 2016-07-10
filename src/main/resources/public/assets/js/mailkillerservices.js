angular.module('mailkillerservices', ['ngResource', 'ui.bootstrap', 'status', 'darthwade.loading']).
    factory('VmServices', function ($resource) {
        return $resource('vmservices/:spaceGuid');
    })
    .factory('VmServiceDetails', function ($resource) {
        return $resource('vmdetails/:serviceInstanceId');
    })
    .factory('VmServiceRemoteStatus', function ($resource) {
        return $resource('vmdetails/:serviceInstanceId/remotestatus');
    })
    .factory('VmServiceBindableApplication', function ($resource) {
        return $resource('vmdetails/:serviceInstanceId/application');
    })
    .factory('VmToVm', function ($resource) {
        return $resource('vmdetails/:serviceInstanceId/vmtovm');
    })
    .factory('VmToVmServices', function ($resource) {
        return $resource('vmtovm/:vmToVmGuid');
    })
    .factory('InviteService', function ($resource) {
        return $resource('inviteresponse/:inviteGuid');
    })
    .factory('VmLoadBalancers', function ($resource) {
        return $resource('vmdetails/:serviceInstanceId/loadbalancers')
    });

function VmServicesController($scope, $loading, $routeParams, Status, VmServices) {
	
    function list() {
        $loading.start('vmservices');
        $scope.vmServices = VmServices.query({spaceGuid: $routeParams.spaceGuid});
        $scope.vmServices.$promise.then(function (result) {
            $scope.vmServices = result;
            $loading.finish('vmservices');
        }, function(error) {
        	Status.error(error)
            $loading.finish('vmservices');
        });
    }

    $scope.init = function () {
        //list();
    };
}

function InviteResponseController($scope, $routeParams, $loading, $http, $interval, $uibModal, Status, InviteService) {
	$scope.retrievingInvite = true;
	$scope.retrievingOrgsAndSpaces = true;
	$scope.retrievingData = function() {
		return $scope.retrievingInvite == true || $scope.retrievingOrgsAndSpaces == true;
	};
	$scope.acceptingResponse = false 
	$scope.rejectingResponse = false
	
    $scope.init = function () {
    	retrieveInviteDetail();
    	retrieveOrgsAndSpaces();
    }
    
    $scope.buttonsDisabled = function() {
    	return ($scope.acceptingResponse == true || $scope.rejectingResponse == true)
    }
    
    $scope.onSpaceChange = function() {
    	for(var index = 0; index < $scope.relatedspaces.length; index++) {
    		if($scope.relatedspaces[index].spaceuuid == $scope.space) {
    			$scope.relatedhosts = $scope.relatedspaces[index].hosts;
    			$scope.currentSpace = $scope.relatedspaces[index].spacename;
    			break;
    		}
    	}
    }
    
    $scope.onOrgChange = function() {
    	for(var index = 0; index < $scope.treeorg.length; index++) {
    		if($scope.treeorg[index].orguuid == $scope.organization) {
    			$scope.relatedspaces = $scope.treeorg[index].spaces;
    			$scope.currentOrg = $scope.treeorg[index].orgname;
    			$scope.relatedhosts = null;
    			break;
    		}
    	}
    }
    
    function retrieveOrgsAndSpaces() {
    	$scope.retrievingOrgsAndSpaces = true;
    	var query = {inviteGuid: $routeParams.inviteGuid};
    	$http.get('inviteresponse/' + query + '/orgsandspaces')
    	.success(function (data) {
    		console.log(data)
    		$scope.treeorg = data
        	$scope.retrievingOrgsAndSpaces = false;
    	})
    	.error(function(data) {
    		console.log(data)
        	$scope.retrievingOrgsAndSpaces = false;
    	});
    }
    
    function retrieveInviteDetail() {
		$scope.retrievingInvite = true;
    	var query = {inviteGuid: $routeParams.inviteGuid};
    	InviteService.get(query).$promise.then(function(invite) {
    		$scope.invite = invite
    		console.log(invite)
    		showStatus(invite);
    		$scope.retrievingInvite = false;
    	}, function(error) {
    		$scope.retrievingInvite = false;
    		Status.error("Could not retrieve invite [" + $routeParams.inviteGuid + "] " 
    				+ error.data.error
    				+ " - " + error.data.message)
    	});
    }
    
    $scope.rejectInvite = function() {
    	console.log("inviteGuid is " + $scope.invite.inviteGuid)
    	$scope.rejectingResponse = true
    	InviteService.remove({inviteGuid:$scope.invite.inviteGuid}, function(invite) {
    		$scope.rejectingResponse = false
    		$scope.invite = invite
    		showStatus(invite);
    		console.log(invite)
    	}, function(error) {
    		$scope.rejectingResponse = false
    		Status.error("Could not reject invite " + error)
    		console.log("error")
    	})
    }
    
    function showStatus(invite) {
    	switch(invite.status) {
    		case "REJECTED" :
    			Status.success("This invitation has been rejected on " + invite.rejectedDate);
    			break
    		case "ACCEPTED" :
    			Status.success("This invitation has been accepted on " + invite.acceptedDate);
    			break;
    		case "PENDING" :
    			Status.success("You have up to " + invite.expiringDate + " to approve or reject this invite");
    			break;
    		case "DROPPED" :
    			Status.error("This invitation has been dropped on " + invite.droppedDate);
    			break;
    	}
    }
    
    $scope.acceptInvite = function() {
    	$scope.acceptingResponse = true 
    	console.log("in submit")
    	$scope.invite.organizationGuid = $scope.organization;
    	$scope.invite.spaceGuid = $scope.space;
    	$scope.invite.host = $scope.host;
    	InviteService.save($scope.invite, function(invite) {
    		$scope.invite = invite
    		$scope.acceptingResponse = false 
    		showStatus(invite);
    		console.log(invite)
    	}, function(error) {
    		$scope.acceptingResponse = false 
    		Status.error("Could not accept invite " + error)
    		console.log("error")
    	})
    }
}


function VmServiceDetailsController($scope, $routeParams, $loading, $http, $interval, $uibModal, VmServiceDetails, VmServiceBindableApplication, VmLoadBalancers, Status, VmServiceRemoteStatus, VmToVm) {
    console.log("in VmServiceDetailsController")
    var refresh = new Array();
    refresh[0] = $interval(refreshVmRetryButton, 1000);
    refresh[1] = $interval(refreshVmAppBindingRetryButtons, 1000);
    refresh[2] = $interval(refreshVmBindingRetryButtons, 1000);
    refresh[3] = $interval(refreshVmDetails, 15000);
    refresh[4] = $interval(retrieveVmRemoteStatus, 30000);
    refresh[5] = $interval(refreshVmtoVm, 30000);
    
    $scope.$on("$destroy", function(event) {
    	// Canceling the refresh otherwise there will be a lot of errors logged on the server side.
    	for(var index = 0; index < refresh.length; index++) {
    		$interval.cancel(refresh[index]);
    	}
    });
    
    $scope.vmToVmStatus = function(vmtovm) {
    	switch(vmtovm.status) {
    	case 'WAITING_APPROVAL':
    		return "Waiting for approval since " + vmtovm.createdDate
    	case 'ACCEPTED' :
    		return "Accepted on " + vmtovm.acceptedDate
    	case 'REJECTED' :
    		return "Rejected on " + vmtovm.rejectedDate
    	case 'DROPPED' :
			return "Dropped on " + vmtovm.droppedDate
		default :
			return "???"
    	}
    };
    
    $scope.canAccept = function(vmtovm) {
    	return vmtovm.status == 'WAITING_APPROVAL';
    }
    $scope.canDrop = function(vmtovm) {
    	return vmtovm.status == 'REJECTED';
    }
    $scope.canReject = function(vmtovm) {
    	return vmtovm.status == 'ACCEPTED' || vmtovm.status == 'WAITING_APPROVAL';
    }

    $scope.acceptVmToVm = function(vmtovm) {
    	$scope.action(vmtovm, 'accept');
    }
    $scope.dropVmToVm = function(vmtovm) {
    	$scope.action(vmtovm, 'drop');
    }
    $scope.rejectVmToVm = function(vmtovm) {
    	console.log("rejecting")
    	$scope.action(vmtovm, 'reject');
    }
    
    $scope.action = function(vmtovm, verb) {
    	console.log(verb + " " + vmtovm.vmToVmGuid)
    	$http.get('vmtovm/' + vmtovm.vmToVmGuid + '/' + verb)
	    	.success(function (data) {
	    		console.log(data)
	    		retrieveVmtoVm();
	    	})
	    	.error(function(data) {
	    		console.log(data)
	    	});
    }
    
    function refreshVmRetryButton() {
        if ($scope.vmDetails && $scope.vmDetails.vroStatus == 'FAILED' && $scope.vmDetails.buttonState.disabled) {
            if ($scope.vmDetails.retryWaitingTimeInSeconds <= 0) {
                retrieveVmDetails();
            } else {
                $scope.vmDetails.buttonState.label = "Retry in " + toReadableTime($scope.vmDetails.retryWaitingTimeInSeconds);
                $scope.vmDetails.retryWaitingTimeInSeconds--;
            }
        }
    }

    function refreshVmAppBindingRetryButtons() {
        if (!$scope.vmBindableApplication) {
            return;
        }
        for (var idx in $scope.vmBindableApplication.boundServices) {
            var binding = $scope.vmBindableApplication.boundServices[idx];
            if ((binding.status == 'FAILED' || binding.status == 'FORBIDDEN') && binding.buttonState.disabled) {
                if (binding.retryWaitingTimeInSeconds <= 0) {
                    retrieveVmBindableApplication();
                } else {
                    binding.buttonState.label = "Retry in " + toReadableTime(binding.retryWaitingTimeInSeconds);
                    binding.retryWaitingTimeInSeconds--;
                }
            }
        }
    }

    function refreshVmBindingRetryButtons() {
        if (!$scope.vmLoadBalancers) {
            return;
        }
        for (var idx in $scope.vmLoadBalancers) {
            var binding = $scope.vmLoadBalancers[idx];
            if (binding.vroBindingStatus == 'FAILED' && binding.buttonState.disabled) {
                if (binding.retryWaitingTimeInSeconds <= 0) {
                    retrieveVmLoadBalancers();
                } else {
                    binding.buttonState.label = "Retry in " + toReadableTime(binding.retryWaitingTimeInSeconds);
                    binding.retryWaitingTimeInSeconds--;
                }
            }
        }
    }

    function toReadableTime(timeInSeconds) {
        if (timeInSeconds > 60) {
            var sec = timeInSeconds % 60;
            var min = (timeInSeconds - sec) / 60;
            return min + "min " + sec + "s";
        }
        else {
            return timeInSeconds + "s";
        }
    }

    function refreshVmDetails() {
        if ($scope.vmDetails
            && ($scope.vmDetails.vroStatus == 'PROVISIONING' || $scope.vmDetails.vroStatus == 'UPDATING')) {
            retrieveVmDetails();
            retrieveVmBindableApplication();
            retrieveVmLoadBalancers();
        }
    }

    function retrieveVmDetails() {
        var query = {serviceInstanceId: $routeParams.serviceInstanceId};
        $loading.start('vmdetails');
        $scope.vmDetails = VmServiceDetails.get(query);
        $scope.vmDetails.$promise.then(function (result) {
        	console.log("retrieveVmDetails() data is %o", result)
        	if(result.serviceInstanceId == null) {
        		console.log("seems the answer is null...");
        	}
            $scope.vmDetails = result;
            var waitingTimeOver = result.retryWaitingTimeInSeconds <= 0;
            $scope.vmDetails.buttonState = {
                disabled: !waitingTimeOver,
                label: (waitingTimeOver ? "Retry" : "Loading ...")
            };
            $loading.finish('vmdetails');
        }, function(data) {
            $loading.finish('vmdetails');
            Status.error(data);
        });
    }

    function retrieveVmBindableApplication() {
        $loading.start('vmBindableApplication');
        var query = {serviceInstanceId: $routeParams.serviceInstanceId};
        $scope.vmBindableApplication = VmServiceBindableApplication.get(query);
        $scope.vmBindableApplication.$promise
        	.then(function (result) {
	            $loading.finish('vmBindableApplication');
	            for (var idx in result.boundServices) {
	                var waitingTimeOver = result.boundServices[idx].retryWaitingTimeInSeconds <= 0;
	                result.boundServices[idx].buttonState = {
	                    disabled: !waitingTimeOver,
	                    label: (waitingTimeOver ? "Retry" : "Loading ...")
	                };
	            }
	            $scope.vmBindableApplication = result;
	        }, function (error) {
	        	console.log("in error")
	            $loading.finish('vmBindableApplication');
	        	Status.error(error)
	        });
    }

    function retrieveVmRemoteStatus() {
        VmServiceRemoteStatus.get({serviceInstanceId: $routeParams.serviceInstanceId}).$promise.then(function (result) {
            $scope.vmRemoteStatus = result;
        });
    }
    
    function refreshVmtoVm() {
        var vmToVm = VmToVm.query({serviceInstanceId: $routeParams.serviceInstanceId});
        vmToVm.$promise.then(function (result) {
            $scope.vmToVm = result;
        });
    }
    
    function retrieveVmtoVm() {
        $loading.start('vmToVm');
        var vmToVm = VmToVm.query({serviceInstanceId: $routeParams.serviceInstanceId});
        vmToVm.$promise.then(function (result) {
            $scope.vmToVm = result;
            $loading.finish('vmToVm');
        }, function(data) {
            $loading.finish('vmToVm');
            Status.error(data);
        });
    }

    function retrieveVmLoadBalancers() {
    	console.log("in retrieveVmLoadBalancers()")
        $loading.start('vmLoadBalancers');
        $scope.vmLoadBalancers = VmLoadBalancers.query({serviceInstanceId: $routeParams.serviceInstanceId});
        $scope.vmLoadBalancers.$promise.then(function (result) {
            for (var idx in result) {
                var waitingTimeOver = result[idx].retryWaitingTimeInSeconds <= 0;
                result[idx].buttonState = {
                    disabled: !waitingTimeOver,
                    label: (waitingTimeOver ? "Retry" : "Loading ...")
                };
            }
            $scope.vmLoadBalancers = result;
            $loading.finish('vmLoadBalancers');
        }, function(error) {
        	Status.error(error)
            $loading.finish('vmLoadBalancers');
        });
    }

    $scope.changeVmPowerState = function (powerState) {
        if (confirm("Really want to change the power state?")) {
            $http.post('vmdetails/' + $routeParams.serviceInstanceId + '/powerstate/' + powerState).
                success(function (data, status, headers, config) {
                    $scope.vmRemoteStatus.powerState = 'CHANGING_STATE';
                    Status.success("Power state command was sent. Please consider that it can take a while until the state will be refreshed in the dashboard.");
                }).
                error(function (data, status, headers, config) {
                    Status.error("Was not able to change the power state.");
                });
        }
    };

    $scope.assignSite = function (site) {
        if (confirm("Really want to assign a new site?")) {
            $http.post('vmdetails/' + $routeParams.serviceInstanceId + '/assignsite/' + site).
                success(function (data, status, headers, config) {
                    $scope.vmDetails.vroStatus = 'UPDATING';
                    Status.success("New site was assigned. The virtual machine is now in updating mode.");
                }).
                error(function (data, status, headers, config) {
                    Status.error("Was not able to assign new site.");
                });
        }
    };

    $scope.rescheduleLoadBalancerBinding = function (binding) {
        $loading.start('vmLoadBalancers');
        $http.post('lbdetails/' + $routeParams.serviceInstanceId + '/binding/' + binding.bindingId + '/retry_binding').
            success(function (data, status, headers, config) {
                Status.success("Load balancer binding was re-enqueued.");
                retrieveVmLoadBalancers();
            }).
            error(function (data, status, headers, config) {
                Status.error("Retry was unsuccessful.");
                $loading.finish('vmLoadBalancers');
            });
    };

    $scope.retryVcapFileSync = function() {
        $loading.start('vmdetails');
        $http.post('vmdetails/' + $routeParams.serviceInstanceId + '/retry_vcapfile').
        success(function (data, status, headers, config) {
            Status.success("VCAP file sync started.");
            retrieveVmDetails();
        }).
        error(function (data, status, headers, config) {
            Status.error("Retry was unsuccessful.");
            $loading.finish('vmdetails');
        });
    };

    $scope.refreshApplicationBindings = function () {
        retrieveVmBindableApplication();
    };

    $scope.refreshLoadBalancerBindings = function () {
        retrieveVmLoadBalancers();
    };

    $scope.rescheduleVm = function () {
        $loading.start('vmdetails');
        $http.post('vmdetails/' + $routeParams.serviceInstanceId + '/retry_provision').
            success(function (data, status, headers, config) {
                Status.success("Virtual machine was re-enqueued.");
                retrieveVmDetails();
            }).
            error(function (data, status, headers, config) {
                Status.error("Retry was unsuccessful.");
                $loading.finish('vmdetails');
            });
    };

    $scope.rescheduleVmBinding = function (binding) {
        $loading.start('vmBindableApplication');
        $http.post('vmdetails/' + $routeParams.serviceInstanceId + '/application/' + binding.bindingId + '/retry_binding').
            success(function (data, status, headers, config) {
                Status.success("Binding was re-enqueued.");
                retrieveVmBindableApplication();
            }).
            error(function (data, status, headers, config) {
                Status.error("Retry was unsuccessful.");
                $loading.finish('vmBindableApplication');
            });
    };

    function retrieveVmSites() {
        $http.get('vmdetails/availablesites').
            success(function (data, status, headers, config) {
                $scope.availableSites = data;
            })
            .error(function() {
            	Status.error("Cannot retrieve available sites")
            });
    }

    $scope.refreshVm = function () {
        retrieveVmDetails();
    };

    $scope.createBindableApplication = function () {
        $loading.start('vmBindableApplication');
        $scope.vmBindableApplication.$save({serviceInstanceId: $routeParams.serviceInstanceId}, function (s, putResponseHeaders) {
            Status.success("Bindable application created.");
            retrieveVmBindableApplication();
        });
    };

    $scope.deleteBindableApplication = function () {
        if (confirm("Do you really want to delete the bindable application?")) {
            $loading.start('vmBindableApplication');
            $scope.vmBindableApplication.$delete({serviceInstanceId: $routeParams.serviceInstanceId}, function (s, putResponseHeaders) {
                Status.success("Bindable application deleted.");
                retrieveVmBindableApplication();
            });
        }
    };
    
    $scope.openInvitesDialog = function () {
        var inviteDialog = $uibModal.open({
        	replace: true,
            animation: true,
            templateUrl: 'assets/templates/vm/invitesdialog.html',
            controller: InvitesDialogCrtl,
            windowClass: 'modal-lgb-popup',
            resolve: {
                serviceInstanceId: function () {
                    return $routeParams.serviceInstanceId;
                }
            }
        });
        // refresh after we closed the dialog
        inviteDialog.result.then(function() {
        	retrieveVmtoVm();
    	}, function() {
    		retrieveVmtoVm();
    	});
    };


    $scope.openSnapshotsDialog = function () {
        $uibModal.open({
        	replace: true,
            animation: true,
            templateUrl: 'snapshotsDialog.html',
            controller: SnapshotsDialogCrtl,
            windowClass: 'modal-lg-popup',
            size: 'lb',
            resolve: {
                serviceInstanceId: function () {
                    return $routeParams.serviceInstanceId;
                }
            }
        });
    };

    $scope.init = function () {
    	console.log("entering init()")
        retrieveVmDetails();
        retrieveVmSites();
        retrieveVmBindableApplication();
        retrieveVmLoadBalancers();
        retrieveVmRemoteStatus();
        retrieveVmtoVm();
    	console.log("leaving init()")
    };
}

function getcurrent(snapshots) {

	for(var index = 0; index < snapshots.length; index++) {
		var snapshot = snapshots[index]
		if(snapshot.current) {
			return snapshot;
		} 
		var found = getcurrent(snapshot.children)
		// console.log("found is " + found + " for " + snapshot.snapshotDescription)
		if(found != null) {
			return found
		}
	}
	return null
}

function InviteMailDialogCrtl($scope, $location, $http, $uibModalInstance, param) {
	$scope.invite = param.invite;
	$scope.serviceInstanceId = param.serviceInstanceId;
	console.log("invite.description=" + $scope.invite.description + ", serviceInstanceId=" + $scope.serviceInstanceId)
	
    $scope.close = function () {
    	$uibModalInstance.close();
    };
    
    $scope. buildLink = function () {
    	return $location.protocol() + "://" 
    			+ $location.host() + ($location.port() == 80 ? '' : ':' + $location.port()) 
    			+ '/#/inviteresponse/'
    			+ $scope.invite.inviteGuid
    }
}

function CreateInviteDialogCrtl($scope, $http, $uibModal, $uibModalInstance, param) {
	$scope.currentHgt = function() {
		return ($scope.popup2.opened) ?  '420px' : '200px';
	}

	$scope.minDate = new Date()
	
	$scope.today = function() {
		$scope.dt = new Date();
	};
	$scope.today();
	
	 $scope.dateOptions = {
	    dateDisabled: false,
	    formatYear: 'yy',
	    maxDate: new Date(2020, 5, 22),
	    minDate: new Date(),
	    startingDay: 1
	  };
	
	$scope.popup2 = {
		    opened: false
	};
	
	$scope.close = function () {
    	$uibModalInstance.dismiss();
    };
    
    $scope.open2 = function() {
        $scope.popup2.opened = true;
    };
    
    $scope.createInvite = function() {
    	console.log("creating invite ")
    	invite = new Object()
    	invite.description = $scope.description
    	invite.valid = $scope.dt.getTime()
    	invite.port = $scope.port
    	$http.post('/vmdetails/' + param.serviceInstanceId + '/createinvite', invite)
	    .error(function (data, status, headers, config) {
	    })
        .success(function (data, status, headers, config) {
        	$uibModalInstance.close();
        });
    }
}

function InvitesDialogCrtl($scope, $http, $uibModal, $uibModalInstance, Status, serviceInstanceId) {
    $scope.serviceInstanceId = serviceInstanceId;
    //$scope.invites = {};
    
    $scope.loadall = function () {
        $scope.loadingInvites = true
        $http.get('/vmdetails/' + serviceInstanceId + '/invites')
        .error(function (data, status, headers, config) {
            $scope.loadingInvites = false
            $uibModalInstance.close();
            Status.error(data.message + " " + status);
        })
        .success(function (data, status, headers, config) {
            $scope.loadingInvites = false
            $scope.invites = data;
            console.log("got " + data.length + " invites")
            if(data.length > 0) {
                console.log("First element is " + data[0])
            }
        });
    }
    $scope.loadall();

    $scope.createInvite = function() {
    	console.log("createInvite(" + $scope.serviceInstanceId + ")")
        var createModal = $uibModal.open({
        	replace: true,
            animation: true,
            templateUrl: 'assets/templates/vm/createinvite.html',
            controller: CreateInviteDialogCrtl,
            windowClass: 'modal-lg-popup',
            resolve: {
                param : function () {
                    return { 'serviceInstanceId': $scope.serviceInstanceId};
                }
            }
        });
    	createModal.result.then(function() {
    		$scope.loadall()
    	})
    }

    $scope.close = function () {
    	$uibModalInstance.close();
    };
    
    $scope.displayNull = function(s) {
    	if(s == null) {
    		return "-";
    	}
    	return s;
    };

    $scope.generateEmail = function(invite) {
        $uibModal.open({
        	replace: true,
            animation: true,
            templateUrl: 'inviteMail.html',
            controller: InviteMailDialogCrtl,
            windowClass: 'modal-lg-popup',
            resolve: {
                param : function () {
                    return { 'serviceInstanceId': serviceInstanceId, 'invite': invite};
                }
            }
        });
    };
    
    $scope.dropInvite = function (invite) {
    	$http.delete('invites/' + invite.inviteGuid + '/drop')
    	.success(function (data) {
    		console.log(data)
    		$scope.loadall();
    	})
    	.error(function(data) {
    		console.log(data)
    	});
    	
    }

    $scope.getStatus = function(invite) {
    	if(invite.status == 'PENDING') {
    		return invite.status + ' till ' + invite.expiringDate
    	}
    	return invite.status;
    };
}

function SnapshotsDialogCrtl($scope, $http, $uibModalInstance, Status, serviceInstanceId) {
    $scope.serviceInstanceId = serviceInstanceId;
    $scope.data = {"selectedSnapshot": {}};

    $scope.loadingSnapshots = true
	$scope.removingAllSnapshots = false
	$scope.selectingSnapshot = false
	$scope.removingSnapshot = false
	$scope.takingSnapshot = false
    
    $http.get('/vmdetails/' + serviceInstanceId + '/snapshots')
	    .error(function (data, status, headers, config) {
	        $scope.loadingSnapshots = false
	        $uibModalInstance.close();
	        Status.error(data.message);
	    })
	    .success(function (data, status, headers, config) {
	        $scope.loadingSnapshots = false
	        $scope.vmSnapshots = data;
	        var currentsnap = getcurrent(data)
	        if(currentsnap != null) {
	        	$scope.data.selectedSnapshot = currentsnap
	    	}
	        console.log($scope.data.selectedSnapshot)
	    });
    
    $scope.disableIfAction = function() {
    	var check =  $scope.loadingSnapshots ||
				    	$scope.removingAllSnapshots ||
				    	$scope.selectingSnapshot ||
				    	$scope.removingSnapshot ||
				    	$scope.takingSnapshot;
    	//console.log("disableIfAction() returns " + check)
    	return check
    };

    
    $scope.disableIfActionOrNoId = function() {
    	var check = $scope.data.selectedSnapshot.snapshotId == null ||  $scope.disableIfAction();
    	//console.log("disableIfActionOrNoId() returns " + check)
    	return check
    };
 
    $scope.selectSnapshot = function() {
    	$scope.selectingSnapshot = true
        $http.post('/vmdetails/' + serviceInstanceId + '/snapshots/select/' + $scope.data.selectedSnapshot.snapshotId + '/' + $scope.data.selectedSnapshot.snapshotName)
		    .error(function (data, status, headers, config) {
		    	$scope.selectingSnapshot = false
		    	$uibModalInstance.close();
		        Status.error(data.message);
		    })
	        .success(function (data, status, headers, config) {
	        	$scope.selectingSnapshot = false
	        	$uibModalInstance.close();
	            Status.success("Snapshot selected.");
	        });
    };

    $scope.takeSnapshot = function(description) {
    	$scope.takingSnapshot = true
        $http.post('/vmdetails/' + serviceInstanceId + '/snapshots/take', description)
	        .error(function (data, status, headers, config) {
	        	$scope.takingSnapshot = true
	        	$uibModalInstance.close();
	            Status.error(data.message);
	        })
	        .success(function (data, status, headers, config) {
	        	$scope.takingSnapshot = true
	        	$uibModalInstance.close();
	            Status.success("Snapshot was taken.");
	        });
    };
    
    $scope.removeSnapshot = function(description) {
    	$scope.removingSnapshot = true
        $http.post('/vmdetails/' + serviceInstanceId + '/snapshots/remove/' + $scope.data.selectedSnapshot.snapshotId + '/' + $scope.data.selectedSnapshot.snapshotName)
	        .error(function (data, status, headers, config) {
	        	$uibModalInstance.close();
	            $scope.removingSnapshot = false
	            Status.error(data.message);
	        })
	        .success(function (data, status, headers, config) {
	        	$uibModalInstance.close();
	            $scope.removingSnapshot = false
	            Status.success("Snapshot was removed.");
	        });
    }
    
    $scope.removeAllSnapshots = function() {
    	$scope.removingAllSnapshots = true
        $http.post('/vmdetails/' + serviceInstanceId + '/snapshots/removeall/' + $scope.data.selectedSnapshot.snapshotId + '/' + $scope.data.selectedSnapshot.snapshotName)
	        .error(function (data, status, headers, config) {
	        	$uibModalInstance.close();
	            $scope.removingAllSnapshots = false
	            Status.error(data.message);
	        })
	        .success(function (data, status, headers, config) {
	        	$uibModalInstance.close();
	            $scope.removingAllSnapshots = false
	            Status.success("Snapshots were removed.");
	        });
    }
    
    $scope.close = function () {
    	$uibModalInstance.close();
    };
}
