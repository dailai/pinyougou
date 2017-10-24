//服务层
app.service('userService',function($http){
	    	
	//增加 
	this.add=function(entity,code){
		return  $http.post('../user/add.do?smscode='+code,entity );
	}
	
	//验证码
	this.createSmsCode=function(phone){
		return $http.get('../user/createSmsCode.do?phone='+phone);
	}
	
});
