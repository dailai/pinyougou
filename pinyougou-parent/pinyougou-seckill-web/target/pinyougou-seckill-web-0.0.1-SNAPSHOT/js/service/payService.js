//服务层
app.service('payService',function($http){
	    	
	//返回支付链接
	this.createNative=function(){
		return  $http.get('../pay/createNative.do');
	}
	
	//查询支付状态
	this.queryPayStatus=function(out_trade_no){
		return  $http.get('../pay/queryPayStatus.do?out_trade_no='+out_trade_no);
	}

});
