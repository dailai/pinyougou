//控制层 
app.controller('indexController', function($scope, loginService) {
	//得到用户名 
	$scope.getName = function() {
				
		loginService.getName().success(
			function(response) {
					$scope.loginName=response.loginName;
			});
		}
			

});
