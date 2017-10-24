//控制层 
app.controller('userController', function($scope, userService) {
	//保存 
	$scope.reg = function() {
				
		if($scope.entity.password!=$scope.password){
			alert("两次密码不一致，请重新输入");
			return ;
		}
		
		userService.add($scope.entity,$scope.smscode).success(
			function(response) {
					alert(response.message);
			});
		}
	
	//产生随机验证码
	$scope.createSmsCode=function(){
		if($scope.entity.phone==null){
			alert("请输入手机号");
		}
		userService.createSmsCode($scope.entity.phone).success(
			function(response){
				alert(response.message);
			}
		);
	}
	
	

});
